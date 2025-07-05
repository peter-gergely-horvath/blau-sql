package com.github.blausql.core.util;

import java.util.HashSet;
import java.util.Objects;

public final class ExceptionUtils {

    private ExceptionUtils() {
        // no instances
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

}
