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

import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor8;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.Match;
import org.revapi.classif.match.MatchContext;

public abstract class TypeInstanceMatch extends Match {

    private final TypeVisitor<TestResult, MatchContext<?>> dispatcher = new SimpleTypeVisitor8<TestResult, MatchContext<?>>() {
        @Override
        public TestResult visitIntersection(IntersectionType t, MatchContext<?> matchContext) {
            return testIntersection(t, matchContext);
        }

        @Override
        public TestResult visitUnion(UnionType t, MatchContext<?> matchContext) {
            return testUnion(t, matchContext);
        }

        @Override
        protected TestResult defaultAction(TypeMirror e, MatchContext<?> matchContext) {
            return TestResult.NOT_PASSED;
        }

        @Override
        public TestResult visitPrimitive(PrimitiveType t, MatchContext<?> matchContext) {
            return testPrimitive(t, matchContext);
        }

        @Override
        public TestResult visitNull(NullType t, MatchContext<?> matchContext) {
            return testNull(t, matchContext);
        }

        @Override
        public TestResult visitArray(ArrayType t, MatchContext<?> matchContext) {
            return testArray(t, matchContext);
        }

        @Override
        public TestResult visitDeclared(DeclaredType t, MatchContext<?> matchContext) {
            return testDeclared(t, matchContext);
        }

        @Override
        public TestResult visitError(ErrorType t, MatchContext<?> matchContext) {
            return testError(t, matchContext);
        }

        @Override
        public TestResult visitTypeVariable(TypeVariable t, MatchContext<?> matchContext) {
            return testTypeVariable(t, matchContext);
        }

        @Override
        public TestResult visitWildcard(WildcardType t, MatchContext<?> matchContext) {
            return testWildcard(t, matchContext);
        }

        @Override
        public TestResult visitExecutable(ExecutableType t, MatchContext<?> matchContext) {
            return testExecutable(t, matchContext);
        }

        @Override
        public TestResult visitNoType(NoType t, MatchContext<?> matchContext) {
            return testNoType(t, matchContext);
        }
    };

    @Override
    public final <M> TestResult testDeclaration(Element declaration, TypeMirror instance, MatchContext<M> ctx) {
        return TestResult.NOT_PASSED;
    }

    @Override
    public <M> TestResult testInstance(TypeMirror instance, MatchContext<M> ctx) {
        return dispatcher.visit(instance, ctx);
    }

    protected <M> TestResult testIntersection(IntersectionType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult testUnion(UnionType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult defaultTest(TypeMirror e, MatchContext<M> matchContext) {
        return TestResult.NOT_PASSED;
    }

    protected <M> TestResult testPrimitive(PrimitiveType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult testNull(NullType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult testArray(ArrayType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult testDeclared(DeclaredType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult testError(ErrorType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult testTypeVariable(TypeVariable t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult testWildcard(WildcardType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult testExecutable(ExecutableType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> TestResult testNoType(NoType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }
}
