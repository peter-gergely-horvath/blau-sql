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

public final class ConnectionDefinition {

    private static final String DEFAULT_STATEMENT_SEPARATOR = ";";

    private String connectionName;
    private String driverClassName;
    private String jdbcUrl;
    private boolean loginAutomatically;
    private String userName;
    private String password;
    private String statementSeparator;
    private Character hotkey;
    private Integer order;


    public ConnectionDefinition(String connectionName) {
        this(connectionName,
                null,
                null,
                false,
                null,
                null,
                DEFAULT_STATEMENT_SEPARATOR,
                null,
                null);
    }

    public ConnectionDefinition(
            String connectionName,
            String driverClassName,
            String jdbcUrl,
            boolean loginAutomatically,
            String userName,
            String password,
            String statementSeparator,
            Character hotkey,
            Integer order) {

        this.connectionName = connectionName;
        this.driverClassName = driverClassName;
        this.jdbcUrl = jdbcUrl;
        this.loginAutomatically = loginAutomatically;
        this.userName = userName;
        this.password = password;
        this.statementSeparator = statementSeparator;
        this.hotkey = hotkey;
        this.order = order;
    }


    /**
     * Copy constructor: constructs an object with the same field values as the parameter
     *
     * @param connectionDefinition the object to copy from, can <b>NOT</b> be {@code null}
     */
    public ConnectionDefinition(ConnectionDefinition connectionDefinition) {
        this(connectionDefinition.getConnectionName(),
                connectionDefinition.getDriverClassName(),
                connectionDefinition.getJdbcUrl(),
                connectionDefinition.getLoginAutomatically(),
                connectionDefinition.getUserName(),
                connectionDefinition.getPassword(),
                connectionDefinition.getStatementSeparator(),
                connectionDefinition.getHotkey(),
                connectionDefinition.getOrder());
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

    public String getStatementSeparator() {
        return statementSeparator;
    }

    public void setStatementSeparator(String statementSeparator) {
        this.statementSeparator = statementSeparator;
    }

    public Character getHotkey() {
        return hotkey;
    }

    public void setHotkey(Character hotkey) {
        this.hotkey = hotkey;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionName, driverClassName, jdbcUrl, loginAutomatically,
                userName, password, statementSeparator, hotkey, order);
    }


    //CHECKSTYLE.OFF: NeedBraces: IDE generated hashCode() implementation
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionDefinition that = (ConnectionDefinition) o;
        return loginAutomatically == that.loginAutomatically
                && Objects.equals(connectionName, that.connectionName)
                && Objects.equals(driverClassName, that.driverClassName)
                && Objects.equals(jdbcUrl, that.jdbcUrl)
                && Objects.equals(userName, that.userName)
                && Objects.equals(password, that.password)
                && Objects.equals(statementSeparator, that.statementSeparator)
                && Objects.equals(hotkey, that.hotkey)
                && Objects.equals(order, that.order);
    }
    //CHECKSTYLE.ON


    @Override
    public String toString() {
        return "ConnectionDefinition{"
                + "connectionName='" + connectionName + '\''
                + ", driverClassName='" + driverClassName + '\''
                + ", jdbcUrl='" + jdbcUrl + '\''
                + ", loginAutomatically=" + loginAutomatically
                + ", userName='" + userName + '\''
                + ", password='" + password + '\''
                + ", statementSeparator='" + statementSeparator + '\''
                + ", hotkey=" + hotkey
                + ", order=" + order
                + '}';
    }
}
