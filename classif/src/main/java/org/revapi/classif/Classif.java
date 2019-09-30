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

/**
 * This is a builder for creating Classif queries. Start with {@link #match()} or its variants with more arguments.
 */
@SuppressWarnings("unused")
public final class Classif {

    private Classif() {
        throw new AssertionError("No!");
    }

    /**
     * @return a new query builder
     */
    public static Builder match() {
        return new Builder(emptyList());
    }

    /**
     * @return a query builder that will return elements matching statements that are referred to by the provided
     * variable names.
     *
     * @see AbstractStatementBuilder#as(String)
     */
    public static Builder match(String var, String... vars) {
        List<String> rets = new ArrayList<>(vars.length + 1);
        rets.add(var);
        rets.addAll(asList(vars));

        return new Builder(rets);
    }

    /**
     * A convenience overload of {@link #match(String, String...)}
     */
    public static Builder match(Collection<String> vars) {
        return new Builder(vars);
    }

    /**
     * Starts building a query for any kind of statement.
     */
    public static GenericStatementBuilder declaration() {
        return new GenericStatementBuilder();
    }

    /**
     * Starts building a query for a field with the matching name.
     *
     * @param name requirements on the name
     * @see NameMatch#all()
     * @see NameMatch#any()
     * @see NameMatch#exact(String)
     * @see NameMatch#pattern(Pattern)
     */
    public static FieldStatementBuilder field(NameMatch name) {
        return new FieldStatementBuilder(name);
    }

    /**
     * Starts building a query for a method with the matching name.
     *
     * @param name requirements on the name
     * @see NameMatch#all()
     * @see NameMatch#any()
     * @see NameMatch#exact(String)
     * @see NameMatch#pattern(Pattern)
     */
    public static MethodStatementBuilder method(NameMatch name) {
        return new MethodStatementBuilder(name);
    }

    /**
     * A convenience method to start building a query for a type with the specified kind and fully qualified name.
     *
     * @param kind the kind of the type
     * @param fqnFirstPart the match for the top-level part of the hierarchical fully qualified name of the type
     * @param fqnRest the matches for the "lower-level" parts of the hierarchical fully qualified name of the type
     * @see #type(TypeKindBuilder, FqnMatchBuilder)
     */
    public static TypeDefinitionStatementBuilder type(TypeKind kind, NameMatch fqnFirstPart, NameMatch... fqnRest) {
        return type(new TypeKindBuilder(kind), fqn(fqnFirstPart, fqnRest));
    }

    /**
     * A convenience method to start building a query for a type with the specified kind and fully qualified name.
     *
     * @param kind the kind of the type
     * @param fqn the match for the hierarchical fully qualified name of the type
     * @see #type(TypeKindBuilder, FqnMatchBuilder)
     */
    public static TypeDefinitionStatementBuilder type(TypeKind kind, FqnMatchBuilder fqn) {
        return new TypeDefinitionStatementBuilder(new TypeKindBuilder(kind), fqn);
    }


    /**
     * A convenience method to start building a query for a type with the specified kind and fully qualified name.
     *
     * @param kind the kind of the type
     * @param fqnFirstPart the match for the top-level part of the hierarchical fully qualified name of the type
     * @param fqnRest the matches for the "lower-level" parts of the hierarchical fully qualified name of the type
     * @see #type(TypeKindBuilder, FqnMatchBuilder)
     */
    public static TypeDefinitionStatementBuilder type(TypeKindBuilder kind, NameMatch fqnFirstPart, NameMatch... fqnRest) {
        return new TypeDefinitionStatementBuilder(kind, fqn(fqnFirstPart, fqnRest));
    }

    /**
     * Starts a query for a type of a specified kind and fully qualified name.
     *
     * @see #kind(TypeKind)
     * @see #fqn(NameMatch, NameMatch...)
     */
    public static TypeDefinitionStatementBuilder type(TypeKindBuilder kind, FqnMatchBuilder fqn) {
        return new TypeDefinitionStatementBuilder(kind, fqn);
    }

