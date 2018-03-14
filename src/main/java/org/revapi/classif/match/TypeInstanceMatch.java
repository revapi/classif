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

public abstract class TypeInstanceMatch extends Match {

    private final TypeVisitor<Boolean, MatchContext<?>> dispatcher = new SimpleTypeVisitor8<Boolean, MatchContext<?>>() {
        @Override
        public Boolean visitIntersection(IntersectionType t, MatchContext<?> matchContext) {
            return testIntersection(t, matchContext);
        }

        @Override
        public Boolean visitUnion(UnionType t, MatchContext<?> matchContext) {
            return testUnion(t, matchContext);
        }

        @Override
        protected Boolean defaultAction(TypeMirror e, MatchContext<?> matchContext) {
            return false;
        }

        @Override
        public Boolean visitPrimitive(PrimitiveType t, MatchContext<?> matchContext) {
            return testPrimitive(t, matchContext);
        }

        @Override
        public Boolean visitNull(NullType t, MatchContext<?> matchContext) {
            return testNull(t, matchContext);
        }

        @Override
        public Boolean visitArray(ArrayType t, MatchContext<?> matchContext) {
            return testArray(t, matchContext);
        }

        @Override
        public Boolean visitDeclared(DeclaredType t, MatchContext<?> matchContext) {
            return testDeclared(t, matchContext);
        }

        @Override
        public Boolean visitError(ErrorType t, MatchContext<?> matchContext) {
            return testError(t, matchContext);
        }

        @Override
        public Boolean visitTypeVariable(TypeVariable t, MatchContext<?> matchContext) {
            return testTypeVariable(t, matchContext);
        }

        @Override
        public Boolean visitWildcard(WildcardType t, MatchContext<?> matchContext) {
            return testWildcard(t, matchContext);
        }

        @Override
        public Boolean visitExecutable(ExecutableType t, MatchContext<?> matchContext) {
            return testExecutable(t, matchContext);
        }

        @Override
        public Boolean visitNoType(NoType t, MatchContext<?> matchContext) {
            return testNoType(t, matchContext);
        }
    };

    @Override
    protected final <M> boolean testDeclaration(Element declaration, TypeMirror instance, MatchContext<M> ctx) {
        return false;
    }

    @Override
    protected <M> boolean testInstance(TypeMirror instance, MatchContext<M> ctx) {
        return dispatcher.visit(instance, ctx);
    }

    protected <M> boolean testIntersection(IntersectionType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean testUnion(UnionType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean defaultTest(TypeMirror e, MatchContext<M> matchContext) {
        return false;
    }

    protected <M> boolean testPrimitive(PrimitiveType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean testNull(NullType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean testArray(ArrayType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean testDeclared(DeclaredType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean testError(ErrorType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean testTypeVariable(TypeVariable t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean testWildcard(WildcardType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean testExecutable(ExecutableType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }

    protected <M> boolean testNoType(NoType t, MatchContext<M> matchContext) {
        return defaultTest(t, matchContext);
    }
}
