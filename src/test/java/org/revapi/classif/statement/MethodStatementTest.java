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
package org.revapi.classif.statement;

import static javax.lang.model.util.ElementFilter.methodsIn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;

import java.util.List;
import java.util.function.BiFunction;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
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

        tests.test("type * { private ^*(); }", PASSED, NOT_PASSED, NOT_PASSED, NOT_PASSED);
        tests.test("type * { protected ^*(); }", NOT_PASSED, PASSED, NOT_PASSED, NOT_PASSED);
        tests.test("type * { packageprivate ^*(); }", NOT_PASSED, NOT_PASSED, PASSED, NOT_PASSED);
        tests.test("type * { public ^*(); }", NOT_PASSED, NOT_PASSED, NOT_PASSED, PASSED);
    }

    @Test
    void testMethodAnnotations() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(annotatedMethod).add(baseMethodInInherited).end().build(),
                annotatedMethod, baseMethodInInherited);

        tests.test("type * { @* ^*(); }", PASSED, NOT_PASSED);
        tests.test("type * { !@* ^*(); }", NOT_PASSED, PASSED);
        tests.test("type * { ^*(); }", PASSED, PASSED);
    }

    @Test
    void testDeclaringType() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(baseMethodInBase).add(annotatedMethod).end().build(),
                baseMethodInBase, annotatedMethod);

        tests.test("type * { Base::^*(); }", PASSED, NOT_PASSED);
        tests.test("type * { !Base::^*(); }", NOT_PASSED, PASSED);
        tests.test("type * { ^*(); }", PASSED, PASSED);
    }

    @Test
    void testName() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(baseMethodInBase).end().build(),
                baseMethodInBase);

        tests.test("type * { ^baseMethod(); }", PASSED);
        tests.test("type * { ^/bas.Method/(); }", PASSED);
    }

    @Test
    void testParameters() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(methodParameter).add(method2Parameters).add(method3Parameters).end().build(),
                methodParameter, method2Parameters, method3Parameters);

        tests.test("type * { ^*(int); }", PASSED, NOT_PASSED, NOT_PASSED);
        tests.test("type * { ^!*(java.lang.String); }", PASSED, PASSED, PASSED);
        tests.test("type * { ^*(!java.lang.String, float); }", NOT_PASSED, PASSED, NOT_PASSED);
        tests.test("type * { ^*(java.lang.String, int|float, java.lang.Cloneable); }", NOT_PASSED, NOT_PASSED, PASSED);
    }

    @Test
    void testParameterAnnotations() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(method2Parameters).add(annotatedParameterMethod).add(method3Parameters).end().build(),
                method2Parameters, annotatedParameterMethod);

        tests.test("type * { ^*(int, float); }", PASSED, PASSED);
        tests.test("type * { ^*(@Annotated int,float); }", NOT_PASSED, NOT_PASSED);
        tests.test("type * { ^*(int, @Annotated float); }", NOT_PASSED, PASSED);
        tests.test("type * { ^*(!@Annotated int, @Annotated float); }", NOT_PASSED, PASSED);
        tests.test("type * { ^*(!@Annotated int, @!Blah float); }", NOT_PASSED, PASSED);
    }

    @Test
    void testParameterGlobs() {
        Tests tests = new Tests(Hierarchy.builder()
                .start(Inherited).add(methodParameter).add(method2Parameters).add(method3Parameters).end().build(),
                methodParameter, method2Parameters, method3Parameters);

        tests.test("type * { ^*(*); }", PASSED, NOT_PASSED, NOT_PASSED);
        tests.test("type * { ^*(**); }", PASSED, PASSED, PASSED);
        tests.test("type * { ^*(*, **); }", PASSED, PASSED, PASSED);
        tests.test("type * { ^*(*,**,*); }", NOT_PASSED, PASSED, PASSED);
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

        tests.test("type * { java.lang.String ^*(java.lang.Object);}",
                useClassTypeParamsMethod, PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, NOT_PASSED,
                superWildcardMethod, NOT_PASSED,
                classTypeParamsBasedWildcard, NOT_PASSED,
                ownTypeParamBasedWildcard, NOT_PASSED);

        tests.test("type * { <java.lang.Object, ? extends java.lang.Number> * ^*(java.lang.Number);}",
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, NOT_PASSED,
                superWildcardMethod, NOT_PASSED,
                classTypeParamsBasedWildcard, NOT_PASSED,
                ownTypeParamBasedWildcard, NOT_PASSED
        );

        tests.test("type * { * ^*(java.util.Set<?>);}",
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, PASSED,
                extendingWildcardMethod, PASSED,
                superWildcardMethod, PASSED,
                classTypeParamsBasedWildcard, PASSED,
                ownTypeParamBasedWildcard, PASSED
        );

        tests.test("type * { * ^*(java.util.Set<? extends java.lang.String>);}",
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, PASSED,
                superWildcardMethod, NOT_PASSED,
                classTypeParamsBasedWildcard, PASSED,
                ownTypeParamBasedWildcard, NOT_PASSED
        );

        tests.test("type * { * ^*(java.util.Set<? super java.lang.Comparable<?>>);}",
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, NOT_PASSED,
                superWildcardMethod, PASSED,
                classTypeParamsBasedWildcard, NOT_PASSED,
                ownTypeParamBasedWildcard, NOT_PASSED
        );

        tests.test("type * { * ^*(java.util.Set<? extends java.lang.Number>);}",
                useClassTypeParamsMethod, NOT_PASSED,
                ownTypeParamsMethod, NOT_PASSED,
                bareWildcardMethod, NOT_PASSED,
                extendingWildcardMethod, NOT_PASSED,
                superWildcardMethod, NOT_PASSED,
                classTypeParamsBasedWildcard, NOT_PASSED,
                ownTypeParamBasedWildcard, PASSED
        );

        tests.test("type * { * ^*(java.util.Set<java.lang.Number>);}",
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

        tests.test("type * { ^*() throws java.lang.Exception; }", PASSED, NOT_PASSED);
        tests.test("type * { ^*() throws java.lang.Exception|%e, **; } class %e=* extends java.lang.Exception;", PASSED,
                PASSED);
        tests.test("type * {^*() throws java.lang.AssertionError, *;}", NOT_PASSED, PASSED);
    }

    @Test
    void testMethodConstraints() {

    }

    @Test
    void testDefaultValue() {

    }

    @Test
    void testOverrides() {

    }

    class Tests {
        final Hierarchy hierarchy;
        final Element[] testedElements;

        Tests(Hierarchy hierarchy, Element... testedElements) {
            this.hierarchy = hierarchy;
            this.testedElements = testedElements;
        }

        void test(String recipe, TestResult... expectedResults) {
            if (expectedResults.length != testedElements.length) {
                fail("Expecting to test " + testedElements.length + " elements.");
            }

            for (int i = 0; i < testedElements.length; ++i) {
                assertEquals(expectedResults[i], Tester.test(env, recipe, hierarchy).get(testedElements[i]),
                        "Failed to match on index " + i + " (" + testedElements[i] + ").");
            }
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        void test(String recipe, Object... elementsAndResults) {
            if (elementsAndResults.length % 2 != 0) {
                throw new IllegalArgumentException("Expecting an even size of the elementsAndResults array.");
            }

            for (int e = 0, r = 1; r < elementsAndResults.length; e += 2, r += 2) {
                assertEquals(elementsAndResults[r], Tester.test(env, recipe, hierarchy).get(elementsAndResults[e]),
                        "Failed to match " + elementsAndResults[e] + " against '" + recipe + "'.");
            }
        }
    }
}
