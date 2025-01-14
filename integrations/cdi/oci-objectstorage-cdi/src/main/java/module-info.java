/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates.
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

/**
 * Provides classes and interfaces that integrate the OCI object
 * storage service into CDI 2.0-based applications.
 */
module io.helidon.integrations.cdi.oci.objectstorage {

    requires jakarta.inject;
    requires oci.java.sdk.common;
    requires oci.java.sdk.objectstorage.generated;
    requires oci.java.sdk.objectstorage.extensions;
    requires jakarta.cdi;
    requires microprofile.config.api;

    exports io.helidon.integrations.cdi.oci.objectstorage;

    provides jakarta.enterprise.inject.spi.Extension
            with io.helidon.integrations.cdi.oci.objectstorage.OCIObjectStorageExtension;

    provides org.eclipse.microprofile.config.spi.ConfigSource
            with io.helidon.integrations.cdi.oci.objectstorage.OciConfigConfigSource;
}
