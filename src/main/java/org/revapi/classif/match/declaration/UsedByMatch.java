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

import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import org.revapi.classif.TestResult;
import org.revapi.classif.match.MatchContext;
import org.revapi.classif.match.instance.TypeReferenceMatch;

public class UsedByMatch extends DeclarationMatch {

    private final boolean onlyDirect;
    private final TypeReferenceMatch type;

    public UsedByMatch(boolean onlyDirect, TypeReferenceMatch type) {
        this.onlyDirect = onlyDirect;
        this.type = type;
    }

    @Override
    protected <M> TestResult testType(TypeElement declaration, TypeMirror instantiation, MatchContext<M> ctx) {
        // TODO this is wrong - the usedby is more different from uses than just by the different inspector call
        TypeVisitor<Stream<DeclaredType>, ?> visitor = UseVisitor.findUses(ctx.modelInspector);

        Stream<DeclaredType> directUseSites = visitor.visit(instantiation);

//        if (onlyDirect) {
//            return directUseSites.anyMatch(u -> type.testInstance(u, ctx));
//        } else {
//            return testRecursively(directUseSites, visitor, ctx);
//        }
        return TestResult.NOT_PASSED;
    }

//    private boolean testRecursively(Stream<DeclaredType> types, TypeVisitor<Stream<DeclaredType>, ?> visitor,
//            MatchContext<?> ctx) {
//
//        return types.reduce(
//                false,
//                (prior, next) -> prior
//                        || type.testInstance(next, ctx)
//                        || testRecursively(visitor.visit(next), visitor, ctx),
//                Boolean::logicalOr);
//    }
}
