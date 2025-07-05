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

 
package com.github.blausql.core.classloader;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

public final class DelegatingDriver implements java.sql.Driver {

    private final java.sql.Driver delegate;

    public DelegatingDriver(java.sql.Driver delegate) {
        Objects.requireNonNull(delegate, "argument delegate cannot be null");
        this.delegate = delegate;
    }

    // --- Delegate methods ---

    public Connection connect(String url, Properties info) throws SQLException {
        return delegate.connect(url, info);
    }

    public boolean acceptsURL(String url) throws SQLException {
        return delegate.acceptsURL(url);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return delegate.getPropertyInfo(url, info);
    }

    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    public boolean jdbcCompliant() {
        return delegate.jdbcCompliant();
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }


}
