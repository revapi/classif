package org.revapi.classif;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.statement.AbstractMatcher;
import org.revapi.classif.statement.TypeKindStatement;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@ExtendWith(CompiledJarExtension.class)
class TypeKindStatementTest {

    @JarSources(root = "/sources/typeKind/", sources = "TestClass.java")
    private CompiledJar.Environment environment;

    @Test
    void test() {
        TypeElement TestClass = environment.elements().getTypeElement("TestClass");
        TypeElement Iface = environment.elements().getTypeElement("TestClass.Iface");
        TypeElement Enum = environment.elements().getTypeElement("TestClass.Enum");
        TypeElement Anno = environment.elements().getTypeElement("TestClass.Anno");
        ExecutableElement method = ElementFilter.methodsIn(TestClass.getEnclosedElements()).get(0);

        AbstractMatcher classes = new TypeKindStatement(false, "class").createMatcher();
        AbstractMatcher interfaces = new TypeKindStatement(false, "interface").createMatcher();
        AbstractMatcher enums = new TypeKindStatement(false, "enum").createMatcher();
        AbstractMatcher annos = new TypeKindStatement(false, "@interface").createMatcher();
        AbstractMatcher types = new TypeKindStatement(false, "type").createMatcher();

        ModelInspector<Element> inspector = new MirroringModelInspector();
        Map<String, AbstractMatcher> vars = Collections.emptyMap();

        assertTrue(classes.test(TestClass, inspector, vars));
        assertFalse(interfaces.test(TestClass, inspector, vars));
        assertFalse(enums.test(TestClass, inspector, vars));
        assertFalse(annos.test(TestClass, inspector, vars));
        assertTrue(types.test(TestClass, inspector, vars));

        assertFalse(classes.test(Iface, inspector, vars));
        assertTrue(interfaces.test(Iface, inspector, vars));
        assertFalse(enums.test(Iface, inspector, vars));
        assertFalse(annos.test(Iface, inspector, vars));
        assertTrue(types.test(Iface, inspector, vars));

        assertFalse(classes.test(Enum, inspector, vars));
        assertFalse(interfaces.test(Enum, inspector, vars));
        assertTrue(enums.test(Enum, inspector, vars));
        assertFalse(annos.test(Enum, inspector, vars));
        assertTrue(types.test(Enum, inspector, vars));

        assertFalse(classes.test(Anno, inspector, vars));
        assertFalse(interfaces.test(Anno, inspector, vars));
        assertFalse(enums.test(Anno, inspector, vars));
        assertTrue(annos.test(Anno, inspector, vars));
        assertTrue(types.test(Anno, inspector, vars));

        assertFalse(classes.test(method, inspector, vars));
        assertFalse(interfaces.test(method, inspector, vars));
        assertFalse(enums.test(method, inspector, vars));
        assertFalse(annos.test(method, inspector, vars));
        assertFalse(types.test(method, inspector, vars));
    }
}