    /**
     * Builds a match for a hierarchical fully qualified name.
     * @param name the match for the top-level part of the hierarchical fully qualified name of the type
     * @param names the matches for the "lower-level" parts of the hierarchical fully qualified name of the type
     */
    public static FqnMatchBuilder fqn(NameMatch name, NameMatch... names) {
        ArrayList<NameMatch> parts = new ArrayList<>(names.length + 1);
        parts.add(name);
        parts.addAll(asList(names));

        return new FqnMatchBuilder(parts);
    }

    /**
     * Starts a query for a type parameter wildcard.
     */
    public static TypeParameterMatchBuilder wildcard() {
        return new TypeParameterWildcardBuilder(true);
    }

    /**
     * Starts a query for a type parameter wildcard extending the provided type.
     *
     * @param type the type the wildcard should extend
     * @see #type()
     * @see #type(TypeKindBuilder, FqnMatchBuilder)
     * @see #anyType()
     * @see #anyTypes()
     */
    public static TypeParameterWildcardBuilder wildcardExtends(TypeReferenceMatchBuilder type) {
        return new TypeParameterWildcardBuilder(true).and(type);
    }

    /**
     * Starts a query for a type parameter wildcard that is a supertype of the provided type.
     *
     * @param type the type the wildcard should be super type of
     * @see #type()
     * @see #type(TypeKindBuilder, FqnMatchBuilder)
     * @see #anyType()
     * @see #anyTypes()
     */
    public static TypeParameterWildcardBuilder wildcardSuper(TypeReferenceMatchBuilder type) {
        return new TypeParameterWildcardBuilder(false).and(type);
    }

    /**
     * Starts building a bound of a type parameter.
     *
     * @param type the bound type
     */
    public static TypeParameterBoundBuilder bound(TypeReferenceMatchBuilder type) {
        return new TypeParameterBoundBuilder().and(type);
    }

    /**
     * Starts building a query for some type.
     */
    public static TypeReferenceMatchBuilder type() {
        return new TypeReferenceMatchBuilder();
    }

    /**
     * Starts building a query for any type.
     */
    public static TypeReferenceMatchBuilder anyType() {
        return type().fqn(NameMatch.any());
    }

    /**
     * Starts building a query for a (possibly empty) list of arbitrary types.
     */
    public static TypeReferenceMatchBuilder anyTypes() {
        return type().fqn(NameMatch.all());
    }

    /**
     * Starts building a constraint on uses of a statement.
     * @param type the type the statement should be using.
     */
    public static UsesMatchBuilder uses(TypeReferenceMatchBuilder type) {
        return new UsesMatchBuilder(type);
    }

    /**
     * Starts building a constraint on a type that limits to only types used by statements referred to by the provided
     * variables.
     */
    public static UsedByMatchBuilder usedBy(String var, String... vars) {
        List<String> vs = new ArrayList<>(vars.length + 1);
        vs.add(var);
        vs.addAll(asList(vars));

        return new UsedByMatchBuilder(vs);
    }

    /**
     * Starts building a type kind match.
     */
    public static TypeKindBuilder kind(TypeKind kind) {
        return new TypeKindBuilder(kind);
    }

    /**
     * Starts building a constraint to only consider methods that override a method from a super type.
     */
    public static OverridesMatchBuilder overrides() {
        return new OverridesMatchBuilder();
    }

    /**
     * Starts building a constraint on modifiers of a statement.
     */
    public static ModifiersMatchBuilder modifiers() {
        return new ModifiersMatchBuilder();
    }

    /**
     * A convenience method to specify more modifiers in a row.
     * @see #modifiers()
     */
    public static ModifiersMatchBuilder modifiers(Modifier first, Modifier...rest) {
        ModifiersMatchBuilder ret = new ModifiersMatchBuilder().$(first);
        for (Modifier m : rest) {
            ret = ret.$(m);
        }

        return ret;
    }

    /**
     * A method to select statements with at least one of the modifiers.
s     */
    public static ModifierClusterMatchBuilder atLeastOne() {
        return new ModifierClusterMatchBuilder();
    }

    /**
     * Starts building a constraint on the default value of a method.
     */
    public static DefaultMethodValueMatchBuilder defaultValue() {
        return new DefaultMethodValueMatchBuilder();
    }

