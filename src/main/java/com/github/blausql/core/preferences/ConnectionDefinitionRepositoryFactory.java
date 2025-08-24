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

package com.github.blausql.core.preferences;

import com.github.blausql.spi.connections.ConnectionDefinitionRepository;
import com.github.blausql.spi.connections.ConnectionDefinitionRepositoryProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Factory class for creating and managing instances of {@link ConnectionDefinitionRepository}.
 * This class uses the Java ServiceLoader mechanism to discover and load custom repository
 * implementations, falling back to the default implementation if none is provided.
 */
public final class ConnectionDefinitionRepositoryFactory {

    private static final DefaultConnectionDefinitionRepositoryProvider DEFAULT_PROVIDER =
            new DefaultConnectionDefinitionRepositoryProvider();

    private static final ConnectionDefinitionRepository REPOSITORY_INSTANCE = createRepository();

    private ConnectionDefinitionRepositoryFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns the singleton instance of the ConnectionDefinitionRepository.
     * If a custom implementation is provided via ServiceLoader, it will be used.
     * If multiple implementations are found, an exception is thrown.
     * If no implementation is found, the default implementation is used.
     *
     * @return the singleton repository instance
     * @throws IllegalStateException if multiple custom implementations are found
     */
    public static ConnectionDefinitionRepository getRepository() {
        return REPOSITORY_INSTANCE;
    }

    private static ConnectionDefinitionRepository createRepository() {
        ServiceLoader<ConnectionDefinitionRepositoryProvider> loader =
                ServiceLoader.load(ConnectionDefinitionRepositoryProvider.class);
        Iterator<ConnectionDefinitionRepositoryProvider> iterator = loader.iterator();
        
        if (!iterator.hasNext()) {
            // No custom implementation found, use default
            return DEFAULT_PROVIDER.createRepository();
        }
        
        ConnectionDefinitionRepositoryProvider provider = iterator.next();
        
        if (iterator.hasNext()) {
            // Multiple implementations found, which is not allowed
            throw new IllegalStateException(
                "Multiple ConnectionDefinitionRepositoryProvider implementations found. " +
                "Only one custom implementation is allowed.");
        }
        
        return provider.createRepository();
    }
}
