package org.revapi.classif;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.Modifier;
import org.revapi.classif.query.statement.AbstractStatementBuilder;
import org.revapi.classif.query.statement.GenericStatementBuilder;
import org.revapi.classif.query.statement.TypeDefinitionStatementBuilder;
import org.revapi.classif.statement.AbstractStatement;
import org.revapi.classif.util.Operator;

public final class Classif {

    private Classif() {
        throw new AssertionError("No!");
    }

    public static QueryBuilder match() {
        return new QueryBuilder();
    }

    public static final class QueryBuilder extends AbstractBuilder<QueryBuilder, AbstractStatement, QueryBuilder> {
        private final List<AbstractStatement> statements = new ArrayList<>();
        private final List<String> returns = new ArrayList<>();
        private boolean strictHierarchy;

        @SuppressWarnings("UnusedAssignment")
        private BiConsumer<? extends AbstractStatementBuilder<?, ?, QueryBuilder>, ? extends AbstractStatement>
                statementHandler = (b, p) -> {
            if (b.isReturn()) {
                returns.add(b.getDefinedVariable());
            }
            statements.add(p);
        };

        private QueryBuilder() {
            super(null, (__, ___) -> {
            });
            throw new AssertionError("No!");
        }

        public GenericStatementBuilder<QueryBuilder> declaration() {
            return new GenericStatementBuilder<>(this, castHandler());
        }

        public TypeDefinitionStatementBuilder<QueryBuilder> type() {
            return new TypeDefinitionStatementBuilder<>(this, castHandler());
        }

        public QueryBuilder returnVariables(String... vars) {
            return returnVariables(asList(vars));
        }

        public QueryBuilder returnVariables(Collection<String> vars) {
            returns.addAll(vars);
            return this;
        }

        public QueryBuilder strictHierarchy() {
            this.strictHierarchy = true;
            return this;
        }

        public StructuralMatcher create() {
            return new StructuralMatcher(new StructuralMatcher.Configuration(strictHierarchy), returns, statements);
        }

        @SuppressWarnings("unchecked")
        private <S, B extends AbstractStatementBuilder<B, S, QueryBuilder>> BiConsumer<B, S> castHandler() {
            return (BiConsumer<B, S>) statementHandler;
        }
    }
}
