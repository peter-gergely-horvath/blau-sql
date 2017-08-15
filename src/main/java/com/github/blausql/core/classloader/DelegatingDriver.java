package com.github.blausql.core.classloader;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public final class DelegatingDriver implements java.sql.Driver {

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

    private final java.sql.Driver delegate;

    public DelegatingDriver(java.sql.Driver delegate) {
        this.delegate = delegate;
    }
}
