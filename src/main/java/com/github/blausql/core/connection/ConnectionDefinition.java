/*
 * Copyright (c) 2017 Peter G. Horvath, All Rights Reserved.
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


public final class ConnectionDefinition {

    private String connectionName;
    private String driverClassName;
    private String jdbcUrl;
    private boolean loginAutomatically;
    private String userName;
    private String password;


    public ConnectionDefinition(
            String connectionName,
            String driverClassName,
            String jdbcUrl,
            boolean loginAutomatically,
            String userName,
            String password) {

        this.connectionName = connectionName;
        this.driverClassName = driverClassName;
        this.jdbcUrl = jdbcUrl;
        this.loginAutomatically = loginAutomatically;
        this.userName = userName;
        this.password = password;
    }


    public static ConnectionDefinition copyOf(ConnectionDefinition connectionDefinition) {
        return new ConnectionDefinition(
                connectionDefinition.getConnectionName(),
                connectionDefinition.getDriverClassName(),
                connectionDefinition.getJdbcUrl(),
                connectionDefinition.getLoginAutomatically(),
                connectionDefinition.getUserName(),
                connectionDefinition.getPassword());
    }


    public String getConnectionName() {
        return connectionName;
    }


    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }


    public String getDriverClassName() {
        return driverClassName;
    }


    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }


    public String getJdbcUrl() {
        return jdbcUrl;
    }


    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }


    public boolean getLoginAutomatically() {
        return loginAutomatically;
    }


    public void setLoginAutomatically(boolean loginAutomatically) {
        this.loginAutomatically = loginAutomatically;
    }


    public String getUserName() {
        return userName;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    //CHECKSTYLE.OFF: AvoidInlineConditionals|MagicNumber: IDE generated hashCode() implementation
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((connectionName == null) ? 0 : connectionName.hashCode());
        result = prime * result
                + ((driverClassName == null) ? 0 : driverClassName.hashCode());
        result = prime * result + ((jdbcUrl == null) ? 0 : jdbcUrl.hashCode());
        result = prime * result + (loginAutomatically ? 1231 : 1237);
        result = prime * result
                + ((password == null) ? 0 : password.hashCode());
        result = prime * result
                + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }
    //CHECKSTYLE.ON


    //CHECKSTYLE.OFF: NeedBraces: IDE generated hashCode() implementation
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConnectionDefinition other = (ConnectionDefinition) obj;
        if (connectionName == null) {
            if (other.connectionName != null)
                return false;
        } else if (!connectionName.equals(other.connectionName))
            return false;
        if (driverClassName == null) {
            if (other.driverClassName != null)
                return false;
        } else if (!driverClassName.equals(other.driverClassName))
            return false;
        if (jdbcUrl == null) {
            if (other.jdbcUrl != null)
                return false;
        } else if (!jdbcUrl.equals(other.jdbcUrl))
            return false;
        if (loginAutomatically != other.loginAutomatically)
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }
    //CHECKSTYLE.ON



    @Override
    public String toString() {
        return new StringBuilder()
                .append("ConnectionDefinition [connectionName=")
                .append(connectionName).append(", driverClassName=")
                .append(driverClassName).append(", jdbcUrl=").append(jdbcUrl)
                .append(", loginAutomatically=").append(loginAutomatically)
                .append(", userName=").append(userName).append(", password=")
                .append(password).append("]")
                .toString();
    }
}
