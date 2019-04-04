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
import java.io.IOException;

public class Inherited extends Base {

    public void baseMethod() {

    }

    public void methodParameter(int i) {

    }

    public void method2Parameters(int i, float j) {

    }

    public void method3Parameters(String a, int b, Cloneable c) {
        
    }

    private void privateMethod() {

    }

    protected void protectedMethod() {

    }

    void packagePrivateMethod() {

    }

    @Annotated
    public void annotatedMethod() {

    }

    public void annotatedParameterMethod(int i, @Annotated float j) {

    }

    public void throwingMethod() throws Exception {
        throw new Exception();
    }

    public void throwingMethod2() throws AssertionError, java.io.IOException {
        throw new IOException();
    }
}
