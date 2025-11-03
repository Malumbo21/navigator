package com.eden.navigatorfx.v2;

public interface Plugin {
        String id();
        String name();
        void registerRoutes(Navigator navigator);
        void initialize();
        default void shutdown(){}
}
