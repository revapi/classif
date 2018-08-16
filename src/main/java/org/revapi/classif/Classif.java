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
package org.revapi.classif;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import static org.revapi.classif.util.Operator.EQ;
import static org.revapi.classif.util.Operator.GE;
import static org.revapi.classif.util.Operator.GT;
import static org.revapi.classif.util.Operator.LE;
import static org.revapi.classif.util.Operator.LT;
import static org.revapi.classif.util.Operator.NE;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.declaration.AnnotationAttributeMatch;
import org.revapi.classif.match.declaration.AnnotationMatch;
import org.revapi.classif.match.declaration.AnnotationValueMatch;
import org.revapi.classif.match.declaration.AnnotationsMatch;
import org.revapi.classif.match.declaration.DeclarationMatch;
import org.revapi.classif.match.declaration.DefaultValueMatch;
import org.revapi.classif.match.declaration.ExtendsMatch;
import org.revapi.classif.match.declaration.ImplementsMatch;
import org.revapi.classif.match.declaration.MethodConstraintsMatch;
import org.revapi.classif.match.declaration.MethodParameterMatch;
import org.revapi.classif.match.declaration.ModifierClusterMatch;
import org.revapi.classif.match.declaration.ModifierMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.OverridesMatch;
import org.revapi.classif.match.declaration.ThrowsMatch;
import org.revapi.classif.match.declaration.TypeConstraintsMatch;
import org.revapi.classif.match.declaration.TypeKindMatch;
import org.revapi.classif.match.declaration.UsedByMatch;
import org.revapi.classif.match.declaration.UsesMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.SingleTypeReferenceMatch;
import org.revapi.classif.match.instance.TypeParameterMatch;
import org.revapi.classif.match.instance.TypeParameterWildcardMatch;
import org.revapi.classif.match.instance.TypeParametersMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.statement.AbstractStatement;
import org.revapi.classif.statement.FieldStatement;
import org.revapi.classif.statement.GenericStatement;
import org.revapi.classif.statement.MethodStatement;
import org.revapi.classif.statement.StatementStatement;
import org.revapi.classif.statement.TypeDefinitionStatement;
import org.revapi.classif.util.Operator;

/**
 * Classif is a structural matcher for Java declarations. It supports variables and nested statements and is cool.
 */
public final class Classif {

