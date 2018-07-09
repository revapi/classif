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

import static org.revapi.classif.TestResult.DEFERRED;
import static org.revapi.classif.TestResult.NOT_PASSED;
import static org.revapi.classif.TestResult.TestableStream.testable;

import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.ModelMatch;

public class UsedByMatch extends DeclarationMatch {

    private final boolean onlyDirect;
    private final List<String> referencedVariables;

    public UsedByMatch(boolean onlyDirect, List<String> referencedVariables) {
        this.onlyDirect = onlyDirect;
        this.referencedVariables = referencedVariables;
    }

    @Override
    protected <M> TestResult testType(TypeElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        TypeVisitor<Stream<Element>, ?> visitor = UseVisitor.findUseSites(ctx.modelInspector);

        Stream<Element> directUseSites = visitor.visit(instantiation);

        if (onlyDirect) {
            return testable(directUseSites).testAny(us -> testable(referencedVariables).testAny(v -> {
                ModelMatch m = ctx.variables.get(v);
                return m == null ? NOT_PASSED : m.test(ctx.modelInspector.fromElement(us), ctx);
            }));
        } else {
            return testRecursively(directUseSites, ctx);
        }
    }

    private <M> TestResult testRecursively(Stream<Element> sites,
            MatchContext<M> ctx) {

        if (sites == null) {
            return DEFERRED;
        } else {
            return testable(sites).testAny(us -> testable(referencedVariables).testAny(v -> {
                ModelMatch m = ctx.variables.get(v);
                return m == null
                        ? NOT_PASSED
                        : m.test(ctx.modelInspector.fromElement(us), ctx)
                        .or(() -> testRecursively(UseVisitor.findUseSites(ctx.modelInspector).visit(us.asType()), ctx));
            }));
        }
    }
}
