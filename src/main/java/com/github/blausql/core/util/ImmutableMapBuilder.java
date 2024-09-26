package com.github.blausql.core.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ImmutableMapBuilder<K, V> {

    private final Map<K, V> map = new HashMap<>();

    public static <K, V> ImmutableMapBuilder<K, V> newInstance() {
        return new ImmutableMapBuilder<>();
    }

    public Map<K, V> build() {
        return Collections.unmodifiableMap(map);
    }

    public ImmutableMapBuilder<K, V> put(K key, V value) {
        map.put(key, value);

        return this;
    }
}
