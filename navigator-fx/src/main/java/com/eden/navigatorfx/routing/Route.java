package com.eden.navigatorfx.routing;

import com.eden.navigatorfx.Navigator;

import java.util.Map;
import java.util.Optional;

/**
 * Navigator Inner Class used into routes map
 */
public class Route {
    // route .fxml Scene path
    final String scenePath;
    // Scene (Stage) title
    final String windowTitle;
    final double sceneWidth;
    final double sceneHeight;
    // route data passed from goTo()
    private static final Request EMPTY_REQUEST = new Request();
    private Request request = EMPTY_REQUEST;

    public Route(String scenePath) {
        this(scenePath, getWindowTitle(), getWindowWidth(), getWindowHeight());
    }

    public Route(String scenePath, String windowTitle) {
        this(scenePath, windowTitle, getWindowWidth(), getWindowHeight());
    }

    public Route(String scenePath, double sceneWidth, double sceneHeight) {
        this(scenePath, getWindowTitle(), sceneWidth, sceneHeight);
    }

    /**
     * Route scene constructor
     *
     * @param scenePath:   .FXML scene file
     * @param windowTitle: Scene (Stage) title
     * @param sceneWidth:  Scene Width
     * @param sceneHeight: Scene Height
     */
    public Route(String scenePath, String windowTitle, double sceneWidth, double sceneHeight) {
        this.scenePath = scenePath;
        this.windowTitle = windowTitle;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;
    }

    public static String getWindowTitle() {
        return Navigator.windowTitle != null ? Navigator.windowTitle : Navigator.WINDOW_TITLE;
    }

    public static double getWindowWidth() {
        return Navigator.windowWidth != null ? Navigator.windowWidth : Navigator.WINDOW_WIDTH;
    }

    public static double getWindowHeight() {
        return Navigator.windowHeight != null ? Navigator.windowHeight : Navigator.WINDOW_HEIGHT;
    }

    public void setRequest(Object request) {
        this.request = new Request(request);
    }

    public String scenePath() {
        return scenePath;
    }

    public double sceneWidth() {
        return sceneWidth;
    }

    public double sceneHeight() {
        return sceneHeight;
    }

    public Request request() {
        return request;
    }

    public static class Request {
        private Object data;
        private Map<String, String> queryParams;

        private Request(Object data) {
            this.data = data;
        }

        private Request() {
        }

        public <T> Optional<T> data() {
            return data == null
                    ? Optional.empty()
                    : Optional.of((T) data);
        }

        /*public String query(String key) {
            return queryParams != null ? queryParams.get(key) : null;
        }
        public String path(String key) {
            return queryParams != null ? queryParams.get(key) : null;
        }*/
    }
}
