package com.github.blausql.util;

import java.util.Objects;

public final class ExceptionUtils {

    private ExceptionUtils() {
        // no instances
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

}
