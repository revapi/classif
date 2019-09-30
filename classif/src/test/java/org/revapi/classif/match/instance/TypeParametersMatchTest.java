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
package org.revapi.classif.match.instance;

import static java.util.Collections.emptySet;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.revapi.classif.MirroringModelInspector;
import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.CompiledJarExtension;
import org.revapi.testjars.junit5.JarSources;

@TestInstance(PER_CLASS)
@ExtendWith(CompiledJarExtension.class)
class TypeParametersMatchTest {

    @JarSources(root = "/sources/typeParameters/", sources = "TestClass.java")
    private CompiledJar.Environment environment;

    @Test
    void test() {
        TypeElement TestClass = environment.elements().getTypeElement("TestClass");

        TypeMirror A = TestClass.getTypeParameters().get(0).asType();
        TypeMirror B = TestClass.getTypeParameters().get(1).asType();
        TypeMirror C = TestClass.getTypeParameters().get(1).asType();
        TypeMirror D = TestClass.getTypeParameters().get(1).asType();
        TypeMirror E = TestClass.getTypeParameters().get(1).asType();
        TypeMirror F = TestClass.getTypeParameters().get(1).asType();
        ExecutableElement wildcardMethod = findMethod(TestClass, "wildcard");
        TypeMirror wildcard = ((DeclaredType) wildcardMethod.getReturnType()).getTypeArguments().get(0);
        ExecutableElement wildcardExtendsMethod = findMethod(TestClass, "wildcardExtends");
        TypeMirror wildcardExtends = ((DeclaredType) wildcardExtendsMethod.getReturnType()).getTypeArguments().get(0);
        ExecutableElement wildcardSuperMethod = findMethod(TestClass, "wildcardSuper");
        TypeMirror wildcardSuper = ((DeclaredType) wildcardSuperMethod.getReturnType()).getTypeArguments().get(0);

        // TODO rest of the tests...
    }

    private ExecutableElement findMethod(TypeElement type, String methodName) {
        return ElementFilter.methodsIn(type.getEnclosedElements()).stream()
                .filter(m -> m.getSimpleName().contentEquals(methodName))
                .findFirst()
                .orElse(null);
    }

    private TestResult doTest(TypeMirror t, TypeParametersMatch match) {
        MatchContext<Element> ctx = new MatchContext<>(new MirroringModelInspector(environment.elements(),
                environment.types()), emptySet());
        return match.testInstance(t, ctx);
    }
}
