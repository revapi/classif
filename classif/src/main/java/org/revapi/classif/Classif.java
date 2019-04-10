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
package org.revapi.classif;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static org.revapi.classif.match.Operator.EQ;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

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
import org.revapi.classif.match.declaration.Modifier;
import org.revapi.classif.match.declaration.ModifierClusterMatch;
import org.revapi.classif.match.declaration.ModifierMatch;
import org.revapi.classif.match.declaration.ModifiersMatch;
import org.revapi.classif.match.declaration.OverridesMatch;
import org.revapi.classif.match.declaration.ThrowsMatch;
import org.revapi.classif.match.declaration.TypeConstraintsMatch;
import org.revapi.classif.match.declaration.TypeKind;
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
import org.revapi.classif.statement.TypeDefinitionStatement;
import org.revapi.classif.util.Nullable;
import org.revapi.classif.match.Operator;

@SuppressWarnings("unused")
public final class Classif {

    private Classif() {
        throw new AssertionError("No!");
    }

    public static Builder match() {
        return new Builder(emptyList());
    }

    public static Builder match(String var, String... vars) {
        List<String> rets = new ArrayList<>(vars.length + 1);
        rets.add(var);
        rets.addAll(asList(vars));

        return new Builder(rets);
    }

    public static Builder match(Collection<String> vars) {
        return new Builder(vars);
    }

    public static GenericStatementBuilder declaration() {
        return new GenericStatementBuilder();
    }

    public static FieldStatementBuilder field(NameMatch name) {
        return new FieldStatementBuilder(name);
    }

    public static MethodStatementBuilder method(NameMatch name) {
        return new MethodStatementBuilder(name);
    }

    public static TypeDefinitionStatementBuilder type(TypeKind kind, NameMatch fqnFirstPart, NameMatch... fqnRest) {
        return type(new TypeKindBuilder(kind), fqn(fqnFirstPart, fqnRest));
    }

    public static TypeDefinitionStatementBuilder type(TypeKind kind, FqnMatchBuilder fqn) {
        return new TypeDefinitionStatementBuilder(new TypeKindBuilder(kind), fqn);
    }

    public static TypeDefinitionStatementBuilder type(TypeKindBuilder kind, NameMatch fqnFirstPart, NameMatch... fqnRest) {
        return new TypeDefinitionStatementBuilder(kind, fqn(fqnFirstPart, fqnRest));
    }

    public static TypeDefinitionStatementBuilder type(TypeKindBuilder kind, FqnMatchBuilder fqn) {
        return new TypeDefinitionStatementBuilder(kind, fqn);
    }

    public static FqnMatchBuilder fqn(NameMatch name, NameMatch... names) {
        ArrayList<NameMatch> parts = new ArrayList<>(names.length + 1);
        parts.add(name);
        for (NameMatch n : names) {
            parts.add(n);
        }

        return new FqnMatchBuilder(parts);
    }

    public static TypeParameterMatchBuilder wildcard() {
        return new TypeParameterWildcardBuilder(true);
    }

    public static TypeParameterWildcardBuilder wildcardExtends(TypeReferenceMatchBuilder type) {
        return new TypeParameterWildcardBuilder(true).and(type);
    }

    public static TypeParameterWildcardBuilder wildcardSuper(TypeReferenceMatchBuilder type) {
        return new TypeParameterWildcardBuilder(false).and(type);
    }

    public static TypeParameterBoundBuilder bound(TypeReferenceMatchBuilder type) {
        return new TypeParameterBoundBuilder().and(type);
    }

    public static TypeReferenceMatchBuilder type() {
        return new TypeReferenceMatchBuilder();
    }

    public static TypeReferenceMatchBuilder anyType() {
        return type().fqn(NameMatch.any());
    }

    public static TypeReferenceMatchBuilder anyTypes() {
        return type().fqn(NameMatch.all());
    }

    public static UsesMatchBuilder uses(TypeReferenceMatchBuilder type) {
        return new UsesMatchBuilder(type);
    }

    public static UsedByMatchBuilder usedBy(String var, String... vars) {
        List<String> vs = new ArrayList<>(vars.length + 1);
        vs.add(var);
        vs.addAll(asList(vars));

        return new UsedByMatchBuilder(vs);
    }

    public static TypeKindBuilder kind(TypeKind kind) {
        return new TypeKindBuilder(kind);
    }

