package com.eden.navigatorfx.v2;

// NavRequest.java
import java.util.Map;
import java.util.Optional;

public class NavRequest {
    private final String url;
    private final Map<String, String> queryParams;
    private final Map<String, String> pathParams;
    private final Object data;

    public NavRequest(String url, Map<String, String> queryParams, Map<String, String> pathParams, Object data) {
        this.url = url;
        this.queryParams = queryParams;
        this.pathParams = pathParams;
        this.data = data;
    }

    public String url() {
        return url;
    }

    public Map<String, String> query() {
        return queryParams;
    }

    public String query(String key) {
        return queryParams.get(key);
    }

    public Map<String, String> path() {
        return pathParams;
    }

    public String path(String key) {
        return pathParams.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> data() {
        return Optional.ofNullable((T) data);
    }
}

