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
package org.revapi.classif.match.instance;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.PASSED;
import static org.revapi.classif.match.NameMatch.all;
import static org.revapi.classif.match.NameMatch.any;
import static org.revapi.classif.match.NameMatch.exact;
import static org.revapi.classif.match.NameMatch.pattern;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@ExtendWith(CompiledJarExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FqnMatchTest {

    @JarSources(root = "/sources/", sources = "fqn/pkg/TestClass.java")
    private CompiledJar.Environment fqnClasses;

    static Stream<Object[]> statements() {
        return Stream.of(
                new Object[] {new FqnMatch(asList(exact("fqn"), exact("pkg"), exact("TestClass"))), PASSED},
                new Object[] {new FqnMatch(asList(exact("fqn"), any(), exact("TestClass"))), PASSED},
                new Object[] {new FqnMatch(asList(exact("fqn"), all(), exact("NotThere"))), NOT_PASSED},
                new Object[] {new FqnMatch(asList(all(), exact("TestClass"))), PASSED},
                new Object[] {new FqnMatch(asList(exact("fqn"), all())), PASSED},
                new Object[] {new FqnMatch(asList(exact("fqn"), exact("TestClass"))), NOT_PASSED},
                new Object[] {new FqnMatch(asList(pattern(Pattern.compile("[fF]qn")), any(), exact("TestClass"))), PASSED},
                new Object[] {new FqnMatch(asList(all())), PASSED},
                // special case - a lone single star means "everything"
                new Object[] {new FqnMatch(asList(any())), PASSED}
        );
    }

    @ParameterizedTest(name = "fqn[{index}]")
    @MethodSource("statements")
    void test(FqnMatch matcher, TestResult expectedMatch) {
        TypeElement testClass = fqnClasses.elements().getTypeElement("fqn.pkg.TestClass");

        TestResult matches = matcher.test(testClass, testClass.asType(),
                new MatchContext<>(new MirroringModelInspector(fqnClasses.elements(), fqnClasses.types()),
                        emptySet()));

        assertEquals(expectedMatch, matches);
    }
}
