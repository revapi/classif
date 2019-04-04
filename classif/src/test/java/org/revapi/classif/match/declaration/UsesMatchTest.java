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
package org.revapi.classif.match.declaration;

import static java.util.stream.Collectors.toSet;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.revapi.classif.Classif.declaration;
import static org.revapi.classif.Classif.type;
import static org.revapi.classif.Classif.uses;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;
import static org.revapi.classif.Tester.assertNotPassed;
import static org.revapi.classif.Tester.assertPassed;
import static org.revapi.classif.match.NameMatch.exact;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.Classif;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.StructuralMatcher;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@ExtendWith(CompiledJarExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UsesMatchTest {

    @JarSources(root = "/sources/uses/", sources = "TestClass.java")
    private CompiledJar.Environment environment;

    @Test
    void testSimpleUseChain() {
        TypeElement TestClass = environment.elements().getTypeElement("TestClass");

        @SuppressWarnings("ConstantConditions")
        ExecutableElement userMethod = ElementFilter.methodsIn(TestClass.getEnclosedElements())
                .stream().filter(m -> m.getSimpleName().contentEquals("userMethod")).findFirst().get();


        MirroringModelInspector insp = new MirroringModelInspector(environment.elements(), environment.types()) {
            @Override
            public Set<Element> getUses(Element model) {
                return ElementFilter.methodsIn(model.getEnclosedElements()).stream()
                        .map(m -> ((DeclaredType) m.getReturnType()).asElement())
                        .collect(toSet());
            }
        };

        StructuralMatcher usesUsed = Classif.match()
                .$(declaration().matched()
                    .$(uses(type().fqn(exact("TestClass"), exact("Used")))))
                .build();

        StructuralMatcher directlyUsesUsed = Classif.match()
                .$(declaration().matched().$(uses(type().fqn(exact("TestClass"), exact("Used"))).directly()))
                .build();

        StructuralMatcher usesUsedInDistance = Classif.match()
                .$(declaration().matched().$(uses(type().fqn(exact("TestClass"), exact("UsedInDistance2")))))
                .build();

        StructuralMatcher directlyUsesUsedInDistance = Classif.match()
                .$(declaration().matched().$(uses(type().fqn(exact("TestClass"), exact("UsedInDistance2")))
                        .directly()))
                .build();

        assertPassed(usesUsed.with(insp).start(userMethod).getTestResult());
        assertPassed(directlyUsesUsed.with(insp).start(userMethod).getTestResult());
        assertPassed(usesUsedInDistance.with(insp).start(userMethod).getTestResult());
        assertNotPassed(directlyUsesUsedInDistance.with(insp).start(userMethod).getTestResult());
    }

    @Test
    void testUseCycle() {
        TypeElement TestClass = environment.elements().getTypeElement("TestClass");

        @SuppressWarnings("ConstantConditions")
        ExecutableElement userMethod = ElementFilter.methodsIn(TestClass.getEnclosedElements())
                .stream().filter(m -> m.getSimpleName().contentEquals("cycleMethod")).findFirst().get();


        MirroringModelInspector insp = new MirroringModelInspector(environment.elements(), environment.types()) {
            @Override
            public Set<Element> getUses(Element model) {
                return ElementFilter.methodsIn(model.getEnclosedElements()).stream()
                        .map(m -> ((DeclaredType) m.getReturnType()).asElement())
                        .collect(toSet());
            }
        };

        StructuralMatcher usesCycleStart = Classif.match()
                .$(declaration().matched().$(uses(type().fqn(exact("TestClass"), exact("UseCycleStart")))))
                .build();

        StructuralMatcher usesCycleEnd = Classif.match()
                .$(declaration().matched().$(uses(type().fqn(exact("TestClass"), exact("UseCycleEnd")))))
                .build();

        StructuralMatcher directlyUsesCycleStart = Classif.match()
                .$(declaration().matched()
                        .$(uses(type().fqn(exact("TestClass"), exact("UseCycleStart"))).directly()))
                .build();

        StructuralMatcher directlyUsesCycleEnd = Classif.match()
                .$(declaration().matched()
                        .$(uses(type().fqn(exact("TestClass"), exact("UseCycleEnd"))).directly()))
                .build();

        assertSame(usesCycleStart.with(insp).start(userMethod).getTestResult(), PASSED);
        assertSame(usesCycleEnd.with(insp).start(userMethod).getTestResult(), PASSED);
        assertSame(directlyUsesCycleStart.with(insp).start(userMethod).getTestResult(), PASSED);
        assertSame(directlyUsesCycleEnd.with(insp).start(userMethod).getTestResult(), NOT_PASSED);
    }
}
