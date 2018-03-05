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

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.revapi.classif.statement.NameStatement.all;
import static org.revapi.classif.statement.NameStatement.any;
import static org.revapi.classif.statement.NameStatement.exact;
import static org.revapi.classif.statement.NameStatement.pattern;

import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.revapi.classif.statement.FqnStatement;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@ExtendWith(CompiledJarExtension.class)
class FqnStatementTest {

    @JarSources(root = "/sources/", sources = "fqn/pkg/TestClass.java")
    private CompiledJar.Environment fqnClasses;

    static Stream<Object[]> statements() {
        return Stream.of(
                new Object[] {new FqnStatement(asList(exact("fqn"), exact("pkg"), exact("TestClass"))), true},
                new Object[] {new FqnStatement(asList(exact("fqn"), any(), exact("TestClass"))), true},
                new Object[] {new FqnStatement(asList(exact("fqn"), all(), exact("NotThere"))), false},
                new Object[] {new FqnStatement(asList(all(), exact("TestClass"))), true},
                new Object[] {new FqnStatement(asList(exact("fqn"), all())), true},
                new Object[] {new FqnStatement(asList(exact("fqn"), exact("TestClass"))), false},
                new Object[] {new FqnStatement(asList(pattern(Pattern.compile("[fF]qn")), any(), exact("TestClass"))), true},
                new Object[] {new FqnStatement(asList(all())), true},
                // special case - a lone single star means "everything"
                new Object[] {new FqnStatement(asList(any())), true}
        );
    }

    @ParameterizedTest(name = "fqn[{index}]")
    @MethodSource("statements")
    void test(FqnStatement stmt, boolean expectedMatch) {
        TypeElement testClass = fqnClasses.elements().getTypeElement("fqn.pkg.TestClass");

        boolean matches = stmt.createMatcher().test(testClass, new MirroringModelInspector(), Collections.emptyMap());

        assertEquals(expectedMatch, matches);
    }
}
