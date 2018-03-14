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

import static java.util.stream.Collectors.toList;

import java.util.List;
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
import org.revapi.classif.match.AnnotationMatch;
import org.revapi.classif.match.ModifierClusterMatch;
import org.revapi.classif.match.ModifierMatch;
import org.revapi.classif.match.ModifiersMatch;
import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.TypeKindMatch;
import org.revapi.classif.match.TypeReferenceMatch;
import org.revapi.classif.statement.AbstractStatement;
import org.revapi.classif.statement.StatementStatement;

public final class Classif {

    private Classif() {
        throw new AssertionError("I shall not be summoned.");
    }

    public static StructuralMatcher compile(String program) {
        ANTLRErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
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
        String all = node.getText();
        all = all.substring(1, all.length() - 1);
        return Pattern.compile(all);
    }

    // basically, Optional.ofNullable(object).map(action).orElse(null) but without the Optional instantiation
    private static <T, R> R ifNotNull(T object, Function<T, R> action) {
        if (object == null) {
            return null;
        } else {
            return action.apply(object);
        }
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
            NameMatch ret = ifNotNull(ctx.resolvedName(), c ->  c.accept(ResolvedNameVisitor.INSTANCE));

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

    private static final class StatementVisitor extends ClassifBaseVisitor<StatementStatement> {
        static final StatementVisitor INSTANCE = new StatementVisitor();
        @Override
        public StatementStatement visitStatement(ClassifParser.StatementContext ctx) {
            List<AnnotationMatch> annos = ctx.annotations().annotation().stream()
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

    private static final class AnnotationVisitor extends ClassifBaseVisitor<AnnotationMatch> {
        static final AnnotationVisitor INSTANCE = new AnnotationVisitor();

    }

    private static final class TypeReferenceVisitor extends ClassifBaseVisitor<TypeReferenceMatch> {
        static final TypeReferenceVisitor INSTANCE = new TypeReferenceVisitor();

        @Override
        public TypeReferenceMatch visitTypeReference(ClassifParser.TypeReferenceContext ctx) {
            // TODO implement
            return null;
        }
    }

    private static final class TypeDefinitionOrGenericStatementVisitor extends ClassifBaseVisitor<StatementStatement> {
        private final List<AnnotationMatch> annotations;
        private final ModifiersMatch modifiers;

        private TypeDefinitionOrGenericStatementVisitor(
                List<AnnotationMatch> annotations, ModifiersMatch modifiers) {
            this.annotations = annotations;
            this.modifiers = modifiers;
        }

        @Override
        public StatementStatement visitTypeDefinitionOrGenericStatement(
                ClassifParser.TypeDefinitionOrGenericStatementContext ctx) {
            if (ctx.typeKind() != null) {
                // type definition
                TypeKindMatch typeKind = TypeKindVisitor.INSTANCE.visitTypeKind(ctx.typeKind());
                // TODO implement
            } else {
                // generic statement
                // TODO implement
            }
            return super.visitTypeDefinitionOrGenericStatement(ctx);
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
}