    public static ThrowsMatchBuilder throws_() {
        return new ThrowsMatchBuilder();
    }

    public static OverridesMatchBuilder overrides() {
        return new OverridesMatchBuilder();
    }

    public static ModifiersMatchBuilder modifiers() {
        return new ModifiersMatchBuilder();
    }

    public static ModifiersMatchBuilder modifiers(Modifier first, Modifier...rest) {
        ModifiersMatchBuilder ret = new ModifiersMatchBuilder().$(first);
        for (Modifier m : rest) {
            ret = ret.$(m);
        }

        return ret;
    }

    public static ModifierClusterMatchBuilder atLeastOne() {
        return new ModifierClusterMatchBuilder();
    }

    public static DefaultMethodValueMatchBuilder defaultValue() {
        return new DefaultMethodValueMatchBuilder();
    }

    public static AnnotationValueBuilder value(Operator operator) {
        return new AnnotationValueBuilder(operator);
    }

    public static AnnotationValueMatchAllBuilder allValues() {
        return new AnnotationValueMatchAllBuilder();
    }

    public static AnnotationMatchBuilder annotation(TypeReferenceMatchBuilder type) {
        return new AnnotationMatchBuilder(type);
    }

    public static AnnotationAttributeValueBuilder attribute(NameMatch name) {
        return new AnnotationAttributeValueBuilder(name);
    }

    public static AnnotationAttributeAnyBuilder anyAttribute() {
        return new AnnotationAttributeAnyBuilder();
    }

    public static AnntotationAttributeAllBuilder anyAttributes() {
        return new AnntotationAttributeAllBuilder();
    }

    public static AnnotationArrayValueBuilder array() {
        return new AnnotationArrayValueBuilder();
    }

    public static AnnotationArrayItemValueBuilder item() {
        return new AnnotationArrayItemValueBuilder();
    }

    public static MethodParameterMatchBuilder parameter(TypeReferenceMatchBuilder type) {
        return new MethodParameterMatchBuilder(type);
    }

    public static MethodParameterMatchBuilder anyParameter() {
        return new MethodParameterMatchBuilder(anyType());
    }

    public static MethodParameterMatchBuilder anyParameters() {
        return new MethodParameterMatchBuilder(anyTypes());
    }

    public static ImplementsMatchBuilder implements_() {
        return new ImplementsMatchBuilder();
    }

    public static ExtendsMatchBuilder extends_(TypeReferenceMatchBuilder type) {
        return new ExtendsMatchBuilder(type);
    }

    public static ModifierClusterMatchBuilder group() {
        return new ModifierClusterMatchBuilder();
    }

    private abstract static class AbstractStatementBuilder<This extends AbstractStatementBuilder<This, S>, S extends AbstractStatement> extends AbstractMatchBuilder<S> {
        protected final List<AnnotationMatchBuilder> annotations = new ArrayList<>();
        protected ModifiersMatchBuilder modifiers = new ModifiersMatchBuilder();
        protected boolean negation;
        @Nullable String definedVariable;
        boolean isMatch;

        private AbstractStatementBuilder() {

        }

        public This called(String name) {
            this.definedVariable = name;
            return castThis();
        }

        public This matched() {
            this.isMatch = true;
            return castThis();
        }

        public This $(ModifiersMatchBuilder modifiers) {
            this.modifiers = modifiers;
            return castThis();
        }

        public This $(AnnotationMatchBuilder annotation) {
            this.annotations.add(annotation);
            return castThis();
        }

        public This negated() {
            this.negation = true;
            return castThis();
        }
    }

    public static final class GenericStatementBuilder extends AbstractStatementBuilder<GenericStatementBuilder, GenericStatement> {
        private @Nullable UsesMatchBuilder uses;

        private GenericStatementBuilder() {

        }

        public GenericStatementBuilder $(UsesMatchBuilder uses) {
            this.uses = uses;
            return this;
        }

        public GenericStatement build() {
            return new GenericStatement(definedVariable, referencedVariables, new AnnotationsMatch(finish(annotations)),
                    finish(modifiers), isMatch, negation, finish(uses));
        }
    }

    public static final class FieldStatementBuilder extends AbstractStatementBuilder<FieldStatementBuilder, FieldStatement> {
        private final NameMatch name;
        private @Nullable TypeReferenceMatchBuilder type;
        private @Nullable TypeReferenceMatchBuilder declaringType;
        private @Nullable UsesMatchBuilder uses;

