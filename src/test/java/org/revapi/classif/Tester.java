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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.revapi.classif.TestResult.DEFERRED;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;

import java.util.Map;

import javax.lang.model.element.Element;

import org.revapi.testjars.CompiledJar;

public class Tester {
    public static TestResult test(CompiledJar.Environment env, Element el, String recipe, Element... nextElements) {
        MatchingProgress<Element> progress = startProgress(env, recipe);

        TestResult res = progress.start(el);
        if (res == PASSED) {
            return PASSED;
        }

        res = progress.finish(el);
        if (res == PASSED) {
            return PASSED;
        }

        for (Element e : nextElements) {
            progress.start(e);
            progress.finish(e);
        }

        return progress.finish().getOrDefault(el, res);
    }

    public static Map<Element, TestResult> testRest(CompiledJar.Environment env, Element el, String recipe, Element... nextElements) {
        MatchingProgress<Element> progress = startProgress(env, recipe);

        progress.start(el);
        progress.finish(el);

        for (Element e : nextElements) {
            progress.start(e);
            progress.finish(e);
        }

        return progress.finish();
    }

    public static TestResult testProgressStart(CompiledJar.Environment env, Element el, String recipe) {
        MatchingProgress<Element> progress = startProgress(env, recipe);

        return progress.start(el);
    }

    public static void assertPassed(TestResult res) {
        assertSame(PASSED, res);
    }

    public static void assertNotPassed(TestResult res) {
        assertSame(NOT_PASSED, res);
    }

    public static void assertDeferred(TestResult res) {
        assertSame(DEFERRED, res);
    }

    private static MatchingProgress<Element> startProgress(CompiledJar.Environment env, String recipe) {
        ModelInspector<Element> insp = new MirroringModelInspector(env.elements(), env.types());

        StructuralMatcher matcher = Classif.compile(recipe);

        return matcher.with(insp);
    }
}
