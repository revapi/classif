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
package org.revapi.classif.match.declaration;

import static org.revapi.classif.Tester.assertNotPassed;
import static org.revapi.classif.Tester.assertPassed;

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
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;
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

        ModelInspector<Element> inspector = new MirroringModelInspector(environment.elements(), environment.types());
        Map<String, ModelMatch> vars = Collections.emptyMap();

        MatchContext<Element> ctx = new MatchContext<>(inspector, vars);
        
        assertPassed(classes.test(TestClass, null, ctx));
        assertNotPassed(interfaces.test(TestClass, null, ctx));
        assertNotPassed(enums.test(TestClass, null, ctx));
        assertNotPassed(annos.test(TestClass, null, ctx));
        assertPassed(types.test(TestClass, null, ctx));

        assertNotPassed(classes.test(Iface, null, ctx));
        assertPassed(interfaces.test(Iface, null, ctx));
        assertNotPassed(enums.test(Iface, null, ctx));
        assertNotPassed(annos.test(Iface, null, ctx));
        assertPassed(types.test(Iface, null, ctx));

        assertNotPassed(classes.test(Enum, null, ctx));
        assertNotPassed(interfaces.test(Enum, null, ctx));
        assertPassed(enums.test(Enum, null, ctx));
        assertNotPassed(annos.test(Enum, null, ctx));
        assertPassed(types.test(Enum, null, ctx));

        assertNotPassed(classes.test(Anno, null, ctx));
        assertNotPassed(interfaces.test(Anno, null, ctx));
        assertNotPassed(enums.test(Anno, null, ctx));
        assertPassed(annos.test(Anno, null, ctx));
        assertPassed(types.test(Anno, null, ctx));

        assertNotPassed(classes.test(method, null, ctx));
        assertNotPassed(interfaces.test(method, null, ctx));
        assertNotPassed(enums.test(method, null, ctx));
        assertNotPassed(annos.test(method, null, ctx));
        assertNotPassed(types.test(method, null, ctx));
    }
}