        private FieldStatementBuilder(NameMatch name) {
            this.name = name;
        }

        public FieldStatementBuilder $(TypeReferenceMatchBuilder type) {
            this.type = type;
            return this;
        }

        public FieldStatementBuilder declaredIn(TypeReferenceMatchBuilder type) {
            this.declaringType = type;
            return this;
        }

        public FieldStatementBuilder $(UsesMatchBuilder uses) {
            this.uses = uses;
            return this;
        }

        @Override
        public FieldStatement build() {
            return new FieldStatement(definedVariable, referencedVariables, new AnnotationsMatch(finish(annotations)),
                    finish(modifiers), isMatch, negation, name, finish(type), finish(declaringType), finish(uses));
        }
    }

    public static final class MethodStatementBuilder extends AbstractStatementBuilder<MethodStatementBuilder, MethodStatement> {
        private final NameMatch name;
        private @Nullable TypeReferenceMatchBuilder returnType;
        private @Nullable TypeReferenceMatchBuilder declaringType;
        private final List<TypeParameterMatchBuilder> typeParameters = new ArrayList<>();
        private @Nullable OverridesMatchBuilder overrides;
        private @Nullable UsesMatchBuilder uses;
        private final List<TypeReferenceMatchBuilder> throws_ = new ArrayList<>();
        private @Nullable DefaultMethodValueMatchBuilder defaultValue;
        private final List<MethodParameterMatchBuilder> parameters = new ArrayList<>();

        private MethodStatementBuilder(NameMatch name) {
            this.name = name;
        }

        public MethodStatementBuilder returns(TypeReferenceMatchBuilder type) {
            this.returnType = type;
            return this;
        }

        public MethodStatementBuilder declaredIn(TypeReferenceMatchBuilder type) {
            this.declaringType = type;
            return this;
        }

        public MethodStatementBuilder $(TypeParameterMatchBuilder typeParameter) {
            this.typeParameters.add(typeParameter);
            return this;
        }

        public MethodStatementBuilder $(OverridesMatchBuilder overrides) {
            this.overrides = overrides;
            return this;
        }

        public MethodStatementBuilder $(UsesMatchBuilder uses) {
            this.uses = uses;
            return this;
        }

        public MethodStatementBuilder throws_(TypeReferenceMatchBuilder type) {
            this.throws_.add(type);
            return this;
        }

        public MethodStatementBuilder $(MethodParameterMatchBuilder parameter) {
            this.parameters.add(parameter);
            return this;
        }

        public MethodStatementBuilder $(DefaultMethodValueMatchBuilder defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Override
        public MethodStatement build() {
            ThrowsMatch throwsMatch = throws_.isEmpty() ? null : new ThrowsMatch(finish(throws_));
            List<DeclarationMatch> constraints = finish(asList(overrides, uses, defaultValue));
            if (throwsMatch != null) {
                constraints.add(throwsMatch);
            }

            TypeParametersMatch typeParamsMatch = typeParameters.isEmpty()
                    ? null
                    : new TypeParametersMatch(finish(typeParameters));

            return new MethodStatement(definedVariable, referencedVariables, new AnnotationsMatch(finish(annotations)),
                    finish(modifiers), isMatch, name, finish(returnType), finish(declaringType),
                    typeParamsMatch, finish(parameters),
                    new MethodConstraintsMatch(constraints), negation);
        }
    }

    public static final class TypeDefinitionStatementBuilder extends AbstractStatementBuilder<TypeDefinitionStatementBuilder, TypeDefinitionStatement> {
        private final TypeKindBuilder typeKind;
        private final FqnMatchBuilder fqn;
        private final List<TypeParameterMatchBuilder> typeParameters = new ArrayList<>();
        private final List<ImplementsMatchBuilder> implemented = new ArrayList<>();
        private @Nullable ExtendsMatchBuilder extended;
        private final List<UsesMatchBuilder> uses = new ArrayList<>();
        private final List<UsedByMatchBuilder> usedBys = new ArrayList<>();
        private final List<AbstractStatementBuilder<?, ? extends AbstractStatement>> innerStatements = new ArrayList<>();

        private TypeDefinitionStatementBuilder(TypeKindBuilder kind, FqnMatchBuilder fqn) {
            this.typeKind = kind;
            this.fqn = fqn;
        }

