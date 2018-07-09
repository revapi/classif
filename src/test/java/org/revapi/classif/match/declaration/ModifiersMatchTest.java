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
package org.revapi.classif.match.declaration;

import static java.util.stream.Collectors.toList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;

import java.util.Collections;
import java.util.stream.Stream;

import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@ExtendWith(CompiledJarExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModifiersMatchTest {

    @JarSources(root = "/sources/", sources = "modifiers/TestClass.java")
    private CompiledJar.Environment classes;

    static Stream<Object[]> modifiers() {
        return Stream.of(
                new Object[] {parse("public"), PASSED},
                new Object[] {parse("public|private"), PASSED},
                new Object[] {parse("public !static"), PASSED},
                new Object[] {parse("!private !static"), PASSED},
                new Object[] {parse("public|private !static|volatile"), PASSED},
                new Object[] {parse("!public !static"), NOT_PASSED}
        );
    }

    @ParameterizedTest
    @MethodSource("modifiers")
    void test(ModifiersMatch statement, TestResult expectedMatch) {
        TypeElement testClass = classes.elements().getTypeElement("modifiers.TestClass");

        TestResult matches = statement.test(testClass, testClass.asType(),
                new MatchContext<>(new MirroringModelInspector(classes.elements(), classes.types()), Collections.emptyMap()));

        assertEquals(expectedMatch, matches);
    }

    private static ModifiersMatch parse(String decl) {
        return new ModifiersMatch(
                Stream.of(decl.split(" "))
                        .map(c -> new ModifierClusterMatch(
                                Stream.of(c.split("\\|"))
                                        .map(ModifiersMatchTest::parseSingleModifier)
                                        .collect(toList())))
                        .collect(toList()));

    }

    private static ModifierMatch parseSingleModifier(String decl) {
        if (decl.startsWith("!")) {
            return new ModifierMatch(true, decl.substring(1));
        } else {
            return new ModifierMatch(false, decl);
        }
    }
}
