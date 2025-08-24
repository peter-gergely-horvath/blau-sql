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

import com.github.blausql.core.connection.ConnectionDefinition;

import java.util.List;

/**
 * Interface for managing database connection definitions.
 * Implementations can provide custom storage mechanisms for connection definitions.
 */
public interface ConnectionDefinitionRepository {

    /**
     * Retrieves all connection definitions.
     *
     * @return a list of all connection definitions
     * @throws LoadException if there is an error loading the connection definitions
     */
    List<ConnectionDefinition> getConnectionDefinitions() throws LoadException;

    /**
     * Saves a connection definition.
     *
     * @param connectionDefinition the connection definition to save
     * @throws SaveException if there is an error saving the connection definition
     */
    void saveConnectionDefinition(ConnectionDefinition connectionDefinition) throws SaveException;

    /**
     * Deletes a connection definition by name.
     *
     * @param connectionName the name of the connection to delete
     * @throws DeleteException if there is an error deleting the connection definition
     */
    void deleteConnectionDefinitionByName(String connectionName) throws DeleteException;
    
    /**
     * Finds a connection definition by name (case-insensitive).
     *
     * @param connectionName the name of the connection to find
     * @return the connection definition, or null if not found
     * @throws LoadException if there is an error loading the connection definitions
     */
    ConnectionDefinition findConnectionDefinitionByName(String connectionName) throws LoadException;
}
