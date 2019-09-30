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
package org.revapi.classif;

import java.io.PrintStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

public class MethodTraceIndentingLogger extends SimpleLogger {
    private int indent = 0;
    public MethodTraceIndentingLogger(String name, Level defaultLevel, boolean showLogName, boolean showShortLogName,
            boolean showDateTime, boolean showContextMap, String dateTimeFormat,
            MessageFactory messageFactory,
            PropertiesUtil props, PrintStream stream) {
        super(name, defaultLevel, showLogName, showShortLogName, showDateTime, showContextMap, dateTimeFormat, messageFactory, props, stream);
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        if (marker == ENTRY_MARKER) {
            indent++;
        } else if (marker == EXIT_MARKER) {
            indent--;
        }

        if (indent > 0) {
            message = new SimpleMessage(indent(indent, message));
        }

        super.logMessage(fqcn, level, marker, message, t);
    }

    private static String indent(int indent, Message message) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; ++i) {
            sb.append("  ");
        }
        sb.append(message.getFormattedMessage());
        return sb.toString();
    }
}
