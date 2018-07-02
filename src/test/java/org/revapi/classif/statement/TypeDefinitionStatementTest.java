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
package org.revapi.classif.statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.Classif;
import org.revapi.classif.MatchingProgress;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.ModelInspector;
import org.revapi.classif.StructuralMatcher;
import org.revapi.classif.TestResult;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class TypeDefinitionStatementTest {
    @JarSources(root = "/sources/typeParameters/", sources = "TestClass.java")
    private CompiledJar.Environment typeParams;

    @JarSources(root = "/sources/constraints/", sources = {"Implements.java", "Extends.java", "Uses.java",
            "UsedBy.java"})
    private CompiledJar.Environment constraints;

    @Test
    void testTypeParameters() {
        TypeElement testClass = typeParams.elements().getTypeElement("TestClass");
        assertTrue(doTest(typeParams, testClass, "class ^TestClass<**>;"));
        assertTrue(doTest(typeParams, testClass, "class ^TestClass<*, **>;"));
        assertTrue(doTest(typeParams, testClass, "class ^TestClass<java.*.Object, **>;"));
        assertTrue(doTest(typeParams, testClass, "class ^TestClass<**, java.lang.Cloneable>;"));
        assertTrue(doTest(typeParams, testClass, "class ^TestClass<**, java.lang.String>;"));
        assertTrue(doTest(typeParams, testClass, "class ^TestClass<*, ? extends java.*.Object, **>;"));
        assertTrue(doTest(typeParams, testClass, "class ^TestClass<java.*.Object, **, ? extends /.*String/, **>;"));
        assertTrue(doTest(typeParams, testClass, "class ^TestClass<**, ? extends /.*\\.String/ & java.lang.Cloneable>;"));
        assertTrue(doTest(typeParams, testClass, "public class ^TestClass;"));
        assertFalse(doTest(typeParams, testClass, "private class ^TestClass;"));
        assertTrue(doTest(typeParams, testClass, "public type ^TestClass;"));
        assertFalse(doTest(typeParams, testClass, "public type ^TestClass<*>;"));
    }

    @Test
    void testAnnotations() {
        // TODO implement
    }

    @Test
    void testConstraints() {
        TypeElement Iface = constraints.elements().getTypeElement("Implements.Iface");
        TypeElement GenericIface = constraints.elements().getTypeElement("Implements.GenericIface");
        TypeElement Impl = constraints.elements().getTypeElement("Implements.Impl");
        TypeElement InheritedImpl = constraints.elements().getTypeElement("Implements.InheritedImpl");
        TypeElement GenericImplGeneric = constraints.elements().getTypeElement("Implements.GenericImplGeneric");
        TypeElement GenericImplConcrete = constraints.elements().getTypeElement("Implements.GenericImplConcrete");

        assertTrue(doTest(constraints, Impl, "type ^ implements Implements.Iface;"));
        assertTrue(doTest(constraints, InheritedImpl, "type ^ implements Implements.Iface;"));
        assertFalse(doTest(constraints, GenericImplGeneric, "type ^ implements Implements.Iface;"));
        assertTrue(doTest(constraints, GenericImplConcrete, "type ^!* implements Implements.Iface;"));

        assertTrue(doTest(constraints, Impl, "type ^ directly implements Implements.Iface;"));
        assertFalse(doTest(constraints, InheritedImpl, "type ^ directly implements Implements.Iface;"));
        assertFalse(doTest(constraints, GenericImplGeneric, "type ^ directly implements Implements.Iface;"));
        assertTrue(doTest(constraints, GenericImplConcrete, "type ^!* directly implements Implements.Iface;"));

        assertFalse(doTest(constraints, Impl, "type ^ implements Implements.GenericIface;"));
        assertTrue(doTest(constraints, GenericImplGeneric, "type ^ implements Implements.GenericIface;"));
        assertTrue(doTest(constraints, GenericImplConcrete, "type ^ implements Implements.GenericIface;"));

        assertFalse(doTest(constraints, Impl, "type ^ implements Implements.GenericIface<java.lang.String, java.lang.String>;"));
        assertTrue(doTest(constraints, GenericImplGeneric, "type ^ implements Implements.GenericIface<java.lang.String, java.lang.String>;"));
        assertFalse(doTest(constraints, GenericImplConcrete, "type ^ implements Implements.GenericIface<java.lang.String, java.lang.String>;"));

        assertFalse(doTest(constraints, Impl, "type ^ implements Implements.GenericIface<java.lang.Object, java.lang.String>;"));
        assertFalse(doTest(constraints, GenericImplGeneric, "type ^ implements Implements.GenericIface<java.lang.Object, java.lang.String>;"));
        assertTrue(doTest(constraints, GenericImplConcrete, "type ^ implements Implements.GenericIface<java.lang.Object, java.lang.String>;"));

        assertFalse(doTest(constraints, InheritedImpl, "type ^ exactly implements Implements.Iface;"));
        assertTrue(doTest(constraints, InheritedImpl, "type ^ exactly implements java.lang.Cloneable, Implements.Iface;"));
        assertTrue(doTest(constraints, InheritedImpl, "type ^ exactly implements Implements.Iface, java.lang.Cloneable;"));

        assertTrue(doTest(constraints, Iface, "type *.Impl implements %i; interface ^%i=*;", Impl));
        assertTrue(doTest(constraints, Impl, "type ^*.Impl implements %i; interface %i=*;", Iface));
        assertTrue(doTest(constraints, GenericIface, "match %i; type *./^Impl.+$/ implements %i; interface %i=*;", GenericImplConcrete));
    }

    private boolean doTest(CompiledJar.Environment env, Element el, String recipe, Element... nextElements) {
        ModelInspector<Element> insp = new MirroringModelInspector(env.elements(), env.types());

        StructuralMatcher matcher = Classif.compile(recipe);

        MatchingProgress<Element> progress = matcher.with(insp);

        boolean res = progress.start(el).toBoolean(false);
        if (res) {
            return true;
        }

        res = progress.finish(el).toBoolean(false);
        if (res) {
            return true;
        }

        for (Element e : nextElements) {
            progress.start(e);
            progress.finish(e);
        }

        return progress.finish().getOrDefault(el, TestResult.NOT_PASSED).toBoolean(false);
    }
}
