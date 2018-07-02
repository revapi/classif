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
package org.revapi.classif;

import org.junit.jupiter.api.Test;
import org.revapi.testjars.CompiledJar;
import org.revapi.testjars.junit5.JarSources;

class MatchingProgressTest {
    @JarSources(root = "/sources/progress/", sources = "SingleNodeMatch.java")
    private CompiledJar.Environment singleNode;

    @Test
    void testSingleNodeMatchesTrivially() {
        //TypeElement type = singleNode.elements().getTypeElement("SingleNodeMatch");

        // TODO implement
    }

    @Test
    void testDependenciesInfluenceMatchResult() {
        // TODO implement
    }

    @Test
    void testDependentsInfluenceMatchResult() {
        // TODO implement
    }

    @Test
    void testDependenciesAndDependentsMustAllMatch() {
        // TODO implement
    }

    @Test
    void testOnlyContributingDependenciesMarkedOnSuccess() {
        // TODO implement
    }

    @Test
    void testOnlyContributingDependentsMarkedOnSuccess() {
        // TODO implement
    }

    @Test
    void testOnlyContributingDependenciesAndDependentsMarkedOnSuccess() {
        // TODO implement
    }

    @Test
    void testChildrenCanChangeOutcomeOfFinish() {
        // TODO implement
    }

    @Test
    void onlyReturningNodesReportedByMatchProgressFinish() {
        // TODO implement
    }
}
