package org.revapi.classif.statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.Classif;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.ModelInspector;
import org.revapi.classif.StructuralMatcher;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class TypeDefinitionStatementTest {
    @JarSources(root = "/sources/typeParameters/", sources = "TestClass.java")
    private CompiledJar.Environment environment;

    private TypeElement TestClass;

    @BeforeAll
    void setup() {
        TestClass = environment.elements().getTypeElement("TestClass");
    }

    @Test
    void test() {
        assertTrue(doTest("class ^TestClass<**>;"));
        assertTrue(doTest("class ^TestClass<*, **>;"));
        assertTrue(doTest("class ^TestClass<java.*.Object, **>;"));
        assertTrue(doTest("class ^TestClass<**, java.lang.Cloneable>;"));
        assertTrue(doTest("class ^TestClass<**, java.lang.String>;"));
        assertTrue(doTest("class ^TestClass<*, ? extends java.*.Object, **>;"));
        assertTrue(doTest("class ^TestClass<*, *, ? extends /.*String/, **>;"));
        assertTrue(doTest("class ^TestClass<**, ? extends /.*\\.String/ & java.lang.Cloneable>;"));
        assertTrue(doTest("public class ^TestClass;"));
        assertFalse(doTest("private class ^TestClass;"));
        assertTrue(doTest("public type ^TestClass;"));
        assertFalse(doTest("public type ^TestClass<*>;"));

        // TODO add annotations
        // TODO add type constraints
    }

    private boolean doTest(String recipe) {
        ModelInspector<Element> insp = new MirroringModelInspector(environment.elements());

        StructuralMatcher matcher = Classif.compile(recipe);

        return matcher.test(TestClass, insp);
    }
}
