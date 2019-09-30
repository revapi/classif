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

import static java.util.Comparator.comparing;

import static javax.lang.model.type.TypeKind.ARRAY;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.VOID;

import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.match.Operator.EQ;
import static org.revapi.classif.match.Operator.NE;
import static org.revapi.classif.util.LogUtil.traceParams;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.util.Glob;
import org.revapi.classif.util.Globbed;
import org.revapi.classif.match.Operator;

public abstract class AnnotationValueMatch implements Globbed {
    private static final Logger LOG = LogManager.getLogger(AnnotationValueMatch.class);

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

    public static AnnotationValueMatch defaultValue(Operator operator) {
        return new DefaultValue(operator);
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

    public <M> TestResult test(ExecutableElement attribute, AnnotationValue value, MatchContext<M> ctx) {
        return LOG.traceExit(
                LOG.traceEntry(traceParams(LOG, "this", this, "attribute", attribute, "value", value, "ctx", ctx)),
                test(value, ctx));
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

    private static final class DefaultValue extends AnnotationValueMatch {
        private DefaultValue(Operator operator) {
            super(operator);
        }

        @Override
        public <M> TestResult test(ExecutableElement attribute, AnnotationValue value, MatchContext<M> ctx) {
            return LOG.traceExit(
                    LOG.traceEntry(traceParams(LOG, "this", this, "attribute", attribute, "value", value, "ctx", ctx)),
                    TestResult.fromBoolean(matchAgainstDefault(attribute, value)));
        }

        @Override
        public <M> TestResult test(AnnotationValue value, MatchContext<M> ctx) {
            throw new IllegalStateException("Default value match should never be invoked this way.");
        }

        @Override
        public String toString() {
            return operator + " default";
        }

        private boolean matchAgainstDefault(ExecutableElement attribute, AnnotationValue val) {
            return new ComparingValueVisitor<Boolean>(false) {
                @Override
                Boolean doVisitBoolean(boolean a, boolean b) {
                    return operator.satisfied(a, b);
                }

                @Override
                Boolean doVisitByte(byte a, byte b) {
                    return operator.satisfied(a, b);
                }

                @Override
                Boolean doVisitChar(char a, char b) {
                    return operator.satisfied(a, b);
                }

                @Override
                Boolean doVisitDouble(double a, double b) {
                    return operator.satisfied(a, b);
                }

                @Override
                Boolean doVisitFloat(float a, float b) {
                    return operator.satisfied(a, b);
                }

                @Override
                Boolean doVisitInt(int a, int b) {
                    return operator.satisfied(a, b);
                }

                @Override
                Boolean doVisitLong(long a, long b) {
                    return operator.satisfied(a, b);
                }

                @Override
                Boolean doVisitShort(short a, short b) {
                    return operator.satisfied(a, b);
                }

                @Override
                Boolean doVisitString(String a, String b) {
                    return operator.satisfied(a, b);
                }

                @Override
                Boolean doVisitType(TypeMirror a, TypeMirror b) {
                    switch (operator) {
                    case EQ:
                        return typesEqual(a, b);
                    case NE:
                        return !typesEqual(a, b);
                    default:
                        return false;
                    }
                }

                @Override
                Boolean doVisitEnumConstant(VariableElement a, VariableElement b) {
                    return Objects.equals(a.getConstantValue(), b.getConstantValue());
                }

                @Override
                Boolean doVisitAnnotation(AnnotationMirror a, AnnotationMirror b) {
                    if (!typesEqual(a.getAnnotationType(), b.getAnnotationType())) {
                        return false;
                    }

                    SortedMap<ExecutableElement, AnnotationValue> as = allAttributes(a);
                    SortedMap<ExecutableElement, AnnotationValue> bs = allAttributes(b);

                    if (as.size() != bs.size()) {
                        return false;
                    }

                    Iterator<Map.Entry<ExecutableElement, AnnotationValue>> ait = as.entrySet().iterator();
                    Iterator<Map.Entry<ExecutableElement, AnnotationValue>> bit = bs.entrySet().iterator();

                    while (ait.hasNext()) {
                        Map.Entry<ExecutableElement, AnnotationValue> ae = ait.next();
                        Map.Entry<ExecutableElement, AnnotationValue> be = bit.next();

                        if (!ae.getKey().getSimpleName().contentEquals(be.getKey().getSimpleName())) {
                            // oddly, the annotations have different sets of attributes (including the defaults) even though
                            // they have the same type
                            return false;
                        }

                        if (!this.visit(ae.getValue(), be.getValue())) {
                            return false;
                        }
                    }

                    return true;
                }

                @Override
                Boolean doVisitArray(List<? extends AnnotationValue> a, List<? extends AnnotationValue> b) {
                    if (a.size() != b.size()) {
                        return false;
                    }

                    for (int i = 0; i < a.size(); ++i) {
                        if (!this.visit(a.get(i), b.get(i))) {
                            return false;
                        }
                    }

                    return true;
                }

                private boolean typesEqual(TypeMirror ta, TypeMirror tb) {
                    return new SimpleTypeVisitor8<Boolean, TypeMirror>(false) {
                        @Override
                        public Boolean visitPrimitive(PrimitiveType t, TypeMirror tb) {
                            return t.getKind() == tb.getKind();
                        }

                        @Override
                        public Boolean visitArray(ArrayType t, TypeMirror tb) {
                            if (tb.getKind() != ARRAY) {
                                return false;
                            }

                            return visit(t.getComponentType(), ((ArrayType) tb).getComponentType());
                        }

                        @Override
                        public Boolean visitDeclared(DeclaredType t, TypeMirror tb) {
                            if (tb.getKind() != DECLARED) {
                                return false;
                            }

                            Element ae = t.asElement();
                            Element be = ((DeclaredType) tb).asElement();

                            if (ae.getKind() != be.getKind()) {
                                return false;
                            }

                            if (!(ae instanceof TypeElement)) {
                                return false;
                            }

                            return ((TypeElement) ae).getQualifiedName()
                                    .contentEquals(((TypeElement) be).getQualifiedName());
                        }

                        @Override
                        public Boolean visitError(ErrorType t, TypeMirror tb) {
                            return false;
                        }

                        @Override
                        public Boolean visitNoType(NoType t, TypeMirror tb) {
                            return t.getKind() == VOID && tb.getKind() == VOID;
                        }
                    }.visit(ta, tb);
                }

                private SortedMap<ExecutableElement, AnnotationValue> allAttributes(AnnotationMirror a) {
                    SortedMap<ExecutableElement, AnnotationValue> ret = new TreeMap<>(comparing(m -> m.getSimpleName().toString()));

                    ElementFilter.methodsIn(a.getAnnotationType().asElement().getEnclosedElements()).forEach(m -> {
                        ret.put(m, m.getDefaultValue());
                    });
                    ret.putAll(a.getElementValues());

                    return ret;
                }
            }.visit(attribute.getDefaultValue(), val);
        }

        private static abstract class ComparingValueVisitor<R> extends SimpleAnnotationValueVisitor8<R, AnnotationValue> {
            ComparingValueVisitor(R defaultValue) {
                super(defaultValue);
            }

            <T> R defaultMatchingAction(T a, T b) {
                return DEFAULT_VALUE;
            }

            R defaultNonMatchingAction(Object a, AnnotationValue b) {
                return defaultAction(a, b);
            }

            @Override
            public final R visitBoolean(boolean a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitBoolean(boolean b, Void __) {
                        return doVisitBoolean(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitBoolean(boolean a, boolean b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitByte(byte a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitByte(byte b, Void aVoid) {
                        return doVisitByte(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitByte(byte a, byte b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitChar(char a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitChar(char b, Void aVoid) {
                        return doVisitChar(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitChar(char a, char b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitDouble(double a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitDouble(double b, Void aVoid) {
                        return doVisitDouble(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitDouble(double a, double b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitFloat(float a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitFloat(float b, Void aVoid) {
                        return doVisitFloat(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitFloat(float a, float b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitInt(int a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitInt(int b, Void aVoid) {
                        return doVisitInt(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitInt(int a, int b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitLong(long a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitLong(long b, Void aVoid) {
                        return doVisitLong(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitLong(long a, long b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitShort(short a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitShort(short b, Void aVoid) {
                        return doVisitShort(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitShort(short a, short b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitString(String a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitString(String b, Void aVoid) {
                        return doVisitString(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitString(String a, String b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitType(TypeMirror a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitType(TypeMirror b, Void aVoid) {
                        return doVisitType(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitType(TypeMirror a, TypeMirror b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitEnumConstant(VariableElement a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitEnumConstant(VariableElement b, Void aVoid) {
                        return doVisitEnumConstant(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitEnumConstant(VariableElement a, VariableElement b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitAnnotation(AnnotationMirror a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitAnnotation(AnnotationMirror b, Void aVoid) {
                        return doVisitAnnotation(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitAnnotation(AnnotationMirror a, AnnotationMirror b) {
                return defaultMatchingAction(a, b);
            }

            @Override
            public final R visitArray(List<? extends AnnotationValue> a, AnnotationValue annotationValue) {
                return new Visitor(annotationValue) {
                    @Override
                    public R visitArray(List<? extends AnnotationValue> b, Void aVoid) {
                        return doVisitArray(a, b);
                    }
                }.visit(annotationValue);
            }

            R doVisitArray(List<? extends AnnotationValue> a, List<? extends AnnotationValue> b) {
                return defaultMatchingAction(a, b);
            }

            private class Visitor extends SimpleAnnotationValueVisitor8<R, Void> {
                private final AnnotationValue annoValue;

                private Visitor(AnnotationValue annoValue) {
                    this.annoValue = annoValue;
                }

                @Override
                protected R defaultAction(Object o, Void __) {
                    return defaultNonMatchingAction(o, annoValue);
                }
            }
        }
    }

}
