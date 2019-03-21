package org.revapi.classif.query.statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import org.revapi.classif.match.NameMatch;
import org.revapi.classif.match.declaration.DeclarationMatch;
import org.revapi.classif.match.declaration.DefaultValueMatch;
import org.revapi.classif.match.declaration.MethodConstraintsMatch;
import org.revapi.classif.match.declaration.MethodParameterMatch;
import org.revapi.classif.match.instance.FqnMatch;
import org.revapi.classif.match.instance.SingleTypeReferenceMatch;
import org.revapi.classif.match.instance.TypeParametersMatch;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.query.AbstractBuilder;
import org.revapi.classif.query.match.declaration.AnnotationValueMatchBuilder;
import org.revapi.classif.query.match.declaration.MethodParameterMatchBuilder;
import org.revapi.classif.query.match.declaration.OverridesMatchBuilder;
import org.revapi.classif.query.match.declaration.ThrowsMatchBuilder;
import org.revapi.classif.query.match.declaration.UsesMatchBuilder;
import org.revapi.classif.query.match.instance.TypeParametersMatchBuilder;
import org.revapi.classif.query.match.instance.TypeReferenceMatchBuilder;
import org.revapi.classif.statement.MethodStatement;
import org.revapi.classif.util.Nullable;
import org.revapi.classif.util.Operator;

public class MethodStatementBuilder<P extends AbstractBuilder<P, ?, ?>> extends AbstractStatementBuilder<MethodStatementBuilder<P>, MethodStatement, P> {
    private NameMatch name;
    private @Nullable TypeReferenceMatch returnType;
    private @Nullable TypeReferenceMatch declaringType;
    private @Nullable TypeParametersMatch typeParams;
    private final List<MethodParameterMatch> parameters = new ArrayList<>();
    private final List<DeclarationMatch> constraints = new ArrayList<>();

    public MethodStatementBuilder(P parent,
            BiConsumer<MethodStatementBuilder<P>, MethodStatement> productConsumer) {
        super(parent, productConsumer);
    }

    public MethodStatementBuilder<P> name(String name) {
        this.name = NameMatch.exact(name);
        return this;
    }

    public MethodStatementBuilder<P> nameMatching(Pattern pattern) {
        this.name = NameMatch.pattern(pattern);
        return this;
    }

    public MethodStatementBuilder<P> anyName() {
        this.name = NameMatch.any();
        return this;
    }

    public TypeReferenceMatchBuilder<MethodStatementBuilder<P>> returnsOneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            returnType = p;
        });
    }

    public TypeReferenceMatchBuilder<MethodStatementBuilder<P>> declaredInOneOfTypes() {
        return new TypeReferenceMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            declaringType = p;
        });
    }

    public TypeParametersMatchBuilder<MethodStatementBuilder<P>> typeParameters() {
        return new TypeParametersMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            typeParams = p;
        });
    }

    public UsesMatchBuilder<MethodStatementBuilder<P>> uses() {
        return new UsesMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            constraints.add(p);
        });
    }

    public ThrowsMatchBuilder<MethodStatementBuilder<P>> throws_() {
        return new ThrowsMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            constraints.add(p);
        });
    }

    public OverridesMatchBuilder<MethodStatementBuilder<P>> overrides() {
        return new OverridesMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            constraints.add(p);
        });
    }

    public AnnotationValueMatchBuilder<MethodStatementBuilder<P>> defaultValue(Operator operator) {
        return new AnnotationValueMatchBuilder<>(this, operator, (b, p) -> {
            copyVariableReferences(b);
            constraints.add(new DefaultValueMatch(p));
        });
    }

    public MethodStatementBuilder<P> anyDefaultValue() {
        constraints.add(new DefaultValueMatch(null));
        return this;
    }

    public MethodParameterMatchBuilder<MethodStatementBuilder<P>> parameter() {
        return new MethodParameterMatchBuilder<>(this, (b, p) -> {
            copyVariableReferences(b);
            parameters.add(p);
        });
    }

    public P endMethod() {
        return finish(new MethodStatement(definedVariable, referencedVariables, annotations, modifiers, returned, name,
                returnType, declaringType, typeParams, parameters, new MethodConstraintsMatch(constraints), negated));
    }
}
