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

import java.util.Map;
import java.util.function.BiFunction;

import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;

/**
 * Implementation of {@link MetricRegistry} which returns no-op metrics implementations.
 */
class NoOpMetricRegistry extends AbstractRegistry<NoOpMetric> {

    private static final Map<MetricType, BiFunction<String, Metadata, NoOpMetric>> NO_OP_METRIC_FACTORIES =
            // Omit gauge because creating a gauge requires an existing delegate instance.
            // These factory methods do not use delegates.
            Map.of(MetricType.COUNTER, NoOpMetricImpl.NoOpCounterImpl::create,
                   MetricType.HISTOGRAM, NoOpMetricImpl.NoOpHistogramImpl::create,
                   MetricType.METERED, NoOpMetricImpl.NoOpMeterImpl::create,
                   MetricType.TIMER, NoOpMetricImpl.NoOpTimerImpl::create,
                   MetricType.SIMPLE_TIMER, NoOpMetricImpl.NoOpSimpleTimerImpl::create,
                   MetricType.CONCURRENT_GAUGE, NoOpMetricImpl.NoOpConcurrentGaugeImpl::create);

    private static final RegistrySettings REGISTRY_SETTINGS = RegistrySettings.builder().enabled(false).build();

    public static NoOpMetricRegistry create(MetricRegistry.Type type) {
        return new NoOpMetricRegistry(type);
    }


    private NoOpMetricRegistry(MetricRegistry.Type type) {
        super(type, NoOpMetric.class);
    }

    @Override
    protected boolean isMetricEnabled(String metricName) {
        return false;
    }

    @Override
    protected Map<MetricType, BiFunction<String, Metadata, NoOpMetric>> prepareMetricFactories() {
        return noOpMetricFactories();
    }

    protected static Map<MetricType, BiFunction<String, Metadata, NoOpMetric>> noOpMetricFactories() {
        return NO_OP_METRIC_FACTORIES;
    }

    @Override
    protected RegistrySettings registrySettings() {
        return REGISTRY_SETTINGS;
    }

    @Override
    protected <T extends Metric> NoOpMetricImpl toImpl(Metadata metadata, T metric) {
        String registryTypeName = registryType().getName();
        MetricType metricType = AbstractRegistry.deriveType(metadata.getTypeRaw(), metric);
        switch (metricType) {
        case COUNTER:
            return NoOpMetricImpl.NoOpCounterImpl.create(registryTypeName, metadata);
        case GAUGE:
            return NoOpMetricImpl.NoOpGaugeImpl.create(registryTypeName, metadata, (Gauge<?>) metric);
        case HISTOGRAM:
            return NoOpMetricImpl.NoOpHistogramImpl.create(registryTypeName, metadata);
        case METERED:
            return NoOpMetricImpl.NoOpMeterImpl.create(registryTypeName, metadata);
        case TIMER:
            return NoOpMetricImpl.NoOpTimerImpl.create(registryTypeName, metadata);
        case SIMPLE_TIMER:
            return NoOpMetricImpl.NoOpSimpleTimerImpl.create(registryTypeName, metadata);
        case CONCURRENT_GAUGE:
            return NoOpMetricImpl.NoOpConcurrentGaugeImpl.create(registryTypeName, metadata);
        case INVALID:
        default:
            throw new IllegalArgumentException("Unexpected metric type " + metricType
                                                       + ": " + metric.getClass().getName());
        }
    }

    @Override
    protected Map<Class<? extends NoOpMetric>, MetricType> prepareMetricToTypeMap() {
        return Map.of(NoOpMetricImpl.NoOpConcurrentGaugeImpl.class, MetricType.CONCURRENT_GAUGE,
                      NoOpMetricImpl.NoOpCounterImpl.class, MetricType.COUNTER,
                      NoOpMetricImpl.NoOpGaugeImpl.class, MetricType.GAUGE,
                      NoOpMetricImpl.NoOpHistogramImpl.class, MetricType.HISTOGRAM,
                      NoOpMetricImpl.NoOpMeterImpl.class, MetricType.METERED,
                      NoOpMetricImpl.NoOpTimerImpl.class, MetricType.TIMER,
                      NoOpMetricImpl.NoOpSimpleTimerImpl.class, MetricType.SIMPLE_TIMER);
    }
}