        public TypeDefinitionStatementBuilder $(TypeParameterMatchBuilder param) {
            this.typeParameters.add(param);
            return this;
        }

        public TypeDefinitionStatementBuilder $(ImplementsMatchBuilder implements_) {
            this.implemented.add(implements_);
            return this;
        }

        public TypeDefinitionStatementBuilder $(ExtendsMatchBuilder extends_) {
            this.extended = extends_;
            return this;
        }

        public TypeDefinitionStatementBuilder $(UsesMatchBuilder uses) {
            this.uses.add(uses);
            return this;
        }

        public TypeDefinitionStatementBuilder $(UsedByMatchBuilder usedBy) {
            this.usedBys.add(usedBy);
            return this;
        }

        public TypeDefinitionStatementBuilder $(FieldStatementBuilder field) {
            this.innerStatements.add(field);
            return this;
        }

        public TypeDefinitionStatementBuilder $(MethodStatementBuilder method) {
            this.innerStatements.add(method);
            return this;
        }

        public TypeDefinitionStatementBuilder $(TypeDefinitionStatementBuilder type) {
            this.innerStatements.add(type);
            return this;
        }

        @Override
        public TypeDefinitionStatement build() {
            TypeDefinitionStatement type = new TypeDefinitionStatement(definedVariable, referencedVariables,
                    new AnnotationsMatch(finish(annotations)), finish(modifiers), finish(typeKind), finish(fqn),
                    typeParameters.isEmpty() ? null : new TypeParametersMatch(finish(typeParameters)),
                    new TypeConstraintsMatch(finish(implemented), finish(extended), finish(uses), finish(usedBys)),
                    negation, isMatch);

            type.getChildren().addAll(finish(innerStatements));

            return type;
        }
    }

    public static final class ImplementsMatchBuilder extends AbstractMatchBuilder<ImplementsMatch> {
        private boolean directly;
        private boolean exactly;
        private final List<TypeReferenceMatchBuilder> types = new ArrayList<>();

        private ImplementsMatchBuilder() {

        }

        public ImplementsMatchBuilder directly() {
            this.directly = true;
            return this;
        }

        public ImplementsMatchBuilder exactly() {
            this.exactly = true;
            return this;
        }

        public ImplementsMatchBuilder $(TypeReferenceMatchBuilder type) {
            this.types.add(type);
            return this;
        }

        @Override
        public ImplementsMatch build() {
            return new ImplementsMatch(directly, exactly, finish(types));
        }
    }

    public static final class ExtendsMatchBuilder extends AbstractMatchBuilder<ExtendsMatch> {
        private boolean directly;
        private final TypeReferenceMatchBuilder type;

        private ExtendsMatchBuilder(TypeReferenceMatchBuilder type) {
            this.type = type;
        }


        public ExtendsMatchBuilder directly() {
            this.directly = true;
            return this;
        }

        @Override
        public ExtendsMatch build() {
            return new ExtendsMatch(directly, finish(type));
        }
    }

    public static final class MethodParameterMatchBuilder extends AbstractMatchBuilder<MethodParameterMatch> {
        private final List<AnnotationMatchBuilder> annotations = new ArrayList<>();
        private final TypeReferenceMatchBuilder type;

        private MethodParameterMatchBuilder(TypeReferenceMatchBuilder type) {
            this.type = type;
        }

        public MethodParameterMatchBuilder $(AnnotationMatchBuilder annotation) {
            this.annotations.add(annotation);
            return this;
        }

        @Override
        public MethodParameterMatch build() {
            return new MethodParameterMatch(new AnnotationsMatch(finish(annotations)), finish(type));
        }
    }

    public static final class Builder extends AbstractMatchBuilder<StructuralMatcher> {
        private final List<AbstractStatementBuilder<?, ? extends AbstractStatement>> statements = new ArrayList<>();
        private final List<String> returns;
        private boolean strictHierarchy;

        private Builder(Collection<String> returns) {
            this.returns = new ArrayList<>(returns);
        }

        public Builder strictHierarchy() {
            this.strictHierarchy = true;
            return this;
        }

        public Builder $(GenericStatementBuilder decl) {
            statements.add(decl);
            return this;
        }

        public Builder $(TypeDefinitionStatementBuilder type) {
            statements.add(type);
            return this;
        }

