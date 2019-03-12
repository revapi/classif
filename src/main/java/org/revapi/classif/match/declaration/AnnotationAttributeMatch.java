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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
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

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.util.Globbed;
import org.revapi.classif.util.Nullable;
import org.revapi.classif.util.Operator;

public final class AnnotationAttributeMatch implements Globbed {
    private final boolean isAny;
    private final boolean isAll;
    private final @Nullable NameMatch name;
    private final @Nullable AnnotationValueMatch valueMatch;
    private final @Nullable Operator matchAgainstDefault;

    public AnnotationAttributeMatch(boolean isAny, boolean isAll, @Nullable NameMatch name,
            @Nullable Operator matchAgainstDefault, @Nullable AnnotationValueMatch valueMatch) {
        this.isAny = isAny;
        this.isAll = isAll;
        this.name = name;
        this.matchAgainstDefault = matchAgainstDefault;
        this.valueMatch = valueMatch;
    }

    @Override
    public boolean isMatchAny() {
        return isAny || (name != null && valueMatch != null && name.isMatchAny() && valueMatch.isMatchAny());
    }

    @Override
    public boolean isMatchAll() {
        return isAll;
    }

    public <M> TestResult test(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> attribute,
            MatchContext<M> matchContext) {
        return TestResult.fromBoolean(isMatchAny() || isMatchAll()
                || (name == null || name.matches(attribute.getKey().getSimpleName().toString())))
                .and(() -> {
                    if (valueMatch == null) {
                        if (matchAgainstDefault == null) {
                            return TestResult.PASSED;
                        } else {
                            return TestResult.fromBoolean(matchAgainstDefault(attribute.getKey(), attribute.getValue()));
                        }
                    } else {
                        return valueMatch.test(attribute.getValue(), matchContext);
                    }
                });
    }

    @Override
    public String toString() {
        if (isAny) {
            return "*";
        }

        if (isAll) {
            return "**";
        }

        String ret = "" + name;
        if (matchAgainstDefault != null) {
            ret += matchAgainstDefault + " default";
        } else {
            ret += valueMatch;
        }

        return ret;
    }

    private boolean matchAgainstDefault(ExecutableElement attribute, AnnotationValue val) {
        assert matchAgainstDefault != null;

        return new ComparingValueVisitor<Boolean>(false) {
            @Override
            Boolean doVisitBoolean(boolean a, boolean b) {
                return matchAgainstDefault.satisfied(a, b);
            }

            @Override
            Boolean doVisitByte(byte a, byte b) {
                return matchAgainstDefault.satisfied(a, b);
            }

            @Override
            Boolean doVisitChar(char a, char b) {
                return matchAgainstDefault.satisfied(a, b);
            }

            @Override
            Boolean doVisitDouble(double a, double b) {
                return matchAgainstDefault.satisfied(a, b);
            }

            @Override
            Boolean doVisitFloat(float a, float b) {
                return matchAgainstDefault.satisfied(a, b);
            }

            @Override
            Boolean doVisitInt(int a, int b) {
                return matchAgainstDefault.satisfied(a, b);
            }

            @Override
            Boolean doVisitLong(long a, long b) {
                return matchAgainstDefault.satisfied(a, b);
            }

            @Override
            Boolean doVisitShort(short a, short b) {
                return matchAgainstDefault.satisfied(a, b);
            }

            @Override
            Boolean doVisitString(String a, String b) {
                return matchAgainstDefault.satisfied(a, b);
            }

            @Override
            Boolean doVisitType(TypeMirror a, TypeMirror b) {
                switch (matchAgainstDefault) {
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