    /**
     * Starts building a constraint on annotation value.
     * @param operator operator to use for the value comparisons
     */
    public static AnnotationValueBuilder value(Operator operator) {
        return new AnnotationValueBuilder(operator);
    }

    /**
     * Matches zero or more annotation values with any value (useful for matching arrays for example).
     */
    public static AnnotationValueMatchAllBuilder allValues() {
        return new AnnotationValueMatchAllBuilder();
    }

    /**
     * Starts building a constraint on an annotation present on some statement.
     * @param type the type of the annotation on the statement
     */
    public static AnnotationMatchBuilder annotation(TypeReferenceMatchBuilder type) {
        return new AnnotationMatchBuilder(type);
    }

    /**
     * Starts building a constraint on an annotation attribute with given name.
     * @param name the match on the name of the annotation attribute.
     */
    public static AnnotationAttributeValueBuilder attribute(NameMatch name) {
        return new AnnotationAttributeValueBuilder(name);
    }

    /**
     * Matches an annotation attribute with any name.
     */
    public static AnnotationAttributeAnyBuilder anyAttribute() {
        return new AnnotationAttributeAnyBuilder();
    }

    /**
     * Matches zero or more attribures of any name and value.
     */
    public static AnnotationAttributeAllBuilder anyAttributes() {
        return new AnnotationAttributeAllBuilder();
    }

    /**
     * Starts a constraint on an array value of an annotation attribute.
     */
    public static AnnotationArrayValueBuilder array() {
        return new AnnotationArrayValueBuilder();
    }

    /**
     * Starts a constraint on an item in an array value of an annotation attribute.
     */
    public static AnnotationArrayItemValueBuilder item() {
        return new AnnotationArrayItemValueBuilder();
    }

    /**
     * Starts a constraint on the parameter of a method.
     * @param type the type of the method parameter
     */
    public static MethodParameterMatchBuilder parameter(TypeReferenceMatchBuilder type) {
        return new MethodParameterMatchBuilder(type);
    }

    /**
     * A constraint requiring a single method parameter with arbitrary type.
     * Equivalent to {@code parameter(anyType())}.
     */
    public static MethodParameterMatchBuilder anyParameter() {
        return new MethodParameterMatchBuilder(anyType());
    }

    /**
     * A constraint requiring zero or more method parameters with arbirary types.
     */
    public static MethodParameterMatchBuilder anyParameters() {
        return new MethodParameterMatchBuilder(anyTypes());
    }

    /**
     * Starts a constraint on the interfaces implemented by a type.
     */
    public static ImplementsMatchBuilder implements_() {
        return new ImplementsMatchBuilder();
    }

    /**
     * Starts a constraints on the super type of a type.
     * @param type the required super type
     */
    public static ExtendsMatchBuilder extends_(TypeReferenceMatchBuilder type) {
        return new ExtendsMatchBuilder(type);
    }

    private abstract static class AbstractStatementBuilder<This extends AbstractStatementBuilder<This, S>, S extends AbstractStatement> extends AbstractMatchBuilder<S> {
        protected final List<AnnotationMatchBuilder> annotations = new ArrayList<>();
        protected ModifiersMatchBuilder modifiers = new ModifiersMatchBuilder();
        protected boolean negation;
        @Nullable String definedVariable;
        boolean isMatch;

        private AbstractStatementBuilder() {

        }

        /**
         * Assigns a name to the statement using which it can be referred to in other places of the query.
         * @see #match(String, String...)
         * @see #usedBy(String, String...)
         * @see SingleTypeReferenceMatchBuilder#ref(String)
         */
        public This as(String name) {
            this.definedVariable = name;
            return castThis();
        }

        /**
         * Makes this statement returned from the query
         */
        public This matched() {
            this.isMatch = true;
            return castThis();
        }

        /**
         * Sets a constraint on modifiers of the statement
         * @param modifiers the modifiers
         * @see #modifiers()
         * @see #modifiers(Modifier, Modifier...)
         * @see #atLeastOne()
         */
        public This $(ModifiersMatchBuilder modifiers) {
            this.modifiers = modifiers;
            return castThis();
        }

