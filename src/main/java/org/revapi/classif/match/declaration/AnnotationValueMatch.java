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
package org.revapi.classif.match.declaration;

import static org.revapi.classif.match.util.Operator.EQ;
import static org.revapi.classif.match.util.Operator.NE;

import java.util.List;
import java.util.regex.Pattern;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.match.util.Glob;
import org.revapi.classif.match.util.Globbed;
import org.revapi.classif.match.util.Operator;

public abstract class AnnotationValueMatch implements Globbed {
    final Operator operator;

    public static AnnotationValueMatch string(Operator operator, String string) {
        return new StringValue(operator, string);
    }

    public static AnnotationValueMatch regex(Operator operator, Pattern regex) {
        return new PatternValue(operator, regex);
    }

    public static AnnotationValueMatch number(Operator operator, Number number) {
        return new NumberValue(operator, number);
    }

    public static AnnotationValueMatch bool(Operator operator, boolean bool) {
        return new BooleanValue(operator, bool);
    }

    public static AnnotationValueMatch any(Operator op) {
        return new AnyValue(op);
    }

    public static AnnotationValueMatch all() {
        return new AllValue();
    }

    public static AnnotationValueMatch enumConstant(Operator operator, FqnMatch fqn, NameMatch name) {
        return new EnumValue(operator, fqn, name);
    }

    public static AnnotationValueMatch type(Operator operator, TypeReferenceMatch type) {
        return new TypeValue(operator, type);
    }

    public static AnnotationValueMatch annotation(Operator operator, AnnotationMatch annotation) {
        return new AnnoValue(operator, annotation);
    }

    public static AnnotationValueMatch array(Operator operator, List<AnnotationValueMatch> values) {
        return new ArrayValue(operator, values);
    }

    private AnnotationValueMatch(Operator operator) {
        this.operator = operator;
    }

    public final boolean isMatchAny() {
        return this instanceof AnyValue;
    }

    public final boolean isMatchAll() {
        return this instanceof AllValue;
    }

    public abstract <M> boolean test(AnnotationValue value, MatchContext<M> ctx);

    private static final class StringValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<Boolean, Void> visitor =
                new SimpleAnnotationValueVisitor8<Boolean, Void>(false) {
                    @Override
                    public Boolean visitChar(char c, Void __) {
                        return stringMatch.length() == 1 && operator.satisfied(c, stringMatch.charAt(0));
                    }

                    @Override
                    public Boolean visitString(String s, Void __) {
                        return operator.satisfied(s, stringMatch);
                    }
                };

        private final String stringMatch;

