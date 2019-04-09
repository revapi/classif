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
package org.revapi.classif.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;

public final class LogUtil {

    private LogUtil() {

    }

    public static Message traceParams(Logger logger, String name, Object param) {
        if (!logger.isTraceEnabled()) {
            return null;
        }

        return new ParameterizedMessage(name + ": {} = `{}`", className(param), param);
    }

    public static Message traceParams(Logger logger, String name1, Object param1, String name2, Object param2) {
        if (!logger.isTraceEnabled()) {
            return null;
        }

        return new ParameterizedMessage(name1 + ": {} = `{}`, " + name2 + ": {} = `{}`", className(param1), param1,
                className(param2), param2);
    }

    public static Message traceParams(Logger logger, String name1, Object param1, String name2, Object param2,
            String name3, Object param3) {
        if (!logger.isTraceEnabled()) {
            return null;
        }

        return createMessage(name1, param1, name2, param2, name3, param3);
    }

    public static Message traceParams(Logger logger, String name1, Object param1, String name2, Object param2,
            String name3, Object param3, String name4, Object param4) {
        if (!logger.isTraceEnabled()) {
            return null;
        }

        return createMessage(name1, param1, name2, param2, name3, param3, name4, param4);
    }

    public static Message traceParams(Logger logger, String name1, Object param1, String name2, Object param2,
            String name3, Object param3, String name4, Object param4, String name5, Object param5) {
        if (!logger.isTraceEnabled()) {
            return null;
        }

        return createMessage(name1, param1, name2, param2, name3, param3, name4, param4, name5, param5);
    }

    public static Message traceParams(Logger logger, String name1, Object param1, String name2, Object param2,
            String name3, Object param3, String name4, Object param4, String name5, Object param5, String name6,
            Object param6) {
        if (!logger.isTraceEnabled()) {
            return null;
        }

        return createMessage(name1, param1, name2, param2, name3, param3, name4, param4, name5, param5, name6, param6);
    }

    public static Message traceParams(Logger logger, String name1, Object param1, String name2, Object param2,
            String name3, Object param3, String name4, Object param4, String name5, Object param5,
            String name6, Object param6, String name7, Object param7) {

        if (!logger.isTraceEnabled()) {
            return null;
        }

        return createMessage(name1, param1, name2, param2, name3, param3, name4, param4, name5, param5, name6, param6,
                name7, param7);
    }

    public static Message traceParams(Logger logger, String name1, Object param1, String name2, Object param2,
            String name3, Object param3, String name4, Object param4, String name5, Object param5,
            String name6, Object param6, String name7, Object param7, String name8, Object param8) {

        if (!logger.isTraceEnabled()) {
            return null;
        }

        return createMessage(name1, param1, name2, param2, name3, param3, name4, param4, name5, param5, name6, param6,
                name7, param7, name8, param8);
    }

    public static Message traceParams(Logger logger, Object... parameterNamesAndValues) {
        if (!logger.isTraceEnabled()) {
            return null;
        }

        return createMessage(parameterNamesAndValues);
    }

    private static Message createMessage(Object... parameterNamesAndValues) {
        StringBuilder sb = new StringBuilder();

        Object[] values = new Object[parameterNamesAndValues.length];
        int vi = 0;
        for (int i = 0; i < parameterNamesAndValues.length; ++i) {
            if (i % 2 == 0) {
                sb.append(parameterNamesAndValues[i]).append(": {} = `{}`, ");
            } else {
                Object val = parameterNamesAndValues[i];
                values[vi++] = className(val);
                values[vi++] = val;
            }
        }

        return new ParameterizedMessage(sb.replace(sb.length() - 2, sb.length(), "").toString(), values);
    }

    private static String className(@Nullable Object object) {
        return object == null ? "?" : object.getClass().getSimpleName();
    }
}