        /**
         * Sets a constraint on annotation on the statement
         * @param annotation the annotation to match
         * @see #annotation(TypeReferenceMatchBuilder)
         */
        public This $(AnnotationMatchBuilder annotation) {
            this.annotations.add(annotation);
            return castThis();
        }

        /**
         * Negates this statement. I.e. it is only matched if it doesn't comply with this query.
         */
        public This negated() {
            this.negation = true;
            return castThis();
        }
    }

    /**
     * Builds a query matching any kind of statement.
     */
    public static final class GenericStatementBuilder extends AbstractStatementBuilder<GenericStatementBuilder, GenericStatement> {
        private @Nullable UsesMatchBuilder uses;

        private GenericStatementBuilder() {

        }

        /**
         * Sets a constraint on what types this statement uses.
         * @see #uses(TypeReferenceMatchBuilder)
         */
        public GenericStatementBuilder $(UsesMatchBuilder uses) {
            this.uses = uses;
            return this;
        }

        public GenericStatement build() {
            return new GenericStatement(definedVariable, referencedVariables, new AnnotationsMatch(finish(annotations)),
                    finish(modifiers), isMatch, negation, finish(uses));
        }
    }

    /**
     * Builds a query on a field.
     */
    public static final class FieldStatementBuilder extends AbstractStatementBuilder<FieldStatementBuilder, FieldStatement> {
        private final NameMatch name;
        private @Nullable TypeReferenceMatchBuilder type;
        private @Nullable TypeReferenceMatchBuilder declaringType;
        private @Nullable UsesMatchBuilder uses;

        private FieldStatementBuilder(NameMatch name) {
            this.name = name;
        }