    private static final DecimalFormat NUMBER_FORMAT = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ROOT);

    static {
        NUMBER_FORMAT.setGroupingUsed(true);
        DecimalFormatSymbols symbols = NUMBER_FORMAT.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('_');
        NUMBER_FORMAT.setDecimalFormatSymbols(symbols);
    }

    private Classif() {
        throw new AssertionError("I shall not be summoned.");
    }

    /**
     * Compile a textural structural match into a matcher object.
     *
     * @param program a textual structural match "program" to be compiled into a matcher
     * @return a structural matcher to be used to match model elements against the provided program
     */
    public static StructuralMatcher compile(String program) {
        ANTLRErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                    int charPositionInLine, String msg, RecognitionException e) {
                throw new ParseCancellationException(ErrorFormatter.formatError(program, line, charPositionInLine, msg));
            }
        };

        ClassifLexer lexer = new ClassifLexer(CharStreams.fromString(program));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        ClassifParser parser = new ClassifParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return parser.program().accept(ProgramVisitor.INSTANCE);
    }

    private static Pattern toRegex(TerminalNode node) {
        return Pattern.compile(stringContents(node));
    }

    private static Number toNumber(TerminalNode node) {
        ParsePosition pos = new ParsePosition(0);
        String s = node.getText();
        Number ret = NUMBER_FORMAT.parse(s, pos);
        if (pos.getErrorIndex() != -1 || pos.getIndex() != s.length()) {
            throw new IllegalArgumentException("Could not parse '" + s + "' as a number");
        }

        return ret;
    }

    private static boolean toBoolean(TerminalNode node) {
        String s = node.getText();
        boolean val = false;
        if ("true".equals(s)) {
            val = true;
        } else if (!"false".equals(s)) {
            throw new IllegalArgumentException("Could not parse '" + s + "' as a boolean.");
        }
        return val;
    }

    private static String stringContents(TerminalNode node) {
        String all = node.getText();
        all = all.substring(1, all.length() - 1);
        return all;
    }

    // basically, Optional.ofNullable(object).map(action).orElse(null) but without the Optional instantiation
    private static <T, R> R ifNotNull(T object, Function<T, R> action) {
        if (object == null) {
            return null;
        } else {
            return action.apply(object);
        }
    }

    private static Operator toOperator(ClassifParser.OperatorContext ctx) {
        Operator op;
        if (ctx.EQ() != null) {
            op = EQ;
        } else if (ctx.NE() != null) {
            op = NE;
        } else if (ctx.LT() != null) {
            op = LT;
        } else if (ctx.LE() != null) {
            op = LE;
        } else if (ctx.GT() != null) {
            op = GT;
        } else if (ctx.GE() != null) {
            op = GE;
        } else {
            throw new IllegalArgumentException("Operator " + ctx.getText() + " not supported.");
        }

        return op;
    }
    
    private static final class ProgramVisitor extends ClassifBaseVisitor<StructuralMatcher> {
        static final ProgramVisitor INSTANCE = new ProgramVisitor();

        @Override
        public StructuralMatcher visitProgram(ClassifParser.ProgramContext ctx) {
            List<String> namedMatches = ifNotNull(ctx.matchStatement(), m -> m.accept(MatchStatementVisitor.INSTANCE));

            List<AbstractStatement> exprs = ctx.statement().stream()
                    .map(s -> s.accept(StatementVisitor.INSTANCE)).collect(toList());

            return new StructuralMatcher(namedMatches, exprs);
        }
    }

    private static final class ResolvedNameVisitor extends ClassifBaseVisitor<NameMatch> {
        static final ResolvedNameVisitor INSTANCE = new ResolvedNameVisitor();

        @Override
        public NameMatch visitResolvedName(ClassifParser.ResolvedNameContext ctx) {
            return NameMatch.exact(ctx.getText());
        }
    }

    private static final class MatchStatementVisitor extends ClassifBaseVisitor<List<String>> {
        static final MatchStatementVisitor INSTANCE = new MatchStatementVisitor();

        @Override
        public List<String> visitMatchStatement(ClassifParser.MatchStatementContext ctx) {
            return ctx.variables().variable().stream().map(v -> v.accept(VariableVisitor.INSTANCE)).collect(toList());
        }
    }

    private static final class VariableVisitor extends ClassifBaseVisitor<String> {
        static final VariableVisitor INSTANCE = new VariableVisitor();

        @Override
        public String visitVariable(ClassifParser.VariableContext ctx) {
            return ctx.resolvedName().getText();
        }
    }

    private static final class NameVisitor extends ClassifBaseVisitor<NameMatch> {
        static final NameVisitor INSTANCE = new NameVisitor();

        @Override
        public NameMatch visitName(ClassifParser.NameContext ctx) {
            NameMatch ret = ifNotNull(ctx.resolvedName(), c -> c.accept(ResolvedNameVisitor.INSTANCE));

            if (ret == null) {
                ret = ifNotNull(ctx.REGEX(), r -> NameMatch.pattern(toRegex(r)));

                if (ret == null) {
                    ret = ifNotNull(ctx.ANY(), r -> NameMatch.any());

                    if (ret == null) {
                        ret = ifNotNull(ctx.ANY_NUMBER_OF_THINGS(), r -> NameMatch.all());
                    }
                }
            }

            return ret;
        }
    }

    private static final class FqnVisitor extends ClassifBaseVisitor<FqnMatch> {
        static final FqnVisitor INSTANCE = new FqnVisitor();

        @Override
        public FqnMatch visitFqn(ClassifParser.FqnContext ctx) {
            return new FqnMatch(ctx.name().stream().map(NameVisitor.INSTANCE::visit).collect(toList()));
        }
    }

    private static final class StatementVisitor extends ClassifBaseVisitor<StatementStatement> {
        static final StatementVisitor INSTANCE = new StatementVisitor();

        @Override
        public StatementStatement visitStatement(ClassifParser.StatementContext ctx) {
            List<ReferencedVariablesAnd<AnnotationMatch>> annos = ctx.annotations().annotation().stream()
                    .map(a -> a.accept(AnnotationVisitor.INSTANCE)).collect(toList());

            ModifiersMatch modifiers = new ModifiersMatch(ctx.modifiers().modifierCluster().stream()
                    .map(cc -> new ModifierClusterMatch(cc.modifier().stream()
                            .map(mc -> {
                                boolean negation = mc.not() != null;
                                String text = negation ? mc.getText().substring(1) : mc.getText();
                                return new ModifierMatch(negation, text);
                            }).collect(toList())))
                    .collect(toList()));

            return new TypeDefinitionOrGenericStatementVisitor(annos, modifiers)
                    .visitTypeDefinitionOrGenericStatement(ctx.typeDefinitionOrGenericStatement());
        }
    }

    private static final class AnnotationVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<AnnotationMatch>> {
        static final AnnotationVisitor INSTANCE = new AnnotationVisitor();

        @Override
        public ReferencedVariablesAnd<AnnotationMatch> visitAnnotation(ClassifParser.AnnotationContext ctx) {
            ReferencedVariablesAnd<AnnotationMatch> ret = new ReferencedVariablesAnd<>();

            boolean negation = ctx.not() != null;

            ReferencedVariablesAnd<TypeReferenceMatch> type = ctx.typeReference().accept(TypeReferenceVisitor.INSTANCE);
            ret.referencedVariables.addAll(type.referencedVariables);

            List<AnnotationAttributeMatch> attrs;
            if (ctx.annotationAttributes() != null) {
                attrs = ctx.annotationAttributes().annotationAttribute().stream()
                        .map(attrctx -> {
                            ReferencedVariablesAnd<AnnotationAttributeMatch> attr =
                                    attrctx.accept(AnnotationAttributeVisitor.INSTANCE);

                            ret.referencedVariables.addAll(attr.referencedVariables);
                            return attr.match;
                        }).collect(toList());
            } else {
                attrs = emptyList();
            }

            ret.match = new AnnotationMatch(negation, type.match, attrs);

            return ret;
        }
    }

    private static final class AnnotationAttributeVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<AnnotationAttributeMatch>> {
        static final AnnotationAttributeVisitor INSTANCE = new AnnotationAttributeVisitor();

        @Override
        public ReferencedVariablesAnd<AnnotationAttributeMatch> visitAnnotationAttribute(
                ClassifParser.AnnotationAttributeContext ctx) {
            ReferencedVariablesAnd<AnnotationAttributeMatch> ret = new ReferencedVariablesAnd<>();

            if (ctx.ANY() != null) {
                ret.match = new AnnotationAttributeMatch(true, false, null, null);
            } else if (ctx.ANY_NUMBER_OF_THINGS() != null) {
                ret.match = new AnnotationAttributeMatch(false, true, null, null);
            } else {
                NameMatch name = ctx.name().accept(NameVisitor.INSTANCE);

                Operator op = toOperator(ctx.operator());

                ReferencedVariablesAnd<AnnotationValueMatch> value
                        = ctx.annotationValue().accept(new AnnotationValueVisitor(op));

                ret.referencedVariables.addAll(value.referencedVariables);

                ret.match = new AnnotationAttributeMatch(false, false, name, value.match);
            }

            return ret;
        }
    }

    private static final class AnnotationValueVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<AnnotationValueMatch>> {
        private final Operator operator;

        private AnnotationValueVisitor(Operator operator) {
            this.operator = operator;
        }

        @Override
        public ReferencedVariablesAnd<AnnotationValueMatch> visitAnnotationValue(
                ClassifParser.AnnotationValueContext ctx) {

            ReferencedVariablesAnd<AnnotationValueMatch> ret = new ReferencedVariablesAnd<>();

            if (ctx.STRING() != null) {
                ret.match = AnnotationValueMatch.string(operator, stringContents(ctx.STRING()));
            } else if (ctx.REGEX() != null) {
                ret.match = AnnotationValueMatch.regex(operator, toRegex(ctx.REGEX()));
            } else if (ctx.NUMBER() != null) {
                Number n = toNumber(ctx.NUMBER());
                ret.match = AnnotationValueMatch.number(operator, n);
            } else if (ctx.BOOLEAN() != null) {
                boolean val = toBoolean(ctx.BOOLEAN());

                ret.match = AnnotationValueMatch.bool(operator, val);
            } else if (ctx.ANY() != null) {
                ret.match = AnnotationValueMatch.any(operator);
            } else if (ctx.typeReference() != null) {
                ReferencedVariablesAnd<TypeReferenceMatch> type
                        = ctx.typeReference().accept(TypeReferenceVisitor.INSTANCE);

                ret.referencedVariables.addAll(type.referencedVariables);
                ret.match = AnnotationValueMatch.type(operator, type.match);
            } else if (ctx.fqn() != null) {
                FqnMatch fqn = ctx.fqn().accept(FqnVisitor.INSTANCE);
                NameMatch name = ctx.name().accept(NameVisitor.INSTANCE);

                ret.match = AnnotationValueMatch.enumConstant(operator, fqn, name);
            } else if (ctx.annotation() != null) {
                ReferencedVariablesAnd<AnnotationMatch> anno = ctx.annotation().accept(AnnotationVisitor.INSTANCE);
                ret.referencedVariables.addAll(anno.referencedVariables);
                ret.match = AnnotationValueMatch.annotation(operator, anno.match);
            } else if (ctx.OPEN_BRACE() != null) {
                if (ctx.annotationValueArrayContents() == null) {
                    ret.match = AnnotationValueMatch.array(operator, emptyList());
                } else {
                    List<ReferencedVariablesAnd<AnnotationValueMatch>> contents
                            = ctx.annotationValueArrayContents().accept(AnnotationValueArrayContentsVisitor.INSTANCE);

                    List<AnnotationValueMatch> vals = new ArrayList<>(contents.size());
                    for (ReferencedVariablesAnd<AnnotationValueMatch> m : contents) {
                        ret.referencedVariables.addAll(m.referencedVariables);
                        vals.add(m.match);
                    }

                    ret.match = AnnotationValueMatch.array(operator, vals);
                }
            }

            return ret;
        }
    }

    private static final class AnnotationValueArrayContentsVisitor extends ClassifBaseVisitor<List<ReferencedVariablesAnd<AnnotationValueMatch>>> {
        static final AnnotationValueArrayContentsVisitor INSTANCE = new AnnotationValueArrayContentsVisitor();

        @SuppressWarnings("Duplicates")
        @Override
        public List<ReferencedVariablesAnd<AnnotationValueMatch>> visitAnnotationValueArrayContents(
                ClassifParser.AnnotationValueArrayContentsContext ctx) {
            List<ReferencedVariablesAnd<AnnotationValueMatch>> ret = new ArrayList<>();

            ReferencedVariablesAnd<AnnotationValueMatch> firstForRecursion = null;

            if (ctx.annotationValueArray_strings() != null) {
                ClassifParser.AnnotationValueArray_stringsContext strCtx = ctx.annotationValueArray_strings();
                ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.string(EQ, stringContents(strCtx.STRING()))));
                strCtx.annotationValueArray_strings_next().forEach(n -> {
                    if (n.STRING() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.string(EQ, stringContents(n.STRING()))));
                    } else if (n.REGEX() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.regex(EQ, toRegex(n.REGEX()))));
                    } else if (n.ANY() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.any(EQ)));
                    } else if (n.ANY_NUMBER_OF_THINGS() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.all()));
                    }
                });
            } else if (ctx.annotationValueArray_numbers() != null) {
                ClassifParser.AnnotationValueArray_numbersContext numCtx = ctx.annotationValueArray_numbers();
                Number num = toNumber(numCtx.NUMBER());
                ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.number(EQ, num)));

                numCtx.annotationValueArray_numbers_next().forEach(n -> {
                    if (n.NUMBER() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.number(EQ, toNumber(n.NUMBER()))));
                    } else if (n.REGEX() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.regex(EQ, toRegex(n.REGEX()))));
                    } else if (n.ANY() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.any(EQ)));
                    } else if (n.ANY_NUMBER_OF_THINGS() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.all()));
                    }
                });
            } else if (ctx.annotationValueArray_booleans() != null) {
                ClassifParser.AnnotationValueArray_booleansContext boolCtx = ctx.annotationValueArray_booleans();
                ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.bool(EQ, toBoolean(boolCtx.BOOLEAN()))));

                boolCtx.annotationValueArray_booleans_next().forEach(n -> {
                    if (n.BOOLEAN() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.bool(EQ, toBoolean(n.BOOLEAN()))));
                    } else if (n.REGEX() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.regex(EQ, toRegex(n.REGEX()))));
                    } else if (n.ANY() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.any(EQ)));
                    } else if (n.ANY_NUMBER_OF_THINGS() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.all()));
                    }
                });
            } else if (ctx.annotationValueArray_types() != null) {
                ClassifParser.AnnotationValueArray_typesContext typeCtx = ctx.annotationValueArray_types();
                ReferencedVariablesAnd<TypeReferenceMatch> m = typeCtx.typeReference()
                        .accept(TypeReferenceVisitor.INSTANCE);
                ReferencedVariablesAnd<AnnotationValueMatch> am = new ReferencedVariablesAnd<>();
                am.referencedVariables.addAll(m.referencedVariables);
                am.match = AnnotationValueMatch.type(EQ, m.match);

                ret.add(am);

                typeCtx.annotationValueArray_types_next().forEach(n -> {
                    if (n.typeReference() != null) {
                        ReferencedVariablesAnd<TypeReferenceMatch> nm = n.typeReference().accept(TypeReferenceVisitor.INSTANCE);
                        ReferencedVariablesAnd<AnnotationValueMatch> nam = new ReferencedVariablesAnd<>();
                        nam.referencedVariables.addAll(nm.referencedVariables);
                        nam.match = AnnotationValueMatch.type(EQ, nm.match);

                        ret.add(nam);
                    } else if (n.ANY() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.any(EQ)));
                    } else if (n.ANY_NUMBER_OF_THINGS() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.all()));
                    }
                });
            } else if (ctx.annotationValueArray_enums() != null) {
                ClassifParser.AnnotationValueArray_enumsContext eCtx = ctx.annotationValueArray_enums();
                FqnMatch fqn = eCtx.fqn().accept(FqnVisitor.INSTANCE);
                NameMatch name = eCtx.name().accept(NameVisitor.INSTANCE);

                ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.enumConstant(EQ, fqn, name)));

                eCtx.annotationValueArray_enums_next().forEach(n -> {
                    if (n.fqn() != null) {
                        FqnMatch nextFqn = n.fqn().accept(FqnVisitor.INSTANCE);
                        NameMatch nextName = n.name().accept(NameVisitor.INSTANCE);
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.enumConstant(EQ, nextFqn, nextName)));
                    } else if (n.ANY() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.any(EQ)));
                    } else if (n.ANY_NUMBER_OF_THINGS() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.all()));
                    }
                });
            } else if (ctx.annotationValueArray_annotations() != null) {
                ClassifParser.AnnotationValueArray_annotationsContext annoCtx = ctx.annotationValueArray_annotations();
                ReferencedVariablesAnd<AnnotationMatch> m = annoCtx.annotation().accept(AnnotationVisitor.INSTANCE);
                ReferencedVariablesAnd<AnnotationValueMatch> am = new ReferencedVariablesAnd<>();
                am.referencedVariables.addAll(m.referencedVariables);
                am.match = AnnotationValueMatch.annotation(EQ, m.match);

                ret.add(am);

                annoCtx.annotationValueArray_annotations_next().forEach(n -> {
                    if (n.annotation() != null) {
                        ReferencedVariablesAnd<AnnotationMatch> nm = n.annotation().accept(AnnotationVisitor.INSTANCE);
                        ReferencedVariablesAnd<AnnotationValueMatch> nam = new ReferencedVariablesAnd<>();
                        nam.referencedVariables.addAll(nm.referencedVariables);
                        nam.match = AnnotationValueMatch.annotation(EQ, nm.match);

                        ret.add(nam);
                    } else if (n.ANY() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.any(EQ)));
                    } else if (n.ANY_NUMBER_OF_THINGS() != null) {
                        ret.add(new ReferencedVariablesAnd<>(AnnotationValueMatch.all()));
                    }
                });
            } else if (ctx.REGEX() != null) {
                firstForRecursion = new ReferencedVariablesAnd<>(AnnotationValueMatch.regex(EQ, toRegex(ctx.REGEX())));
            } else if (ctx.ANY() != null) {
                firstForRecursion = new ReferencedVariablesAnd<>(AnnotationValueMatch.any(EQ));
            } else if (ctx.ANY_NUMBER_OF_THINGS() != null) {
                firstForRecursion = new ReferencedVariablesAnd<>(AnnotationValueMatch.all());
            }

            if (firstForRecursion != null) {
                ret.add(firstForRecursion);

                if (ctx.annotationValueArrayContents() != null) {
                    List<ReferencedVariablesAnd<AnnotationValueMatch>> tail = ctx.annotationValueArrayContents().accept(this);
                    ret.addAll(tail);
                }
            }

            return ret;
        }
    }

    private static final class TypeReferenceVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<TypeReferenceMatch>> {
        static final TypeReferenceVisitor INSTANCE = new TypeReferenceVisitor();

        @Override
        public ReferencedVariablesAnd<TypeReferenceMatch> visitTypeReference(ClassifParser.TypeReferenceContext ctx) {
            ReferencedVariablesAnd<TypeReferenceMatch> ret = new ReferencedVariablesAnd<>();

            ret.match = new TypeReferenceMatch(ctx.singleTypeReference().stream().map(sctx -> {
                FqnMatch fqn = sctx.fqn() == null
                        ? null
                        : FqnVisitor.INSTANCE.visit(sctx.fqn());

                ReferencedVariablesAnd<TypeParametersMatch> tps = sctx.typeParameters() == null
                        ? null
                        : TypeParametersVisitor.INSTANCE.visit(sctx.typeParameters());

                if (tps != null) {
                    ret.referencedVariables.addAll(tps.referencedVariables);
                }

                String variable = sctx.variable() == null
                        ? null
                        : VariableVisitor.INSTANCE.visit(sctx.variable());

                if (variable != null) {
                    ret.referencedVariables.add(variable);
                }

                boolean negation = sctx.not() != null;

                int arrayDimension = sctx.arrayType().size();

                return new SingleTypeReferenceMatch(fqn, tps == null ? null : tps.match, variable, negation,
                        arrayDimension);
            }).collect(toList()));

            return ret;
        }
    }

    private static final class TypeParamWildcardVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<TypeParameterWildcardMatch>> {
        static final TypeParamWildcardVisitor INSTANCE = new TypeParamWildcardVisitor();

        @Override
        public ReferencedVariablesAnd<TypeParameterWildcardMatch> visitTypeParamWildcard(
                ClassifParser.TypeParamWildcardContext ctx) {

            ReferencedVariablesAnd<TypeParameterWildcardMatch> ret = new ReferencedVariablesAnd<>();

            boolean isExtends = ctx.EXTENDS() != null;

            List<TypeReferenceMatch> bounds = ctx.typeReference().stream().map(tr -> {
                ReferencedVariablesAnd<TypeReferenceMatch> m = TypeReferenceVisitor.INSTANCE.visitTypeReference(tr);
                ret.referencedVariables.addAll(m.referencedVariables);
                return m.match;
            }).collect(toList());

            ret.match = new TypeParameterWildcardMatch(isExtends, bounds);

            return ret;
        }
    }

    private static final class TypeParametersVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<TypeParametersMatch>> {
        static final TypeParametersVisitor INSTANCE = new TypeParametersVisitor();

        @Override
        public ReferencedVariablesAnd<TypeParametersMatch> visitTypeParameters(
                ClassifParser.TypeParametersContext ctx) {
            ReferencedVariablesAnd<TypeParametersMatch> ret = new ReferencedVariablesAnd<>();

            List<TypeParameterMatch> params = ctx.typeParam().stream().map(tp -> {
                if (tp.typeParamWildcard() != null) {
                    ReferencedVariablesAnd<TypeParameterWildcardMatch> w = TypeParamWildcardVisitor.INSTANCE.visitTypeParamWildcard(tp.typeParamWildcard());
                    ret.referencedVariables.addAll(w.referencedVariables);

                    return new TypeParameterMatch(w.match, emptyList());
                } else {
                    return new TypeParameterMatch(null, tp.typeReference().stream().map(tr -> {
                        ReferencedVariablesAnd<TypeReferenceMatch> m = TypeReferenceVisitor.INSTANCE.visitTypeReference(tr);
                        ret.referencedVariables.addAll(m.referencedVariables);
                        return m.match;
                    }).collect(toList()));
                }
            }).collect(toList());

            ret.match = new TypeParametersMatch(params);

            return ret;
        }
    }

    private static final class TypeDefinitionOrGenericStatementVisitor extends ClassifBaseVisitor<StatementStatement> {
        private final AnnotationsMatch annotations;
        private final ModifiersMatch modifiers;
        private final List<String> referenced;

        private TypeDefinitionOrGenericStatementVisitor(
                List<ReferencedVariablesAnd<AnnotationMatch>> annotations, ModifiersMatch modifiers) {
            this.referenced = new ArrayList<>();
            this.annotations = new AnnotationsMatch(annotations.stream().map(a -> {
                referenced.addAll(a.referencedVariables);
                return a.match;
            }).collect(toList()));
            this.modifiers = modifiers;
        }

        @Override
        public StatementStatement visitTypeDefinitionOrGenericStatement(
                ClassifParser.TypeDefinitionOrGenericStatementContext ctx) {
            if (ctx.typeKind() != null) {
                // type definition
                TypeKindMatch typeKind = TypeKindVisitor.INSTANCE.visitTypeKind(ctx.typeKind());
                boolean isMatch = ctx.returned() != null;
                List<String> reffed = new ArrayList<>(2);

                TypeConstraintsMatch constraints = null;

                if (ctx.typeConstraints() != null) {
                    ReferencedVariablesAnd<TypeConstraintsMatch> constrs =
                            TypeConstraintsVisitor.INSTANCE.visit(ctx.typeConstraints());
                    reffed.addAll(constrs.referencedVariables);
                    constraints = constrs.match;
                }

                if (ctx.possibleTypeAssignment() == null) {
                    reffed.addAll(referenced);
                    TypeDefinitionStatement type = new TypeDefinitionStatement(null, reffed, annotations, modifiers,
                            typeKind, new FqnMatch(singletonList(NameMatch.any())), null, constraints, false, isMatch);

                    for (ClassifParser.ElementStatementContext elementStatementContext : ctx.elementStatement()) {
                        type.getChildren().add(ElementStatementVisitor.INSTANCE.visit(elementStatementContext));
                    }

                    return type;
                } else {
                    FqnMatch fqn = FqnVisitor.INSTANCE.visit(ctx.possibleTypeAssignment().fqn());
                    ReferencedVariablesAnd<TypeParametersMatch> tps = null;
                    if (ctx.possibleTypeAssignment().typeParameters() != null) {
                        tps = TypeParametersVisitor.INSTANCE.visit(ctx.possibleTypeAssignment().typeParameters());
                    }

                    boolean negation = ctx.possibleTypeAssignment().not() != null;

                    String variable = null;
                    if (ctx.possibleTypeAssignment().assignment() != null) {
                        variable = ctx.possibleTypeAssignment().assignment().resolvedName().getText();
                    }

                    if (tps != null) {
                        reffed.addAll(tps.referencedVariables);
                    }

                    reffed.addAll(referenced);

                    TypeDefinitionStatement type =
                            new TypeDefinitionStatement(variable, reffed, annotations, modifiers, typeKind, fqn,
                            tps == null ? null : tps.match, constraints, negation, isMatch);

                    for (ClassifParser.ElementStatementContext elementStatementContext : ctx.elementStatement()) {
                        type.getChildren().add(ElementStatementVisitor.INSTANCE.visit(elementStatementContext));
                    }

                    return type;
                }
            } else {
                // generic statement
                boolean isMatch = ctx.returned() != null;
                boolean negation = ctx.not() != null;
                String variable = ctx.assignment() == null ? null : ctx.assignment().resolvedName().getText();

                ReferencedVariablesAnd<UsesMatch> uses = null;

                if (ctx.genericConstraints() != null) {
                    uses = GenericConstraintVisitor.INSTANCE.visit(ctx.genericConstraints());
                }

                return new GenericStatement(variable, uses == null ? emptyList() : uses.referencedVariables,
                        annotations, modifiers, isMatch, negation, uses == null ? null : uses.match);
            }
        }
    }

    private static final class TypeKindVisitor extends ClassifBaseVisitor<TypeKindMatch> {
        private static final TypeKindVisitor INSTANCE = new TypeKindVisitor();

        @Override
        public TypeKindMatch visitTypeKind(ClassifParser.TypeKindContext ctx) {
            boolean neg = ctx.not() != null;
            String typeKind = neg ? ctx.getText().substring(ctx.not().getText().length()) : ctx.getText();

            return new TypeKindMatch(neg, typeKind);
        }
    }

    private static final class GenericConstraintVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<UsesMatch>> {
        static final GenericConstraintVisitor INSTANCE = new GenericConstraintVisitor();

        @Override
        public ReferencedVariablesAnd<UsesMatch> visitGenericConstraints(ClassifParser.GenericConstraintsContext ctx) {
            ReferencedVariablesAnd<TypeReferenceMatch> type = ctx.typeReference().accept(TypeReferenceVisitor.INSTANCE);

            boolean onlyDirect = ctx.DIRECTLY() != null;

            ReferencedVariablesAnd<UsesMatch> ret = new ReferencedVariablesAnd<>();
            ret.referencedVariables = type.referencedVariables;
            ret.match = new UsesMatch(onlyDirect, type.match);

            return ret;
        }
    }

    private static final class TypeConstraintsVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<TypeConstraintsMatch>> {
        static final TypeConstraintsVisitor INSTANCE = new TypeConstraintsVisitor();

        @Override
        public ReferencedVariablesAnd<TypeConstraintsMatch> visitTypeConstraints(
                ClassifParser.TypeConstraintsContext ctx) {

            ReferencedVariablesAnd<TypeConstraintsMatch> ret = new ReferencedVariablesAnd<>();
            List<ImplementsMatch> implemented = new ArrayList<>(2);
            List<UsesMatch> uses = new ArrayList<>(2);
            List<UsedByMatch> usedBys = new ArrayList<>(2);
            ExtendsMatch[] extended = new ExtendsMatch[1];

            ctx.typeConstraint().forEach(constraintCtx -> {
                if (constraintCtx.IMPLEMENTS() != null) {
                    boolean onlyDirect = constraintCtx.DIRECTLY() != null;
                    boolean exact = constraintCtx.EXACTLY() != null;
                    List<TypeReferenceMatch> types = constraintCtx.typeReference().stream().map(typeRef -> {
                        ReferencedVariablesAnd<TypeReferenceMatch> m = TypeReferenceVisitor.INSTANCE.visit(typeRef);
                        ret.referencedVariables.addAll(m.referencedVariables);
                        return m.match;
                    }).collect(toList());

                    implemented.add(new ImplementsMatch(onlyDirect, exact, types));
                } else if (constraintCtx.USES() != null) {
                    boolean onlyDirect = constraintCtx.DIRECTLY() != null;
                    ReferencedVariablesAnd<TypeReferenceMatch> type =
                            constraintCtx.typeReference().get(0).accept(TypeReferenceVisitor.INSTANCE);

                    ret.referencedVariables.addAll(type.referencedVariables);
                    uses.add(new UsesMatch(onlyDirect, type.match));
                } else if (constraintCtx.EXTENDS() != null) {
                    boolean onlyDirect = constraintCtx.DIRECTLY() != null;
                    ReferencedVariablesAnd<TypeReferenceMatch> type =
                            constraintCtx.typeReference().get(0).accept(TypeReferenceVisitor.INSTANCE);

                    ret.referencedVariables.addAll(type.referencedVariables);
                    extended[0] = new ExtendsMatch(onlyDirect, type.match);
                } else if (constraintCtx.USED_BY() != null) {
                    boolean onlyDirect = constraintCtx.DIRECTLY() != null;
                    List<String> variables = constraintCtx.variables().variable().stream()
                            .map(v -> v.accept(VariableVisitor.INSTANCE)).collect(toList());

                    ret.referencedVariables.addAll(variables);
                    usedBys.add(new UsedByMatch(onlyDirect, variables));
                }
            });

            ret.match = new TypeConstraintsMatch(implemented, extended[0], uses, usedBys);
            return ret;
        }
    }

    private static final class ElementStatementVisitor extends ClassifBaseVisitor<StatementStatement> {
        static final ElementStatementVisitor INSTANCE = new ElementStatementVisitor();

        @Override
        public StatementStatement visitElementStatement(ClassifParser.ElementStatementContext ctx) {
            List<ReferencedVariablesAnd<AnnotationMatch>> annos = ctx.annotations().annotation().stream()
                    .map(a -> a.accept(AnnotationVisitor.INSTANCE)).collect(toList());

            ModifiersMatch modifiers = new ModifiersMatch(ctx.modifiers().modifierCluster().stream()
                    .map(cc -> new ModifierClusterMatch(cc.modifier().stream()
                            .map(mc -> {
                                boolean negation = mc.not() != null;
                                String text = negation ? mc.getText().substring(1) : mc.getText();
                                return new ModifierMatch(negation, text);
                            }).collect(toList())))
                    .collect(toList()));

            return new FieldOrMethodStatementVisitor(annos, modifiers)
                    .visitFieldOrMethodStatement(ctx.fieldOrMethodStatement());
        }
    }

    private static final class FieldOrMethodStatementVisitor extends ClassifBaseVisitor<StatementStatement> {
        private final List<ReferencedVariablesAnd<AnnotationMatch>> annotations;
        private final ModifiersMatch modifiers;

        private FieldOrMethodStatementVisitor(
                List<ReferencedVariablesAnd<AnnotationMatch>> annotations, ModifiersMatch modifiers) {
            this.annotations = annotations;
            this.modifiers = modifiers;
        }

        @Override
        public StatementStatement visitFieldOrMethodStatement(ClassifParser.FieldOrMethodStatementContext ctx) {
            if (ctx.typeParameters() != null) {
                ReferencedVariablesAnd<TypeParametersMatch> typeParams =
                        ctx.typeParameters().accept(TypeParametersVisitor.INSTANCE);

                return new MethodAfterTypeParametersStatementVisitor(annotations, modifiers, typeParams)
                        .visitMethodAfterTypeParametersStatement(ctx.methodAfterTypeParametersStatement());
            } else {
                return new FieldOrMethodWithoutTypeParametersVisitor(annotations, modifiers)
                        .visitFieldOrMethodWithoutTypeParameters(ctx.fieldOrMethodWithoutTypeParameters());
            }
        }
    }

    private static final class MethodAfterTypeParametersStatementVisitor extends ClassifBaseVisitor<MethodStatement> {
        private final List<ReferencedVariablesAnd<AnnotationMatch>> annotations;
        private final ModifiersMatch modifiers;
        private final ReferencedVariablesAnd<TypeParametersMatch> typeParams;

        MethodAfterTypeParametersStatementVisitor(
                List<ReferencedVariablesAnd<AnnotationMatch>> annotations,
                ModifiersMatch modifiers,
                ReferencedVariablesAnd<TypeParametersMatch> typeParams) {
            this.annotations = annotations;
            this.modifiers = modifiers;
            this.typeParams = typeParams;
        }

        @Override
        public MethodStatement visitMethodAfterTypeParametersStatement(
                ClassifParser.MethodAfterTypeParametersStatementContext ctx) {
            ReferencedVariablesAnd<TypeReferenceMatch> returnType = null;
            ReferencedVariablesAnd<TypeReferenceMatch> declaringType = null;

            List<ClassifParser.TypeReferenceContext> types = ctx.typeReference();
            if (types != null && !types.isEmpty()) {
                if (ctx.DOUBLE_COLON() != null) {
                    if (types.size() == 1) {
                        declaringType = types.get(0).accept(TypeReferenceVisitor.INSTANCE);
                    } else {
                        returnType = types.get(0).accept(TypeReferenceVisitor.INSTANCE);
                        declaringType = types.get(1).accept(TypeReferenceVisitor.INSTANCE);
                    }
                } else {
                    returnType = types.get(0).accept(TypeReferenceVisitor.INSTANCE);
                }
            }

            return new MethodNameAndRestStatementVisitor(annotations, modifiers, returnType, typeParams, declaringType)
                    .visit(ctx.methodNameAndRestStatement());
        }
    }

    private static final class FieldOrMethodWithoutTypeParametersVisitor extends ClassifBaseVisitor<StatementStatement> {
        private final List<ReferencedVariablesAnd<AnnotationMatch>> annotations;
        private final ModifiersMatch modifiers;

        FieldOrMethodWithoutTypeParametersVisitor(
                List<ReferencedVariablesAnd<AnnotationMatch>> annotations,
                ModifiersMatch modifiers) {
            this.annotations = annotations;
            this.modifiers = modifiers;
        }

        @Override
        public StatementStatement visitFieldOrMethodWithoutTypeParameters(
                ClassifParser.FieldOrMethodWithoutTypeParametersContext ctx) {
            ReferencedVariablesAnd<TypeReferenceMatch> type = null;
            if (ctx.typeReference() != null) {
                type = ctx.typeReference().accept(TypeReferenceVisitor.INSTANCE);
            }

            return new FieldNameOrMethodWithoutReturnType(annotations, modifiers, type)
                    .visitFieldNameOrMethodWithoutReturnType(ctx.fieldNameOrMethodWithoutReturnType());
        }
    }

    private static final class FieldNameOrMethodWithoutReturnType extends ClassifBaseVisitor<StatementStatement> {
        private final List<ReferencedVariablesAnd<AnnotationMatch>> annotations;
        private final ModifiersMatch modifiers;
        private final ReferencedVariablesAnd<TypeReferenceMatch> type;

        private FieldNameOrMethodWithoutReturnType(
                List<ReferencedVariablesAnd<AnnotationMatch>> annotations,
                ModifiersMatch modifiers,
                ReferencedVariablesAnd<TypeReferenceMatch> type) {
            this.annotations = annotations;
            this.modifiers = modifiers;
            this.type = type;
        }

        @Override
        public StatementStatement visitFieldNameOrMethodWithoutReturnType(
                ClassifParser.FieldNameOrMethodWithoutReturnTypeContext ctx) {
            ReferencedVariablesAnd<TypeReferenceMatch> declaringType = null;
            if (ctx.typeReference() != null) {
                declaringType = ctx.typeReference().accept(TypeReferenceVisitor.INSTANCE);
            }

            boolean isReturn = ctx.returned() != null;

            String definedName = ctx.assignment() == null ? null : ctx.assignment().resolvedName().getText();

            boolean negation = ctx.not() != null;

            NameMatch name = ctx.name().accept(NameVisitor.INSTANCE);

            if (ctx.methodRestStatement() != null) {
                return new MethodRestStatementVisitor(annotations, modifiers, type, declaringType, null, isReturn, negation,
                        name, definedName).visitMethodRestStatement(ctx.methodRestStatement());
            } else {
                List<String> referenced = new ArrayList<>(4);

                UsesMatch fieldConstraints = null;
                if (ctx.fieldConstraints() != null) {
                    ClassifParser.FieldConstraintsContext constraints = ctx.fieldConstraints();
                    ReferencedVariablesAnd<TypeReferenceMatch> useType = constraints.typeReference()
                            .accept(TypeReferenceVisitor.INSTANCE);
                    boolean direct = constraints.DIRECTLY() != null;
                    fieldConstraints = new UsesMatch(direct, useType.match);
                    referenced.addAll(useType.referencedVariables);
                }

                AnnotationsMatch annos = new AnnotationsMatch(annotations.stream()
                        .peek(ref -> referenced.addAll(ref.referencedVariables))
                        .map(ref -> ref.match)
                        .collect(toList()));

                if (type != null) {
                    referenced.addAll(type.referencedVariables);
                }

                if (declaringType != null) {
                    referenced.addAll(declaringType.referencedVariables);
                }

                return new FieldStatement(definedName, referenced, annos, modifiers, isReturn, negation, name,
                        type == null ? null : type.match, declaringType == null ? null : declaringType.match,
                        fieldConstraints);
            }
        }
    }

    private static final class MethodNameAndRestStatementVisitor extends ClassifBaseVisitor<MethodStatement> {
        private final List<ReferencedVariablesAnd<AnnotationMatch>> annotations;
        private final ModifiersMatch modifiers;
        private final ReferencedVariablesAnd<TypeReferenceMatch> returnType;
        private final ReferencedVariablesAnd<TypeReferenceMatch> declaringType;
        private final ReferencedVariablesAnd<TypeParametersMatch> typeParams;

        private MethodNameAndRestStatementVisitor(
                List<ReferencedVariablesAnd<AnnotationMatch>> annotations,
                ModifiersMatch modifiers,
                ReferencedVariablesAnd<TypeReferenceMatch> returnType,
                ReferencedVariablesAnd<TypeParametersMatch> typeParams,
                ReferencedVariablesAnd<TypeReferenceMatch> declaringType) {
            this.annotations = annotations;
            this.modifiers = modifiers;
            this.returnType = returnType;
            this.typeParams = typeParams;
            this.declaringType = declaringType;
        }

        @Override
        public MethodStatement visitMethodNameAndRestStatement(ClassifParser.MethodNameAndRestStatementContext ctx) {
            boolean isReturn = ctx.returned() != null;
            boolean negation = ctx.not() != null;
            NameMatch name = ctx.name().accept(NameVisitor.INSTANCE);
            String definedName = ctx.assignment() == null ? null : ctx.assignment().resolvedName().getText();

            return new MethodRestStatementVisitor(annotations, modifiers, returnType, declaringType, typeParams, isReturn, negation,
                    name, definedName).visitMethodRestStatement(ctx.methodRestStatement());
        }
    }

    private static final class MethodRestStatementVisitor extends ClassifBaseVisitor<MethodStatement> {
        private final List<ReferencedVariablesAnd<AnnotationMatch>> annotations;
        private final ModifiersMatch modifiers;
        private final ReferencedVariablesAnd<TypeReferenceMatch> returnType;
        private final ReferencedVariablesAnd<TypeReferenceMatch> declaringType;
        private final ReferencedVariablesAnd<TypeParametersMatch> typeParams;
        private final boolean isReturn;
        private final boolean negation;
        private final NameMatch name;
        private final String definedName;

        private MethodRestStatementVisitor(
                List<ReferencedVariablesAnd<AnnotationMatch>> annotations,
                ModifiersMatch modifiers,
                ReferencedVariablesAnd<TypeReferenceMatch> returnType,
                ReferencedVariablesAnd<TypeReferenceMatch> declaringType,
                ReferencedVariablesAnd<TypeParametersMatch> typeParams, boolean isReturn, boolean negation,
                NameMatch name, String definedName) {
            this.annotations = annotations;
            this.modifiers = modifiers;
            this.returnType = returnType;
            this.declaringType = declaringType;
            this.typeParams = typeParams;
            this.isReturn = isReturn;
            this.negation = negation;
            this.name = name;
            this.definedName = definedName;
        }

        @Override
        public MethodStatement visitMethodRestStatement(ClassifParser.MethodRestStatementContext ctx) {
            ReferencedVariablesAnd<List<MethodParameterMatch>> params = null;
            ReferencedVariablesAnd<MethodConstraintsMatch> constraints = null;

            List<String> reffed = new ArrayList<>();

            if (ctx.parameterList() != null) {
                params = ctx.parameterList().accept(ParameterListVisitor.INSTANCE);
                reffed.addAll(params.referencedVariables);
            }

            if (ctx.methodConstraints() != null) {
                constraints = ctx.methodConstraints().accept(MethodConstraintsVisitor.INSTANCE);
                reffed.addAll(constraints.referencedVariables);
            }

            AnnotationsMatch annos = null;
            if (!annotations.isEmpty()) {
                List<AnnotationMatch> ams = new ArrayList<>(annotations.size());
                annotations.forEach(a -> {
                    reffed.addAll(a.referencedVariables);
                    ams.add(a.match);
                });

                annos = new AnnotationsMatch(ams);
            }

            if (returnType != null) {
                reffed.addAll(returnType.referencedVariables);
            }

            if (declaringType != null) {
                reffed.addAll(declaringType.referencedVariables);
            }

            if (typeParams != null) {
                reffed.addAll(typeParams.referencedVariables);
            }

            return new MethodStatement(definedName, reffed, annos, modifiers, isReturn, name,
                    returnType == null ? null : returnType.match, declaringType == null ? null : declaringType.match,
                    typeParams == null ? null : typeParams.match, params == null ? emptyList() : params.match,
                    constraints == null ? null : constraints.match, negation);
        }
    }

    private static final class ParameterListVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<List<MethodParameterMatch>>> {
        static final ParameterListVisitor INSTANCE = new ParameterListVisitor();

        @Override
        public ReferencedVariablesAnd<List<MethodParameterMatch>> visitParameterList(ClassifParser.ParameterListContext ctx) {
            ReferencedVariablesAnd<List<MethodParameterMatch>> ret = new ReferencedVariablesAnd<>();
            ret.match = new ArrayList<>();

            ctx.methodParameter().forEach(p -> {
                AnnotationsMatch annos = null;
                if (p.annotations() != null) {
                    annos = new AnnotationsMatch(p.annotations().annotation().stream()
                            .map(a -> a.accept(AnnotationVisitor.INSTANCE))
                            .peek(a -> ret.referencedVariables.addAll(a.referencedVariables))
                            .map(a -> a.match)
                            .collect(toList()));
                }

                ReferencedVariablesAnd<TypeReferenceMatch> type = p.typeReference().accept(TypeReferenceVisitor.INSTANCE);
                ret.referencedVariables.addAll(type.referencedVariables);

                ret.match.add(new MethodParameterMatch(annos, type.match));
            });

            return ret;
        }
    }

    private static final class MethodConstraintsVisitor extends ClassifBaseVisitor<ReferencedVariablesAnd<MethodConstraintsMatch>> {
        static final MethodConstraintsVisitor INSTANCE = new MethodConstraintsVisitor();

        @Override
        public ReferencedVariablesAnd<MethodConstraintsMatch> visitMethodConstraints(
                ClassifParser.MethodConstraintsContext ctx) {

            ReferencedVariablesAnd<MethodConstraintsMatch> ret = new ReferencedVariablesAnd<>();
            List<ClassifParser.MethodConstraintContext> mCtxs = ctx.methodConstraint();
            List<DeclarationMatch> constraints = new ArrayList<>(mCtxs.size());

            for (ClassifParser.MethodConstraintContext mctx : mCtxs) {
                if (mctx.USES() != null) {
                    boolean directly = mctx.DIRECTLY() != null;
                    ReferencedVariablesAnd<TypeReferenceMatch> type = mctx.typeReference().get(0).accept(TypeReferenceVisitor.INSTANCE);

                    constraints.add(new UsesMatch(directly, type.match));
                    ret.referencedVariables.addAll(type.referencedVariables);
                } else if (mctx.THROWS() != null) {
                    List<ClassifParser.TypeReferenceContext> thrownCtx = mctx.typeReference();
                    List<TypeReferenceMatch> thrown = new ArrayList<>(thrownCtx.size());
                    for (ClassifParser.TypeReferenceContext tctx : thrownCtx) {
                        ReferencedVariablesAnd<TypeReferenceMatch> type = tctx.accept(TypeReferenceVisitor.INSTANCE);
                        thrown.add(type.match);
                        ret.referencedVariables.addAll(type.referencedVariables);
                    }

                    constraints.add(new ThrowsMatch(thrown));
                } else if (mctx.OVERRIDES() != null) {
                    List<ClassifParser.TypeReferenceContext> types = mctx.typeReference();
                    ClassifParser.TypeReferenceContext typeCtx = types.isEmpty() ? null : types.get(0);
                    ReferencedVariablesAnd<TypeReferenceMatch> type = typeCtx == null ? null : typeCtx.accept(TypeReferenceVisitor.INSTANCE);

                    constraints.add(new OverridesMatch(type == null ? null : type.match));
                    if (type != null) {
                        ret.referencedVariables.addAll(type.referencedVariables);
                    }
                } else if (mctx.DEFAULT() != null) {
                    ReferencedVariablesAnd<AnnotationValueMatch> match = mctx.NO() != null
                            ? null
                            : mctx.annotationValue().accept(new AnnotationValueVisitor(toOperator(mctx.operator())));

                    constraints.add(new DefaultValueMatch(match == null ? null : match.match));
                    if (match != null) {
                        ret.referencedVariables.addAll(match.referencedVariables);
                    }
                }
            }

            ret.match = new MethodConstraintsMatch(constraints);

            return ret;
        }
    }

    private static final class ReferencedVariablesAnd<T> {
        List<String> referencedVariables = new ArrayList<>(2);
        T match;

        ReferencedVariablesAnd() {

        }

        ReferencedVariablesAnd(T match) {
            this.match = match;
        }
    }
}
