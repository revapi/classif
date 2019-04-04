/*
 * Copyright 2018-2019 Lukas Krejci
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.revapi.classif.dsl;

final class ErrorFormatter {

    private ErrorFormatter() {}

    static String formatError(String recipe, int line, int col, String errorMessage) {
        String[] lines = recipe.split("\r?\n");
        StringBuilder sb = new StringBuilder("\n");

        int maxDigits = getNumberOfDigits(lines.length + 1);
        int maxLineLength = maxLineLength(lines) + maxDigits + 3;

        int curLine = 0;
        int beforeLine = line - 1;

        while (curLine <= beforeLine) {
            appendLineNo(sb, curLine + 1, maxDigits);
            sb.append(lines[curLine++]).append("\n");
        }

        int charsUntilCol = col + maxDigits + 3;

        appendNTimes(sb, '-', charsUntilCol);
        sb.append('^');
        appendNTimes(sb, '-', maxLineLength - charsUntilCol - 1);
        sb.append("\n");

        appendNTimes(sb, ' ', col + maxDigits + 3);
        sb.append("|\n");
        sb.append(errorMessage).append("\n");

        appendNTimes(sb, '-', maxLineLength);
        sb.append("\n");

        while (curLine < lines.length) {
            appendLineNo(sb, curLine + 1, maxDigits);
            sb.append(lines[curLine++]).append("\n");
        }

        return sb.toString();
    }

    private static void appendLineNo(StringBuilder sb, int lineNo, int maxDigits) {
        int lineDigits = getNumberOfDigits(lineNo);
        while (lineDigits++ < maxDigits) {
            sb.append(" ");
        }
        sb.append(lineNo);
        sb.append(" | ");
    }

    private static void appendNTimes(StringBuilder sb, char str, int nofTimes) {
        for (int i = 0; i++ < nofTimes;) {
            sb.append(str);
        }
    }

    private static int maxLineLength(String[] lines) {
        int max = 0;

        for (String l : lines) {
            int len = l.length();
            if (len > max) {
                max = len;
            }
        }

        return max;
    }

    private static int getNumberOfDigits(int positive) {
        if (positive < 100000) {
            if (positive < 100) {
                return positive < 10 ? 1 : 2;
            } else {
                if (positive < 1000) {
                    return 3;
                } else if (positive < 10000) {
                    return 4;
                } else {
                    return 5;
                }
            }
        } else {
            if (positive < 10000000) {
                return positive < 1000000 ? 6 : 7;
            } else {
                if (positive < 100000000) {
                    return 8;
                } else if (positive < 1000000000) {
                    return 9;
                } else {
                    return 10;
                }
            }
        }
    }
}
