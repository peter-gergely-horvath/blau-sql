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

package com.github.blausql.core.connection;

import java.util.Objects;

/**
 * Unchecked exception indicating that a SQL statement failed to execute.
 * This is a generic exception type; extraction of SQLState and vendor codes (if needed)
 * should happen at higher layers (e.g., UI) from the original cause.
 */
public final class QueryExecutionException extends RuntimeException {

    public QueryExecutionException(String message, Throwable cause) {
        super(Objects.requireNonNullElse(message, "SQL execution failed"), cause);
    }

    public QueryExecutionException(Throwable cause) {
        super("SQL execution failed", cause);
    }
}
