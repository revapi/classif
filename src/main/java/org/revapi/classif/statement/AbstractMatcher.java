/*
 * Copyright 2014-2018 Lukas Krejci
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

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor8;

import org.revapi.classif.ModelInspector;

public abstract class AbstractMatcher extends TreeNode<AbstractMatcher> {
    private final IdentityHashMap<Object, Boolean> decisionCache = new IdentityHashMap<>();

    public final <E> boolean test(E element, ModelInspector<E> inspector, Map<String, AbstractMatcher> variables) {
        element = requireNonNull(element);
        inspector = requireNonNull(inspector);
        variables = requireNonNull(variables);

        Boolean match = decisionCache.get(element);
        if (match == null) {
            match = dispatchTest(element, inspector, variables);

            if (match) {
                AbstractMatcher parent = getParent();
                if (parent != null) {
                    match = parent.test(inspector.getEnclosing(element), inspector, variables);
                }
            }

            decisionCache.put(element, match);
        }

        return match;
    }

    public final <E> boolean dispatchTest(E el, ModelInspector<E> inspector,
            Map<String, AbstractMatcher> variables) {
        return new SimpleElementVisitor8<Boolean, Void>() {
            @Override
            protected Boolean defaultAction(Element e, Void aVoid) {
                return false;
            }

            @Override
            public Boolean visitVariable(VariableElement e, Void __) {
                return testVariable(el, inspector, variables);
            }

            @Override
            public Boolean visitType(TypeElement e, Void __) {
                return testType(el, inspector, variables);
            }

            @Override
            public Boolean visitExecutable(ExecutableElement e, Void __) {
                return testMethod(el, inspector, variables);
            }
        }.visit(inspector.toElement(el));
    }

    public <E> boolean testType(E type, ModelInspector<E> inspector,
            Map<String, AbstractMatcher> variables) {
        return defaultElementTest(type, inspector, variables);
    }

    public <E> boolean testMethod(E method, ModelInspector<E> inspector,
            Map<String, AbstractMatcher> variables) {
        return defaultElementTest(method, inspector, variables);
    }

    public <E> boolean testVariable(E var, ModelInspector<E> inspector,
            Map<String, AbstractMatcher> variables) {
        return defaultElementTest(var, inspector, variables);
    }

    protected <E> boolean defaultElementTest(E element, ModelInspector<E> inspector,
            Map<String, AbstractMatcher> variables) {
        return false;
    }
}
