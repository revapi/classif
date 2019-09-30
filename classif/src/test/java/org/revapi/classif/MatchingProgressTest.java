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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.revapi.classif.Classif.extends_;
import static org.revapi.classif.Classif.match;
import static org.revapi.classif.Classif.method;
import static org.revapi.classif.Classif.type;
import static org.revapi.classif.match.NameMatch.any;
import static org.revapi.classif.match.NameMatch.exact;
import static org.revapi.classif.match.declaration.TypeKind.ANY;
import static org.revapi.classif.match.declaration.TypeKind.CLASS;
import static org.revapi.classif.support.Tester.assertDeferred;
import static org.revapi.classif.support.Tester.assertNotPassed;
import static org.revapi.classif.support.Tester.assertPassed;
import static org.revapi.classif.support.Tester.test;
import static org.revapi.classif.support.Tester.testProgressStart;
import static org.revapi.classif.support.Tester.testRest;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.support.Tester.Hierarchy;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class MatchingProgressTest {
    private static final Logger LOG = LogManager.getLogger(MatchingProgressTest.class);

    @JarSources(root = "/sources/progress/", sources = {"SingleNodeMatch.java", "Dependencies.java"})
    private CompiledJar.Environment env;

    @Test
    void testSingleNodeMatchesTrivially() {
        TypeElement type = env.elements().getTypeElement("SingleNodeMatch");
        assertPassed(testProgressStart(env, type,
                match().$(type(ANY, exact("SingleNodeMatch")).matched()).build()));
    }

    @Test
    void testDependenciesInfluenceMatchResult() {
        TypeElement A = env.elements().getTypeElement("Dependencies.A");
        TypeElement B = env.elements().getTypeElement("Dependencies.B");

        // type ^Dependencies.B extends %x {} class %x=* directly extends java.lang.Object {}
        StructuralMatcher recipe = match()
                .$(type(ANY, exact("Dependencies"), exact("B")).matched().$(extends_(type().ref("x"))))
                .$(type(CLASS, any()).as("x")
                        .$(extends_(type().fqn(exact("java"), exact("lang"), exact("Object"))).directly()))
                .build();

        assertPassed(test(env, B, recipe, A));
        assertDeferred(testProgressStart(env, B, recipe));
        assertNotPassed(test(env, B, recipe));
    }

    @Test
    void testDependentsInfluenceMatchResult() {
        TypeElement A = env.elements().getTypeElement("Dependencies.A");
        TypeElement B = env.elements().getTypeElement("Dependencies.B");

        // type Dependencies.B extends %x{} class ^%x=* directly extends java.lang.Object {}
        StructuralMatcher recipe = match()
                .$(type(ANY, exact("Dependencies"), exact("B")).$(extends_(type().ref("x"))))
                .$(type(CLASS, any()).matched().as("x")
                        .$(extends_(type().fqn(exact("java"), exact("lang"), exact("Object"))).directly()))
                .build();

        assertPassed(test(env, A, recipe, B));
        assertDeferred(testProgressStart(env, A, recipe));
        assertNotPassed(test(env, A, recipe));
    }

    @Test
    void testDependenciesAndDependentsMustAllMatch() {
        TypeElement A = env.elements().getTypeElement("Dependencies.A");
        TypeElement B = env.elements().getTypeElement("Dependencies.B");
        TypeElement C = env.elements().getTypeElement("Dependencies.C");

        // type ^%y=Dependencies.B extends %x {} class %x=* directly extends java.lang.Object {} class * extends %y {}
        StructuralMatcher recipe = match()
                .$(type(ANY, exact("Dependencies"), exact("B")).matched().as("y").$(extends_(type().ref("x"))))
                .$(type(CLASS, any()).as("x")
                        .$(extends_(type().fqn(exact("java"), exact("lang"), exact("Object"))).directly()))
                .$(type(CLASS, any()).$(extends_(type().ref("y"))))
                .build();

        //assertDeferred(testProgressStart(env, B, recipe));
        //assertNotPassed(test(env, B, recipe, A));
        assertPassed(test(env, B, recipe, A, C));
    }

    @Test
    void onlyReturningNodesReportedByMatchProgressFinish() {
        TypeElement A = env.elements().getTypeElement("Dependencies.A");
        TypeElement B = env.elements().getTypeElement("Dependencies.B");
        TypeElement C = env.elements().getTypeElement("Dependencies.C");

        // type ^%y=* extends %x {} class %x=* directly extends java.lang.Object {} class * extends %y {}
        StructuralMatcher recipe = match()
                .$(type(ANY, any()).matched().as("y").$(extends_(type().ref("x"))))
                .$(type(CLASS, any()).as("x")
                        .$(extends_(type().fqn(exact("java"), exact("lang"), exact("Object"))).directly()))
                .$(type(CLASS, any()).$(extends_(type().ref("y"))))
                .build();

        Map<Element, TestResult> rest = testRest(env, A, recipe, C, B);

        assertEquals(3, rest.size());
        assertNotPassed(rest.get(A));
        assertPassed(rest.get(B));
        assertNotPassed(rest.get(C));
    }

    @Test
    void testConstraintsWithoutVariablesResolveImmediately() {
        TypeElement A = env.elements().getTypeElement("Dependencies.A");
        TypeElement B = env.elements().getTypeElement("Dependencies.B");

        // type ^* directly extends java.lang.Object
        StructuralMatcher recipe = match()
                .$(type(ANY, any()).matched()
                        .$(extends_(type().fqn(exact("java"), exact("lang"), exact("Object"))).directly()))
                .build();

        assertPassed(test(env, A, recipe));
        assertNotPassed(test(env, B, recipe));
    }

    @Test
    void testChainedDependents() {
        Element A = env.elements().getTypeElement("Dependencies.A");
        Element B = env.elements().getTypeElement("Dependencies.B");
        Element D = env.elements().getTypeElement("Dependencies.D");
        Element method1 = ElementFilter.methodsIn(D.getEnclosedElements()).get(0);
        Element method2 = ElementFilter.methodsIn(D.getEnclosedElements()).get(1);

        Hierarchy code = Hierarchy.builder()
                .add(A)
                .add(B)
                .start(D)
                .add(method1)
                .add(method2)
                .end()
                .build();

        // class Dependencies.D { %a ^*(); } class Dependencies.B extends %a {} class %a=* {}
        StructuralMatcher recipe = match()
                .$(type(CLASS, exact("Dependencies"), exact("D"))
                        .$(method(any()).matched().returns(type().ref("a"))))
                .$(type(CLASS, exact("Dependencies"), exact("B")).$(extends_(type().ref("a"))))
                .$(type(CLASS, any()).as("a"))
                .build();

        Map<Element, TestResult> res = test(env, recipe, code);

        assertPassed(res.get(method1));
        assertNotPassed(res.get(method2));
    }
}
