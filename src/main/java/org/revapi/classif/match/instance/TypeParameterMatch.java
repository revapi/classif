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
package org.revapi.classif.match.instance;

import static java.util.Collections.emptyList;

import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.TestableStream.testable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Function;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.util.Globbed;
import org.revapi.classif.util.Nullable;

public final class TypeParameterMatch extends TypeInstanceMatch implements Globbed {
    private final @Nullable TypeParameterWildcardMatch wildcard;
    private final List<TypeReferenceMatch> bounds;

    public TypeParameterMatch(@Nullable TypeParameterWildcardMatch wildcard, List<TypeReferenceMatch> bounds) {
        this.wildcard = wildcard;
        this.bounds = bounds;
    }

    @Override
    public boolean isMatchAny() {
        if (wildcard != null || bounds.size() != 1) {
            return false;
        }

        TypeReferenceMatch ref = bounds.get(0);
        return ref.isMatchAny();
    }

    @Override
    public boolean isMatchAll() {
        if (wildcard != null || bounds.size() != 1) {
            return false;
        }

        TypeReferenceMatch ref = bounds.get(0);
        return ref.isMatchAll();
    }

    @Override
    protected <M> TestResult testIntersection(IntersectionType t, MatchContext<M> matchContext) {
        // each required bound has to be present in the actual bounds
        // there can be more actual bounds than required, but we still match
        return testable(bounds).testAll(m -> testable(t.getBounds()).testAny(b -> m.testInstance(b, matchContext)));
    }

    @Override
    protected <M> TestResult testDeclared(DeclaredType t, MatchContext<M> matchContext) {
        return bounds == null ? NOT_PASSED : testable(bounds).testAll(b -> b.testInstance(t, matchContext));
    }

    @Override
    protected <M> TestResult testError(ErrorType t, MatchContext<M> matchContext) {
        return testDeclared(t, matchContext);
    }

    @Override
    protected <M> TestResult testTypeVariable(TypeVariable t, MatchContext<M> matchContext) {
        if (wildcard != null) {
            return testWildcard(((Function<TypeVariable, WildcardType>) x -> extendsWildcard(x.getUpperBound())).apply(t), matchContext);
        } else if (bounds != null) {
            // we can only test if the bounds match java.lang.Object here, because that is the only way
            // a type variable can be declared without an "extends" clause.
            TypeMirror bound = t.getUpperBound();
            if (isJavaLangObject(bound, matchContext)) {
                return testable(bounds).testAll(m -> m.testInstance(bound, matchContext));
            } else {
                return NOT_PASSED;
            }
        } else {
            return NOT_PASSED;
        }
    }

    @Override
    protected <M> TestResult testWildcard(WildcardType t, MatchContext<M> matchContext) {
        if (wildcard != null) {
            return wildcard.testInstance(t, matchContext);
        } else if (bounds != null && t.getSuperBound() == null && t.getExtendsBound() == null) {
            // the wildcard represents java.lang.Object. We allow for matching java.lang.Object even if it is specified
            // as a bound and not as a wildcard.
            TypeMirror bound = matchContext.modelInspector.getJavaLangObjectElement().asType();
            return testable(bounds).testAll(m -> m.testInstance(bound, matchContext));
        } else {
            return NOT_PASSED;
        }
    }

    private static boolean isJavaLangObject(TypeMirror t, MatchContext<?> ctx) {
        if (!(t instanceof DeclaredType)) {
            return false;
        } else {
            DeclaredType dt = (DeclaredType) t;
            return ctx.modelInspector.getJavaLangObjectElement().equals(dt.asElement());
        }
    }

    private static WildcardType extendsWildcard(TypeMirror bound) {
        return new SyntheticExtendsWildcardType(bound);
    }

    private static final class SyntheticExtendsWildcardType implements WildcardType {
        private final TypeMirror bound;

        private SyntheticExtendsWildcardType(TypeMirror bound) {
            this.bound = bound;
        }

        @Override
        public TypeMirror getExtendsBound() {
            return bound;
        }

        @Override
        public TypeMirror getSuperBound() {
            return null;
        }

        @Override
        public TypeKind getKind() {
            return TypeKind.WILDCARD;
        }

        @Override
        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitWildcard(this, p);
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            return emptyList();
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
            return (A[]) new Object[0];
        }
    }
}
