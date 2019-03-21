package org.revapi.classif.query.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.revapi.classif.match.declaration.ExtendsMatch;
import org.revapi.classif.match.declaration.ImplementsMatch;
import org.revapi.classif.match.declaration.TypeConstraintsMatch;
import org.revapi.classif.match.declaration.TypeKindMatch;
import org.revapi.classif.match.declaration.UsedByMatch;
import org.revapi.classif.match.declaration.UsesMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.TypeParametersMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.AbstractBuilderWithReferencedVariables;
import org.revapi.classif.query.match.declaration.ExtendsMatchBuilder;
import org.revapi.classif.query.match.declaration.ImplementsMatchBuilder;
import org.revapi.classif.query.match.declaration.TypeKindMatchBuilder;
import org.revapi.classif.query.match.declaration.UsedByMatchBuilder;
import org.revapi.classif.query.match.declaration.UsesMatchBuilder;
import org.revapi.classif.query.match.instance.FqnMatchBuilder;
import org.revapi.classif.query.match.instance.TypeParametersMatchBuilder;
import org.revapi.classif.statement.AbstractStatement;
import org.revapi.classif.statement.TypeDefinitionStatement;
import org.revapi.classif.util.Nullable;

public class TypeDefinitionStatementBuilder<P extends AbstractBuilder<P, ?, ?>>
        extends AbstractStatementBuilder<TypeDefinitionStatementBuilder<P>, TypeDefinitionStatement, P> {

    private TypeKindMatch kind;
    private FqnMatch fqn;
    private @Nullable TypeParametersMatch typeParams;
    private final List<ImplementsMatch> implemented = new ArrayList<>();
    private @Nullable ExtendsMatch extended;
    private final List<UsesMatch> uses = new ArrayList<>();
    private final List<UsedByMatch> usedBy = new ArrayList<>();
    private final List<AbstractStatement> children = new ArrayList<>();

    public TypeDefinitionStatementBuilder(P parent,
            BiConsumer<TypeDefinitionStatementBuilder<P>, TypeDefinitionStatement> productConsumer) {
        super(parent, productConsumer);
    }

    public TypeKindMatchBuilder<TypeDefinitionStatementBuilder<P>> kind() {
        return new TypeKindMatchBuilder<>(this, (b, p) -> kind = p);
    }

    public FqnMatchBuilder<TypeDefinitionStatementBuilder<P>> fqn() {
        return new FqnMatchBuilder<>(this, (b, p) -> fqn = p);
    }

    public TypeParametersMatchBuilder<TypeDefinitionStatementBuilder<P>> typeParameters() {
        return new TypeParametersMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            typeParams = p;
        });
    }

    public ImplementsMatchBuilder<TypeDefinitionStatementBuilder<P>> implements_() {
        return new ImplementsMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            implemented.add(p);
        });
    }

    public ExtendsMatchBuilder<TypeDefinitionStatementBuilder<P>> extends_() {
        return new ExtendsMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            extended = p;
        });
    }

    public UsesMatchBuilder<TypeDefinitionStatementBuilder<P>> uses() {
        return new UsesMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            uses.add(p);
        });
    }

    public UsedByMatchBuilder<TypeDefinitionStatementBuilder<P>> usedBy() {
        return new UsedByMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            this.usedBy.add(p);
        });
    }

    public FieldStatementBuilder<TypeDefinitionStatementBuilder<P>> field() {
        return new FieldStatementBuilder<>(this, (b, p) -> {
            // the variable references are owned by the field statement, so no copying of variables here
            children.add(p);
        });
    }

    public MethodStatementBuilder<TypeDefinitionStatementBuilder<P>> method() {
        return new MethodStatementBuilder<>(this, (b, p) -> {
            // the variable references are owned by the method statement, so no copying of variables here
            children.add(p);
        });
    }

    public TypeDefinitionStatementBuilder<TypeDefinitionStatementBuilder<P>> innerClass() {
        return new TypeDefinitionStatementBuilder<>(this, (b, p) -> {
            // the variable references are owned by the inner class statement, so no copying of variables here
            children.add(p);
        });
    }

    public P endType() {
        TypeDefinitionStatement ret = new TypeDefinitionStatement(definedVariable, referencedVariables, annotations,
                modifiers, kind, fqn, typeParams, new TypeConstraintsMatch(implemented, extended, uses, usedBy),
                negated, returned);

        ret.getChildren().addAll(children);

        return finish(ret);
    }

    private <B extends AbstractBuilderWithReferencedVariables<B, ?, ?>, R extends AbstractStatement> BiConsumer<B, R> acceptChild() {
        return (b, p) -> {
            copyVariableReferences(b);
            children.add(p);
        };
    }
}