        @Override
        public StructuralMatcher build() {
            return new StructuralMatcher(new StructuralMatcher.Configuration(strictHierarchy), returns, finish(statements));
        }
    }

    public static final class FqnMatchBuilder extends AbstractMatchBuilder<FqnMatch> {
        private final List<NameMatch> parts;

        private FqnMatchBuilder(List<NameMatch> parts) {
            this.parts = parts;
        }

        public FqnMatchBuilder dot(NameMatch name) {
            parts.add(name);
            return this;
        }

        public FqnMatchBuilder parts(NameMatch... names) {
            parts.addAll(asList(names));
            return this;
        }

        @Override
        public FqnMatch build() {
            return new FqnMatch(parts);
        }
    }

    public static abstract class TypeParameterMatchBuilder extends AbstractMatchBuilder<TypeParameterMatch> {
        private TypeParameterMatchBuilder() {

        }

        public abstract TypeParameterMatch build();
    }

    public static final class TypeParameterWildcardBuilder extends TypeParameterMatchBuilder {
        private final boolean isExtends;
        private final List<TypeReferenceMatchBuilder> bounds = new ArrayList<>();

        private TypeParameterWildcardBuilder(boolean isExtends) {
            this.isExtends = isExtends;
        }

        public TypeParameterWildcardBuilder and(TypeReferenceMatchBuilder type) {
            bounds.add(type);
            return this;
        }

        @Override
        public TypeParameterMatch build() {
            return new TypeParameterMatch(new TypeParameterWildcardMatch(isExtends, finish(bounds)), null);
        }
    }

    public static final class TypeParameterBoundBuilder extends TypeParameterMatchBuilder {
        private final List<TypeReferenceMatchBuilder> bounds = new ArrayList<>();

        private TypeParameterBoundBuilder() {

        }

        public TypeParameterBoundBuilder and(TypeReferenceMatchBuilder type) {
            bounds.add(type);
            return this;
        }

        @Override
        public TypeParameterMatch build() {
            return new TypeParameterMatch(null, finish(bounds));
        }
    }

    private abstract static class SingleTypeReferenceMatchBuilder<This extends SingleTypeReferenceMatchBuilder<This, M>, M> extends AbstractMatchBuilder<M> {
        protected @Nullable FqnMatchBuilder fqn;
        protected final List<TypeParameterMatchBuilder> typeParameters = new ArrayList<>();
        protected @Nullable String variable;
        protected boolean negation;
        protected int arrayDimension;

        SingleTypeReferenceMatchBuilder() {

        }

        public This negated() {
            this.negation = true;
            return castThis();
        }

        public This array(int arrayDimension) {
            this.arrayDimension = arrayDimension;
            return castThis();
        }

        public This var(String variableName) {
            this.variable = variableName;
            return castThis();
        }

        public This fqn(NameMatch first, NameMatch... rest) {
            this.fqn = Classif.fqn(first, rest);
            return castThis();
        }

        public This $(TypeParameterMatchBuilder typeParameter) {
            this.typeParameters.add(typeParameter);
            return castThis();
        }

        void clear() {
            fqn = null;
            typeParameters.clear();
            variable = null;
            negation = false;
            arrayDimension = 0;
        }
    }

    public static final class TypeReferenceMatchBuilder extends SingleTypeReferenceMatchBuilder<TypeReferenceMatchBuilder, TypeReferenceMatch> {
        private final List<SingleTypeReferenceMatch> types = new ArrayList<>();

        private TypeReferenceMatchBuilder() {

        }

        public TypeReferenceMatchBuilder or() {
            if (variable != null) {
                referencedVariables.add(variable);
            }

            TypeParametersMatch typeParams = typeParameters.isEmpty()
                    ? null
                    : new TypeParametersMatch(finish(typeParameters));

            types.add(new SingleTypeReferenceMatch(finish(fqn), typeParams, variable, negation, arrayDimension));
            clear();
            return this;
        }

        @Override
        public TypeReferenceMatch build() {
            or();
            return new TypeReferenceMatch(types);
        }
    }

    public static final class UsesMatchBuilder extends AbstractMatchBuilder<UsesMatch> {
        private boolean onlyDirect;
        private final TypeReferenceMatchBuilder type;

        private UsesMatchBuilder(TypeReferenceMatchBuilder type) {
            this.type = type;
        }

        public UsesMatchBuilder directly() {
            this.onlyDirect = true;
            return this;
        }

