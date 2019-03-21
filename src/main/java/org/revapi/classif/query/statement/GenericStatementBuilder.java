package org.revapi.classif.query.statement;

import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.UsesMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.match.declaration.UsesMatchBuilder;
import org.revapi.classif.statement.GenericStatement;

public class GenericStatementBuilder<P extends AbstractBuilder<P, ?, ?>>
        extends AbstractStatementBuilder<GenericStatementBuilder<P>, GenericStatement, P> {

    private UsesMatch uses;

    public GenericStatementBuilder(P parent, BiConsumer<GenericStatementBuilder<P>, GenericStatement> productConsumer) {
        super(parent, productConsumer);
    }

    @Override
    public List<String> getVariableReferences() {
        return referencedVariables;
    }

    public UsesMatchBuilder<GenericStatementBuilder<P>> uses() {
        return new UsesMatchBuilder<>(this, (b, u) -> {
            this.uses = u;
            copyVariableReferences(b);
        });
    }

    public P endDeclaration() {
        GenericStatement statement = new GenericStatement(definedVariable, referencedVariables, annotations, modifiers, returned, negated, uses);
        return finish(statement);
    }
}
