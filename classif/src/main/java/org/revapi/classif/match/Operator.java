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
package org.revapi.classif.match;

public enum Operator {
    EQ, NE, LT, LE, GT, GE;

    public <T extends Comparable<T>> boolean satisfied(T o1, T o2) {
        switch (this) {
            case EQ:
                return o1.compareTo(o2) == 0;
            case NE:
                return o1.compareTo(o2) != 0;
            case LT:
                return o1.compareTo(o2) < 0;
            case LE:
                return o1.compareTo(o2) <= 0;
            case GT:
                return o1.compareTo(o2) > 0;
            case GE:
                return o1.compareTo(o2) >= 0;
            default:
                return true;
        }
    }

    @Override
    public String toString() {
        switch (this) {
        case EQ:
            return "=";
        case NE:
            return "!=";
        case LT:
            return "<";
        case LE:
            return "<=";
        case GT:
            return ">";
        case GE:
            return ">=";
        default:
            return "??";
        }
    }
}
