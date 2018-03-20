package org.revapi.classif.statement;

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
class GenericStatementTest {
    @JarSources(root = "/sources/typeParameters/", sources = "TestClass.java")
    private CompiledJar.Environment environment;

    private TypeElement TestClass;

    @BeforeAll
    void setup() {
        TestClass = environment.elements().getTypeElement("TestClass");
    }

    @Test
    void test() {
        assertTrue(doTest("^;"));
        assertTrue(doTest("public ^;"));
        assertTrue(doTest("public ^*;"));
        assertTrue(doTest("private ^!*;"));
    }

    private boolean doTest(String recipe) {
        ModelInspector<Element> insp = new MirroringModelInspector(environment.elements());

        StructuralMatcher matcher = Classif.compile(recipe);

        return matcher.test(TestClass, insp);
    }
}
