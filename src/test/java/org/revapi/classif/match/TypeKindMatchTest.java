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
package org.revapi.classif.match;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.ModelInspector;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@ExtendWith(CompiledJarExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TypeKindMatchTest {

    @JarSources(root = "/sources/typeKind/", sources = "TestClass.java")
    private CompiledJar.Environment environment;

    @Test
    void test() {
        TypeElement TestClass = environment.elements().getTypeElement("TestClass");
        TypeElement Iface = environment.elements().getTypeElement("TestClass.Iface");
        TypeElement Enum = environment.elements().getTypeElement("TestClass.Enum");
        TypeElement Anno = environment.elements().getTypeElement("TestClass.Anno");
        ExecutableElement method = ElementFilter.methodsIn(TestClass.getEnclosedElements()).get(0);

        DeclarationMatch classes = new TypeKindMatch(false, "class");
        DeclarationMatch interfaces = new TypeKindMatch(false, "interface");
        DeclarationMatch enums = new TypeKindMatch(false, "enum");
        DeclarationMatch annos = new TypeKindMatch(false, "@interface");
        DeclarationMatch types = new TypeKindMatch(false, "type");

        ModelInspector<Element> inspector = new MirroringModelInspector(environment.elements());
        Map<String, ModelMatch> vars = Collections.emptyMap();

        MatchContext<Element> ctx = new MatchContext<>(inspector, vars);
        
        assertTrue(classes.test(TestClass, null, ctx));
        assertFalse(interfaces.test(TestClass, null, ctx));
        assertFalse(enums.test(TestClass, null, ctx));
        assertFalse(annos.test(TestClass, null, ctx));
        assertTrue(types.test(TestClass, null, ctx));

        assertFalse(classes.test(Iface, null, ctx));
        assertTrue(interfaces.test(Iface, null, ctx));
        assertFalse(enums.test(Iface, null, ctx));
        assertFalse(annos.test(Iface, null, ctx));
        assertTrue(types.test(Iface, null, ctx));

        assertFalse(classes.test(Enum, null, ctx));
        assertFalse(interfaces.test(Enum, null, ctx));
        assertTrue(enums.test(Enum, null, ctx));
        assertFalse(annos.test(Enum, null, ctx));
        assertTrue(types.test(Enum, null, ctx));

        assertFalse(classes.test(Anno, null, ctx));
        assertFalse(interfaces.test(Anno, null, ctx));
        assertFalse(enums.test(Anno, null, ctx));
        assertTrue(annos.test(Anno, null, ctx));
        assertTrue(types.test(Anno, null, ctx));

        assertFalse(classes.test(method, null, ctx));
        assertFalse(interfaces.test(method, null, ctx));
        assertFalse(enums.test(method, null, ctx));
        assertFalse(annos.test(method, null, ctx));
        assertFalse(types.test(method, null, ctx));
    }
}
