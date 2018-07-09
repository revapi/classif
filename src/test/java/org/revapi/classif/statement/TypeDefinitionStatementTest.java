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

import static java.util.Collections.emptySet;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.revapi.classif.Tester.assertNotPassed;
import static org.revapi.classif.Tester.assertPassed;
import static org.revapi.classif.Tester.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.ModelInspector;
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
        assertPassed(test(typeParams, testClass, "class ^TestClass<**>;"));
        assertPassed(test(typeParams, testClass, "class ^TestClass<*, **>;"));
        assertPassed(test(typeParams, testClass, "class ^TestClass<java.*.Object, **>;"));
        assertPassed(test(typeParams, testClass, "class ^TestClass<**, java.lang.Cloneable>;"));
        assertPassed(test(typeParams, testClass, "class ^TestClass<**, java.lang.String>;"));
        assertPassed(test(typeParams, testClass, "class ^TestClass<*, ? extends java.*.Object, **>;"));
        assertPassed(test(typeParams, testClass, "class ^TestClass<java.*.Object, **, ? extends /.*String/, **>;"));
        assertPassed(test(typeParams, testClass, "class ^TestClass<**, ? extends /.*\\.String/ & java.lang.Cloneable>;"));
        assertPassed(test(typeParams, testClass, "public class ^TestClass;"));
        assertNotPassed(test(typeParams, testClass, "private class ^TestClass;"));
        assertPassed(test(typeParams, testClass, "public type ^TestClass;"));
        assertNotPassed(test(typeParams, testClass, "public type ^TestClass<*>;"));
    }

    @Test
    void testAnnotations() {
        // TODO implement
    }

    @Test
    void testImplements() {
        TypeElement Iface = constraints.elements().getTypeElement("Implements.Iface");
        TypeElement GenericIface = constraints.elements().getTypeElement("Implements.GenericIface");
        TypeElement Impl = constraints.elements().getTypeElement("Implements.Impl");
        TypeElement InheritedImpl = constraints.elements().getTypeElement("Implements.InheritedImpl");
        TypeElement GenericImplGeneric = constraints.elements().getTypeElement("Implements.GenericImplGeneric");
        TypeElement GenericImplConcrete = constraints.elements().getTypeElement("Implements.GenericImplConcrete");

        assertPassed(test(constraints, Impl, "type ^ implements Implements.Iface;"));
        assertPassed(test(constraints, InheritedImpl, "type ^ implements Implements.Iface;"));
        assertNotPassed(test(constraints, GenericImplGeneric, "type ^ implements Implements.Iface;"));
        assertPassed(test(constraints, GenericImplConcrete, "type ^!* implements Implements.Iface;"));

        assertPassed(test(constraints, Impl, "type ^ directly implements Implements.Iface;"));
        assertNotPassed(test(constraints, InheritedImpl, "type ^ directly implements Implements.Iface;"));
        assertNotPassed(test(constraints, GenericImplGeneric, "type ^ directly implements Implements.Iface;"));
        assertPassed(test(constraints, GenericImplConcrete, "type ^!* directly implements Implements.Iface;"));

        assertNotPassed(test(constraints, Impl, "type ^ implements Implements.GenericIface;"));
        assertPassed(test(constraints, GenericImplGeneric, "type ^ implements Implements.GenericIface;"));
        assertPassed(test(constraints, GenericImplConcrete, "type ^ implements Implements.GenericIface;"));

        assertNotPassed(test(constraints, Impl, "type ^ implements Implements.GenericIface<java.lang.String, java.lang.String>;"));
        assertPassed(test(constraints, GenericImplGeneric, "type ^ implements Implements.GenericIface<java.lang.String, java.lang.String>;"));
        assertNotPassed(test(constraints, GenericImplConcrete, "type ^ implements Implements.GenericIface<java.lang.String, java.lang.String>;"));

        assertNotPassed(test(constraints, Impl, "type ^ implements Implements.GenericIface<java.lang.Object, java.lang.String>;"));
        assertNotPassed(test(constraints, GenericImplGeneric, "type ^ implements Implements.GenericIface<java.lang.Object, java.lang.String>;"));
        assertPassed(test(constraints, GenericImplConcrete, "type ^ implements Implements.GenericIface<java.lang.Object, java.lang.String>;"));

        assertNotPassed(test(constraints, InheritedImpl, "type ^ exactly implements Implements.Iface;"));
        assertPassed(test(constraints, InheritedImpl, "type ^ exactly implements java.lang.Cloneable, Implements.Iface;"));
        assertPassed(test(constraints, InheritedImpl, "type ^ exactly implements Implements.Iface, java.lang.Cloneable;"));

        assertPassed(test(constraints, Iface, "type *.Impl implements %i; interface ^%i=*;", Impl));
        assertPassed(test(constraints, Impl, "type ^*.Impl implements %i; interface %i=*;", Iface));
        assertPassed(test(constraints, GenericIface, "match %i; type *./^Impl.+$/ implements %i; interface %i=*;", GenericImplConcrete));
    }

    @Test
    void testExtends() {
        TypeElement B = constraints.elements().getTypeElement("Extends.B");
        TypeElement GB = constraints.elements().getTypeElement("Extends.GB");
        TypeElement GC = constraints.elements().getTypeElement("Extends.GC");
        TypeElement GD = constraints.elements().getTypeElement("Extends.GD");

        assertPassed(test(constraints, B, "type ^ extends java.lang.Object;"));
        assertPassed(test(constraints, B, "type ^ extends Extends.A;"));
        assertNotPassed(test(constraints, B, "type ^ directly extends java.lang.Object;"));
        assertPassed(test(constraints, B, "type ^ directly extends Extends.A;"));

        assertPassed(test(constraints, GB, "type ^ extends Extends.GA<java.lang.String>;"));
        assertPassed(test(constraints, GC, "type ^ directly extends Extends.GA<? extends java.lang.Number>;"));
        assertPassed(test(constraints, GC, "type ^ directly extends Extends.GA<java.lang.Number>;"));
        assertPassed(test(constraints, GD, "type ^ extends Extends.GA<java.lang.Integer>;"));
        assertPassed(test(constraints, GD, "type ^ directly extends Extends.GC<java.lang.Integer>;"));
        assertPassed(test(constraints, GD, "type ^ directly extends Extends.GC<? extends java.lang.Integer>;"));
    }

    @Test
    void testUses() {
        TypeElement B = constraints.elements().getTypeElement("Extends.B");
        TypeElement GB = constraints.elements().getTypeElement("Extends.GB");
        TypeElement GC = constraints.elements().getTypeElement("Extends.GC");
        TypeElement GD = constraints.elements().getTypeElement("Extends.GD");

        @SuppressWarnings("Duplicates")
        ModelInspector<Element> insp = new MirroringModelInspector(constraints.elements(), constraints.types()) {
            @Override
            public Set<Element> getUses(Element model) {
                // return some uses for the elements... sneakily introduce a cycle there to check that we're guarding
                // against that...
                if (model == B) {
                    return new HashSet<Element>(2) {{
                        add(GB);
                        add(GC);
                    }};
                } else if (model == GC) {
                    return new HashSet<Element>(1) {{
                        add(B);
                    }};
                } else if (model == GB) {
                    return new HashSet<Element>(1) {{
                        add(GD);
                    }};
                } else {
                    return emptySet();
                }
            }
        };

        assertPassed(test(insp, GB, "type ^ uses Extends.GD;"));
        assertPassed(test(insp, B, "type ^ uses Extends.GD;"));
        assertNotPassed(test(insp, B, "type ^ directly uses Extends.GD;"));
        assertPassed(test(insp, GB, "type ^ directly uses Extends.GD;"));
    }

    @Test
    void testUsedBy() {
        TypeElement B = constraints.elements().getTypeElement("Extends.B");
        TypeElement GB = constraints.elements().getTypeElement("Extends.GB");
        TypeElement GC = constraints.elements().getTypeElement("Extends.GC");
        TypeElement GD = constraints.elements().getTypeElement("Extends.GD");

        @SuppressWarnings("Duplicates")
        ModelInspector<Element> insp = new MirroringModelInspector(constraints.elements(), constraints.types()) {
            @Override
            public Set<Element> getUseSites(Element model) {
                // return some uses for the elements... sneakily introduce a cycle there to check that we're guarding
                // against that...
                if (model == B) {
                    return new HashSet<Element>(2) {{
                        add(GB);
                        add(GC);
                    }};
                } else if (model == GC) {
                    return new HashSet<Element>(1) {{
                        add(B);
                    }};
                } else if (model == GB) {
                    return new HashSet<Element>(1) {{
                        add(GD);
                    }};
                } else {
                    return emptySet();
                }
            }
        };

        assertPassed(test(insp, GB, "type ^ usedby %u; type %u=Extends.GD;", GD));
        assertPassed(test(insp, B, "type ^ usedby %u; type %u=Extends.GD;", GD));
        assertNotPassed(test(insp, B, "type ^ directly usedby %u; type %u=Extends.GD;", GD));
        assertPassed(test(insp, GB, "type ^ directly usedby %u; type %u=Extends.GD;", GD));
    }
}
