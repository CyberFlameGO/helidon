/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.metrics.api;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.helidon.config.Config;
import io.helidon.config.ConfigValue;

import org.eclipse.microprofile.metrics.MetricRegistry;

class MetricsSettingsImpl implements MetricsSettings {

    private static final RegistrySettings DEFAULT_REGISTRY_SETTINGS = RegistrySettings.create();

    private final boolean isEnabled;
    private final KeyPerformanceIndicatorMetricsSettings kpiMetricsSettings;
    private final BaseMetricsSettings baseMetricsSettings;
    private final EnumMap<MetricRegistry.Type, RegistrySettings> registrySettings;

    private MetricsSettingsImpl(MetricsSettingsImpl.Builder builder) {
        isEnabled = builder.isEnabled;
        kpiMetricsSettings = builder.kpiMetricsSettingsBuilder.build();
        baseMetricsSettings = builder.baseMetricsSettingsBuilder.build();
        registrySettings = builder.registrySettings;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public KeyPerformanceIndicatorMetricsSettings keyPerformanceIndicatorSettings() {
        return kpiMetricsSettings;
    }

    @Override
    public BaseMetricsSettings baseMetricsSettings() {
        return baseMetricsSettings;
    }

    @Override
    public boolean isMetricEnabled(MetricRegistry.Type registryType, String metricName) {
        if (!isEnabled) {
            return false;
        }
        RegistrySettings registrySettings = this.registrySettings.get(registryType);
        return registrySettings == null || registrySettings.isMetricEnabled(metricName);
    }

    @Override
    public RegistrySettings registrySettings(MetricRegistry.Type registryType) {
        return registrySettings.getOrDefault(registryType, DEFAULT_REGISTRY_SETTINGS);
    }

    // For testing and within-package use only
    Map<MetricRegistry.Type, RegistrySettings> registrySettings() {
        return registrySettings;
    }

    static class Builder implements MetricsSettings.Builder {

        private boolean isEnabled = true;
        private KeyPerformanceIndicatorMetricsSettings.Builder kpiMetricsSettingsBuilder =
                KeyPerformanceIndicatorMetricsSettings.builder();
        private BaseMetricsSettings.Builder baseMetricsSettingsBuilder = BaseMetricsSettings.builder();
        private final EnumMap<MetricRegistry.Type, RegistrySettings> registrySettings = prepareRegistrySettings();

        private static EnumMap<MetricRegistry.Type, RegistrySettings> prepareRegistrySettings() {
            EnumMap<MetricRegistry.Type, RegistrySettings> result = new EnumMap<>(MetricRegistry.Type.class);
            for (MetricRegistry.Type type : MetricRegistry.Type.values()) {
                result.put(type, RegistrySettings.create());
            }
            return result;
        }

        protected Builder() {
        }

        protected Builder(MetricsSettings serviceSettings) {
            isEnabled = serviceSettings.isEnabled();
            kpiMetricsSettingsBuilder = KeyPerformanceIndicatorMetricsSettings.builder(
                    serviceSettings.keyPerformanceIndicatorSettings());
            baseMetricsSettingsBuilder = BaseMetricsSettings.builder(serviceSettings.baseMetricsSettings());

            for (MetricRegistry.Type metricRegistryType : MetricRegistry.Type.values()) {
                registrySettings.put(metricRegistryType,
                                     ((MetricsSettingsImpl) serviceSettings).registrySettings().get(metricRegistryType));
            }
        }

        @Override
        public MetricsSettingsImpl build() {
            return new MetricsSettingsImpl(this);
        }

        @Override
        public Builder enabled(boolean value) {
            isEnabled = value;
            return this;
        }

        @Override
        public Builder baseMetricsSettings(BaseMetricsSettings.Builder baseMetricsSettingsBuilder) {
            this.baseMetricsSettingsBuilder = baseMetricsSettingsBuilder;
            return this;
        }

        @Override
        public Builder config(Config metricsSettingsConfig) {
            baseMetricsSettingsBuilder.config(metricsSettingsConfig.get(BaseMetricsSettings.Builder.BASE_METRICS_CONFIG_KEY));
            kpiMetricsSettingsBuilder.config(metricsSettingsConfig
                                                     .get(KeyPerformanceIndicatorMetricsSettings.Builder
                                                                  .KEY_PERFORMANCE_INDICATORS_CONFIG_KEY));
            metricsSettingsConfig.get(MetricsSettings.Builder.ENABLED_CONFIG_KEY)
                    .asBoolean()
                    .ifPresent(this::enabled);

            metricsSettingsConfig.get(REGISTRIES_CONFIG_KEY)
                    .asList(TypedRegistrySettingsImpl::create)
                    .ifPresent(this::addAllTypedRegistrySettings);
            return this;
        }

        @Override
        public Builder keyPerformanceIndicatorSettings(
                KeyPerformanceIndicatorMetricsSettings.Builder kpiMetricsSettings) {
            this.kpiMetricsSettingsBuilder = kpiMetricsSettings;
            return this;
        }

        @Override
        public MetricsSettings.Builder registrySettings(MetricRegistry.Type registryType, RegistrySettings registrySettings) {
            this.registrySettings.put(registryType, registrySettings);
            return this;
        }

        private void addAllTypedRegistrySettings(List<TypedRegistrySettingsImpl> typedRegistrySettingsList) {
            for (TypedRegistrySettingsImpl typedRegistrySettings : typedRegistrySettingsList) {
                registrySettings.put(typedRegistrySettings.registryType, typedRegistrySettings);
            }
        }

        private static class TypedRegistrySettingsImpl extends RegistrySettingsImpl {

            static TypedRegistrySettingsImpl create(Config registrySettingsConfig) {

                RegistrySettingsImpl.Builder builder = RegistrySettingsImpl.builder();
                builder.config(registrySettingsConfig);

                ConfigValue<String> typeNameValue = registrySettingsConfig.get(RegistrySettings.Builder.TYPE_CONFIG_KEY)
                        .asString();
                if (!typeNameValue.isPresent()) {
                    throw new IllegalArgumentException("Missing metric registry type in registry settings: "
                                                               + registrySettingsConfig);
                }
                MetricRegistry.Type type = MetricRegistry.Type.valueOf(typeNameValue.get().toUpperCase(Locale.ROOT));
                return new TypedRegistrySettingsImpl(type, builder);
            }

            private final MetricRegistry.Type registryType;

            private TypedRegistrySettingsImpl(MetricRegistry.Type registryType, RegistrySettingsImpl.Builder builder) {
                super(builder);
                this.registryType = registryType;
            }

        }
    }
}
