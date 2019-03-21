package org.revapi.classif.query.statement;

import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.declaration.UsesMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.match.declaration.UsesMatchBuilder;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;
import org.revapi.classif.statement.FieldStatement;

public class FieldStatementBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractStatementBuilder<FieldStatementBuilder<P>, FieldStatement, P> {
    private NameMatch name;
    private TypeReferenceMatch fieldType;
    private TypeReferenceMatch declaringType;
    private UsesMatch uses;

    public FieldStatementBuilder(P parent,
            BiConsumer<FieldStatementBuilder<P>, FieldStatement> productConsumer) {
        super(parent, productConsumer);
    }

    public FieldStatementBuilder<P> name(String name) {
        this.name = NameMatch.exact(name);
        return this;
    }

    public FieldStatementBuilder<P> nameMatching(Pattern pattern) {
        this.name = NameMatch.pattern(pattern);
        return this;
    }

    public FieldStatementBuilder<P> anyName() {
        this.name = NameMatch.any();
        return this;
    }

    public TypeReferenceMatchBuilder<FieldStatementBuilder<P>> ofOneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            this.fieldType = p;
        });
    }

    public TypeReferenceMatchBuilder<FieldStatementBuilder<P>> declaredInOneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
           copyVariableReferences(b);
           this.declaringType = p;
        });
    }

    public UsesMatchBuilder<FieldStatementBuilder<P>> uses() {
        return new UsesMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            uses = p;
        });
    }

    public P endField() {
        return finish(new FieldStatement(definedVariable, referencedVariables, annotations, modifiers, returned,
                negated, name, fieldType, declaringType, uses));
    }
}
