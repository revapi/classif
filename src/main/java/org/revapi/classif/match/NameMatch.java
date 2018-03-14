/*
 * Copyright 2018 Lukas Krejci
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
package org.revapi.classif.match;

import java.util.regex.Pattern;

public abstract class NameMatch {
    private NameMatch() {

    }

    public boolean isMatchAllRemaining() {
        return false;
    }

    public boolean isMatchAny() {
        return false;
    }

    public String getExactMatch() {
        return null;
    }

    public Pattern getPattern() {
        return null;
    }

    public static NameMatch exact(String name) {
        return new MatchExact(name);
    }

    public static NameMatch pattern(Pattern name) {
        return new MatchPattern(name);
    }

    public static NameMatch any() {
        return new MatchAny();
    }

    public static NameMatch all() {
        return new MatchAll();
    }

    public abstract boolean matches(String name);

    private static final class MatchExact extends NameMatch {
        private final String match;

        private MatchExact(String match) {
            this.match = match;
        }

        @Override
        public String getExactMatch() {
            return match;
        }

        @Override
        public boolean matches(String name) {
            return match.equals(name);
        }
    }

    private static final class MatchPattern extends NameMatch {
        private final Pattern pattern;

        private MatchPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public Pattern getPattern() {
            return pattern;
        }

        @Override
        public boolean matches(String name) {
            return pattern.matcher(name).matches();
        }
    }

    private static final class MatchAny extends NameMatch {
        @Override
        public boolean isMatchAny() {
            return true;
        }

        @Override
        public boolean matches(String name) {
            return true;
        }
    }

    private static final class MatchAll extends NameMatch {

        @Override
        public boolean matches(String name) {
            return true;
        }

        @Override
        public boolean isMatchAllRemaining() {
            return true;
        }
    }
}
