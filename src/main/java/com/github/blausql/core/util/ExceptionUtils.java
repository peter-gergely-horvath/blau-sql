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

 
package com.github.blausql.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public final class ExceptionUtils {

    private ExceptionUtils() {
        // no instances
    }

    public static Optional<String> extractMessageFrom(Throwable t) {
        StringBuilder sb = new StringBuilder();

        if (t instanceof SQLException) {
            SQLException sqlEx = (SQLException) t;

            String sqlState = sqlEx.getSQLState();
            if (sqlState != null && !sqlState.isBlank()) {
                sb.append("SQLState: ").append(sqlState)
                        .append(TextUtils.LINE_SEPARATOR);
            }

            int errorCode = sqlEx.getErrorCode();
            sb.append("Error Code: ").append(errorCode)
                    .append(TextUtils.LINE_SEPARATOR);
        }

        String localizedMessage = t.getLocalizedMessage();
        String message = t.getMessage();

        if (localizedMessage != null && !"".equals(localizedMessage)) {

            sb.append(localizedMessage).append(TextUtils.LINE_SEPARATOR);

        } else if (message != null && !"".equals(message)) {

            sb.append(message).append(TextUtils.LINE_SEPARATOR);
        }

        String throwableAsString = sb.toString();

        if (!throwableAsString.isBlank()) {
            return Optional.of(throwableAsString.trim());
        } else {
            return Optional.empty();
        }
    }

    public static <U extends Throwable> boolean causesContainAnyType(Throwable throwableToExamine,
                                                                     Class<U>[] searchedThrowableClasses) {
        for (Class<U> searchedThrowableClass : searchedThrowableClasses) {
            if (causesContainType(throwableToExamine, searchedThrowableClass)) {
                return true;
            }
        }

        return false;
    }

    public static boolean causesContainType(
            Throwable throwableToExamine, Class<? extends Throwable> searchedThrowableClass) {

        Objects.requireNonNull(throwableToExamine, "argument throwableToExamine cannot be null");
        Objects.requireNonNull(searchedThrowableClass, "argument searchedThrowableClass cannot be null");

        Throwable throwable = throwableToExamine;
        do {
            if (searchedThrowableClass.equals(throwable.getClass())) {
                return true;
            }

            throwable = throwable.getCause();

        } while (throwable != null);

        return false;

    }


    public static Throwable getRootCause(Throwable throwable) {

        if (throwable == null) {
            return null;
        }

        HashSet<Throwable> seen = new HashSet<>();

        Throwable rootCause = throwable;

        Throwable cause;
        while ((cause = rootCause.getCause()) != null) {
            rootCause = cause;

            boolean wasSeenBefore = !seen.add(cause);
            if (wasSeenBefore) {
                throw new IllegalStateException("Cannot extract root cause: loop detected in causes chain", throwable);
            }
        }
        return rootCause;
    }

    public static String getStackTraceAsString(Throwable throwableToDisplay) {

        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {

            throwableToDisplay.printStackTrace(printWriter);
            return stringWriter.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
