package org.revapi.classif.match.declaration;

import static java.util.stream.Collectors.toSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


        MirroringModelInspector insp = new MirroringModelInspector(environment.elements()) {
            @Override
            public Set<Element> getUses(Element model) {
                return ElementFilter.methodsIn(model.getEnclosedElements()).stream()
                        .map(m -> ((DeclaredType) m.getReturnType()).asElement())
                        .collect(toSet());
            }
        };

        assertTrue(Classif.compile("^ uses TestClass.Used;").test(userMethod, insp));
        assertTrue(Classif.compile("^ directly uses TestClass.Used;").test(userMethod, insp));
        assertTrue(Classif.compile("^ uses TestClass.UsedInDistance2;").test(userMethod, insp));
        assertFalse(Classif.compile("^ directly uses TestClass.UsedInDistance2;").test(userMethod, insp));
    }

    @Test
    void testUseCycle() {
        TypeElement TestClass = environment.elements().getTypeElement("TestClass");

        @SuppressWarnings("ConstantConditions")
        ExecutableElement userMethod = ElementFilter.methodsIn(TestClass.getEnclosedElements())
                .stream().filter(m -> m.getSimpleName().contentEquals("cycleMethod")).findFirst().get();


        MirroringModelInspector insp = new MirroringModelInspector(environment.elements()) {
            @Override
            public Set<Element> getUses(Element model) {
                return ElementFilter.methodsIn(model.getEnclosedElements()).stream()
                        .map(m -> ((DeclaredType) m.getReturnType()).asElement())
                        .collect(toSet());
            }
        };

        assertTrue(Classif.compile("^ uses TestClass.UseCycleStart;").test(userMethod, insp));
        assertTrue(Classif.compile("^ uses TestClass.UseCycleEnd;").test(userMethod, insp));
        assertTrue(Classif.compile("^ directly uses TestClass.UseCycleStart;").test(userMethod, insp));
        assertFalse(Classif.compile("^ directly uses TestClass.UseCycleEnd;").test(userMethod, insp));
    }
}
