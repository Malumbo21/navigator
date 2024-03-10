package com.eden.navigatorfx.update;

import java.io.IOException;
import java.util.Collections;
/*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eden.navigatorfx.update.exceptions.FxmlLoadingException;
import com.eden.navigatorfx.update.exceptions.MissingViewCreatorException;
import com.eden.navigatorfx.update.exceptions.NavigationException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Navigator allows to manage scenes switching on JavaFX Application with an
 * easy API
 * Inspired by Vue JS vue-router
 *
 * @author Malumbo Sinkamba
 * @version 1.0.0
 */

public class Navigator {

    private static Stage stage;
    private static SceneStack sceneStack;
    private static final Map<Route, Function<NavRequest, Parent>> viewCreators = new HashMap<>();
    private static Object baseReference;
    private static String applicationName;
    private Navigator() {
    } // Prevent instantiation

    public static void configure(Object baseReference, String applicationName, Stage primaryStage, StageStyle stageStyle,
            int width, int height) {
        stage = primaryStage;
        Navigator.applicationName = applicationName;
        stage.setTitle(applicationName);
        stage.setScene(null);
        stageStyle = stageStyle != null ? stageStyle : StageStyle.DECORATED;
        stage.initStyle(stageStyle);
        stage.setWidth(width > 0 ? width : 1024);
        stage.setHeight(height > 0 ? height : 768);
        sceneStack = new SceneStack();
    }

    public static void when(Route route, Function<NavRequest, Parent> viewCreator) {
        viewCreators.put(route, viewCreator);
    }

    public static void when(String url, String name, Function<NavRequest, Parent> viewCreator) {
        when(route(url, name), viewCreator);
    }
    public static void when(String url, Function<NavRequest, Parent> viewCreator) {
        when(route(url, ""), viewCreator);
    }

    public static void when(String url, String name, int width, int height, Function<NavRequest, Parent> viewCreator) {
        when(route(url, name, width, height), viewCreator);
    }

    public static Route route(String url, String name) {
        return new Route(url, name);
    }

    public static Route route(String url, String name, double width, double height) {
        return new Route(url, name);
    }

    public static Function<NavRequest, Parent> fxml(String path) {
        return request -> {
            try {
                var loader = new FXMLLoader(
                        Objects.requireNonNull(baseReference.getClass()
                                .getResource("/" + path)));
                Parent root = loader.load();
                if (loader.getController() != null) {
                    // Optionally pass NavRequest data to the controller
                    // if (loader.getController() instanceof NavRequestAware controller) {
                    // controller.setNavRequest(request);
                    // }
                }
                return root;
            } catch (IOException e) {
                throw new FxmlLoadingException(path, e);
            } catch (Exception e) { // Handle other potential exceptions
                throw new NavigationException("Navigation failed: " + request.url(), e);
            }
        };
    }

    public static void navigateTo(String url) {
        navigateTo(url, null);
    }

    public static void start(){
        navigateTo("/");
    }
    public static void start(String url){
        navigateTo(url);
    }

    public static void navigateTo(String url, Object data) {
        String normalizedUrl = normalizeUrl(url);
        Map<String, String> pathParams = extractPathParams(normalizedUrl);
        Map<String, String> queryParams = extractQueryParams(normalizedUrl);
        var route = route(normalizedUrl, "");
        Function<NavRequest, Parent> viewCreator = viewCreators.get(route);

        if (viewCreator == null) {
            throw new MissingViewCreatorException(url);
        }

        NavRequest request = new NavRequest(normalizedUrl, queryParams, pathParams, data);
        Parent view = viewCreator.apply(request);
        var scene = new Scene(view,route.width(),route.height());
        stage.setTitle(title(route));
        sceneStack.push(scene);
        stage.setScene(scene);
        if(stage.isShowing()){
            stage.show();
        }
    }

    private static String title(Route currentRoute){
        return applicationName +" / "+currentRoute.name();
    }

    public void navigateToNamed(String name) {
        navigateToNamed(name, null);
    }

    public void navigateToNamed(String name, Object data) {
        if(name.isBlank()){
            throw new IllegalArgumentException("provide a valid name");
        }
        Optional<Route> route = viewCreators.keySet().stream()
                .filter(r -> r.name().equals(name))
                .findFirst();

        if (route.isPresent()) {
            navigateTo(route.get().url(), data);
        } else {
            throw new MissingViewCreatorException(name);
        }
    }

    public static void back() {
        if (sceneStack.canPop()) {
            stage.setScene(sceneStack.pop());
        }
    }

    private static String normalizeUrl(String url) {
        if (url.isBlank()) {
            return "/";
        }
        return url.startsWith("/") ? url : "/" + url;
    }

    private static Map<String, String> extractPathParams(String url) {
        Map<String, String> pathParams = new HashMap<>();
        for (var route : viewCreators.keySet()) {
            var template = route.url;
            if (template.contains(":")) {
                // Replace each colon with a capture group for non-slash characters
                String regexTemplate = template.replaceAll(":", "([^/]+)");
                Pattern pattern = Pattern.compile(regexTemplate);
                Matcher matcher = pattern.matcher(url);
                if (matcher.matches()) {
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        String paramName = template.split(":")[i - 1];
                        String paramValue = matcher.group(i);
                        pathParams.put(paramName, paramValue);
                    }
                    break;
                }
            }
        }
        return pathParams;
    }

    private static Map<String, String> extractQueryParams(String url) {
        Map<String, String> queryParams = new HashMap<>();
        int queryIndex = url.indexOf('?');
        if (queryIndex != -1) {
            String queryString = url.substring(queryIndex + 1);
            for (String param : queryString.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }

    private static record Route(String url, String name, int width, int height) {
        Route(String url, String name) {
            this(url, name, 0, 0);
        }

        public int hashCode() {
            return url.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            else if (obj == null)
                return false;
            else if (obj instanceof String urlOrName)
                return url().equals(urlOrName) || name.equals(urlOrName);
            else if (obj instanceof Route route)
                return url().equals(route.url()) || name.equals(route.name());
            else
                return false;
        }

    }

    public static class NavRequest {

        private final String url;
        private final Map<String, String> queryParams;
        private final Map<String, String> pathParams;
        private final Object data;

        public NavRequest(String url, Map<String, String> queryParams, Map<String, String> pathParams, Object data) {
            this.url = url;
            this.queryParams = queryParams != null ? Collections.unmodifiableMap(queryParams) : Collections.emptyMap();
            this.pathParams = pathParams != null ? Collections.unmodifiableMap(pathParams) : Collections.emptyMap();
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

        public <T> Optional<T> data() {
            if (data == null)
                return Optional.empty();

            return Optional.ofNullable((T) data);
        }
    }
}