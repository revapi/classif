package org.revapi.classif.statement;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.revapi.classif.Tester.Hierarchy.builder;
import static org.revapi.classif.Tester.assertNotPassed;
import static org.revapi.classif.Tester.assertPassed;
import static org.revapi.classif.Tester.test;

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
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.ModelInspector;
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
        Map<Element, TestResult> res =
                test(env, "type ^ { public TestClass.B field; }", builder().start(A).add(field).end().add(AA).add(B).build());

        assertPassed(res.get(A));
        assertNotPassed(res.get(B));

        // our simple model inspector doesn't take inherited members into account, so this should not pass
        assertNotPassed(res.get(AA));

        assertNotPassed(res.get(field));
    }


    @Test
    void testFieldTypeByReference() {
        Map<Element, TestResult> res =
                test(env, "type ^ { public %t field; } class %t=*.B;", builder().start(A).add(field).end().add(AA).add(B).build());

        assertPassed(res.get(A));
        assertNotPassed(res.get(B));

        // our simple model inspector doesn't take inherited members into account, so this should not pass
        assertNotPassed(res.get(AA));

        assertNotPassed(res.get(field));
    }

    @Test
    void testReturnField() {
        Map<Element, TestResult> res =
                test(env, "type * { public %t ^field; } class %t=*.B;", builder().start(A).add(field).end().add(AA).add(B).build());

        assertNotPassed(res.get(A));
        assertNotPassed(res.get(B));
        assertNotPassed(res.get(AA));
        assertPassed(res.get(field));
    }

    @Test
    void testNegationByName() {
        Map<Element, TestResult> res =
                test(env, "type * { public *.B ^!feeld; }", builder().start(A).add(field).end().add(AA).add(B).build());

        assertNotPassed(res.get(A));
        assertNotPassed(res.get(B));
        assertNotPassed(res.get(AA));
        assertPassed(res.get(field));
    }

    @Test
    void testNegationByType() {
        Map<Element, TestResult> res =
                test(env, "type * { public *.C ^!field; }", builder().start(A).add(field).end().add(AA).add(B).build());

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

        Map<Element, TestResult> res =
                test(insp, "type * { public * ^field uses *.AA; } class %t=*.B;", builder().start(A).add(field).end().add(AA).add(B).build());

        assertNotPassed(res.get(A));
        assertNotPassed(res.get(B));
        assertNotPassed(res.get(AA));
        assertPassed(res.get(field));
    }
}
