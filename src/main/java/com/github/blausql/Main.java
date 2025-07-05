/*
 * Copyright (c) 2017-2020 Peter G. Horvath, All Rights Reserved.
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


package com.github.blausql;

import com.github.blausql.ui.MainMenuWindow;

import java.io.IOException;

//CHECKSTYLE.OFF: FinalClass: must be extensible for the testing frameworks
public class Main {

    static {
        // Disable Apache Commons logging completely
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    }

    public static void exitApplication(int exitCode) {
        System.exit(exitCode);
    }

    private Main() {
        // no instances allowed
    }

    public static void main(String[] args) {

        try (StandardTerminalUI terminalUI = new StandardTerminalUI()) {

            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(terminalUI));

            terminalUI.showWindowCenter(new MainMenuWindow(terminalUI));

            exitApplication(0);

        } catch (IOException ex) {
            // handles cases TerminalUI.getInstance() throws an exception, too
            handleUnexpectedException(Thread.currentThread(), ex);
        }
    }


    private static final class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private final StandardTerminalUI terminalUI;

        private UncaughtExceptionHandler(StandardTerminalUI terminalUI) {
            this.terminalUI = terminalUI;
        }

        public void uncaughtException(Thread t, Throwable e) {

            closeUISafely();

            handleUnexpectedException(t, e);
        }

        private void closeUISafely() {
            try {
                terminalUI.close();
            } catch (Throwable uiCloseThrowable) {
                System.err.println("--- IGNORING Throwable during abrupt UI shutdown ---");
                uiCloseThrowable.printStackTrace();

                System.err.println();
                System.err.println();
            }
        }
    }

    private static void handleUnexpectedException(Thread t, Throwable e) {
        System.err.format("--- UNHANDLED EXCEPTION in Thread '%s': exiting the JVM! --- %n", t.getName());

        e.printStackTrace();

        System.err.println();

        logApplicationVersionSafely();

        System.err.println("The application encountered a fatal error and must shut down.");
        System.err.println("Please report this via the GitHub issue tracker, ");
        System.err.println("attaching the above debug information!");

        exitApplication(1);
    }


    private static void logApplicationVersionSafely() {

        String version = "unknown";

        try {
            version = Version.VERSION_STRING;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            // added out of paranoia: should not happen,
            // if it does happen, it is best to just ignore
        }

        System.err.format("Application version: %s %n", version);
        System.err.println();
    }
}
//CHECKSTYLE.ON
