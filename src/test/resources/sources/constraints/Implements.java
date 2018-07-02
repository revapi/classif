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
public class Implements {

    public interface Iface {

    }

    public interface GenericIface<T, U extends String> {

    }

    public class Impl implements Iface {

    }

    public class InheritedImpl extends Impl implements java.lang.Cloneable {

    }

    public class GenericImplGeneric<T extends String> implements GenericIface<T, String> {

    }

    public class GenericImplConcrete implements GenericIface<Object, String> {

    }
}
