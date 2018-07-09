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

import static org.revapi.classif.TestResult.TestableStream.testable;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;

public final class AnnotationsMatch extends DeclarationMatch {
    private final List<AnnotationMatch> annotations;

    public AnnotationsMatch(List<AnnotationMatch> annotations) {
        this.annotations = annotations;
    }

    @Override
    protected <M> TestResult defaultTest(Element e, TypeMirror inst, MatchContext<M> matchContext) {
        return annotations.stream().reduce(
                TestResult.PASSED,
                (res, next) -> res.and(() ->
                        testable(e.getAnnotationMirrors()).testAny(a -> next.test(a, matchContext))),
                TestResult::and);
    }
}
