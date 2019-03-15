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
package org.revapi.classif;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.revapi.classif.Tester.assertDeferred;
import static org.revapi.classif.Tester.assertNotPassed;
import static org.revapi.classif.Tester.assertPassed;
import static org.revapi.classif.Tester.test;
import static org.revapi.classif.Tester.testProgressStart;
import static org.revapi.classif.Tester.testRest;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class MatchingProgressTest {
    @JarSources(root = "/sources/progress/", sources = {"SingleNodeMatch.java", "Dependencies.java"})
    private CompiledJar.Environment env;

    @Test
    void testSingleNodeMatchesTrivially() {
        TypeElement type = env.elements().getTypeElement("SingleNodeMatch");
        assertPassed(testProgressStart(env, type, "type ^SingleNodeMatch {}"));
    }

    @Test
    void testDependenciesInfluenceMatchResult() {
        TypeElement A = env.elements().getTypeElement("Dependencies.A");
        TypeElement B = env.elements().getTypeElement("Dependencies.B");

        String recipe = "type ^B extends %x {} class %x=* directly extends java.lang.Object {}";
        assertPassed(test(env, B, recipe, A));
        assertDeferred(testProgressStart(env, B, recipe));
        assertNotPassed(test(env, B, recipe));
    }

    @Test
    void testDependentsInfluenceMatchResult() {
        TypeElement A = env.elements().getTypeElement("Dependencies.A");
        TypeElement B = env.elements().getTypeElement("Dependencies.B");

        String recipe = "type B extends %x{} class ^%x=* directly extends java.lang.Object {}";

        assertPassed(test(env, A, recipe, B));
        assertDeferred(testProgressStart(env, A, recipe));
        assertNotPassed(test(env, A, recipe));
    }

    @Test
    void testDependenciesAndDependentsMustAllMatch() {
        TypeElement A = env.elements().getTypeElement("Dependencies.A");
        TypeElement B = env.elements().getTypeElement("Dependencies.B");
        TypeElement C = env.elements().getTypeElement("Dependencies.C");

        String recipe = "type ^%y=B extends %x {} class %x=* directly extends java.lang.Object {} class * extends %y {}";

        assertDeferred(testProgressStart(env, B, recipe));
        assertNotPassed(test(env, B, recipe, A));
        assertPassed(test(env, B, recipe, A, C));
    }

    @Test
    void onlyReturningNodesReportedByMatchProgressFinish() {
        TypeElement A = env.elements().getTypeElement("Dependencies.A");
        TypeElement B = env.elements().getTypeElement("Dependencies.B");
        TypeElement C = env.elements().getTypeElement("Dependencies.C");

        String recipe = "type ^%y=B extends %x {} class %x=* directly extends java.lang.Object {} class * extends %y {}";

        Map<Element, TestResult> rest = testRest(env, A, recipe, C, B);

        assertEquals(2, rest.size());
        assertNotPassed(rest.get(A));
        assertNotPassed(rest.get(C));
    }
}