        /**
         * Sets a constraint on the type of the field.
         * @see #type()
         * @see #type(TypeKindBuilder, FqnMatchBuilder)
         * @see #anyType()
         */
        public FieldStatementBuilder $(TypeReferenceMatchBuilder type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the constraint on the type the field is declared in.
         * @see #type()
         * @see #type(TypeKindBuilder, FqnMatchBuilder)
         * @see #anyType()
         */
        public FieldStatementBuilder declaredIn(TypeReferenceMatchBuilder type) {
            this.declaringType = type;
            return this;
        }

        /**
         * Sets the constraint on what types this field uses.
         * @see #uses(TypeReferenceMatchBuilder)
         */
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

    /**
     * Builds a query on a method.
     */
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

        /**
         * Sets the constraint on the return type of the method.
         * @see #type()
         * @see #type(TypeKindBuilder, FqnMatchBuilder)
         * @see #anyType()
         */
        public MethodStatementBuilder returns(TypeReferenceMatchBuilder type) {
            this.returnType = type;
            return this;
        }

        /**
         * Sets the constraint on the type the method is declared in.
         * @see #type()
         * @see #type(TypeKindBuilder, FqnMatchBuilder)
         * @see #anyType()
         */
        public MethodStatementBuilder declaredIn(TypeReferenceMatchBuilder type) {
            this.declaringType = type;
            return this;
        }

        /**
         * Sets the constraint on the method type parameter. Multiple calls of this method define constraints on the
         * consecutive method type parameters.
         * @param typeParameter the type parameter of the method
         * @see #bound(TypeReferenceMatchBuilder)
         * @see #wildcard()
         * @see #wildcardSuper(TypeReferenceMatchBuilder)
         * @see #wildcardExtends(TypeReferenceMatchBuilder)
         */
        public MethodStatementBuilder $(TypeParameterMatchBuilder typeParameter) {
            this.typeParameters.add(typeParameter);
            return this;
        }

        /**
         * Sets the constraint on what this method overrides.
         * @see #overrides()
         */
        public MethodStatementBuilder $(OverridesMatchBuilder overrides) {
            this.overrides = overrides;
            return this;
        }

        /**
         * Sets a constraint on what types this method uses.
         * @see #uses(TypeReferenceMatchBuilder)
         */
        public MethodStatementBuilder $(UsesMatchBuilder uses) {
            this.uses = uses;
            return this;
        }

        /**
         * Sets a constraint on what this method throws.
         * @see #type()
         * @see #type(TypeKindBuilder, FqnMatchBuilder)
         * @see #anyType()
         */
        public MethodStatementBuilder throws_(TypeReferenceMatchBuilder type) {
            this.throws_.add(type);
            return this;
        }

        /**
         * Sets the constraint on the method parameter. Multiple calls of this method define constraints on the
         * consecutive method parameters.
         * @param parameter the parameter of the method
         * @see #anyParameter()
         * @see #anyParameters()
         * @see #parameter(TypeReferenceMatchBuilder)
         */
        public MethodStatementBuilder $(MethodParameterMatchBuilder parameter) {
            this.parameters.add(parameter);
            return this;
        }

        /**
         * Sets a constraint on the default value of a method.
         * @see #defaultValue()
         */
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

    /**
     * Builds a query for a type.
     */
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

        /**
         * Sets the constraint on the generic type parameter. Multiple calls of this method define constraints on the
         * consecutive generic type parameters.
         * @param param the generic parameter of the type
         * @see #bound(TypeReferenceMatchBuilder)
         * @see #wildcard()
         * @see #wildcardSuper(TypeReferenceMatchBuilder)
         * @see #wildcardExtends(TypeReferenceMatchBuilder)
         */
        public TypeDefinitionStatementBuilder $(TypeParameterMatchBuilder param) {
            this.typeParameters.add(param);
            return this;
        }

        /**
         * Sets the constraint on the interfaces implemented by the queried type.
         * @see #implements_()
         */
        public TypeDefinitionStatementBuilder $(ImplementsMatchBuilder implements_) {
            this.implemented.add(implements_);
            return this;
        }

        /**
         * Sets the constraint on the super type of the queried type.
         * @see #extends_(TypeReferenceMatchBuilder)
         */
        public TypeDefinitionStatementBuilder $(ExtendsMatchBuilder extends_) {
            this.extended = extends_;
            return this;
        }

        /**
         * Sets the constraint on the types used by the queried type.
         * @see #uses(TypeReferenceMatchBuilder)
         */
        public TypeDefinitionStatementBuilder $(UsesMatchBuilder uses) {
            this.uses.add(uses);
            return this;
        }

        /**
         * Sets the constraint on the types used by the queried type.
         * @see #usedBy(String, String...)
         */
        public TypeDefinitionStatementBuilder $(UsedByMatchBuilder usedBy) {
            this.usedBys.add(usedBy);
            return this;
        }

        /**
         * Sets the constraint on a field declared in the queried type. Multiple calls of this method will require
         * the queried type to declare all the queried fields.
         * @see #field(NameMatch)
         */
        public TypeDefinitionStatementBuilder $(FieldStatementBuilder field) {
            this.innerStatements.add(field);
            return this;
        }

        /**
         * Sets the constraint on a method declared in the queried type. Multiple calls of this method will require
         * the queried type to declare all the queried methods.
         * @see #method(NameMatch)
         */
        public TypeDefinitionStatementBuilder $(MethodStatementBuilder method) {
            this.innerStatements.add(method);
            return this;
        }

        /**
         * Sets the constraint on a nested type declared in the queried type. Multiple calls of this method will require
         * the queried type to declare all the queried nested types.
         * @see #type()
         * @see #type(TypeKindBuilder, FqnMatchBuilder)
         * @see #anyType()
         */
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

    /**
     * Creates a constraint on the implemented interfaces of a type.
     */
    public static final class ImplementsMatchBuilder extends AbstractMatchBuilder<ImplementsMatch> {
        private boolean directly;
        private boolean exactly;
        private final List<TypeReferenceMatchBuilder> types = new ArrayList<>();

        private ImplementsMatchBuilder() {

        }

        /**
         * Modifies the constraint to only consider directly implemented interfaces.
         */
        public ImplementsMatchBuilder directly() {
            this.directly = true;
            return this;
        }

        /**
         * Modifies the constraint so that it checks that the type implements only the listed interfaces and no else.
         */
        public ImplementsMatchBuilder exactly() {
            this.exactly = true;
            return this;
        }

        /**
         * Adds a type to the list of the implemented interfaces. The order in which the interfaces are added is not
         * significant.
         *
         * @param type the type of the interface that should be implemented
         */
        public ImplementsMatchBuilder $(TypeReferenceMatchBuilder type) {
            this.types.add(type);
            return this;
        }

        @Override
        public ImplementsMatch build() {
            return new ImplementsMatch(directly, exactly, finish(types));
        }
    }

    /**
     * Builds a constraint on the super type of the queried type.
     */
    public static final class ExtendsMatchBuilder extends AbstractMatchBuilder<ExtendsMatch> {
        private boolean directly;
        private final TypeReferenceMatchBuilder type;

        private ExtendsMatchBuilder(TypeReferenceMatchBuilder type) {
            this.type = type;
        }


        /**
         * Modifies the constraint such that it only matches types that are direct subtypes of the provided super type.
         */
        public ExtendsMatchBuilder directly() {
            this.directly = true;
            return this;
        }

        @Override
        public ExtendsMatch build() {
            return new ExtendsMatch(directly, finish(type));
        }
    }

    /**
     * Builds a constraint on a method parameter.
     */
    public static final class MethodParameterMatchBuilder extends AbstractMatchBuilder<MethodParameterMatch> {
        private final List<AnnotationMatchBuilder> annotations = new ArrayList<>();
        private final TypeReferenceMatchBuilder type;

        private MethodParameterMatchBuilder(TypeReferenceMatchBuilder type) {
            this.type = type;
        }

        /**
         * Sets the constraint on annotation present on the method parameter.
         * @see #annotation(TypeReferenceMatchBuilder)
         */
        public MethodParameterMatchBuilder $(AnnotationMatchBuilder annotation) {
            this.annotations.add(annotation);
            return this;
        }

        @Override
        public MethodParameterMatch build() {
            return new MethodParameterMatch(new AnnotationsMatch(finish(annotations)), finish(type));
        }
    }

    /**
     * Builds a Classif query.
     */
    public static final class Builder extends AbstractMatchBuilder<StructuralMatcher> {
        private final List<AbstractStatementBuilder<?, ? extends AbstractStatement>> statements = new ArrayList<>();
        private final List<String> returns;
        private boolean strictHierarchy;

        private Builder(Collection<String> returns) {
            this.returns = new ArrayList<>(returns);
        }

        /**
         * Modifies the matching so that strict hierarchy match is required for nested types. The default mode is to
         * match both top-level types and nested types using a top level type statement. If {@code strictHierarchy} is
         * set, then a subtype is only matched if the query also reflects its enclosing types.
         */
        public Builder strictHierarchy() {
            this.strictHierarchy = true;
            return this;
        }

        /**
         * Adds a statement on any type of Java element to the query.
         * @see #declaration()
         */
        public Builder $(GenericStatementBuilder decl) {
            statements.add(decl);
            return this;
        }

       /**
         * Adds a type statement to the query.
         * @see #type()
         * @see #type(TypeKindBuilder, FqnMatchBuilder)
         * @see #anyType()
         */
        public Builder $(TypeDefinitionStatementBuilder type) {
            statements.add(type);
            return this;
        }

        @Override
        public StructuralMatcher build() {
            return new StructuralMatcher(new StructuralMatcher.Configuration(strictHierarchy), returns, finish(statements));
        }
    }

    /**
     * Adds a constraint on the fully qualified name of the type.
     */
    public static final class FqnMatchBuilder extends AbstractMatchBuilder<FqnMatch> {
        private final List<NameMatch> parts;

        private FqnMatchBuilder(List<NameMatch> parts) {
            this.parts = parts;
        }

        /**
         * Adds another level to the hierarchical fully qualified name.
         */
        public FqnMatchBuilder dot(NameMatch name) {
            parts.add(name);
            return this;
        }

        /**
         * Adds a couple of levels to the hierarchical fully qualified name in the order.
         */
        public FqnMatchBuilder parts(NameMatch... names) {
            parts.addAll(asList(names));
            return this;
        }

        @Override
        public FqnMatch build() {
            return new FqnMatch(parts);
        }
    }

    /**
     * A super-class for the more specific type parameter constraints.
     */
    public static abstract class TypeParameterMatchBuilder extends AbstractMatchBuilder<TypeParameterMatch> {
        private TypeParameterMatchBuilder() {

        }

        public abstract TypeParameterMatch build();
    }

    /**
     * Adds a constraint on the qualified bound of the type parameter. This works for both the named type parameters
     * and wildcards, despite the name. See Classif documentation for the explanation.
     */
    public static final class TypeParameterWildcardBuilder extends TypeParameterMatchBuilder {
        private final boolean isExtends;
        private final List<TypeReferenceMatchBuilder> bounds = new ArrayList<>();

        private TypeParameterWildcardBuilder(boolean isExtends) {
            this.isExtends = isExtends;
        }

        /**
         * Adds another type to the bound, making it an intersection bound, e.g.
         * {@code T extends Cloneable & Serializable}.
         */
        public TypeParameterWildcardBuilder and(TypeReferenceMatchBuilder type) {
            bounds.add(type);
            return this;
        }

        @Override
        public TypeParameterMatch build() {
            return new TypeParameterMatch(new TypeParameterWildcardMatch(isExtends, finish(bounds)), null);
        }
    }

    /**
     * Builds a concrete type parameter bound, e.g. the {@code String} in the {@code Set<String>}.
     */
    public static final class TypeParameterBoundBuilder extends TypeParameterMatchBuilder {
        private final List<TypeReferenceMatchBuilder> bounds = new ArrayList<>();

        private TypeParameterBoundBuilder() {

        }

        /**
         * Adds another type to the bound, making it an intersection bound, e.g.
         * {@code Cloneable & Serializable}. While this is not a valid Java construct (you cannot specify
         * {@code Set<Cloneable & Serializable>}), it may be useful to test the types like that.
         */
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

        /**
         * Negates the type match
         */
        public This negated() {
            this.negation = true;
            return castThis();
        }

        /**
         * Modifies the match to match array types of given dimension.
         * E.g. {@code type(exact("java"), exact("lang"), exact("String")).array(2)} would match {@code String[][]}.
         */
        public This array(int arrayDimension) {
            this.arrayDimension = arrayDimension;
            return castThis();
        }

        /**
         * The constraint will only match types that correspond to the type statement
         * {@link TypeDefinitionStatementBuilder#as(String) called} this name. E.g. this type reference matches the
         * type using its {@code var}iable name.
         */
        public This ref(String variableName) {
            this.variable = variableName;
            return castThis();
        }

        /**
         * Modifies the constraint to match only types with the provided fully qualified name.
         * @see Classif#fqn(NameMatch, NameMatch...)
         */
        public This fqn(NameMatch first, NameMatch... rest) {
            this.fqn = Classif.fqn(first, rest);
            return castThis();
        }

        /**
         * Adds a type parameter constraint on the type match.
         * @see #wildcard()
         * @see #wildcardExtends(TypeReferenceMatchBuilder)
         * @see #wildcardSuper(TypeReferenceMatchBuilder)
         * @see #bound(TypeReferenceMatchBuilder)
         */
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

    /**
     * Creates a type constraint.
     *
     * @see #type()
     * @see #type(TypeKindBuilder, FqnMatchBuilder)
     * @see #type(TypeKind, FqnMatchBuilder)
     * @see #type(TypeKind, NameMatch, NameMatch...)
     * @see #type(TypeKindBuilder, NameMatch, NameMatch...)
     */
    public static final class TypeReferenceMatchBuilder extends SingleTypeReferenceMatchBuilder<TypeReferenceMatchBuilder, TypeReferenceMatch> {
        private final List<SingleTypeReferenceMatch> types = new ArrayList<>();

        private TypeReferenceMatchBuilder() {

        }

        /**
         * Finishes off the current type match and starts a new one. The resulting match will succeed if either the
         * old one or the new one to be built will match.
         */
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

    /**
     * Creates a constraint on types used by a statement.
     * @see #uses(TypeReferenceMatchBuilder)
     */
    public static final class UsesMatchBuilder extends AbstractMatchBuilder<UsesMatch> {
        private boolean onlyDirect;
        private final TypeReferenceMatchBuilder type;

        private UsesMatchBuilder(TypeReferenceMatchBuilder type) {
            this.type = type;
        }

        /**
         * By default indirect usages are also considered. Using this method only direct usages are allowed. A direct
         * usage means that the type appears directly in the statement.
         */
        public UsesMatchBuilder directly() {
            this.onlyDirect = true;
            return this;
        }

        @Override
        public UsesMatch build() {
            return new UsesMatch(onlyDirect, finish(type));
        }
    }

    /**
     * Creates a constraint on a type by specifying what it should be used by.
     * @see #usedBy(String, String...)
     */
    public static final class UsedByMatchBuilder extends AbstractMatchBuilder<UsedByMatch> {
        private boolean onlyDirect;

        private UsedByMatchBuilder(List<String> variables) {
            this.referencedVariables.addAll(variables);
        }

        /**
         * Limits the constraint so that it only matches types that are directly used by the matching statements.
         * The default is to match also indirect usages.
         */
        public UsedByMatchBuilder directly() {
            this.onlyDirect = true;
            return this;
        }

        public UsedByMatch build() {
            return new UsedByMatch(onlyDirect, referencedVariables);
        }
    }

    /**
     * Builds a type kind constraint.
     * @see #kind(TypeKind)
     * @see #type(TypeKindBuilder, NameMatch, NameMatch...)
     * @see #type(TypeKindBuilder, FqnMatchBuilder)
     */
    public static final class TypeKindBuilder extends AbstractMatchBuilder<TypeKindMatch> {
        private boolean negated;
        private final TypeKind kind;

        private TypeKindBuilder(TypeKind kind) {
            this.kind = kind;
        }

        /**
         * Negates the type kind match.
         */
        public TypeKindBuilder negated() {
            this.negated = true;
            return this;
        }

        @Override
        public TypeKindMatch build() {
            return new TypeKindMatch(negated, kind);
        }
    }

    /**
     * Builds a throws constraint on method statements.
     *
     * @see MethodStatementBuilder#throws_(TypeReferenceMatchBuilder)
     */
    public static final class ThrowsMatchBuilder extends AbstractMatchBuilder<ThrowsMatch> {
        private final List<TypeReferenceMatchBuilder> types = new ArrayList<>();

        /**
         * Adds a type to the list of thrown types.
         * @see #type()
         * @see #type(TypeKindBuilder, FqnMatchBuilder)
         * @see #anyType()
         * @see #anyTypes()
         */
        public ThrowsMatchBuilder $(TypeReferenceMatchBuilder type) {
            types.add(type);
            return this;
        }

        @Override
        public ThrowsMatch build() {
            return new ThrowsMatch(finish(types));
        }
    }

    /**
     * Builds a constraint limiting the method statement to match methods overriding some other method.
     */
    public static final class OverridesMatchBuilder extends AbstractMatchBuilder<OverridesMatch> {
        private @Nullable Classif.TypeReferenceMatchBuilder type;

        private OverridesMatchBuilder() {

        }

        /**
         * If specified, the statement will only match methods that override from the specified type.
         * @see #type()
         * @see #type(TypeKindBuilder, FqnMatchBuilder)
         * @see #anyType()
         */
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

        AnnotationValueMatch buildValueMatch() {
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

    public static final class AnnotationAttributeAllBuilder extends AnnotationAttributeMatchBuilder {
        private AnnotationAttributeAllBuilder() {

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
        final List<String> referencedVariables = new ArrayList<>();

        public abstract M build();

        private void copyVariables(@Nullable Classif.AbstractMatchBuilder<?> other) {
            if (other != null) {
                referencedVariables.addAll(other.referencedVariables);
            }
        }

        <X> @Nullable X finish(@Nullable AbstractMatchBuilder<X> builder) {
            if (builder == null) {
                return null;
            }

            X ret = builder.build();

            // important to copy the variables only after the builder had the chance to finish itself up
            copyVariables(builder);

            return ret;
        }

        <X> List<X> finish(List<@Nullable ? extends AbstractMatchBuilder<? extends X>> builders) {
            return builders.stream()
                    .filter(Objects::nonNull)
                    .map(this::finish)
                    .collect(toList());
        }

        @SuppressWarnings("unchecked")
        <T> T castThis() {
            return (T) this;
        }
    }
}
