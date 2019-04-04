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
package org.revapi.classif.statement;

import static java.util.regex.Pattern.compile;

import static javax.lang.model.util.ElementFilter.methodsIn;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.revapi.classif.Classif.annotation;
import static org.revapi.classif.Classif.anyParameter;
import static org.revapi.classif.Classif.anyParameters;
import static org.revapi.classif.Classif.anyType;
import static org.revapi.classif.Classif.anyTypes;
import static org.revapi.classif.Classif.bound;
import static org.revapi.classif.Classif.extends_;
import static org.revapi.classif.Classif.method;
import static org.revapi.classif.Classif.modifiers;
import static org.revapi.classif.Classif.parameter;
import static org.revapi.classif.Classif.type;
import static org.revapi.classif.Classif.wildcard;
import static org.revapi.classif.Classif.wildcardExtends;
import static org.revapi.classif.Classif.wildcardSuper;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;
import static org.revapi.classif.match.NameMatch.any;
import static org.revapi.classif.match.NameMatch.exact;
import static org.revapi.classif.match.NameMatch.pattern;
import static org.revapi.classif.match.declaration.Modifier.PACKAGE_PRIVATE;
import static org.revapi.classif.match.declaration.Modifier.PRIVATE;
import static org.revapi.classif.match.declaration.Modifier.PROTECTED;
import static org.revapi.classif.match.declaration.Modifier.PUBLIC;
import static org.revapi.classif.match.declaration.TypeKind.ANY;
import static org.revapi.classif.match.declaration.TypeKind.CLASS;

