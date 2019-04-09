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
package org.revapi.classif.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class SizedCollections {

    private SizedCollections() {

    }

    public static <K, V> IdentityHashMap<K, V> newIdentityHashMapWithExactSize(int size) {
        return new IdentityHashMap<>(size * 2 / 3);
    }

    public static <K, V> HashMap<K, V> newHashMapWithExactSize(int size) {
        return new HashMap<>(size, 1.0f);
    }

    public static <T> Collector<T, ?, List<T>> toListWithSize(int size) {
        return Collectors.toCollection(() -> new ArrayList<>(size));
    }
}