        @Override
        public UsesMatch build() {
            return new UsesMatch(onlyDirect, finish(type));
        }
    }

    public static final class UsedByMatchBuilder extends AbstractMatchBuilder<UsedByMatch> {
        private boolean onlyDirect;

        private UsedByMatchBuilder(List<String> variables) {
            this.referencedVariables.addAll(variables);
        }

        public UsedByMatchBuilder directly() {
            this.onlyDirect = true;
            return this;
        }

        public UsedByMatch build() {
            return new UsedByMatch(onlyDirect, referencedVariables);
        }
    }

    public static final class TypeKindBuilder extends AbstractMatchBuilder<TypeKindMatch> {
        private boolean negated;
        private final TypeKind kind;

        private TypeKindBuilder(TypeKind kind) {
            this.kind = kind;
        }

        public TypeKindBuilder negated() {
            this.negated = true;
            return this;
        }

        @Override
        public TypeKindMatch build() {
            return new TypeKindMatch(negated, kind);
        }
    }

    public static final class ThrowsMatchBuilder extends AbstractMatchBuilder<ThrowsMatch> {
        private final List<TypeReferenceMatchBuilder> types = new ArrayList<>();

        public ThrowsMatchBuilder $(TypeReferenceMatchBuilder type) {
            types.add(type);
            return this;
        }

        @Override
        public ThrowsMatch build() {
            return new ThrowsMatch(finish(types));
        }
    }

    public static final class OverridesMatchBuilder extends AbstractMatchBuilder<OverridesMatch> {
        private @Nullable Classif.TypeReferenceMatchBuilder type;

        private OverridesMatchBuilder() {

        }

        public OverridesMatchBuilder from(TypeReferenceMatchBuilder type) {
            this.type = type;
            return this;
        }

        @Override
        public OverridesMatch build() {
            return new OverridesMatch(finish(type));
        }
    }

    public static final class ModifiersMatchBuilder extends AbstractMatchBuilder<ModifiersMatch> {
        private final List<ModifierClusterMatchBuilder> clusters = new ArrayList<>();

        public ModifiersMatchBuilder $(Modifier modifier) {
            clusters.add(new ModifierClusterMatchBuilder().$(modifier));
            return this;
        }

        public ModifiersMatchBuilder not(Modifier modifier) {
            clusters.add(new ModifierClusterMatchBuilder().not(modifier));
            return this;
        }

        public ModifiersMatchBuilder oneOf(ModifierClusterMatchBuilder mods) {
            clusters.add(mods);
            return this;
        }

        public ModifiersMatch build() {
            return new ModifiersMatch(finish(clusters));
        }
    }

    public static final class ModifierClusterMatchBuilder extends AbstractMatchBuilder<ModifierClusterMatch> {
        private final List<ModifierMatch> mods = new ArrayList<>();

        private ModifierClusterMatchBuilder() {

        }

        public ModifierClusterMatchBuilder $(Modifier modifier) {
            mods.add(new ModifierMatch(false, modifier));
            return this;
        }

        public ModifierClusterMatchBuilder not(Modifier modifier) {
            mods.add(new ModifierMatch(true, modifier));
            return this;
        }

        @Override
        public ModifierClusterMatch build() {
            return new ModifierClusterMatch(mods);
        }
    }

    public static abstract class AnnotationValueMatchBuilder extends AbstractMatchBuilder<AnnotationValueMatch> {

    }

    public static final class AnnotationValueMatchAllBuilder extends AnnotationValueMatchBuilder {
        public AnnotationValueMatch build() {
            return AnnotationValueMatch.all();
        }
    }

    static abstract class BaseAnnotationValueBuilder<This extends AbstractMatchBuilder<M>, M> extends AbstractMatchBuilder<M> {
        private static final Object ANY = new Object();

        protected final Operator operator;
        protected Object value;

        BaseAnnotationValueBuilder(Operator operator) {
            this.operator = operator;
        }

        public This string(String value) {
            this.value = value;
            return castThis();
        }

        public This regex(Pattern regex) {
            this.value = regex;
            return castThis();
        }

        public This number(Number number) {
            this.value = number;
            return castThis();
        }

        public This bool(boolean bool) {
            this.value = bool;
            return castThis();
        }

        public This any(Operator op) {
            this.value = ANY;
            return castThis();
        }

