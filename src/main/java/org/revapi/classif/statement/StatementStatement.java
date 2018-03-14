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
package org.revapi.classif.statement;

import java.util.Collections;
import java.util.List;

import org.revapi.classif.match.AnnotationMatch;
import org.revapi.classif.match.ModifiersMatch;

public abstract class StatementStatement extends AbstractStatement {
    protected final List<AnnotationMatch> annotations;
    protected final ModifiersMatch modifiers;

    protected StatementStatement(String definedVariable, List<String> referencedVariables,
            List<AnnotationMatch> annotations, ModifiersMatch modifiers, boolean isMatch) {
        super(definedVariable, referencedVariables, Collections.emptyList(), isMatch);
        this.annotations = annotations;
        this.modifiers = modifiers;
    }
}
