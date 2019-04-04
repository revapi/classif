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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.revapi.classif.TestResult.DEFERRED;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;

import org.revapi.classif.util.TreeNode;
import org.revapi.testjars.CompiledJar;

public class Tester {
    public static TestResult test(CompiledJar.Environment env, Element el, StructuralMatcher recipe, Element... nextElements) {
        return test(inspector(env), el, recipe, nextElements);
    }

    public static TestResult test(ModelInspector<Element> insp, Element el, StructuralMatcher recipe, Element... nextElements) {
        MatchingProgress<Element> progress = startProgress(insp, recipe);

        progress.start(el);
        TestResult res = progress.finish(el);

        for (Element e : nextElements) {
            progress.start(e);
            progress.finish(e);
        }

        return progress.finish().getOrDefault(el, res);
    }

    public static Map<Element, TestResult> test(CompiledJar.Environment env, StructuralMatcher recipe, Hierarchy elementHierarchy) {
        return test(inspector(env), recipe, elementHierarchy);
    }

    public static Map<Element, TestResult> test(ModelInspector<Element> insp, StructuralMatcher recipe, Hierarchy elementHierarchy) {
        MatchingProgress<Element> progress = startProgress(insp, recipe);
        Map<Element, TestResult> ret = new HashMap<>();

        for (Hierarchy el : elementHierarchy.getChildren()) {
            test(progress, el, ret);
        }

        ret.putAll(progress.finish());

        return ret;
    }

    private static void test(MatchingProgress<Element> progress, Hierarchy element, Map<Element, TestResult> results) {
        if (progress.start(element.element).isDescend()) {

            for (Hierarchy child : element.getChildren()) {
                test(progress, child, results);
            }
        }
        results.put(element.element, progress.finish(element.element));
    }

    public static Map<Element, TestResult> testRest(CompiledJar.Environment env, Element el, StructuralMatcher recipe, Element... nextElements) {
        return testRest(inspector(env), el, recipe, nextElements);
    }

    public static Map<Element, TestResult> testRest(ModelInspector<Element> insp, Element el, StructuralMatcher recipe, Element... nextElements) {
        MatchingProgress<Element> progress = startProgress(insp, recipe);

        progress.start(el);
        progress.finish(el);

        for (Element e : nextElements) {
            progress.start(e);
            progress.finish(e);
        }

        return progress.finish();
    }

    public static TestResult testProgressStart(CompiledJar.Environment env, Element el, StructuralMatcher recipe) {
        return testProgressStart(inspector(env), el, recipe);
    }

    public static TestResult testProgressStart(ModelInspector<Element> insp, Element el, StructuralMatcher recipe) {
        MatchingProgress<Element> progress = startProgress(insp, recipe);

        return progress.start(el).getTestResult();
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

    private static MatchingProgress<Element> startProgress(ModelInspector<Element> insp, StructuralMatcher matcher) {
        return matcher.with(insp);
    }

    private static ModelInspector<Element> inspector(CompiledJar.Environment env) {
        return new MirroringModelInspector(env.elements(), env.types());
    }

    public static final class Hierarchy extends TreeNode<Hierarchy> {
        private final Element element;

        private Hierarchy(Element element) {
            this.element = element;
        }

        public static RootBuilder builder() {
            return new RootBuilder();
        }

        public static final class RootBuilder {
            private final Set<Builder> children = new HashSet<>();

            private RootBuilder() {

            }

            public Builder<RootBuilder> start(Element element) {
                Builder<RootBuilder> ret = new Builder<>(this, element);
                children.add(ret);
                return ret;
            }

            public RootBuilder add(Element element) {
                return start(element).end();
            }

            public Hierarchy build() {
                Hierarchy ret = new Hierarchy(null);
                for (Builder child : children) {
                    ret.getChildren().add(child.build());
                }

                return ret;
            }
        }

        public static final class Builder<Parent> {
            private final Set<Builder<Builder<Parent>>> children = new HashSet<>();
            private final Element element;
            private final Parent parent;

            private Builder(Parent parent, Element element) {
                this.parent = parent;
                this.element = element;
            }

            public Builder<Builder<Parent>> start(Element element) {
                Builder<Builder<Parent>> ret = new Builder<>(this, element);
                children.add(ret);
                return ret;
            }

            public Builder<Parent> add(Element element) {
                return start(element).end();
            }

            public Parent end() {
                return parent;
            }

            private Hierarchy build() {
                Hierarchy ret = new Hierarchy(element);
                for (Builder child : children) {
                    ret.getChildren().add(child.build());
                }

                return ret;
            }
        }
    }
}
