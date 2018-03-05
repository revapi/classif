/*
 * Copyright 2014-2018 Lukas Krejci
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

import static java.util.stream.Collectors.toList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.stream.Stream;

import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.revapi.classif.statement.ModifierClusterStatement;
import org.revapi.classif.statement.ModifierStatement;
import org.revapi.classif.statement.ModifiersStatement;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@ExtendWith(CompiledJarExtension.class)
class ModifiersStatementTest {

    @JarSources(root = "/sources/", sources = "modifiers/TestClass.java")
    private CompiledJar.Environment classes;

    static Stream<Object[]> modifiers() {
        return Stream.of(
                new Object[] {parse("public"), true},
                new Object[] {parse("public|private"), true},
                new Object[] {parse("public !static"), true},
                new Object[] {parse("!private !static"), true},
                new Object[] {parse("public|private !static|volatile"), true},
                new Object[] {parse("!public !static"), false}
        );
    }

    @ParameterizedTest
    @MethodSource("modifiers")
    void test(ModifiersStatement statement, boolean expectedMatch) {
        TypeElement testClass = classes.elements().getTypeElement("modifiers.TestClass");

        boolean matches = statement.createMatcher().test(testClass, new MirroringModelInspector(), Collections.emptyMap());

        assertEquals(expectedMatch, matches);
    }

    private static ModifiersStatement parse(String decl) {
        return new ModifiersStatement(
                Stream.of(decl.split(" "))
                        .map(c -> new ModifierClusterStatement(
                                Stream.of(c.split("\\|"))
                                        .map(ModifiersStatementTest::parseSingleModifier)
                                        .collect(toList())))
                        .collect(toList()));

    }

    private static ModifierStatement parseSingleModifier(String decl) {
        if (decl.startsWith("!")) {
            return new ModifierStatement(true, decl.substring(1));
        } else {
            return new ModifierStatement(false, decl);
        }
    }
}