        public This enumConstant(FqnMatchBuilder fqn, NameMatch name) {
            this.value = new AbstractMap.SimpleImmutableEntry<>(fqn, name);
            return castThis();
        }

        public This type(TypeReferenceMatchBuilder type) {
            this.value = type;
            return castThis();
        }

        public This annotation(AnnotationMatchBuilder annotation) {
            this.value = annotation;
            return castThis();
        }

        public This of(AnnotationArrayValueBuilder values) {
            this.value = values;
            return castThis();
        }

        protected AnnotationValueMatch buildValueMatch() {
            if (value instanceof String) {
                return AnnotationValueMatch.string(operator, (String) value);
            } else if (value instanceof Pattern) {
                return AnnotationValueMatch.regex(operator, (Pattern) value);
            } else if (value instanceof Number) {
                return AnnotationValueMatch.number(operator, (Number) value);
            } else if (value instanceof Boolean) {
                return AnnotationValueMatch.bool(operator, (Boolean) value);
            } else if (value == ANY) {
                return AnnotationValueMatch.any(operator);
            } else if (value instanceof Map.Entry) {
                @SuppressWarnings("unchecked") Map.Entry<FqnMatchBuilder, NameMatch> e = (Map.Entry) value;
                return AnnotationValueMatch.enumConstant(operator, finish(e.getKey()), e.getValue());
            } else if (value instanceof TypeReferenceMatchBuilder) {
                return AnnotationValueMatch.type(operator, finish((TypeReferenceMatchBuilder) value));
            } else if (value instanceof AnnotationMatchBuilder) {
                return AnnotationValueMatch.annotation(operator, finish((AnnotationMatchBuilder) value));
            } else if (value instanceof AnnotationArrayValueBuilder) {
                return AnnotationValueMatch.array(operator, finish((AnnotationArrayValueBuilder) value));
            } else {
                throw new IllegalStateException("Unhandled annotation value type: " + value);
            }
        }
    }

    static abstract class AbstractAnnotationValueBuilder<This extends AbstractAnnotationValueBuilder<This>> extends AnnotationValueMatchBuilder {
        protected final BaseAnnotationValueBuilder<This, AnnotationValueMatch> bld;

        AbstractAnnotationValueBuilder(Operator operator) {
            bld = new BaseAnnotationValueBuilder<This, AnnotationValueMatch>(operator) {
                @Override
                public AnnotationValueMatch build() {
                    return buildValueMatch();
                }
            };
        }

        public This string(String value) {
            bld.string(value);
            return castThis();
        }

        public This regex(Pattern regex) {
            bld.regex(regex);
            return castThis();
        }

        public This number(Number number) {
            bld.number(number);
            return castThis();
        }

        public This bool(boolean bool) {
            bld.bool(bool);
            return castThis();
        }

        public This any() {
            bld.any(bld.operator);
            return castThis();
        }

        public This enumConstant(FqnMatchBuilder fqn, NameMatch name) {
            bld.enumConstant(fqn, name);
            return castThis();
        }

        public This $(TypeReferenceMatchBuilder type) {
            bld.type(type);
            return castThis();
        }

        public This $(AnnotationMatchBuilder annotation) {
            bld.annotation(annotation);
            return castThis();
        }

        public This $(AnnotationArrayValueBuilder values) {
            bld.of(values);
            return castThis();
        }

        @Override
        public AnnotationValueMatch build() {
            return bld.build();
        }
    }

    public static class AnnotationValueBuilder extends AbstractAnnotationValueBuilder<AnnotationValueBuilder> {
        private static final Object DEFAULT_VALUE = new Object();

        private AnnotationValueBuilder(Operator operator) {
            super(operator);
        }

        public AnnotationValueBuilder defaultValue() {
            bld.value = DEFAULT_VALUE;
            return this;
        }

        @Override
        public AnnotationValueMatch build() {
            if (bld.value == DEFAULT_VALUE) {
                return AnnotationValueMatch.defaultValue(bld.operator);
            } else {
                return super.build();
            }
        }
    }

    public static final class AnnotationArrayValueBuilder extends AbstractMatchBuilder<List<AnnotationValueMatch>> {
        private final List<AnnotationArrayItemValueBuilder> matches = new ArrayList<>();

        private AnnotationArrayValueBuilder() {

        }

