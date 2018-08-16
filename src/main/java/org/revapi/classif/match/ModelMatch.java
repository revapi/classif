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

import static java.util.Objects.requireNonNull;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor8;

import org.revapi.classif.TestResult;
import org.revapi.classif.util.TreeNode;

public abstract class ModelMatch extends TreeNode<ModelMatch> {

    public final <M> TestResult test(M model, MatchContext<M> ctx) {
        requireNonNull(model);
        requireNonNull(ctx);

        return new SimpleElementVisitor8<TestResult, Void>() {
            @Override
            protected TestResult defaultAction(Element e, Void aVoid) {
                return TestResult.NOT_PASSED;
            }

            @Override
            public TestResult visitVariable(VariableElement e, Void __) {
                return testVariable(model, ctx);
            }

            @Override
            public TestResult visitType(TypeElement e, Void __) {
                return testType(model, ctx);
            }

            @Override
            public TestResult visitExecutable(ExecutableElement e, Void __) {
                return testMethod(model, ctx);
            }
        }.visit(ctx.modelInspector.toElement(model));
    }

    public <M> TestResult testType(M type, MatchContext<M> ctx) {
        return defaultElementTest(type, ctx);
    }

    public <M> TestResult testMethod(M method, MatchContext<M> ctx) {
        return defaultElementTest(method, ctx);
    }

    public <M> TestResult testVariable(M var, MatchContext<M> ctx) {
        return defaultElementTest(var, ctx);
    }

    protected <M> TestResult defaultElementTest(M model, MatchContext<M> ctx) {
        return TestResult.NOT_PASSED;
    }
}
