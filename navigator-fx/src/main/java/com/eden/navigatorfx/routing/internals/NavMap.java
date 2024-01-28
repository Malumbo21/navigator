package com.eden.navigatorfx.routing.internals;

import com.eden.navigatorfx.exceptions.DuplicateRouteException;
import com.eden.navigatorfx.exceptions.RouteNotFoundException;
import com.eden.navigatorfx.routing.ParentRoute;
import com.eden.navigatorfx.routing.Route;

public class NavMap extends ExpandedMap<String, NavMap.NavEntry> {
    @Override
    public NavEntry get(Object key) {
        if (key instanceof String path) {
            path = sanitize(path);
            if (path.contains("/")) {
                int index = path.indexOf("/");
                var rootPath = path.substring(0, index);
                var parent = super.get(rootPath);

                if (parent == null) throw new RouteNotFoundException(path);
                var subPath = path.substring(index + 1);
                ParentRoute parentRoute = (ParentRoute) parent.route();
                var sub = parentRoute.routes().get(subPath);

                return new NavEntry(parentRoute, sub.route());
            }
            return super.get(path);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public NavEntry put(String path, NavEntry entry) {
        path = sanitize(path);

        if (containsKey(path)) throw new DuplicateRouteException(path);

        return super.put(path, entry);
    }

    private static String sanitize(String path) {
        return (path.startsWith("/")) ? path.substring(1) : path;
    }

    public record NavEntry(ParentRoute parent, Route route) {

        public NavEntry(Route route) {
            this(null, route);
        }

        public boolean hasParent() {
            return parent != null;
        }
    }
}