import java.util.List;
import java.util.function.BiFunction;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.Classif;
import org.revapi.classif.StructuralMatcher;
import org.revapi.classif.TestResult;
import org.revapi.classif.Tester;
import org.revapi.classif.Tester.Hierarchy;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class MethodStatementTest {
    @JarSources(root = "/sources/methods/", sources = {"Base.java", "Inherited.java", "Annotated.java",
            "TypeParameters.java"})
    private CompiledJar.Environment env;

    private TypeElement Base;
    private TypeElement Inherited;
    private TypeElement TypeParameters;
    private ExecutableElement baseMethodInBase;
    private ExecutableElement baseMethodInInherited;
    private ExecutableElement methodParameter;
    private ExecutableElement method2Parameters;
    private ExecutableElement method3Parameters;
    private ExecutableElement privateMethod;
    private ExecutableElement protectedMethod;
    private ExecutableElement packagePrivateMethod;
    private ExecutableElement annotatedMethod;
    private ExecutableElement annotatedParameterMethod;
    private ExecutableElement throwingMethod;
    private ExecutableElement throwingMethod2;
    private ExecutableElement useClassTypeParamsMethod;
    private ExecutableElement ownTypeParamsMethod;
    private ExecutableElement bareWildcardMethod;
    private ExecutableElement extendingWildcardMethod;
    private ExecutableElement superWildcardMethod;
    private ExecutableElement classTypeParamsBasedWildcard;
    private ExecutableElement ownTypeParamBasedWildcard;

    @BeforeEach
    void loadElements() {
        Elements els = env.elements();
        Base = els.getTypeElement("Base");
        Inherited = els.getTypeElement("Inherited");
        TypeParameters = els.getTypeElement("TypeParameters");
        
        List<? extends ExecutableElement> methodsInBase = methodsIn(Base.getEnclosedElements());
        List<? extends ExecutableElement> methodsInInherited = methodsIn(Inherited.getEnclosedElements());
        List<? extends ExecutableElement> methodsInTypeParameters = methodsIn(TypeParameters.getEnclosedElements());
        
        BiFunction<List<? extends ExecutableElement>, String, ExecutableElement> findByName = (list, name) ->
                list.stream().filter(m -> m.getSimpleName().contentEquals(name)).findFirst().get();

        baseMethodInBase = findByName.apply(methodsInBase, "baseMethod");
        baseMethodInInherited = findByName.apply(methodsInInherited, "baseMethod");
        methodParameter = findByName.apply(methodsInInherited, "methodParameter");
        method2Parameters = findByName.apply(methodsInInherited, "method2Parameters");
        method3Parameters = findByName.apply(methodsInInherited, "method3Parameters");
        privateMethod = findByName.apply(methodsInInherited, "privateMethod");
        protectedMethod = findByName.apply(methodsInInherited, "protectedMethod");
        packagePrivateMethod = findByName.apply(methodsInInherited, "packagePrivateMethod");
        annotatedMethod = findByName.apply(methodsInInherited, "annotatedMethod");
        annotatedParameterMethod = findByName.apply(methodsInInherited, "annotatedParameterMethod");
        throwingMethod = findByName.apply(methodsInInherited, "throwingMethod");
        throwingMethod2 = findByName.apply(methodsInInherited, "throwingMethod2");
        useClassTypeParamsMethod = findByName.apply(methodsInTypeParameters, "useClassTypeParamsMethod");
        ownTypeParamsMethod = findByName.apply(methodsInTypeParameters, "ownTypeParamsMethod");
        bareWildcardMethod = findByName.apply(methodsInTypeParameters, "bareWildcardMethod");
        extendingWildcardMethod = findByName.apply(methodsInTypeParameters, "extendingWildcardMethod");
        superWildcardMethod = findByName.apply(methodsInTypeParameters, "superWildcardMethod");
        classTypeParamsBasedWildcard = findByName.apply(methodsInTypeParameters, "classTypeParamsBasedWildcard");
        ownTypeParamBasedWildcard = findByName.apply(methodsInTypeParameters, "ownTypeParamBasedWildcard");
    }

    @Test
    void testModifiers() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited)
                .add(privateMethod)
                .add(protectedMethod)
                .add(packagePrivateMethod)
                .add(baseMethodInInherited)
                .end()
                .build(), privateMethod, protectedMethod, packagePrivateMethod, baseMethodInInherited);

        StructuralMatcher mPrivate = Classif.match().$(type(ANY, any()).$(method(any()).matched()
                .$(modifiers(PRIVATE)))).build();

        StructuralMatcher mProtected = Classif.match().$(type(ANY, any()).$(method(any()).matched()
                .$(modifiers(PROTECTED)))).build();

        StructuralMatcher mPackagePrivate = Classif.match().$(type(ANY, any()).$(method(any()).matched()
                .$(modifiers(PACKAGE_PRIVATE)))).build();

        StructuralMatcher mPublic = Classif.match().$(type(ANY, any()).$(method(any()).matched()
                .$(modifiers(PUBLIC)))).build();

        tests.test(mPrivate, PASSED, NOT_PASSED, NOT_PASSED, NOT_PASSED);
        tests.test(mProtected, NOT_PASSED, PASSED, NOT_PASSED, NOT_PASSED);
        tests.test(mPackagePrivate, NOT_PASSED, NOT_PASSED, PASSED, NOT_PASSED);
        tests.test(mPublic, NOT_PASSED, NOT_PASSED, NOT_PASSED, PASSED);
    }

    @Test
    void testMethodAnnotations() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(annotatedMethod).add(baseMethodInInherited).end().build(),
                annotatedMethod, baseMethodInInherited);

        // "type * { @* ^*(); }
        StructuralMatcher annotated = Classif.match()
                .$(type(ANY, any())
                        .$(method(any()).matched()
                                .$(annotation(type().fqn(any())))))
                .build();

        // type * { !@* ^*(); }
        StructuralMatcher notAnnotated = Classif.match()
                .$(type(ANY, any())
                        .$(method(any()).matched()
                                .$(annotation(type().fqn(any())).negated())))
                .build();

        // type * { ^*(); }
        StructuralMatcher uninterested = Classif.match()
                .$(type(ANY, any())
                        .$(method(any()).matched()))
                .build();

        tests.test(annotated, PASSED, NOT_PASSED);
        tests.test(notAnnotated, NOT_PASSED, PASSED);
        tests.test(uninterested, PASSED, PASSED);
    }

    @Test
    void testDeclaringType() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(baseMethodInBase).add(annotatedMethod).end().build(),
                baseMethodInBase, annotatedMethod);

        // type * { Base::^*(); }
        StructuralMatcher declared = Classif.match()
                .$(type(ANY, any())
                        .$(method(any()).matched().declaredIn(type().fqn(exact("Base")))))
                .build();

        // type * { !Base::^*(); }
        StructuralMatcher negated = Classif.match()
                .$(type(ANY, any())
                        .$(method(any()).matched().declaredIn(type().negated().fqn(exact("Base")))))
                .build();

        // type * { ^*(); }
        StructuralMatcher undeclared = Classif.match()
                .$(type(ANY, any())
                        .$(method(any()).matched()))
                .build();

        tests.test(declared, PASSED, NOT_PASSED);
        tests.test(negated, NOT_PASSED, PASSED);
        tests.test(undeclared, PASSED, PASSED);
    }

    @Test
    void testName() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(baseMethodInBase).end().build(),
                baseMethodInBase);

        tests.test(Classif.match().$(type(ANY, any()).$(method(exact("baseMethod")).matched())).build(), PASSED);
        tests.test(Classif.match().$(type(ANY, any()).$(method(pattern(compile("bas.Method"))).matched())).build(), PASSED);
    }

    @Test
    void testParameters() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(methodParameter).add(method2Parameters).add(method3Parameters).end().build(),
                methodParameter, method2Parameters, method3Parameters);

        // type * { ^*(int); }
        StructuralMatcher singleParam = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched().$(parameter(type().fqn(exact("int")))))).build();

        // type * { ^!*(java.lang.String); }
        StructuralMatcher notMethod = Classif.match()
                .$(type(ANY, any()).$(method(any()).negated().matched()
                        .$(parameter(type().fqn(exact("java"), exact("lang"), exact("String")))))).build();

        // type * { ^*(!java.lang.String, float); }
        StructuralMatcher notParam = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(parameter(type().negated().fqn(exact("java"), exact("lang"), exact("String"))))
                        .$(parameter(type().fqn(exact("float"))))))
                .build();

        // type * { ^*(java.lang.String, int|float, java.lang.Cloneable); }
        StructuralMatcher orParam = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(parameter(type().fqn(exact("java"), exact("lang"), exact("String"))))
                        .$(parameter(type().fqn(exact("int")).or().fqn(exact("float"))))
                        .$(parameter(type().fqn(exact("java"), exact("lang"), exact("Cloneable"))))))
                .build();

        tests.test(singleParam, PASSED, NOT_PASSED, NOT_PASSED);
        tests.test(notMethod, PASSED, PASSED, PASSED);
        tests.test(notParam, NOT_PASSED, PASSED, NOT_PASSED);
        tests.test(orParam, NOT_PASSED, NOT_PASSED, PASSED);
    }

    @Test
    void testParameterAnnotations() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited)
                .add(method2Parameters)
                .add(annotatedParameterMethod)
                .add(method3Parameters)
                .end()
                .build(),
                method2Parameters, annotatedParameterMethod);

        StructuralMatcher notAnnotated = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(parameter(type().fqn(exact("int"))))
                        .$(parameter(type().fqn(exact("float"))))))
                .build();

        StructuralMatcher annotatedFirst = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(parameter(type().fqn(exact("int"))).$(annotation(type().fqn(exact("Annotated"))))))).build();

        StructuralMatcher annotatedSecond = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(parameter(type().fqn(exact("int"))))
                        .$(parameter(type().fqn(exact("float"))).$(annotation(type().fqn(exact("Annotated")))))))
                .build();

        StructuralMatcher negatedFirstAnnotation = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(parameter(type().fqn(exact("int"))).$(annotation(type().fqn(exact("Annotated"))).negated()))
                        .$(parameter(type().fqn(exact("float"))).$(annotation(type().fqn(exact("Annotated")))))))
                .build();

        StructuralMatcher negatedSecondAnnotation = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(parameter(type().fqn(exact("int"))).$(annotation(type().fqn(exact("Annotated")))))
                        .$(parameter(type().fqn(exact("float"))).$(annotation(type().fqn(exact("Annotated")))
                                .negated()))))
                .build();

        tests.test(notAnnotated, PASSED, PASSED);
        tests.test(annotatedFirst, NOT_PASSED, NOT_PASSED);
        tests.test(annotatedSecond, NOT_PASSED, PASSED);
        tests.test(negatedFirstAnnotation, NOT_PASSED, PASSED);
        tests.test(negatedSecondAnnotation, NOT_PASSED, NOT_PASSED);
    }

    @Test
    void testParameterGlobs() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(methodParameter).add(method2Parameters).add(method3Parameters).end().build(),
                methodParameter, method2Parameters, method3Parameters);

        StructuralMatcher oneParam = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(anyParameter())))
                .build();

        StructuralMatcher allParams = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(anyParameters())))
                .build();

        StructuralMatcher oneOrMoreParams = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(anyParameter())
                        .$(anyParameters())))
                .build();

        StructuralMatcher firstAndLastParameters = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(anyParameter())
                        .$(anyParameters())
                        .$(anyParameter())))
                .build();

        tests.test(oneParam, PASSED, NOT_PASSED, NOT_PASSED);
        tests.test(allParams, PASSED, PASSED, PASSED);
        tests.test(oneOrMoreParams, PASSED, PASSED, PASSED);
        tests.test(firstAndLastParameters, NOT_PASSED, PASSED, PASSED);
    }

    @Test
    void testTypeParameters() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(TypeParameters)
                .add(useClassTypeParamsMethod)
                .add(ownTypeParamsMethod)
                .add(bareWildcardMethod)
                .add(extendingWildcardMethod)
                .add(superWildcardMethod)
                .add(classTypeParamsBasedWildcard)
                .add(ownTypeParamBasedWildcard)
                .end()
                .build());

        StructuralMatcher noTypeParamsSpecified = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(parameter(type().fqn(exact("java"), exact("lang"), exact("Object"))))))
                .build();

        tests.test(noTypeParamsSpecified,
                useClassTypeParamsMethod, PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, NOT_PASSED,
                superWildcardMethod, NOT_PASSED,
                classTypeParamsBasedWildcard, NOT_PASSED,
                ownTypeParamBasedWildcard, NOT_PASSED);

        StructuralMatcher methodTypeParameters = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .$(bound(type().fqn(exact("java"), exact("lang"), exact("Object"))))
                        .$(wildcardExtends(type().fqn(exact("java"), exact("lang"), exact("Number"))))
                        .returns(type().fqn(any()))
                        .$(parameter(type().fqn(exact("java"), exact("lang"), exact("Number"))))))
                .build();

        tests.test(methodTypeParameters,
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, NOT_PASSED,
                superWildcardMethod, NOT_PASSED,
                classTypeParamsBasedWildcard, NOT_PASSED,
                ownTypeParamBasedWildcard, NOT_PASSED
        );

        StructuralMatcher wildcardBound = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .returns(type().fqn(any()))
                        .$(parameter(type().fqn(exact("java"), exact("util"), exact("Set")).$(wildcard())))))
                .build();

        tests.test(wildcardBound,
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, PASSED,
                extendingWildcardMethod, PASSED,
                superWildcardMethod, PASSED,
                classTypeParamsBasedWildcard, PASSED,
                ownTypeParamBasedWildcard, PASSED
        );

        StructuralMatcher extendsBound = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .returns(type().fqn(any()))
                        .$(parameter(type().fqn(exact("java"), exact("util"), exact("Set"))
                                .$(wildcardExtends(type().fqn(exact("java"), exact("lang"), exact("String"))))))))
                .build();

        tests.test(extendsBound,
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, PASSED,
                superWildcardMethod, NOT_PASSED,
                classTypeParamsBasedWildcard, PASSED,
                ownTypeParamBasedWildcard, NOT_PASSED
        );

        StructuralMatcher superBound = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .returns(type().fqn(any()))
                        .$(parameter(type().fqn(exact("java"), exact("util"), exact("Set"))
                                .$(wildcardSuper(
                                        type().fqn(exact("java"), exact("lang"), exact("Comparable"))
                                                .$(wildcard())))))))
                .build();

        tests.test(superBound,
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, NOT_PASSED,
                superWildcardMethod, PASSED,
                classTypeParamsBasedWildcard, NOT_PASSED,
                ownTypeParamBasedWildcard, NOT_PASSED
        );

        StructuralMatcher extendsBound2 = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .returns(type().fqn(any()))
                        .$(parameter(type().fqn(exact("java"), exact("util"), exact("Set"))
                                .$(wildcardExtends(type().fqn(exact("java"), exact("lang"), exact("Number"))))))))
                .build();

        tests.test(extendsBound2,
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, NOT_PASSED,
                superWildcardMethod, NOT_PASSED,
                classTypeParamsBasedWildcard, NOT_PASSED,
                ownTypeParamBasedWildcard, PASSED
        );

        StructuralMatcher exactBound = Classif.match()
                .$(type(ANY, any()).$(method(any()).matched()
                        .returns(type().fqn(any()))
                        .$(parameter(type().fqn(exact("java"), exact("util"), exact("Set"))
                                .$(bound(type().fqn(exact("java"), exact("lang"), exact("Number"))))))))
                .build();

        tests.test(exactBound,
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, NOT_PASSED,
                superWildcardMethod, NOT_PASSED,
                classTypeParamsBasedWildcard, NOT_PASSED,
                ownTypeParamBasedWildcard, NOT_PASSED
        );
    }

    @Test
    void testThrows() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(throwingMethod).add(throwingMethod2).end()
                .add(env.elements().getTypeElement("java.io.IOException"))
                .build(),
                throwingMethod, throwingMethod2);

        // type * { ^*() throws java.lang.Exception; }
        StructuralMatcher singleException = Classif.match()
                .$(type(ANY, any())
                        .$(method(any()).matched()
                                .throws_(type().fqn(exact("java"), exact("lang"), exact("Exception")))))
                .build();

        tests.test(singleException, PASSED, NOT_PASSED);

        // type * { ^*() throws java.lang.Exception|%e, **; } class %e=* extends java.lang.Exception {}
        StructuralMatcher moreExceptions = Classif.match()
                .$(type(ANY, any())
                        .$(method(any()).matched()
                                .throws_(type().fqn(exact("java"), exact("lang"), exact("Exception"))
                                        .or().var("e"))
                                .throws_(anyTypes())))
                .$(type(CLASS, any()).called("e")
                        .$(extends_(type().fqn(exact("java"), exact("lang"), exact("Exception")))))
                .build();

        tests.test(moreExceptions, PASSED, PASSED);

        // type * {^*() throws java.lang.AssertionError, *;}
        StructuralMatcher twoExceptions = Classif.match()
                .$(type(ANY, any())
                        .$(method(any()).matched()
                                .throws_(type().fqn(exact("java"), exact("lang"), exact("AssertionError")))
                                .throws_(anyType())))
                .build();

        tests.test(twoExceptions, NOT_PASSED, PASSED);
    }

    @Test
    void testMethodConstraints() {
        // TODO implement
    }

    @Test
    void testDefaultValue() {
        // TODO implement
    }

    @Test
    void testOverrides() {
        // TODO implement
    }

    class Tests {
        final Hierarchy hierarchy;
        final Element[] testedElements;

        Tests(Hierarchy hierarchy, Element... testedElements) {
            this.hierarchy = hierarchy;
            this.testedElements = testedElements;
        }

        void test(StructuralMatcher recipe, TestResult... expectedResults) {
            if (expectedResults.length != testedElements.length) {
                fail("Expecting to test " + testedElements.length + " elements.");
            }

            for (int i = 0; i < testedElements.length; ++i) {
                Assertions.assertEquals(expectedResults[i], Tester.test(env, recipe, hierarchy).get(testedElements[i]),
                        "Failed to match on index " + i + " (" + testedElements[i] + ").");
            }
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        void test(StructuralMatcher recipe, Object... elementsAndResults) {
            if (elementsAndResults.length % 2 != 0) {
                throw new IllegalArgumentException("Expecting an even size of the elementsAndResults array.");
            }

            for (int e = 0, r = 1; r < elementsAndResults.length; e += 2, r += 2) {
                Assertions.assertEquals(elementsAndResults[r], Tester.test(env, recipe, hierarchy).get(elementsAndResults[e]),
                        "Failed to match " + elementsAndResults[e] + " against '" + recipe + "'.");
            }
        }
    }
}
