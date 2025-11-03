package com.eden.navigatorfx.v2;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SharedState {
    private static SharedState instance;

    private Map<String, Object> state = new ConcurrentHashMap<>();

    private SharedState() {
    }

    public static synchronized SharedState getInstance() {
        if (instance == null) {
            instance = new SharedState();
        }
        return instance;
    }

    public void put(String key, Object value) {
        state.put(key, value);
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(state.get(key));
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = state.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    // Additional utility methods as needed
}

