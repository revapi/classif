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
package org.revapi.classif.matcher;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import org.revapi.classif.ModelInspector;
import org.revapi.classif.statement.AbstractMatcher;
import org.revapi.classif.statement.AnnotationStatement;

public abstract class AbstractStatementMatcher extends AbstractMatcher {
    private final List<AbstractMatcher> annotations;

    protected AbstractStatementMatcher(List<AnnotationStatement> annotations) {
        this.annotations = annotations.stream().map(AnnotationStatement::createMatcher).collect(toList());
    }
}