        public AnnotationArrayValueBuilder $(AnnotationArrayItemValueBuilder value) {
            matches.add(value);
            return this;
        }

        @Override
        public List<AnnotationValueMatch> build() {
            return finish(matches);
        }
    }

    public static final class AnnotationArrayItemValueBuilder extends AnnotationValueBuilder {
        private AnnotationArrayItemValueBuilder() {
            super(EQ);
        }

        public AnnotationArrayItemValueBuilder anyValues() {
            bld.value = null;
            return this;
        }

        @Override
        public AnnotationValueMatch build() {
            if (bld.value == null) {
                return AnnotationValueMatch.all();
            } else {
                return super.build();
            }
        }
    }

    public static final class AnnotationMatchBuilder extends AbstractMatchBuilder<AnnotationMatch> {
        private boolean negation;
        private final TypeReferenceMatchBuilder type;
        private final List<AnnotationAttributeMatchBuilder> attributes = new ArrayList<>();

        private AnnotationMatchBuilder(TypeReferenceMatchBuilder type) {
            this.type = type;
        }

        public AnnotationMatchBuilder negated() {
            this.negation = true;
            return this;
        }

        public AnnotationMatchBuilder $(AnnotationAttributeMatchBuilder attribute) {
            this.attributes.add(attribute);
            return this;
        }

        @Override
        public AnnotationMatch build() {
            return new AnnotationMatch(negation, finish(type), finish(attributes));
        }
    }

    public abstract static class AnnotationAttributeMatchBuilder extends AbstractMatchBuilder<AnnotationAttributeMatch> {
        private AnnotationAttributeMatchBuilder() {

        }

        public abstract AnnotationAttributeMatch build();
    }

    public static final class AnnotationAttributeAnyBuilder extends AnnotationAttributeMatchBuilder {
        private AnnotationAttributeAnyBuilder() {

        }

        public AnnotationAttributeMatch build() {
            return new AnnotationAttributeMatch(true, false, null, null);
        }
    }

    public static final class AnntotationAttributeAllBuilder extends AnnotationAttributeMatchBuilder {
        private AnntotationAttributeAllBuilder() {

        }

        public AnnotationAttributeMatch build() {
            return new AnnotationAttributeMatch(false, true, null, null);
        }
    }

    public static final class AnnotationAttributeValueBuilder extends AnnotationAttributeMatchBuilder {
        private final NameMatch name;
        private @Nullable Classif.AnnotationValueMatchBuilder valueMatch;

        private AnnotationAttributeValueBuilder(NameMatch name) {
            this.name = name;
        }

        public AnnotationAttributeValueBuilder $(AnnotationValueMatchBuilder value) {
            this.valueMatch = value;
            return this;
        }

        public AnnotationAttributeMatch build() {
            return new AnnotationAttributeMatch(false, false, name, finish(valueMatch));
        }
    }

    public static final class DefaultMethodValueMatchBuilder extends AbstractMatchBuilder<DefaultValueMatch> {
        private boolean negation;
        private AnnotationValueBuilder value;

        private DefaultMethodValueMatchBuilder() {

        }

        public DefaultMethodValueMatchBuilder not() {
            this.negation = true;
            return this;
        }

        public DefaultMethodValueMatchBuilder $(AnnotationValueBuilder value) {
            this.value = value;
            return this;
        }

        @Override
        public DefaultValueMatch build() {
            return new DefaultValueMatch(negation, finish(value));
        }
    }

    public abstract static class AbstractMatchBuilder<M> {
        protected final List<String> referencedVariables = new ArrayList<>();

        public abstract M build();

        private void copyVariables(@Nullable Classif.AbstractMatchBuilder<?> other) {
            if (other != null) {
                referencedVariables.addAll(other.referencedVariables);
            }
        }

        protected <X> @Nullable X finish(@Nullable AbstractMatchBuilder<X> builder) {
            if (builder == null) {
                return null;
            }

            X ret = builder.build();

            // important to copy the variables only after the builder had the chance to finish itself up
            copyVariables(builder);

            return ret;
        }

        protected <X> List<X> finish(List<@Nullable ? extends AbstractMatchBuilder<? extends X>> builders) {
            return builders.stream()
                    .filter(Objects::nonNull)
                    .map(this::finish)
                    .collect(toList());
        }

        @SuppressWarnings("unchecked")
        protected <T> T castThis() {
            return (T) this;
        }
    }
}
