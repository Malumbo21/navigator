package com.eden.navigatorfx;

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

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import atlantafx.base.controls.ModalPane;
import com.eden.navigatorfx.routing.utils.NavigationTransition;
import com.eden.navigatorfx.exceptions.NavigationFailedException;
import com.eden.navigatorfx.routing.ParentRoute;
import com.eden.navigatorfx.routing.Route;
import com.eden.navigatorfx.routing.internals.NavMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import com.eden.navigatorfx.routing.internals.NavMap.*;
import org.jetbrains.annotations.NotNull;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static javafx.application.Platform.runLater;


/**
 * Navigator allows to manage scenes switching on JavaFX Application with an easy API
 * Inspired by Angular JS $routeProvider
 *
 * @author Malumbo Sinkamba
 * @version 1.0.0
 */
public final class Navigator {
    public static final String WINDOW_TITLE = "";
    public static final Double WINDOW_WIDTH = 800.0;
    public static final Double WINDOW_HEIGHT = 600.0;
    private static final Double FADE_ANIMATION_DURATION = 800.0;

    // Navigator Singleton
    private static Navigator router;
    // Navigator Main Class reference to get main package
    private static Object mainRef;
    // Navigator Application Stage reference to set scenes
    private static Stage window;

    // Application Stage title
    public static String windowTitle;
    // Application Stage size
    public static Double windowWidth;
    public static Double windowHeight;

    // routes switching animation
    private static NavigationTransition animationType = NavigationTransition.NONE;
    private static Double animationDuration;

    private static boolean lazyLoadRoutes = false;
    private static Supplier<Image> loadingImageSupplier;
    private static Image loadingImage;


    // Navigator routes map
    private static final NavMap routes = new NavMap();
    // Navigator current route
    private static Route currentRoute;

    /**
     * Navigator constructor kept private to apply Singleton pattern
     */
    private Navigator() {
    }

    public static void bind(Object ref, Stage win) {
        checkInstances(ref, win);
    }

    public static void bind(Object ref, Stage win, String winTitle) {
        checkInstances(ref, win);
        windowTitle = winTitle;
    }

    public static void bind(Object ref, Stage win, double winWidth, double winHeight) {
        checkInstances(ref, win);
        windowWidth = winWidth;
        windowHeight = winHeight;
    }

    /**
     * Navigator binder with Application Stage and main package
     *
     * @param ref:       Main Class reference
     * @param win:       Application Stage
     * @param winTitle:  Application Stage title
     * @param winWidth:  Application Stage width
     * @param winHeight: Application Stage height
     */
    public static void bind(Object ref, Stage win, String winTitle, double winWidth, double winHeight) {
        checkInstances(ref, win);
        windowTitle = winTitle;
        windowWidth = winWidth;
        windowHeight = winHeight;
    }

    /**
     * set Navigator references only if they are not set yet
     *
     * @param ref: Main Class reference
     * @param win: Application Stage
     */
    private static void checkInstances(Object ref, Stage win) {
        if (mainRef == null)
            mainRef = ref;
        if (router == null)
            router = new Navigator();
        if (window == null)
            window = win;
    }

    public static void when(String routeLabel, String scenePath) {
        Route route = new Route(scenePath);
        registerRoute(routeLabel, route);
    }

    public static void when(String routeLabel, String scenePath, String winTitle) {
        Route route = new Route(scenePath, winTitle);
        registerRoute(routeLabel, route);
    }

    public static void when(String routeLabel, String scenePath, double sceneWidth, double sceneHeight) {
        Route route = new Route(scenePath, sceneWidth, sceneHeight);
        registerRoute(routeLabel, route);
    }

    /**
     * Define a Navigator route
     *
     * @param routeLabel:  Route label identifier
     * @param scenePath:   .FXML scene file
     * @param winTitle:    Scene (Stage) title
     * @param sceneWidth:  Scene Width
     * @param sceneHeight: Scene Height
     */
    public static void when(String routeLabel, String scenePath, String winTitle, double sceneWidth, double sceneHeight) {
        Route route = new Route(scenePath, winTitle, sceneWidth, sceneHeight);
        registerRoute(routeLabel, route);
    }

    private static void registerRoute(String routeLabel, Route route) {
        if (route instanceof ParentRoute parent) {
            routes.put(routeLabel, new NavEntry(parent));

        } else {
            routes.put(routeLabel, new NavEntry(route));
        }
    }

    public static void when(ParentRoute route) {
        registerRoute(route.path(), route);
    }

    public static void navigateTo(String routeLabel) {
        navigateTo(routeLabel, null);
    }

    public static void start(String routeLabel) {
        navigateTo(routeLabel);
    }

    /**
     * Switch between Navigator route and show corresponding scenes
     *
     * @param routeLabel: Route label identifier
     * @param data:       Data passed to route
     */
    public static void navigateTo(String routeLabel, Object data) {
        // get corresponding record
        var record = routes.get(routeLabel);
        // set record data
        loadRoute(data, record, routeLabel);
    }

