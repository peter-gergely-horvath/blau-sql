/*
 * Copyright (c) 2017-2025 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.blausql.spi.connections;

/**
 * Service provider interface for custom {@link ConnectionConfigurationRepository} implementations.
 *
 * <p>Implementations of this interface are discovered using the Java ServiceLoader mechanism.
 * To register a custom repository implementation, create a provider class that implements this interface
 * and register it by creating a file named:
 * {@code META-INF/services/com.github.blausql.spi.connections.ConnectionConfigurationRepositoryProvider}
 * in your JAR file, containing the fully qualified class name of your provider implementation.
 */
public interface ConnectionConfigurationRepositoryProvider {

    /**
     * Creates and returns a new instance of the connection configuration repository.
     *
     * @return a new repository instance
     */
    ConnectionConfigurationRepository createRepository();
}
