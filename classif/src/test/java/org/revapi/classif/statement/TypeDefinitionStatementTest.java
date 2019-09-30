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

import static java.util.Collections.emptySet;
import static java.util.regex.Pattern.compile;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.revapi.classif.Classif.anyType;
import static org.revapi.classif.Classif.anyTypes;
import static org.revapi.classif.Classif.bound;
import static org.revapi.classif.Classif.extends_;
import static org.revapi.classif.Classif.fqn;
import static org.revapi.classif.Classif.implements_;
import static org.revapi.classif.Classif.modifiers;
import static org.revapi.classif.Classif.type;
import static org.revapi.classif.Classif.usedBy;
import static org.revapi.classif.Classif.uses;
import static org.revapi.classif.Classif.wildcardExtends;
import static org.revapi.classif.support.Tester.assertNotPassed;
import static org.revapi.classif.support.Tester.assertPassed;
import static org.revapi.classif.match.NameMatch.all;
import static org.revapi.classif.match.NameMatch.any;
import static org.revapi.classif.match.NameMatch.exact;
import static org.revapi.classif.match.NameMatch.pattern;
import static org.revapi.classif.match.declaration.Modifier.PRIVATE;
import static org.revapi.classif.match.declaration.Modifier.PUBLIC;
import static org.revapi.classif.match.declaration.TypeKind.ANY;
import static org.revapi.classif.match.declaration.TypeKind.CLASS;
import static org.revapi.classif.match.declaration.TypeKind.INTERFACE;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.Classif;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.ModelInspector;
import org.revapi.classif.StructuralMatcher;
import org.revapi.classif.support.Tester;
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

        // class ^TestClass<**>{}
        StructuralMatcher anyTypeParams = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(bound(anyTypes())))
                .build();

        assertPassed(Tester.test(typeParams, testClass, anyTypeParams));

        // class ^TestClass<*, **>{}
        StructuralMatcher moreThanOneTypeParams = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(bound(anyType()))
                        .$(bound(anyTypes())))
                .build();

        assertPassed(Tester.test(typeParams, testClass, moreThanOneTypeParams));

        // class ^TestClass<java.*.Object, **>{}
        StructuralMatcher concreteFirstTypeParam = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(bound(type().fqn(exact("java"), any(), exact("Object"))))
                        .$(bound(anyTypes())))
                .build();

        assertPassed(Tester.test(typeParams, testClass, concreteFirstTypeParam));

        // class ^TestClass<**, ? extends java.lang.Cloneable>{}
        StructuralMatcher concreteLastTypeParam = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(bound(anyTypes()))
                        .$(wildcardExtends(type().fqn(exact("java"), exact("lang"), exact("Cloneable")))))
                .build();

        assertPassed(Tester.test(typeParams, testClass, concreteLastTypeParam));

        // class ^TestClass<**, ? extends java.lang.String>{}
        StructuralMatcher concreteLastTypeParam2 = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(bound(anyTypes()))
                        .$(wildcardExtends(type().fqn(exact("java"), exact("lang"), exact("String")))))
                .build();

        assertPassed(Tester.test(typeParams, testClass, concreteLastTypeParam2));

        // class ^TestClass<*, ? extends java.*.Object, **>{}
        StructuralMatcher concreteSecondTypeParam = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(bound(anyType()))
                        .$(wildcardExtends(type().fqn(exact("java"), any(), exact("Object"))))
                        .$(bound(anyTypes())))
                .build();

        assertPassed(Tester.test(typeParams, testClass, concreteSecondTypeParam));

        // class ^TestClass<java.*.Object, **, ? extends **./.*String/, **>{}
        StructuralMatcher allGlobInBetween = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(bound(type().fqn(exact("java"), any(), exact("Object"))))
                        .$(bound(anyTypes()))
                        .$(wildcardExtends(type().fqn(all(), pattern(compile(".*String")))))
                        .$(bound(anyTypes())))
                .build();

        assertPassed(Tester.test(typeParams, testClass, allGlobInBetween));

        // class ^TestClass<**, ? extends **.String & java.lang.Cloneable>{}
        StructuralMatcher intersectionBounds = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(bound(anyTypes()))
                        .$(wildcardExtends(type().fqn(all(), exact("String")))
                                .and(type().fqn(exact("java"), exact("lang"), exact("Cloneable"))))
                        .$(bound(anyTypes())))
                .build();

        assertPassed(Tester.test(typeParams, testClass, intersectionBounds));

        // public class ^TestClass{}
        StructuralMatcher publicModifier = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(modifiers(PUBLIC)))
                .build();

        assertPassed(Tester.test(typeParams, testClass, publicModifier));

        // private class ^TestClass{}
        StructuralMatcher privateModifier = Classif.match()
                .$(type(CLASS, exact("TestClass")).matched()
                        .$(modifiers(PRIVATE)))
                .build();

        assertNotPassed(Tester.test(typeParams, testClass, privateModifier));

        // public type ^TestClass{}
        StructuralMatcher anyTypeKind = Classif.match()
                .$(type(ANY, exact("TestClass")).matched()
                        .$(modifiers(PUBLIC)))
                .build();

        assertPassed(Tester.test(typeParams, testClass, anyTypeKind));

        // public type ^TestClass<*>{}
        StructuralMatcher singleTypeParam = Classif.match()
                .$(type(ANY, exact("TestClass")).matched()
                        .$(modifiers(PUBLIC))
                        .$(bound(anyType())))
                .build();

        assertNotPassed(Tester.test(typeParams, testClass, singleTypeParam));
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


        // type ^ implements Implements.Iface{}
        StructuralMatcher implementsIface = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(implements_()
                                .$(type().fqn(exact("Implements"), exact("Iface")))))
                .build();

        // type ^!* implements Implements.Iface{}
        StructuralMatcher doesntImplementIface = Classif.match()
                .$(type(ANY, any()).matched().negated()
                        .$(implements_()
                                .$(type().fqn(exact("Implements"), exact("Iface")))))
                .build();

        assertPassed(Tester.test(constraints, Impl, implementsIface));
        assertPassed(Tester.test(constraints, InheritedImpl, implementsIface));
        assertNotPassed(Tester.test(constraints, GenericImplGeneric, implementsIface));
        assertPassed(Tester.test(constraints, GenericImplConcrete, doesntImplementIface));

        // type ^ directly implements Implements.Iface{}
        StructuralMatcher directlyImplementsIface = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(implements_().directly()
                                .$(type().fqn(exact("Implements"), exact("Iface")))))
                .build();

        // type ^!* implements Implements.Iface{}
        StructuralMatcher doesntDirectlyImplementIface = Classif.match()
                .$(type(ANY, any()).matched().negated()
                        .$(implements_().directly()
                                .$(type().fqn(exact("Implements"), exact("Iface")))))
                .build();

        assertPassed(Tester.test(constraints, Impl, directlyImplementsIface));
        assertNotPassed(Tester.test(constraints, InheritedImpl, directlyImplementsIface));
        assertNotPassed(Tester.test(constraints, GenericImplGeneric, directlyImplementsIface));
        assertPassed(Tester.test(constraints, GenericImplConcrete, doesntDirectlyImplementIface));

        // type ^ implements Implements.Iface{}
        StructuralMatcher implementsGenericIface = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(implements_()
                                .$(type().fqn(exact("Implements"), exact("GenericIface")))))
                .build();

        assertNotPassed(Tester.test(constraints, Impl, implementsGenericIface));
        assertPassed(Tester.test(constraints, GenericImplGeneric, implementsGenericIface));
        assertPassed(Tester.test(constraints, GenericImplConcrete, implementsGenericIface));

        // type ^ implements Implements.GenericIface<java.lang.String, java.lang.String>{}
        StructuralMatcher implementsGenericIfaceWithStringTypeParams = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(implements_()
                                .$(type().fqn(exact("Implements"), exact("GenericIface"))
                                        .$(bound(type().fqn(exact("java"), exact("lang"), exact("String"))))
                                        .$(bound(type().fqn(exact("java"), exact("lang"), exact("String")))))))
                .build();

        assertNotPassed(Tester.test(constraints, Impl, implementsGenericIfaceWithStringTypeParams));
        assertNotPassed(Tester.test(constraints, GenericImplGeneric, implementsGenericIfaceWithStringTypeParams));
        assertNotPassed(Tester.test(constraints, GenericImplConcrete, implementsGenericIfaceWithStringTypeParams));

        // type ^ implements Implements.GenericIface<java.lang.Object, java.lang.String>{}
        StructuralMatcher implementsGenericIfaceWithObjectAndStringTypeParams = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(implements_()
                                .$(type().fqn(exact("Implements"), exact("GenericIface"))
                                        .$(bound(type().fqn(exact("java"), exact("lang"), exact("Object"))))
                                        .$(bound(type().fqn(exact("java"), exact("lang"), exact("String")))))))
                .build();

        assertNotPassed(Tester.test(constraints, Impl, implementsGenericIfaceWithObjectAndStringTypeParams));
        assertNotPassed(Tester.test(constraints, GenericImplGeneric, implementsGenericIfaceWithObjectAndStringTypeParams));
        assertPassed(Tester.test(constraints, GenericImplConcrete, implementsGenericIfaceWithObjectAndStringTypeParams));

        // type ^ exactly implements Implements.Iface{}
        StructuralMatcher exactlyImplementsIncomplete = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(implements_()
                                .$(type().fqn(exact("Implements"), exact("Iface")))
                                .exactly()))
                .build();

        // type ^ exactly implements java.lang.Cloneable, Implements.Iface{}
        StructuralMatcher exactlyImplementsComplete = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(implements_()
                                .$(type().fqn(exact("java"), exact("lang"), exact("Cloneable")))
                                .$(type().fqn(exact("Implements"), exact("Iface")))
                                .exactly()))
                .build();

        // type ^ exactly implements Implements.Iface, java.lang.Cloneable{}
        StructuralMatcher exactlyImplementsCompleteSwappedOrder = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(implements_()
                                .$(type().fqn(exact("Implements"), exact("Iface")))
                                .$(type().fqn(exact("java"), exact("lang"), exact("Cloneable")))
                                .exactly()))
                .build();

        assertNotPassed(Tester.test(constraints, InheritedImpl, exactlyImplementsIncomplete));
        assertPassed(Tester.test(constraints, InheritedImpl, exactlyImplementsComplete));
        assertPassed(Tester.test(constraints, InheritedImpl, exactlyImplementsCompleteSwappedOrder));

        // type *.Impl implements %i{} interface ^%i=*{}
        StructuralMatcher implConstraintOnMatched = Classif.match()
                .$(type(ANY, fqn(any(), exact("Impl")))
                        .$(implements_().$(type().ref("i"))))
                .$(type(INTERFACE, any()).as("i").matched())
                .build();

        // type ^*.Impl implements %i{} interface %i=*{}
        StructuralMatcher implByVariable = Classif.match()
                .$(type(ANY, fqn(any(), exact("Impl"))).matched()
                        .$(implements_().$(type().ref("i"))))
                .$(type(INTERFACE, any()).as("i"))
                .build();

        // match %i; type *./.*Impl.+$/ implements %i{} interface %i=*{}
        StructuralMatcher implConstraintOnMatched2 = Classif.match("i")
                .$(type(ANY, fqn(any(), pattern(compile(".*Impl.+$"))))
                        .$(implements_().$(type().ref("i"))))
                .$(type(INTERFACE, any()).as("i"))
                .build();

        assertPassed(Tester.test(constraints, Iface, implConstraintOnMatched, Impl));
        assertPassed(Tester.test(constraints, Impl, implByVariable, Iface));
        assertPassed(Tester.test(constraints, GenericIface, implConstraintOnMatched2, GenericImplConcrete));
    }

    @Test
    void testExtends() {
        TypeElement B = constraints.elements().getTypeElement("Extends.B");
        TypeElement GB = constraints.elements().getTypeElement("Extends.GB");
        TypeElement GC = constraints.elements().getTypeElement("Extends.GC");
        TypeElement GD = constraints.elements().getTypeElement("Extends.GD");

        // type ^ extends java.lang.Object{}
        StructuralMatcher extendsObject = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(type().fqn(exact("java"), exact("lang"), exact("Object")))))
                .build();

        // type ^ directly extends java.lang.Object{}
        StructuralMatcher directlyExtendsObject = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(type().fqn(exact("java"), exact("lang"), exact("Object"))).directly()))
                .build();

        assertPassed(Tester.test(constraints, B, extendsObject));
        assertNotPassed(Tester.test(constraints, B, directlyExtendsObject));

        // type ^ extends Extends.A{}
        StructuralMatcher extendsA = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(type().fqn(exact("Extends"), exact("A")))))
                .build();

        // type ^ directly extends Extends.A{}
        StructuralMatcher directlyExtendsA = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(type().fqn(exact("Extends"), exact("A"))).directly()))
                .build();

        assertPassed(Tester.test(constraints, B, extendsA));
        assertPassed(Tester.test(constraints, B, directlyExtendsA));

        // type ^ extends Extends.GA<java.lang.String>{}
        StructuralMatcher extendsGAString = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(type().fqn(exact("Extends"), exact("GA"))
                                .$(bound(type().fqn(exact("java"), exact("lang"), exact("String")))))))
                .build();

        assertPassed(Tester.test(constraints, GB, extendsGAString));

        // type ^ directly extends Extends.GA<java.lang.Number>{}
        StructuralMatcher directlyExtendsGANumber = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(
                                type().fqn(exact("Extends"), exact("GA"))
                                        .$(bound(type().fqn(exact("java"), exact("lang"), exact("Number")))))
                                .directly()))
                .build();

        // type ^ directly extends Extends.GA<? extends java.lang.Number>{}
        StructuralMatcher directlyExtendsGAExtendsNumber = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(
                                type().fqn(exact("Extends"), exact("GA"))
                                        .$(wildcardExtends(type().fqn(exact("java"), exact("lang"), exact("Number")))))
                                .directly()))
                .build();

        assertNotPassed(Tester.test(constraints, GC, directlyExtendsGANumber));
        assertPassed(Tester.test(constraints, GC, directlyExtendsGAExtendsNumber));

        // type ^ extends Extends.GA<java.lang.Integer>{}
        StructuralMatcher extendsGAInteger = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(type().fqn(exact("Extends"), exact("GA"))
                                .$(bound(type().fqn(exact("java"), exact("lang"), exact("Integer")))))))
                .build();

        // type ^ directly extends Extends.GC<java.lang.Integer>{}
        StructuralMatcher directlyExtendsGCInteger = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(
                                type().fqn(exact("Extends"), exact("GC"))
                                        .$(bound(type().fqn(exact("java"), exact("lang"), exact("Integer")))))
                                .directly()))
                .build();

        // type ^ directly extends Extends.GC<? extends java.lang.Integer>{}
        StructuralMatcher directlyExtendsGCExtendsInteger = Classif.match()
                .$(type(ANY, any()).matched()
                        .$(extends_(
                                type().fqn(exact("Extends"), exact("GC"))
                                        .$(wildcardExtends(type().fqn(exact("java"), exact("lang"), exact("Integer")))))
                                .directly()))
                .build();

        assertPassed(Tester.test(constraints, GD, extendsGAInteger));
        assertPassed(Tester.test(constraints, GD, directlyExtendsGCInteger));
        assertNotPassed(Tester.test(constraints, GD, directlyExtendsGCExtendsInteger));
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

        // type ^ uses Extends.GD{}
        StructuralMatcher usesGD = Classif.match()
                .$(type(ANY, any()).matched().$(uses(type().fqn(exact("Extends"), exact("GD")))))
                .build();

        // type ^ directly uses Extends.GD{}
        StructuralMatcher directlyUsesGD = Classif.match()
                .$(type(ANY, any()).matched().$(uses(type().fqn(exact("Extends"), exact("GD"))).directly()))
                .build();

        assertPassed(Tester.test(insp, GB, usesGD));
        assertPassed(Tester.test(insp, B, usesGD));
        assertNotPassed(Tester.test(insp, B, directlyUsesGD));
        assertPassed(Tester.test(insp, GB, directlyUsesGD));
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

        // type ^ usedby %u {} type %u=Extends.GD {}
        StructuralMatcher usedBy = Classif.match()
                .$(type(ANY, any()).matched().$(usedBy("u")))
                .$(type(ANY, exact("Extends"), exact("GD")).as("u"))
                .build();

        // type ^ directly usedby %u {} type %u=Extends.GD {}
        StructuralMatcher directlyUsedBy = Classif.match()
                .$(type(ANY, any()).matched().$(usedBy("u").directly()))
                .$(type(ANY, exact("Extends"), exact("GD")).as("u"))
                .build();

        assertPassed(Tester.test(insp, GB, usedBy, GD));
        assertPassed(Tester.test(insp, B, usedBy, GD));
        assertNotPassed(Tester.test(insp, B, directlyUsedBy, GD));
        assertPassed(Tester.test(insp, GB, directlyUsedBy, GD));
    }
}
