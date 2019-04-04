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
public class TestClass {

    public static class UsedInDistance2 {

    }

    public static class Used {
        public UsedInDistance2 method() {
            return null;
        }
    }

    public Used userMethod() {
        return null;
    }

    public static class UseCycleStart {
        public UseCycleEnd method() {
            return null;
        }
    }

    public static class UseCycleEnd {
        public UseCycleStart method() {
            return null;
        }
    }

    public void cycleMethod(UseCycleStart useCycle) {
    }
}
