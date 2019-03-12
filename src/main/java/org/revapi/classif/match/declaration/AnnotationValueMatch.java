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
package org.revapi.classif.match.declaration;

import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.util.Operator.EQ;
import static org.revapi.classif.util.Operator.NE;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.util.Glob;
import org.revapi.classif.util.Globbed;
import org.revapi.classif.util.Operator;

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

    public abstract <M> TestResult test(AnnotationValue value, MatchContext<M> ctx);

    private static final class StringValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<TestResult, Void> visitor =
                new SimpleAnnotationValueVisitor8<TestResult, Void>(NOT_PASSED) {
                    @Override
                    public TestResult visitChar(char c, Void __) {
                        return TestResult.fromBoolean(stringMatch.length() == 1 && operator.satisfied(c, stringMatch.charAt(0)));
                    }

                    @Override
                    public TestResult visitString(String s, Void __) {
                        return TestResult.fromBoolean(operator.satisfied(s, stringMatch));
                    }
                };

        private final String stringMatch;

        private StringValue(Operator operator, String stringMatch) {
            super(operator);
            this.stringMatch = stringMatch;
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            return visitor.visit(value);
        }

        @Override
        public String toString() {
            return operator + " " + stringMatch;
        }
    }

    private static final class PatternValue extends AnnotationValueMatch {
        private static final AnnotationValueVisitor<TestResult, Pattern> VISITOR = new SimpleAnnotationValueVisitor8<TestResult, Pattern>(NOT_PASSED) {
            @Override
            public TestResult visitBoolean(boolean b, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(b ? "true" : "false").matches());
            }

            @Override
            public TestResult visitByte(byte b, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(Byte.toString(b)).matches());
            }

            @Override
            public TestResult visitChar(char c, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(Character.toString(c)).matches());
            }

            @Override
            public TestResult visitDouble(double d, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(Double.toString(d)).matches());
            }

            @Override
            public TestResult visitFloat(float f, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(Float.toString(f)).matches());
            }

            @Override
            public TestResult visitInt(int i, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(Integer.toString(i)).matches());
            }

            @Override
            public TestResult visitLong(long i, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(Long.toString(i)).matches());
            }

            @Override
            public TestResult visitShort(short s, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(Short.toString(s)).matches());
            }

            @Override
            public TestResult visitString(String s, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(s).matches());
            }

            @Override
            public TestResult visitType(TypeMirror t, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(t.toString() + ".class").matches());
            }

            @Override
            public TestResult visitEnumConstant(VariableElement c, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher(c.asType().toString() + "." + c.getSimpleName().toString()).matches());
            }

            @Override
            public TestResult visitAnnotation(AnnotationMirror a, Pattern pattern) {
                return TestResult.fromBoolean(pattern.matcher("@" + a.toString()).matches());
            }
        };

        private final Pattern pattern;

        private PatternValue(Operator operator, Pattern pattern) {
            super(operator);
            this.pattern = pattern;
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return NOT_PASSED;
            }

            TestResult match = VISITOR.visit(value, pattern);

            return operator == EQ ? match : match.negate();
        }

        @Override
        public String toString() {
            return operator + " /" + pattern + "/";
        }
    }

    private static final class NumberValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<TestResult, Void> visitor =
                new SimpleAnnotationValueVisitor8<TestResult, Void>(NOT_PASSED) {
                    @Override
                    public TestResult visitByte(byte b, Void __) {
                        return TestResult.fromBoolean(operator.satisfied(b, number.byteValue()));
                    }

                    @Override
                    public TestResult visitDouble(double d, Void __) {
                        return TestResult.fromBoolean(operator.satisfied(d, number.doubleValue()));
                    }

                    @Override
                    public TestResult visitFloat(float f, Void __) {
                        return TestResult.fromBoolean(operator.satisfied(f, number.floatValue()));
                    }

                    @Override
                    public TestResult visitInt(int i, Void __) {
                        return TestResult.fromBoolean(operator.satisfied(i, number.intValue()));
                    }

                    @Override
                    public TestResult visitLong(long i, Void __) {
                        return TestResult.fromBoolean(operator.satisfied(i, number.longValue()));
                    }

                    @Override
                    public TestResult visitShort(short s, Void __) {
                        return TestResult.fromBoolean(operator.satisfied(s, number.shortValue()));
                    }
                };

        private final Number number;

        private NumberValue(Operator operator, Number number) {
            super(operator);
            this.number = number;
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            return visitor.visit(value);
        }

        @Override
        public String toString() {
            return operator + " " + number;
        }
    }

    private static final class BooleanValue extends AnnotationValueMatch {
        private final boolean matchValue;

        private BooleanValue(Operator operator, boolean matchValue) {
            super(operator);
            this.matchValue = matchValue;
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            Object val = value.getValue();
            return TestResult.fromBoolean(val instanceof Boolean && operator.satisfied((Boolean) val, matchValue));
        }

        @Override
        public String toString() {
            return operator + " " + matchValue;
        }
    }

    private static final class AnyValue extends AnnotationValueMatch {
        private AnyValue(Operator operator) {
            super(operator);
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            return TestResult.fromBoolean(operator == EQ);
        }

        @Override
        public String toString() {
            return operator + " *";
        }
}

    private static final class AllValue extends AnnotationValueMatch {
        private AllValue() {
            super(EQ);
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            return TestResult.PASSED;
        }

        @Override
        public String toString() {
            return operator + " **";
        }
    }

    private static final class EnumValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<TestResult, MatchContext<?>> visitor =
                new SimpleAnnotationValueVisitor8<TestResult, MatchContext<?>>(NOT_PASSED) {
                    @Override
                    public TestResult visitEnumConstant(VariableElement c, MatchContext<?> ctx) {
                        return fqn.testInstance(c.asType(), ctx)
                                .and(() -> TestResult.fromBoolean(name.matches(c.getSimpleName().toString())));
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
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return NOT_PASSED;
            }

            TestResult res = visitor.visit(value, ctx);
            return (operator == EQ) ? res : res.negate();
        }

        @Override
        public String toString() {
            return operator + " " + fqn + "." + name;
        }
    }

    private static final class TypeValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<TestResult, MatchContext<?>> visitor =
                new SimpleAnnotationValueVisitor8<TestResult, MatchContext<?>>(NOT_PASSED) {
                    @Override
                    public TestResult visitType(TypeMirror t, MatchContext<?> ctx) {
                        return type.testInstance(t, ctx);
                    }
                };

        private final TypeReferenceMatch type;

        private TypeValue(Operator operator, TypeReferenceMatch type) {
            super(operator);
            this.type = type;
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return NOT_PASSED;
            }

            TestResult res = visitor.visit(value, ctx);
            return (operator == EQ) ? res : res.negate();
        }

        @Override
        public String toString() {
            return operator + " " + type;
        }
    }

    private static final class AnnoValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<TestResult, MatchContext<?>> visitor =
                new SimpleAnnotationValueVisitor8<TestResult, MatchContext<?>>(NOT_PASSED) {
                    @Override
                    public TestResult visitAnnotation(AnnotationMirror a, MatchContext<?> matchContext) {
                        return match.test(a, matchContext);
                    }
                };
        private final AnnotationMatch match;

        private AnnoValue(Operator operator, AnnotationMatch match) {
            super(operator);
            this.match = match;
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return NOT_PASSED;
            }

            TestResult res = visitor.visit(value, ctx);
            return (operator == EQ) ? res : res.negate();
        }

        @Override
        public String toString() {
            return operator + " " + match;
        }
    }

    private static final class ArrayValue extends AnnotationValueMatch {
        private final AnnotationValueVisitor<TestResult, MatchContext<?>> visitor =
                new SimpleAnnotationValueVisitor8<TestResult, MatchContext<?>>(NOT_PASSED) {
                    @Override
                    public TestResult visitArray(List<? extends AnnotationValue> vals, MatchContext<?> ctx) {
                        return match.test((m, v) -> m.test(v, ctx), vals);
                    }
                };

        private final Glob<AnnotationValueMatch> match;

        private ArrayValue(Operator operator, List<AnnotationValueMatch> matches) {
            super(operator);
            match = new Glob<>(matches);
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            if (operator != EQ && operator != NE) {
                return NOT_PASSED;
            }

            TestResult res = visitor.visit(value, ctx);
            return (operator == EQ) ? res : res.negate();
        }

        @Override
        public String toString() {
            return operator + " [" + match.getMatches().stream().map(Object::toString).collect(Collectors.joining(", "))
                    + "]";
        }
    }
}
