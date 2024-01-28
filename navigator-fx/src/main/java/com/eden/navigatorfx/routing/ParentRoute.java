package com.eden.navigatorfx.routing;

import com.eden.navigatorfx.Navigator;
import com.eden.navigatorfx.routing.internals.NavMap;

public class ParentRoute extends Route {
    private final NavMap routes = new NavMap();
    private final String path;

    ParentRoute(String path, String scenePath) {
        super(scenePath);
        this.path = path;
    }

    ParentRoute(String path, String scenePath, String windowTitle) {
        super(scenePath, windowTitle);
        this.path = path;
    }

    ParentRoute(String path, String scenePath, double sceneWidth, double sceneHeight) {
        super(scenePath, sceneWidth, sceneHeight);
        this.path = path;
    }

    ParentRoute(String path, String scenePath, String windowTitle, double sceneWidth, double sceneHeight) {
        super(scenePath, windowTitle, sceneWidth, sceneHeight);
        this.path = path;
    }

    public static ParentRoute parent(String path, String scenePath) {
        return new ParentRoute(path, scenePath);
    }

    public static ParentRoute parent(String path, String scenePath, String windowTitle) {
        return new ParentRoute(path, scenePath, windowTitle);
    }

    public static ParentRoute parent(String path, String scenePath, double sceneWidth, double sceneHeight) {
        return new ParentRoute(path, scenePath, sceneWidth, sceneHeight);
    }

    public static ParentRoute parent(String path, String scenePath, String windowTitle,
                                     double sceneWidth, double sceneHeight) {
        return new ParentRoute(path, scenePath, windowTitle, sceneWidth, sceneHeight);
    }

    public NavMap routes() {
        return routes;
    }

    public String path() {
        return path;
    }

    public ParentRoute when(String routeLabel, String subPath) {
        var route = new Route(subPath);
        registerSubRoute(routeLabel, route);
        return this;
    }

    private void registerSubRoute(String routeLabel, Route route) {
        routes.put(routeLabel, new NavMap.NavEntry(this,route));
    }

    public ParentRoute when(String routeLabel, String subPath, String winTitle) {
        var route = new Route(subPath, winTitle);
        registerSubRoute(routeLabel, route);
        return this;
    }

    public ParentRoute when(String routeLabel, String subPath, double sceneWidth, double sceneHeight) {
        var route = new Route(subPath, sceneWidth, sceneHeight);
        registerSubRoute(routeLabel, route);
        return this;
    }

    public ParentRoute when(String routeLabel, String subPath, String winTitle, double sceneWidth, double sceneHeight) {
        Navigator.when(subPath, winTitle, sceneWidth, sceneHeight);
        var route = new Route(subPath, winTitle, sceneWidth, sceneHeight);
        registerSubRoute(routeLabel, route);
        return this;
    }
}