        private StringValue(Operator operator, String stringMatch) {
            super(operator);
            this.stringMatch = stringMatch;
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            return visitor.visit(value);
        }
    }

    private static final class PatternValue extends AnnotationValueMatch {
        private static final AnnotationValueVisitor<Boolean, Pattern> VISITOR = new SimpleAnnotationValueVisitor8<Boolean, Pattern>(false) {
            @Override
            public Boolean visitBoolean(boolean b, Pattern pattern) {
                return pattern.matcher(b ? "true" : "false").matches();
            }

            @Override
            public Boolean visitByte(byte b, Pattern pattern) {
                return pattern.matcher(Byte.toString(b)).matches();
            }

            @Override
            public Boolean visitChar(char c, Pattern pattern) {
                return pattern.matcher(Character.toString(c)).matches();
            }

            @Override
            public Boolean visitDouble(double d, Pattern pattern) {
                return pattern.matcher(Double.toString(d)).matches();
            }

            @Override
            public Boolean visitFloat(float f, Pattern pattern) {
                return pattern.matcher(Float.toString(f)).matches();
            }

            @Override
            public Boolean visitInt(int i, Pattern pattern) {
                return pattern.matcher(Integer.toString(i)).matches();
            }

            @Override
            public Boolean visitLong(long i, Pattern pattern) {
                return pattern.matcher(Long.toString(i)).matches();
            }

            @Override
            public Boolean visitShort(short s, Pattern pattern) {
                return pattern.matcher(Short.toString(s)).matches();
            }

            @Override
            public Boolean visitString(String s, Pattern pattern) {
                return pattern.matcher(s).matches();
            }

            @Override
            public Boolean visitType(TypeMirror t, Pattern pattern) {
                return pattern.matcher(t.toString() + ".class").matches();
            }

            @Override
            public Boolean visitEnumConstant(VariableElement c, Pattern pattern) {
                return pattern.matcher(c.asType().toString() + "." + c.getSimpleName().toString()).matches();
            }

            @Override
            public Boolean visitAnnotation(AnnotationMirror a, Pattern pattern) {
                return pattern.matcher("@" + a.toString()).matches();
            }
        };

        private final Pattern pattern;

        private PatternValue(Operator operator, Pattern pattern) {
            super(operator);
            this.pattern = pattern;
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return false;
            }

            boolean match = VISITOR.visit(value, pattern);

            return (operator == EQ) == match;
        }
    }

    private static final class NumberValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<Boolean, Void> visitor =
                new SimpleAnnotationValueVisitor8<Boolean, Void>(false) {
                    @Override
                    public Boolean visitByte(byte b, Void __) {
                        return operator.satisfied(b, number.byteValue());
                    }

                    @Override
                    public Boolean visitDouble(double d, Void __) {
                        return operator.satisfied(d, number.doubleValue());
                    }

                    @Override
                    public Boolean visitFloat(float f, Void __) {
                        return operator.satisfied(f, number.floatValue());
                    }

                    @Override
                    public Boolean visitInt(int i, Void __) {
                        return operator.satisfied(i, number.intValue());
                    }

                    @Override
                    public Boolean visitLong(long i, Void __) {
                        return operator.satisfied(i, number.longValue());
                    }

                    @Override
                    public Boolean visitShort(short s, Void __) {
                        return operator.satisfied(s, number.shortValue());
                    }
                };

        private final Number number;

        private NumberValue(Operator operator, Number number) {
            super(operator);
            this.number = number;
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            return visitor.visit(value);
        }
    }

    private static final class BooleanValue extends AnnotationValueMatch {
        private final boolean matchValue;

        private BooleanValue(Operator operator, boolean matchValue) {
            super(operator);
            this.matchValue = matchValue;
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            Object val = value.getValue();
            return val instanceof Boolean && operator.satisfied((Boolean) val, matchValue);
        }
    }

    private static final class AnyValue extends AnnotationValueMatch {
        private AnyValue(Operator operator) {
            super(operator);
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            return operator == EQ;
        }
    }

    private static final class AllValue extends AnnotationValueMatch {
        private AllValue() {
            super(EQ);
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            return true;
        }
    }

    private static final class EnumValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<Boolean, MatchContext<?>> visitor =
                new SimpleAnnotationValueVisitor8<Boolean, MatchContext<?>>(false) {
                    @Override
                    public Boolean visitEnumConstant(VariableElement c, MatchContext<?> ctx) {
                        return fqn.testInstance(c.asType(), ctx) && name.matches(c.getSimpleName().toString());
                    }
                };

        private final FqnMatch fqn;
        private final NameMatch name;

        private EnumValue(Operator operator, FqnMatch fqn, NameMatch name) {
            super(operator);
            this.fqn = fqn;
            this.name = name;
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return false;
            }

            boolean res = visitor.visit(value, ctx);
            return (operator == EQ) == res;
        }
    }

    private static final class TypeValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<Boolean, MatchContext<?>> visitor =
                new SimpleAnnotationValueVisitor8<Boolean, MatchContext<?>>(false) {
                    @Override
                    public Boolean visitType(TypeMirror t, MatchContext<?> ctx) {
                        return type.testInstance(t, ctx);
                    }
                };

        private final TypeReferenceMatch type;

        private TypeValue(Operator operator, TypeReferenceMatch type) {
            super(operator);
            this.type = type;
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return false;
            }

            boolean res = visitor.visit(value, ctx);
            return (operator == EQ) == res;
        }
    }

    private static final class AnnoValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<Boolean, MatchContext<?>> visitor =
                new SimpleAnnotationValueVisitor8<Boolean, MatchContext<?>>(false) {
                    @Override
                    public Boolean visitAnnotation(AnnotationMirror a, MatchContext<?> matchContext) {
                        return match.test(a, matchContext);
                    }
                };
        private final AnnotationMatch match;

        private AnnoValue(Operator operator, AnnotationMatch match) {
            super(operator);
            this.match = match;
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return false;
            }

            boolean res = visitor.visit(value, ctx);
            return (operator == EQ) == res;
        }
    }

    private static final class ArrayValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<Boolean, MatchContext<?>> visitor =
                new SimpleAnnotationValueVisitor8<Boolean, MatchContext<?>>(false) {
                    @Override
                    public Boolean visitArray(List<? extends AnnotationValue> vals, MatchContext<?> ctx) {
                        return match.test((m, v) -> m.test(v, ctx), vals);
                    }
                };

        private final Glob<AnnotationValueMatch> match;

        private ArrayValue(Operator operator, List<AnnotationValueMatch> matches) {
            super(operator);
            match = new Glob<>(matches);
        }

        @Override
        public <M> boolean test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return false;
            }

            boolean res = visitor.visit(value, ctx);
            return (operator == EQ) == res;
        }
    }
}
