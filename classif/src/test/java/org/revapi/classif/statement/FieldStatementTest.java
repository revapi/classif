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
package org.revapi.classif.statement;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.revapi.classif.Classif.field;
import static org.revapi.classif.Classif.modifiers;
import static org.revapi.classif.Classif.type;
import static org.revapi.classif.Classif.uses;
import static org.revapi.classif.Tester.Hierarchy.builder;
import static org.revapi.classif.Tester.assertNotPassed;
import static org.revapi.classif.Tester.assertPassed;
import static org.revapi.classif.Tester.test;
import static org.revapi.classif.match.NameMatch.any;
import static org.revapi.classif.match.NameMatch.exact;
import static org.revapi.classif.match.declaration.Modifier.PUBLIC;
import static org.revapi.classif.match.declaration.TypeKind.ANY;
import static org.revapi.classif.match.declaration.TypeKind.CLASS;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.Classif;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.ModelInspector;
import org.revapi.classif.StructuralMatcher;
import org.revapi.classif.TestResult;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class FieldStatementTest {
    @JarSources(root = "/sources/fields/", sources = "TestClass.java")
    private CompiledJar.Environment env;


    private TypeElement A;
    private TypeElement B;
    private TypeElement AA;
    private VariableElement field;

    @BeforeEach
    void findElements() {
        A = env.elements().getTypeElement("TestClass.A");
        AA = env.elements().getTypeElement("TestClass.AA");
        B = env.elements().getTypeElement("TestClass.B");
        field = ElementFilter.fieldsIn(A.getEnclosedElements()).get(0);
    }

    @Test
    void testFieldPresence() {
        // type ^ { public TestClass.B field; }
        StructuralMatcher recipe = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(field(exact("field")).$(modifiers(PUBLIC)).$(type().fqn(exact("TestClass"), exact("B"))))).build();

        Map<Element, TestResult> res =
                test(env, recipe, builder().start(A).add(field).end().add(AA).add(B).build());

        assertPassed(res.get(A));
        assertNotPassed(res.get(B));

        // our simple model inspector doesn't take inherited members into account, so this should not pass
        assertNotPassed(res.get(AA));

        assertNotPassed(res.get(field));
    }


    @Test
    void testFieldTypeByReference() {
        // type ^ { public %t field; } class %t=*.B {}
        StructuralMatcher recipe = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(field(exact("field")).$(modifiers(PUBLIC)).$(type().var("t"))))
                .$(type(CLASS, any(), exact("B")).called("t"))
                .build();

        Map<Element, TestResult> res =
                test(env, recipe, builder().start(A).add(field).end().add(AA).add(B).build());

        assertPassed(res.get(A));
        assertNotPassed(res.get(B));

        // our simple model inspector doesn't take inherited members into account, so this should not pass
        assertNotPassed(res.get(AA));

        assertNotPassed(res.get(field));
    }

    @Test
    void testReturnField() {
        // type * { public %t ^field; } class %t=*.B {}
        StructuralMatcher recipe = Classif.match()
                .$(type(ANY, any())
                        .$(field(exact("field")).$(modifiers(PUBLIC)).matched().$(type().var("t"))))
                .$(type(CLASS, any(), exact("B")).called("t"))
                .build();

        Map<Element, TestResult> res =
                test(env, recipe, builder().start(A).add(field).end().add(AA).add(B).build());

        assertNotPassed(res.get(A));
        assertNotPassed(res.get(B));
        assertNotPassed(res.get(AA));
        assertPassed(res.get(field));
    }

    @Test
    void testNegationByName() {
        // type * { public *.B ^!feeld; }
        StructuralMatcher recipe = Classif.match()
                .$(type(ANY, any())
                        .$(field(exact("feeld")).negated().$(modifiers(PUBLIC)).matched()
                                .$(type().fqn(any(), exact("B")))))
                .build();

        Map<Element, TestResult> res =
                test(env, recipe, builder().start(A).add(field).end().add(AA).add(B).build());

        assertNotPassed(res.get(A));
        assertNotPassed(res.get(B));
        assertNotPassed(res.get(AA));
        assertPassed(res.get(field));
    }

    @Test
    void testNegationByType() {
        // type * { public *.C ^!field; }
        StructuralMatcher recipe = Classif.match()
                .$(type(ANY, any())
                        .$(field(exact("field")).$(modifiers(PUBLIC)).matched().$(type().fqn(any(), exact("C")))
                                .negated()))
                .build();

        Map<Element, TestResult> res =
                test(env, recipe, builder().start(A).add(field).end().add(AA).add(B).build());

        assertNotPassed(res.get(A));
        assertNotPassed(res.get(B));
        assertNotPassed(res.get(AA));
        assertPassed(res.get(field));
    }

    @Test
    void testConstraints() {

        ModelInspector<Element> insp = new MirroringModelInspector(env.elements(), env.types()) {
            @Override
            public Set<Element> getUses(Element model) {
                if (model == B) {
                    return Collections.singleton(AA);
                } else {
                    return super.getUses(model);
                }
            }
        };

        // type * { public * ^field uses *.AA; } class %t=*.B {}
        StructuralMatcher recipe = Classif.match()
                .$(type(ANY, any())
                        .$(field(exact("field")).matched().$(type().fqn(any()))
                                .$(uses(type().fqn(any(), exact("AA"))))))
                .$(type(CLASS, any(), exact("B")).called("t"))
                .build();

        Map<Element, TestResult> res =
                test(insp, recipe, builder().start(A).add(field).end().add(AA).add(B).build());

        assertNotPassed(res.get(A));
        assertNotPassed(res.get(B));
        assertNotPassed(res.get(AA));
        assertPassed(res.get(field));
    }

    @Test
    void testDeclaration() {
        // type * { public *.A::^field; }
        StructuralMatcher recipe = Classif.match()
                .$(type(ANY, any())
                        .$(field(exact("field")).matched().declaredIn(type().fqn(any(), exact("A")))))
                .build();

        Map<Element, TestResult> res = test(env, recipe, builder().start(AA).add(field).end().build());
        assertNotPassed(res.get(AA));
        assertPassed(res.get(field));
    }
}
