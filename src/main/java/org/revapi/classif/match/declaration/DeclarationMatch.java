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

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor8;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.Match;
import org.revapi.classif.match.MatchContext;

public abstract class DeclarationMatch extends Match {

    public <M> TestResult testDeclaration(Element declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        return new SimpleElementVisitor8<TestResult, Void>(TestResult.NOT_PASSED) {
            @Override
            public TestResult visitVariable(VariableElement e, Void __) {
                return testFieldOrArgument(e, instantiation, ctx);
            }

            @Override
            public TestResult visitType(TypeElement e, Void __) {
                return testType(e, instantiation, ctx);
            }

            @Override
            public TestResult visitExecutable(ExecutableElement e, Void __) {
                return testMethod(e, instantiation, ctx);
            }
        }.visit(declaration);
    }

    @Override
    public final <M> TestResult testInstance(TypeMirror instance, MatchContext<M> ctx) {
        return TestResult.NOT_PASSED;
    }

    protected <M> TestResult testType(TypeElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        return defaultTest(declaration, instantiation, ctx);
    }

    protected <M> TestResult testFieldOrArgument(VariableElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        return defaultTest(declaration, instantiation, ctx);
    }

    protected <M> TestResult testMethod(ExecutableElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        return defaultTest(declaration, instantiation, ctx);
    }

    protected <M> TestResult defaultTest(Element declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        return TestResult.NOT_PASSED;
    }

}
