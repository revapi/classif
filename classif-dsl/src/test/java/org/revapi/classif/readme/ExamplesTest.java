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
package org.revapi.classif.readme;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.revapi.classif.Tester.assertNotPassed;
import static org.revapi.classif.Tester.assertPassed;
import static org.revapi.classif.Tester.test;
import static org.revapi.classif.dsl.ClassifDSL.compile;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.util.ElementFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.TestResult;
import org.revapi.classif.Tester.Hierarchy;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class ExamplesTest {
    private static final Logger LOG = LogManager.getLogger(ExamplesTest.class);

    @JarSources(root = "/sources/examples/", sources = {
            "Anno.java",
            "Stable.java",
            "Unstable.java",
            "Iface.java",
            "Example1.java",
            "Example2.java",
            "Example3.java",
            "Example4.java",
            "Example5.java",
            "Example6_1.java",
            "Example6_2.java",
            "Example6_3.java",
            "Example7.java",
            "Example8.java",
            "Example9.java",
            "Example10.java",
    })
    private CompiledJar.Environment env;

    @BeforeEach
    void reportStart(TestInfo testInfo) {
        LOG.trace("============================== " + testInfo.getDisplayName());
    }

    @Test
    void example1() {
        Element example1 = env.elements().getTypeElement("Example1");

        Map<Element, TestResult> res = test(env, compile("@Anno(a = 'x') ^;"), Hierarchy.builder()
                .add(example1)
                .build());

        assertPassed(res.get(example1));
    }

    @Test
    void example2() {
        Element example2 = env.elements().getTypeElement("Example2");

        Map<Element, TestResult> res = test(env, compile("@Anno(a != 'x') ^;"), Hierarchy.builder()
                .add(example2)
                .build());

        assertPassed(res.get(example2));
    }

    @Test
    void example3() {
        Element annotated = env.elements().getTypeElement("Example3.Annotated");
        Element notAnnotated = env.elements().getTypeElement("Example3.NotAnnotated");

        Map<Element, TestResult> res = test(env, compile("!@Anno ^;"), Hierarchy.builder()
                .add(annotated)
                .add(notAnnotated)
                .build());

        assertPassed(res.get(notAnnotated));
        assertNotPassed(res.get(annotated));
    }

    @Test
    void example4() {
        Element example4 = env.elements().getTypeElement("pkg.Example4");
        Element example5 = env.elements().getTypeElement("pkg2.Example5");

        Map<Element, TestResult> res = test(env, compile("class ^pkg.* {}"), Hierarchy.builder()
                .add(example4)
                .add(example5)
                .build());

        assertPassed(res.get(example4));
        assertNotPassed(res.get(example5));
    }

    @Test
    void example5() {
        Element example4 = env.elements().getTypeElement("pkg.Example4");
        Element example5 = env.elements().getTypeElement("pkg2.Example5");

        Map<Element, TestResult> res = test(env, compile("class ^!/p[kK]g/.* {}"), Hierarchy.builder()
                .add(example4)
                .add(example5)
                .build());

        assertNotPassed(res.get(example4));
        assertPassed(res.get(example5));
    }

    @Test
    void example6() {
        Element example6_1 = env.elements().getTypeElement("pkg.Example6_1");
        Element example6_2 = env.elements().getTypeElement("pkg.pkg.Example6_2");
        Element example6_3 = env.elements().getTypeElement("pkg.Example6_3");

        Map<Element, TestResult> res = test(env, compile("interface ^pkg.** {}"), Hierarchy.builder()
                .add(example6_1)
                .add(example6_2)
                .add(example6_3)
                .build());

        assertPassed(res.get(example6_1));
        assertPassed(res.get(example6_2));
        assertNotPassed(res.get(example6_3));
    }

    @Test
    void example7() {
        Element notMatched = env.elements().getTypeElement("any.pkg.Example7.PrivateNotMatched");
        Element matched = env.elements().getTypeElement("any.pkg.Example7.MatchedPrivate");

        Map<Element, TestResult> res = test(env, compile("type ^**./.*Private/ {}"), Hierarchy.builder()
                .add(notMatched)
                .add(matched)
                .build());

        assertNotPassed(res.get(notMatched));
        assertPassed(res.get(matched));
    }

    @Test
    void example8() {
        Element example8 = env.elements().getTypeElement("Example8");
        Element unfinished = env.elements().getTypeElement("Example8.Unfinished");
        Element method = ElementFilter.methodsIn(example8.getEnclosedElements()).get(0);

        Map<Element, TestResult> res = test(env, compile("^ uses %c; @Unstable class %c=* {}"),
                Hierarchy.builder()
                        .start(example8)
                        .add(unfinished)
                        .add(method)
                        .end()
                        .build());

        assertNotPassed(res.get(example8));
        assertNotPassed(res.get(unfinished));
        assertPassed(res.get(method));
    }

    @Test
    void example9() {
        Element unfinished = env.elements().getTypeElement("Example9.Unfinished");
        Element finished = env.elements().getTypeElement("Example9.Finished");

        Map<Element, TestResult> res = test(env, compile("match %e; @Unstable type %e=* {} @Stable * uses %e;"),
                Hierarchy.builder()
                        .add(unfinished)
                        .add(finished)
                        .build());

        assertNotPassed(res.get(finished));
        assertPassed(res.get(unfinished));
    }

    @Test
    void example10() {
        Element iface = env.elements().getTypeElement("Iface");
        Element base = env.elements().getTypeElement("Example10.Base");
        Element inheritor = env.elements().getTypeElement("Example10.Inheritor");
        Element user = env.elements().getTypeElement("Example10.User");
        Element method1 = ElementFilter.methodsIn(user.getEnclosedElements()).get(0);
        Element method2 = ElementFilter.methodsIn(user.getEnclosedElements()).get(1);

        Map<Element, TestResult> res = test(env,
                compile("^ directly uses %impl; type %impl=* directly implements Iface {}"),
                Hierarchy.builder()
                        .add(iface)
                        .add(base)
                        .add(inheritor)
                        .start(user)
                        .add(method1)
                        .add(method2)
                        .end()
                        .build());

        assertNotPassed(res.get(iface));
        assertNotPassed(res.get(base));
        assertPassed(res.get(inheritor));
        assertNotPassed(res.get(user));
        assertPassed(res.get(method1));
        assertNotPassed(res.get(method2));
    }
}
