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
package org.revapi.classif.match.declaration;

import static org.revapi.classif.util.LogUtil.traceParams;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.EntryMessage;
import org.revapi.classif.TestResult;
import org.revapi.classif.progress.context.MatchContext;
import org.revapi.classif.match.instance.TypeReferenceMatch;
import org.revapi.classif.util.Glob;

public final class AnnotationMatch {
    private static final Logger LOG = LogManager.getLogger(AnnotationMatch.class);

    private final boolean negation;
    private final TypeReferenceMatch type;
    private final Glob<AnnotationAttributeMatch> attributes;

    public AnnotationMatch(boolean negation, TypeReferenceMatch type,
            List<AnnotationAttributeMatch> attributes) {

        this.negation = negation;
        this.type = type;
        this.attributes = new Glob<>(attributes);
    }

    public <M> TestResult test(AnnotationMirror a , MatchContext<M> ctx) {
        EntryMessage methodTrace = LOG.traceEntry(traceParams(LOG, "this", this, "annotation", a, "ctx", ctx));

        //test1: @A(a = 1, b = 2)
        //test2: @A(a = 1)
        //actual: @A(a = 1), and b is an attribute with default value 2
        //both of the above tests need to match
        TestResult ret = type.testInstance(a.getAnnotationType(), ctx).and(() ->
                attributes.testUnorderedWithOptionals((m, at) -> m.test(at, ctx), explicitAttributes(a).entrySet(),
                defaultAttributes(a).entrySet()));

        return LOG.traceExit(methodTrace, negation ? ret.negate() : ret);
    }

    public boolean isNegation() {
        return negation;
    }

    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        if (negation) {
            bld.append("!");
        }

        bld.append("@").append(type);

        bld.append("(");
        bld.append(attributes.getMatches().stream().map(Object::toString).collect(Collectors.joining(", ")));
        bld.append(")");

        return bld.toString();
    }

    //needed just to make the compiler happy
    @SuppressWarnings("unchecked")
    private Map<ExecutableElement, AnnotationValue> explicitAttributes(AnnotationMirror a) {
        return (Map<ExecutableElement, AnnotationValue>) a.getElementValues();
    }

    private Map<ExecutableElement, AnnotationValue> defaultAttributes(AnnotationMirror a) {
        Map<ExecutableElement, AnnotationValue> explicit = new LinkedHashMap<>(a.getElementValues());
        Map<ExecutableElement, AnnotationValue> ret = new LinkedHashMap<>(a.getElementValues());

        DeclaredType type = a.getAnnotationType();
        for (ExecutableElement m : ElementFilter.methodsIn(type.asElement().getEnclosedElements())) {
            if (!explicit.containsKey(m)) {
                ret.put(m, m.getDefaultValue());
            }
        }

        return ret;
    }
}
