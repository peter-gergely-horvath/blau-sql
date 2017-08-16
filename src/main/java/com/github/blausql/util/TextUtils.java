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


package com.github.blausql.util;

public final class TextUtils {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private TextUtils() {
        // no external instances
    }

    public static String breakLine(String theString, int maxLineLen) {
        StringBuilder multilineStringBuilder = new StringBuilder();

        for (int i = 0; i < theString.length(); i += maxLineLen) {
            multilineStringBuilder.append(
                    theString.substring(i, Math.min(i + maxLineLen, theString.length())));

            multilineStringBuilder.append(LINE_SEPARATOR);
        }
        return multilineStringBuilder.toString();
    }

    public static String separateLines(String... lines) {
        StringBuilder multilineStringBuilder = new StringBuilder();

        for (String aLine : lines) {
            multilineStringBuilder.append(aLine);
            multilineStringBuilder.append(LINE_SEPARATOR);
        }

        return multilineStringBuilder.toString();
    }
}