    static ExecutorService executor = Executors.newFixedThreadPool(2);

    private static void blockWindow(boolean block) {
        var windowScene = window.getScene();
        if (windowScene != null && windowScene.getRoot() != null) {
            windowScene.getRoot().setDisable(block);
        }
    }

    /**
     * Helper method of navigateTo() which load and show new scene
     *
     * @throws IOException: throw FXMLLoader exception if file is not loaded correctly
     */
    private static void loadRoute(Object data, NavEntry entry, String routeLabel) {
        var updateScene = new AtomicBoolean(true);
        setCurrentRouteWithData(data, entry, updateScene);

        boolean update = updateScene.get();
        boolean hasChild = entry.hasParent();

        var parentSubtask = !update ? supplyAsync(() -> window.getScene().getRoot(), executor)
                : loadResource(entry.route().scenePath());

        var childTask = getChildTask(entry);


        parentSubtask.whenComplete((parent, throwable) -> {
            if (parent == null) {
                throw new NavigationFailedException(routeLabel, throwable);
            }
            var route = entry.route();
            // set new route scene
            if (update) {
                Scene scene = new Scene(parent, route.sceneWidth(), route.sceneHeight());
                runLater(() -> window.setScene(scene));
            } else {
                if (window.getScene().getRoot() instanceof BorderPane pane) {
                    try {
                        bindChildToCenter(pane, childTask.join());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            runLater(() -> {
                window.setTitle(Route.getWindowTitle());
                if(!window.isShowing()) window.show();
                routeAnimation(parent);
            });
        }).exceptionally((err)->{
            err.printStackTrace();
            return null;
        });
    }

    private static void bindChildToCenter(BorderPane pane, @NotNull Parent childComponent) {
        ScrollPane parent;
        if (pane.getCenter() == null || !(pane.getCenter() instanceof ScrollPane)) {
            parent = new ScrollPane();
            parent.setMinWidth(Control.USE_COMPUTED_SIZE);
            parent.setMinHeight(Control.USE_COMPUTED_SIZE);
            parent.setPrefWidth(Control.USE_COMPUTED_SIZE);
            parent.setPrefHeight(Control.USE_COMPUTED_SIZE);
            runLater(()->pane.setCenter(parent));
        } else {
            parent = (ScrollPane) pane.getCenter();
            if (parent.getContent() instanceof Pane child) {
                child.prefWidthProperty().unbind();
                child.prefHeightProperty().unbind();
            }
        }
        parent.setStyle("-fx-background-color: green");
        parent.setFitToWidth(true);
        parent.setFitToHeight(true);
        parent.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        parent.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        if (childComponent instanceof Pane child) {
            child.prefWidthProperty().bind(parent.widthProperty());
//            child.prefHeightProperty().bind(parent.heightProperty());
          /*  child.setPrefWidth(Control.USE_COMPUTED_SIZE);
            child.setPrefHeight(Control.USE_COMPUTED_SIZE);*/
        }
        runLater(()->parent.setContent(childComponent));
    }

    private static CompletableFuture<Parent> getChildTask(@NotNull NavEntry entry) {
        if (entry.hasParent()) {
            return loadResource(entry.route().scenePath());
        }
        return null;
    }

    private static void setCurrentRouteWithData(Object data, NavEntry entry, AtomicBoolean updateScene) {
        if (entry.hasParent()) {
            var parent = entry.parent();
            if (currentRoute != null && currentRoute.scenePath().equals(parent.scenePath())) {
                updateScene.set(false);
            } else {
                currentRoute = parent;
            }
        } else {
            currentRoute = entry.route();
        }
        if (data != null) {
            currentRoute.setRequest(data);
        }
    }

    private static CompletableFuture<Parent> loadResource(String path) {
        return supplyAsync(() -> {
            try {
                return FXMLLoader.load(
                        Objects.requireNonNull(mainRef.getClass().getResource("/" + path))
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }


    public static void lazyLoadRoutes(Supplier<Image> imageSupplier) {
        lazyLoadRoutes = true;
        loadingImageSupplier = imageSupplier;
    }

    public static void eagerLoadRoutes() {
        lazyLoadRoutes = false;
        loadingImage = null;
    }

    /**
     * set Navigator switching animation
     *
     * @param anType:     Animation type
     * @param anDuration: Animation duration
     */
    public static void setAnimationType(NavigationTransition anType, double anDuration) {
        animationType = anType;
        animationDuration = anDuration;
    }

    /**
     * Animate routes switching based on animation type
     */
    private static void routeAnimation(Parent node) {
        switch (animationType) {
            case FADE -> {
                Double fd = animationDuration != null ? animationDuration : FADE_ANIMATION_DURATION;
                var ftCurrent = new FadeTransition(Duration.millis(fd), node);
                ftCurrent.setFromValue(0.0);
                ftCurrent.setToValue(1.0);
                ftCurrent.play();
            }
            case NONE -> {
            }
        }
    }

    /**
     * Get current route data
     */
    public static Route.Request currentRequest() {
        return currentRoute.request();
    }


}