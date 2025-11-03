package com.eden.navigatorfx.v2;

// Navigator.java

import com.eden.navigatorfx.v2.layout.NavLayoutFn;
import com.eden.navigatorfx.v2.layout.NavLayout;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Navigator {
  private ResourceLoader resourceLoader;
  private String applicationName;
  private Stage primaryStage;
  private StageStyle stageStyle;
  private double width;
  private double height;
  private String baseFXMLPath;
  private Scene primaryScene;
  private final ReadOnlyObjectWrapper<Route> currentRoute = new ReadOnlyObjectWrapper<>();
  private final Map<String, LoadedLayout> layoutCache = new HashMap<>();
  private LoadedLayout currentLayout;
  private String currentLayoutKey;

  // Route management
  private final Map<String, Route> routes = new ConcurrentHashMap<>();
  private final Deque<NavigationState> navigationStack = new ArrayDeque<>();
  private final BooleanProperty canGoBackProperty = new SimpleBooleanProperty(false);

  // History management
  private int maxHistorySize = Integer.MAX_VALUE;

  // Performance optimizations (Scene caching)
  private final Map<String, Parent> sceneCache = new HashMap<>();
  private boolean cachingEnabled = true;

  // Middleware and route guards
  private final List<Middleware> middlewares = new ArrayList<>();
  private final List<Plugin> registeredPlugins = new ArrayList<>();

  // For nested routes
  private String parentUrlPrefix = "";
  private Route parentRoute = null;
  //main instance
  private static Navigator instance;

  // Private constructor to prevent instantiation
  private Navigator (ResourceLoader resourceLoader, String applicationName, Stage primaryStage,
                     StageStyle stageStyle, double width, double height) {
    this.resourceLoader = resourceLoader;
    this.applicationName = applicationName;
    this.primaryStage = primaryStage;
    this.stageStyle = stageStyle;
    this.width = width;
    this.height = height;
    this.primaryStage.initStyle(this.stageStyle);

    this.primaryStage.setTitle(applicationName);
    this.primaryStage.setWidth(width);
    this.primaryStage.setHeight(height);
  }

  private Navigator () {
  }

  // Constructor for child Navigators (nested routes)
  private Navigator (String parentUrlPrefix, Route parentRoute) {
    this.parentUrlPrefix = parentUrlPrefix;
    this.parentRoute = parentRoute;
  }

  public interface ResourceLoader extends Function<String, URL> {

  }

  // Initialization Methods

  /**
   * Binds the Navigator to the primary stage and initializes application properties.
   */
  public static Navigator bind (ResourceLoader resourceLoader, String applicationName, Stage primaryStage,
                                StageStyle stageStyle, double width, double height) {
    return setInstance(
        new Navigator(resourceLoader, applicationName, primaryStage, stageStyle, width, height)
    );
  }

  public static Navigator bind (Object baseReference, String applicationName, Stage primaryStage,
                                StageStyle stageStyle, double width, double height) {
    return bind(baseReference.getClass(),
                applicationName, primaryStage, stageStyle, width, height
    );
  }

  public static Navigator bind (Class<?> baseClass, String applicationName, Stage primaryStage,
                                StageStyle stageStyle, double width, double height) {
    return bind(baseClass::getResource,
                applicationName, primaryStage, stageStyle, width, height
    );
  }

  /**
   * Sets the base scene using the specified FXML path.
   */
  public Navigator baseScene (String fxmlPath) {
    instance().baseFXMLPath = fxmlPath;
    Platform.runLater(() -> {
      try {
        Parent root = loadFXML(fxmlPath);
        Scene scene = new Scene(root, defaultWidth(), defaultHeight());
        setPrimaryScene(scene);
        getPrimaryStage().setScene(getPrimaryScene());
        getPrimaryStage().show();
        getNavigationStack().push(new NavigationState("/", null));
      } catch (Exception e) {
        throw new NavigationException("Failed to load base scene from FXML: " + fxmlPath, e);
      }
    });
    return this;
  }

  private static Scene getPrimaryScene () {
    return instance().primaryScene;
  }

  private static void setPrimaryScene (Scene scene) {
    instance().primaryScene = scene;
  }

  private static Stage getPrimaryStage () {
    return instance().primaryStage;
  }

  // New Methods for Defining Routes

  /**
   * Adds multiple routes at once.
   */
  public static Navigator defineRoutes (Route... routesArray) {
    for (Route route : routesArray) {
      if (route instanceof RouteLayout layout) {
        addLayoutRoute(layout);
      } else {
        addRoute(route);
      }
    }
    return instance();
  }


  /**
   * Helper method to get an array of routes
   */
  public static Route[] routes (Route... routes) {
    return routes;
  }

  private static synchronized Navigator setInstance (Navigator navigator) {
    instance = navigator;
    return navigator;
  }

  public static synchronized Navigator instance () {
    if (instance == null) {
      throw new IllegalStateException("Navigator instance has not been bound, use Navigator.bind to bind new instance");
    }
    return instance;
  }

  public static Optional<Route> currentRoute () {
    return Optional.ofNullable(instance().currentRoute.get());
  }

  public static ObjectProperty<Route> currentRouteProperty () {
    return instance().currentRoute;
  }

  public static void setCurrentRoute (Route currentRoute) {
    instance().currentRoute.set(currentRoute);
  }

  /**
   * Registers a plugin with the Navigator.
   */
  public static void register (Plugin... plugins) {
    var instance = instance();
    for (Plugin plugin : plugins) {
      registeredPlugins().add(plugin);
      plugin.initialize();
      plugin.registerRoutes(instance);
    }
  }

  /**
   * Unregisters a plugin from the Navigator.
   */
  public static void unregister (Plugin plugin) {
    registeredPlugins().remove(plugin);
    plugin.shutdown();
  }

  private static List<Plugin> registeredPlugins () {
    return instance().registeredPlugins;
  }

  /**
   * Shutdown all registered plugins.
   */
  public static void shutdownPlugins () {
    for (Plugin plugin : registeredPlugins()) {
      plugin.shutdown();
    }
    registeredPlugins().clear();
  }

  /**
   * Creates a Route instance with the specified URL and FXML path.
   */
  public static Route route (String url, String fxmlPath) {
    return route(url, fxmlPath, defaultWidth(), defaultHeight());
  }

  /**
   * Creates a Route instance with the specified URL, FXML path, width and height.
   */
  public static Route route (String url, String fxmlPath, double width, double height) {
    return new Route(fxmlPath, url, width, height);
  }

  /**
   * Creates a Route instance with the specified URL and view creator function.
   */
  public static Route route (String url, Function<NavRequest, Parent> viewCreator) {
    return route(url, viewCreator, defaultWidth(), defaultHeight());
  }

  public static RouteLayout layout (String layoutPath) {
    return new RouteLayout(layoutPath);
  }

  public static RouteLayout layout (String layoutPath, Route child, Route... children) {
    return layout(layoutPath).children(child, children);
  }

  public static RouteLayout layout (Function<NavRequest, Parent> layoutCreator) {
    return new RouteLayout(layoutCreator);
  }

  public static RouteLayout layout (Parent layout, ContentConsumer contentConsumer) {
    return new RouteLayout(layout, contentConsumer);
  }

  public static RouteLayout layout (Function<NavRequest,Parent> layoutCreator, ContentConsumer contentConsumer) {
    return new RouteLayout(layoutCreator, contentConsumer);
  }

  private static double defaultWidth () {
    return instance().width;
  }

  private static double defaultHeight () {
    return instance().height;
  }

  /**
   * Creates a Route instance with the specified URL and view creator function.
   */
  public static Route route (String url, Function<NavRequest, Parent> viewCreator, double width, double height) {
    return new Route(viewCreator, url, width, height);
  }

  /**
   * Creates a Route instance for nested routes.
   */
  public static Route route (String parentUrl, String parentFxmlPath, Consumer<Navigator> childRoutes) {
    Route parentRoute = new Route(parentFxmlPath, parentUrl);
    Navigator childNavigator = new Navigator(parentUrl, parentRoute);
    childRoutes.accept(childNavigator);
    return parentRoute;
  }

  /**
   * Creates a Route instance for nested routes with a view creator.
   */
  public static Route route (String parentUrl, Function<NavRequest, Parent> parentViewCreator, Consumer<Navigator> childRoutes) {
    Route parentRoute = new Route(parentViewCreator, parentUrl);
    Navigator childNavigator = new Navigator(parentUrl, parentRoute);
    childRoutes.accept(childNavigator);
    return parentRoute;
  }

  // Navigation Methods

  /**
   * Navigates to the specified URL.
   */
  public static NavigationTask navigateTo (String url) {
    return navigateTo(url, null);
  }

  /**
   * Navigates to the specified URL with data.
   */
  public static NavigationTask navigateTo (String url, Object data) {
    NavigationTask task = new NavigationTask(url, data);
    Platform.runLater(task);
    return task;
  }

  /**
   * Navigates to the specified route by name with data.
   */
  public static NavigationTask navigateToNamed (String name, Object data) {
    var route = getRoutes()
        .values()
        .stream()
        .filter(r -> r.getName().equals(name))
        .findAny()
        .orElseThrow(() -> new RouteNotFoundException("Route not found with name : " + name));
    return navigateTo(route.getFullUrl(), data);
  }

  /**
   * Navigates to the specified route by name.
   */
  public static NavigationTask navigateToNamed (String name) {
    return navigateToNamed(name, null);
  }

  /**
   * Navigates back to the previous scene if possible.
   */
  public static void back () {
    if (canGoBack()) {
      getNavigationStack().pop(); // Remove current state
      NavigationState previousState = getNavigationStack().peek();
      if (previousState != null) {
        navigateTo(previousState.url, previousState.data);
      }
    } else {
      throw new NavigationException("Cannot navigate back; no previous navigation state.");
    }
  }

  private static Deque<NavigationState> getNavigationStack () {
    return instance().navigationStack;
  }

  /**
   * Checks if back navigation is possible.
   */
  public static boolean canGoBack () {
    return getNavigationStack().size() > 1;
  }

  // History Management Methods

  /**
   * Sets the maximum size of the navigation history.
   */
  public static void setMaxHistorySize (int size) {
    if (size < 1) {
      throw new IllegalArgumentException("History size must be at least 1");
    }
    instance().maxHistorySize = size;
  }

  /**
   * Clears the navigation history.
   */
  public static void clearHistory () {
    getNavigationStack().clear();
  }

  // Scene Caching Methods

  /**
   * Enables or disables scene caching.
   */
  public static void setCachingEnabled (boolean enabled) {
    instance().cachingEnabled = enabled;
  }

  /**
   * Clears the scene cache.
   */
  public static void clearCache () {
    instance().sceneCache.clear();
  }

  // Middleware Methods

  /**
   * Adds a middleware function to be executed before navigation.
   */
  public Navigator use (Middleware... middlewares) {
    for(var middleware : middlewares){
      getMiddlewares().add(middleware);
    }
    return this;
  }

  public static void middleware (Middleware... middlewares){
    instance().use(middlewares);
  }

  private static List<Middleware> getMiddlewares () {
    return instance().middlewares;
  }

  // Helper Methods

  private static void addRoute (Route route) {
    String[] segments = route.fullUrl.split("/");
    if (segments.length == 0) {
      getRoutes().put("", route);
      return;
    }
    Map<String, Route> currentRoutes = getRoutes();
    for (int i = 1; i < segments.length; i++) {
      String segment = segments[i];
      if (segment.isEmpty()) continue;
      Route existingRoute = currentRoutes.get(segment);
      if (existingRoute == null) {
        if (i == segments.length - 1) {
          // Last segment, add the route
          currentRoutes.put(segment, route);
          existingRoute = route;
        } else {
          // Intermediate segment, create a placeholder route
          existingRoute = new Route((String) null, "/" + String.join("/", Arrays.copyOfRange(segments, 1, i + 1)));
          currentRoutes.put(segment, existingRoute);
        }
      }
      route.parentRoute = existingRoute;
      currentRoutes = existingRoute.childRoutes;
    }
  }

  private static void addLayoutRoute (RouteLayout layout) {
    for (var child : layout.children()) {
      if (layout.getLayoutPath() != null) {
        child.setLayoutPath(layout.getLayoutPath());
      } else if (layout.getLayoutCreator() != null) {
        child.setLayoutCreator(layout.getLayoutCreator());
      }
      addRoute(child);
    }
  }

  private static Map<String, Route> getRoutes () {
    return instance().routes;
  }

  private static NavRequest parseUrl (String url, Object data) {
    String[] parts = url.split("\\?");
    String path = parts[0];
    Map<String, String> queryParams = new HashMap<>();
    if (parts.length > 1) {
      String query = parts[1];
      Arrays.stream(query.split("&")).forEach(param -> {
        String[] kv = param.split("=");
        if (kv.length > 1) {
          queryParams.put(kv[0], kv[1]);
        } else if (kv.length == 1) {
          queryParams.put(kv[0], "");
        }
      });
    }
    return new NavRequest(path, queryParams, new HashMap<>(), data);
  }


  private static Route matchRoute (String url) {
    String[] segments = url.split("/");
    if (segments.length == 0) return null;
    return matchRouteRecursive(getRoutes(), segments, 1);
  }

  private static Route matchRouteRecursive (Map<String, Route> currentRoutes, String[] segments, int index) {
    if (index >= segments.length) return null;
    String segment = segments[index];
    if (segment.isEmpty()) {
      // Skip empty segments
      return matchRouteRecursive(currentRoutes, segments, index + 1);
    }
    Route route = currentRoutes.get(segment);
    if (route == null) return null;
    if (index == segments.length - 1) return route;
    return matchRouteRecursive(route.childRoutes, segments, index + 1);
  }

  private static void injectNavRequest (Object controller, NavRequest navRequest) {
    if (controller == null) return;
    if (controller instanceof BaseController) {
      ((BaseController) controller).setRequest(navRequest);
    } else {
      try {
        Method setRequestMethod = controller.getClass().getMethod("setRequest", NavRequest.class);
        setRequestMethod.invoke(controller, navRequest);
      } catch (NoSuchMethodException e) {
        // Controller does not have setRequest method
        // Optionally log a warning
      } catch (Exception e) {
        throw new NavigationException("Failed to inject NavRequest into controller: " + controller.getClass().getName(), e);
      }
    }
  }

  private static Parent loadFXML (String fxmlPath) {
    try {
      URL resource = getResource(fxmlPath);
      if (resource == null) {
        throw new ResourceNotFoundException("FXML file not found: " + fxmlPath);
      }
      return FXMLLoader.load(resource);
    } catch (Exception e) {
      throw new NavigationException("Failed to load FXML file: " + fxmlPath, e);
    }
  }

  private static URL getResource (String path) {
    URL resource = null;
    ResourceLoader resourceLoader = instance().resourceLoader;
    // First, try using the baseReference's class loader
    if (resourceLoader != null) {
      resource = resourceLoader.apply(path);
    }
    // If not found, try using the context class loader
    if (resource == null) {
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      resource = contextClassLoader.getResource(path.startsWith("/") ? path.substring(1) : path);
    }
    // If still not found, try using the Navigator's class loader
    if (resource == null) {
      resource = Navigator.class.getResource(path);
    }
    return resource;
  }

  private static LoadedLayout getLayout (Route route, String layoutKey, NavRequest navRequest) throws IOException {
    var layoutCache = instance().layoutCache;
    if (layoutCache.containsKey(layoutKey)) {
      return layoutCache.get(layoutKey);
    } else {
      Parent layoutRoot;
      NavLayout layoutController = null;

      // Load the layout
      if (route.getLayoutRoot() != null) {
        // Use the provided layout root
        layoutRoot = route.getLayoutRoot();
        layoutController = new NavLayoutFn(layoutRoot, route.getSetContentFunction());
      } else if (route.getLayoutPath() != null) {
        FXMLLoader layoutLoader = new FXMLLoader(getResource(route.getLayoutPath()));
        layoutRoot = layoutLoader.load();
        layoutController = layoutLoader.getController();
      } else if (route.getLayoutCreator() != null) {
        layoutRoot = route.getLayoutCreator().apply(navRequest);
        var userdata = layoutRoot.getUserData();
        if (layoutRoot instanceof NavLayout layout) {
          layoutController = layout;
        } else if (userdata != null
            && userdata instanceof NavLayout layout) {
          layoutController = layout;
        }
        if (layoutController == null) {
          throw new NavigationException("Layout controller not found for Java layout");
        }
        // Assume layoutCreator provides both root and controller
      } else {
        throw new NavigationException("No layout information available");
      }

      LoadedLayout loadedLayout = new LoadedLayout(layoutRoot, layoutController);
      layoutCache.put(layoutKey, loadedLayout);
      return loadedLayout;
    }
  }


  // Inner Classes and Interfaces

  // NavigationTask Class
  public static class NavigationTask implements Runnable {
    private final String url;
    private final Object data;
    private Map<Class<? extends NavigationException>, Consumer<? super NavigationException>> errorHandlers;
    private TransitionType transitionType = TransitionType.NONE;
    private NavigationException navigationException;

    public NavigationTask (String url, Object data) {
      this.url = url;
      this.data = data;
      this.errorHandlers = new HashMap<>();
    }

    public NavigationTask onError (Class<? extends NavigationException> exceptionClass, Consumer<? super NavigationException> handler) {
      if (exceptionClass == null || handler == null) {
        throw new IllegalArgumentException("Exception class and handler must not be null");
      }
      // Register the handler
      this.errorHandlers.put(exceptionClass, handler);
      // If an exception has already occurred, and matches, invoke the handler immediately
      if (navigationException != null && exceptionClass.isAssignableFrom(navigationException.getClass())) {
        handler.accept(navigationException);
      }
      return this;
    }

    public NavigationTask onError (Consumer<? super NavigationException> handler) {
      if (handler == null) {
        throw new IllegalArgumentException("Exception class and handler must not be null");
      }
      // Register the handler
      this.errorHandlers.clear();
      this.errorHandlers.put(NavigationException.class, handler);
      // If an exception has already occurred, and matches, invoke the handler immediately
      if (navigationException != null) {
        handler.accept(navigationException);
      }
      return this;
    }

    public NavigationTask withTransition (TransitionType type) {
      this.transitionType = type;
      return this;
    }

    @Override
    public void run() {
      try {
        // Parse URL and data
        NavRequest navRequest = Navigator.parseUrl(url, data);
        // Apply global middleware
        for (Middleware middleware : getMiddlewares()) {
          MiddlewareResponse response = middleware.beforeNavigate(navRequest);
          if (!response.shouldProceed()) {
            checkResponse(response.getAction(), response.getMessage(), response.getConsumer());
            response.getRedirectUrl().ifPresent(redirectUrl -> {
              Navigator.navigateTo(redirectUrl, data);
            });
            return;
          }
        }
        // Match newRoute
        Route currentRoute = currentRoute().orElse(null);
        Route newRoute = Navigator.matchRoute(navRequest.url());
        if (newRoute == null) {
          throw new RouteNotFoundException("Route not found for URL: " + navRequest.url());
        }
        if (currentRoute != null && currentRoute.beforeExit != null) {
          currentRoute.beforeExit.accept(navRequest);
        }

        // Apply newRoute guard if present
        if (newRoute.guard != null) {
          var response = newRoute.guard.allowNavigate(navRequest);
          if (!response.isAllowed()) {
            checkResponse(response.getAction(), response.getMessage(), response.getConsumer());
            var redirectUrl = response.getRedirectUrl().orElse(newRoute.redirectUrl);
            if (redirectUrl != null) {
              Navigator.navigateTo(redirectUrl);
            } else {
              throw new NavigationException("Navigation blocked by newRoute guard for URL: " + navRequest.url());
            }
            return;
          }
        }
        // Invoke beforeEnter on new route
        if (newRoute.beforeEnter != null) {
          newRoute.beforeEnter.accept(navRequest);
        }
        Parent root = null;
        Parent view = loadViewForRoute(newRoute, navRequest);
        // Display the view
        if (newRoute.displayInDialog) {
          // Create a new Stage for the dialog
          Stage dialogStage = new Stage();
          dialogStage.initModality(newRoute.modality);
          dialogStage.setTitle(newRoute.stageName != null ? newRoute.stageName : getApplicationName());
          dialogStage.setScene(new Scene(view, newRoute.width > 0 ? newRoute.width : defaultWidth(), newRoute.height > 0 ? newRoute.height : defaultHeight()));
          dialogStage.initOwner(getPrimaryStage());
          dialogStage.show();
        } else {
          //Layout setup
          String layoutKey = null;
          if (newRoute.getLayoutPath() != null) {
            layoutKey = newRoute.getLayoutPath();
          } else if (newRoute.getLayoutCreator() != null) {
            layoutKey = newRoute.getLayoutCreator().toString();
          }
          if (layoutKey != null) {
            if (instance().currentLayoutKey != null
                && layoutKey.equalsIgnoreCase(instance().currentLayoutKey)) {
              LoadedLayout loadedLayout = instance().currentLayout;
              if (loadedLayout.controller instanceof NavLayout controller) {
                controller.setContent(view);
              } else {
                throw new NavigationException("Layout controller must implement LayoutController");
              }
              root = loadedLayout.root;
            } else {
              LoadedLayout loadedLayout = getLayout(newRoute, layoutKey, navRequest);
              if (loadedLayout.controller instanceof NavLayout controller) {
                controller.setContent(view);
              } else {
                throw new NavigationException("Layout controller must implement LayoutController");
              }
              instance().currentLayoutKey = layoutKey;
              instance().currentLayout = loadedLayout;
              root = loadedLayout.root;
            }
          } else {
            root = view;
            instance().currentLayoutKey = null;
            instance().currentLayout = null;
          }

          // Set the view in the primary scene
          getPrimaryScene().setRoot(root);
          Stage stage = getPrimaryStage();
          stage.setTitle(newRoute.stageName != null ? newRoute.stageName : getApplicationName());
          if (!(stage.isMaximized() || stage.isFullScreen())) {
            stage.setWidth(newRoute.width > 0 ? newRoute.width : defaultWidth());
            stage.setHeight(newRoute.height > 0 ? newRoute.height : defaultHeight());
            stage.centerOnScreen();
          }
          // Manage navigation history
          getNavigationStack().push(new NavigationState(url, data));
          while (getNavigationStack().size() > getMaxHistorySize()) {
            getNavigationStack().removeFirst();
          }
        }

        // Invoke afterEnter on new route
        if (newRoute.afterEnter != null) {
          newRoute.afterEnter.accept(navRequest);
        }

        // Invoke afterExit on previous route
        if (currentRoute != null && currentRoute.afterExit != null) {
          currentRoute.afterExit.accept(navRequest);
        }
        setCurrentRoute(newRoute);

        // Apply transition if specified
        applyTransition(root, transitionType);

      } catch (NavigationException e) {
        handleException(e);
      } catch (Exception e) {
        NavigationException ne = new NavigationException("Failed to navigate to URL: " + url, e);
        handleException(ne);
      }
    }

    private void checkResponse (Optional<Runnable> actionOpt, Optional<String> messageOpt, Optional<Consumer<String>> consumerOpt) {
      actionOpt.ifPresent(Runnable::run);
      messageOpt.ifPresent(message -> {
        consumerOpt.ifPresentOrElse(
            messageConsumer -> messageConsumer.accept(message),
            () -> {
              Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
                alert.showAndWait();
              });
            }
        );
      });
    }

    private void handleException (NavigationException exception) {
      this.navigationException = exception;
      boolean handled = false;
      for (Map.Entry<Class<? extends NavigationException>, Consumer<? super NavigationException>> entry : errorHandlers.entrySet()) {
        if (entry.getKey().isAssignableFrom(exception.getClass())) {
          entry.getValue().accept(exception);
          handled = true;
          // Do not break here if you want to allow multiple handlers
          // For now, we'll break to prevent multiple invocations
          break;
        }
      }
      if (!handled) {
        // If no handlers are registered for this exception, log it or re-throw
        exception.printStackTrace();
        // Optionally re-throw the exception
        // throw exception;
      }
    }

    private void applyTransition (Parent newView, TransitionType type) {
      switch (type) {
        case FADE:
          FadeTransition ft = new FadeTransition(Duration.millis(500), newView);
          ft.setFromValue(0);
          ft.setToValue(1);
          ft.play();
          break;
        case SLIDE_LEFT:
          TranslateTransition tt = new TranslateTransition(Duration.millis(300), newView);
          tt.setFromX(getPrimaryScene().getWidth());
          tt.setToX(0);
          tt.play();
          break;
        case SLIDE_RIGHT:
          TranslateTransition tr = new TranslateTransition(Duration.millis(300), newView);
          tr.setFromX(-getPrimaryScene().getWidth());
          tr.setToX(0);
          tr.play();
          break;
        case NONE:
        default:
          // No transition
          break;
      }
    }
  }

  private static Parent loadViewForRoute (Route newRoute, NavRequest navRequest) throws IOException {
    Parent view;
    String cacheKey = newRoute.getFullUrl();

    // Use cached scene if available
    if (!newRoute.displayInDialog && isCachingEnabled() && getSceneCache().containsKey(cacheKey)) {
      view = getSceneCache().get(cacheKey);
    } else {
      // Load the scene
      if (newRoute.fxmlPath != null) {
        FXMLLoader loader = new FXMLLoader(getResource(newRoute.fxmlPath));
        view = loader.load();
        Object controller = loader.getController();
        injectNavRequest(controller, navRequest);
      } else if (newRoute.viewCreator != null) {
        view = newRoute.viewCreator.apply(navRequest);
      } else {
        throw new NavigationException("No view found for URL: " + navRequest.url());
      }

      if (!newRoute.styles().isEmpty()) {
        view.getStylesheets().addAll(newRoute.styles());
      }

      // Cache the scene
      if (!newRoute.displayInDialog && isCachingEnabled()) {
        getSceneCache().put(cacheKey, view);
      }
    }
    return view;
  }

  private static String getApplicationName () {
    return instance().applicationName;
  }

  private static int getMaxHistorySize () {
    return instance().maxHistorySize;
  }

  private static boolean isCachingEnabled () {
    return instance().cachingEnabled;
  }

  private static Map<String, Parent> getSceneCache () {
    return instance().sceneCache;
  }

  // Route Class
  public static class Route {
    String fxmlPath;
    String name;
    String stageName;
    double width;
    double height;
    Function<NavRequest, Parent> viewCreator;
    RouteGuard guard;
    String redirectUrl;
    String fullUrl;
    boolean displayInDialog = false;
    Modality modality = Modality.NONE;
    Route parentRoute = null;
    private String layoutPath;
    private Function<NavRequest, Parent> layoutCreator;
    private Parent layoutRoot;
    private BiConsumer<Parent, Parent> setContentFunction;
    private final Map<String, Route> childRoutes = new HashMap<>();
    private final Map<String, Object> metadata = new HashMap<>();
    private final List<String> styleSheets = new ArrayList<>();
    private Consumer<NavRequest> beforeEnter;
    private Consumer<NavRequest> afterEnter;
    private Consumer<NavRequest> beforeExit;
    private Consumer<NavRequest> afterExit;

    // Constructors
    Route (String fxmlPath, String fullUrl) {
      this.fxmlPath = fxmlPath;
      this.fullUrl = fullUrl;
    }

    Route (String fxmlPath, String fullUrl, double width, double height) {
      this.fxmlPath = fxmlPath;
      this.fullUrl = fullUrl;
      this.width = width;
      this.height = height;
    }

    Route (Function<NavRequest, Parent> viewCreator, String fullUrl) {
      this.viewCreator = viewCreator;
      this.fullUrl = fullUrl;
    }

    Route (Function<NavRequest, Parent> viewCreator, String fullUrl, double width, double height) {
      this.viewCreator = viewCreator;
      this.fullUrl = fullUrl;
      this.width = width;
      this.height = height;
    }

    public Route withName (String name) {
      this.name = name;
      return this;
    }

    public String getName () {
      return name;
    }

    public void setLayoutPath (String layoutPath) {
      this.layoutPath = layoutPath;
    }

    public String getLayoutPath () {
      return layoutPath;
    }

    public void setLayoutCreator (Function<NavRequest, Parent> layoutCreator) {
      this.layoutCreator = layoutCreator;
    }

    public Parent getLayoutRoot () {
      return layoutRoot;
    }

    public BiConsumer<Parent, Parent> getSetContentFunction () {
      return setContentFunction;
    }

    public void setLayoutRoot (Parent layoutRoot) {
      this.layoutRoot = layoutRoot;
    }

    public void setSetContentFunction (BiConsumer<Parent, Parent> setContentFunction) {
      this.setContentFunction = setContentFunction;
    }

    public Function<NavRequest, Parent> getLayoutCreator () {
      return layoutCreator;
    }

    // Method to associate a layout with the route
    public Route withLayout (String layoutPath) {
      this.layoutPath = layoutPath;
      return this;
    }

    public Route withLayout (Function<NavRequest, Parent> layoutCreator) {
      this.layoutCreator = layoutCreator;
      return this;
    }

    /**
     * add stylesheet to this route.
     */
    public Route withStylesheet (String... stylesheets) {
      this.styleSheets.addAll(Arrays.asList(stylesheets));
      return this;
    }

    /**
     * Sets a metadata for this route.
     */
    public Route meta (String key, Object value) {
      this.metadata.put(key, value);
      return this;
    }

    /**
     * gets meta-data for this route.
     */
    public Map<String, Object> meta () {
      return metadata;
    }

    /**
     * gets meta-data by key for this route.
     */
    public <T> Optional<T> meta (String key) {
      var value = metadata.get(key);
      if (value == null) {
        return Optional.empty();
      }
      return Optional.of((T) value);
    }

    /**
     * Sets a guard for this route.
     */
    public Route withGuard (RouteGuard guard) {
      this.guard = guard;
      return this;
    }

    /**
     * Sets a guard for this route.
     */
    public Route withGuardPredicate (Predicate<NavRequest> guard) {
      this.guard = request -> {
        if (guard.test(request)) {
          return RouteGuardResponse.allow();
        } else {
          return RouteGuardResponse.block();
        }
      };
      return this;
    }

    /**
     * Sets a redirect URL to be used when the guard blocks navigation.
     */
    public Route redirectTo (String url) {
      this.redirectUrl = url;
      return this;
    }

    /**
     * Configures the route to be displayed in a dialog.
     */
    public Route asDialog (boolean modal) {
      this.displayInDialog = true;
      this.modality = modal ? Modality.APPLICATION_MODAL : Modality.NONE;
      return this;
    }

    public Route asDialog () {
      return asDialog(true);
    }

    /**
     * Sets custom stage properties for this route.
     */
    public Route withStage (String stageName, double width, double height) {
      this.stageName = stageName;
      this.width = width;
      this.height = height;
      return this;
    }

    // Getter for fullUrl
    public String getFullUrl () {
      return fullUrl;
    }

    public List<String> styles () {
      return styleSheets;
    }

    public Route beforeEnter (Consumer<NavRequest> beforeEnter) {
      this.beforeEnter = beforeEnter;
      return this;
    }

    public Route afterEnter (Consumer<NavRequest> afterEnter) {
      this.afterEnter = afterEnter;
      return this;
    }

    public Route beforeExit (Consumer<NavRequest> beforeExit) {
      this.beforeExit = beforeExit;
      return this;
    }

    public Route afterExit (Consumer<NavRequest> afterExit) {
      this.afterExit = afterExit;
      return this;
    }
  }

  public static class RouteLayout extends Route {
    private final List<Route> children = new ArrayList<>();

    public RouteLayout (Parent layoutRoot, ContentConsumer contentConsumer) {
      super((String) null, null); // No URL or FXML path for the layout itself
      setLayoutRoot(Objects.requireNonNull(layoutRoot, "Layout component can't be null"));
      setSetContentFunction(Objects.requireNonNull(contentConsumer, "Layout content consumer can't be null"));
    }

    public RouteLayout (Function<NavRequest,Parent> layoutSupplier, ContentConsumer contentConsumer) {
      super((String) null, null); // No URL or FXML path for the layout itself
      this.setLayoutCreator(Objects.requireNonNull(layoutSupplier, "Layout Creator can't be null"));
      setSetContentFunction(Objects.requireNonNull(contentConsumer, "Layout content consumer can't be null"));
    }

    public RouteLayout (String layoutPath) {
      super((String) null, null); // No URL or FXML path for the layout itself
      this.setLayoutPath(Objects.requireNonNull(layoutPath, "Layout path can't be null"));
    }

    public RouteLayout (Function<NavRequest, Parent> layoutCreator) {
      super((String) null, null); // No URL or FXML path for the layout itself
      this.setLayoutCreator(Objects.requireNonNull(layoutCreator, "Layout Creator can't be null"));
    }

    public RouteLayout children (Route route, Route... routes) {
      this.children.add(route);
      this.children.addAll(List.of(routes));
      return this;
    }

    public List<Route> children () {
      return children;
    }

  }

  public static class LoadedLayout {
    Parent root;
    NavLayout controller;

    public LoadedLayout (Parent root, NavLayout controller) {
      this.root = root;
      this.controller = controller;
    }
  }


  // NavigationState Class
  private static class NavigationState {
    String url;
    Object data;

    NavigationState (String url, Object data) {
      this.url = url;
      this.data = data;
    }
  }

  // TransitionType Enum
  public enum TransitionType {
    NONE,
    FADE,
    SLIDE_LEFT,
    SLIDE_RIGHT
  }

  // Middleware Interfaces
  @FunctionalInterface
  public interface Middleware {
    MiddlewareResponse beforeNavigate (NavRequest request);

    default Middleware andThen (Middleware after) {
      return (NavRequest request) -> {
        var response = beforeNavigate(request);
        if (!response.shouldProceed()) {
          return response;
        }
        return after.beforeNavigate(request);
      };
    }

    default Middleware compose (Middleware before) {
      return (NavRequest request) -> {
        var response = before.beforeNavigate(request);
        if (!response.shouldProceed()) {
          return response;
        }
        return beforeNavigate(request);
      };
    }
  }

  public interface ContentConsumer extends BiConsumer<Parent,Parent>{
    void accept(Parent layout,Parent content);
  }

  @FunctionalInterface
  public interface RouteGuard {
    RouteGuardResponse allowNavigate (NavRequest request);
  }

  public static class RouteGuardResponse {
    private final boolean allowNavigate;
    private final Optional<String> redirectUrl;
    private final Optional<String> message;
    private final Optional<Runnable> action;
    private final Optional<Consumer<String>> consumer;

    private RouteGuardResponse (boolean allowNavigate, String redirectUrl, String message, Runnable action, Consumer<String> consumer) {
      this.allowNavigate = allowNavigate;
      this.redirectUrl = Optional.ofNullable(redirectUrl);
      this.message = Optional.ofNullable(message);
      this.action = Optional.ofNullable(action);
      this.consumer = Optional.ofNullable(consumer);
    }

    public static RouteGuardResponse allow () {
      return new RouteGuardResponse(true, null, null, null, null);
    }

    public static RouteGuardResponse block (String message, Runnable action) {
      return new RouteGuardResponse(false, null, message, action, null);
    }

    public static RouteGuardResponse block (String message) {
      return new RouteGuardResponse(false, null, message, null, null);
    }

    public static RouteGuardResponse block () {
      return new RouteGuardResponse(false, null, null, null, null);
    }

    public static RouteGuardResponse block (String message, Consumer<String> action) {
      return new RouteGuardResponse(false, null, message, null, action);
    }

    public static RouteGuardResponse redirect (String redirectUrl, String message, Runnable action) {
      return new RouteGuardResponse(false, redirectUrl, message, action, null);
    }

    public static RouteGuardResponse redirect (String redirectUrl, Runnable action) {
      return new RouteGuardResponse(false, redirectUrl, null, action, null);
    }

    public static RouteGuardResponse redirect (String redirectUrl) {
      return new RouteGuardResponse(false, redirectUrl, null, null, null);
    }

    public boolean isAllowed () {
      return allowNavigate;
    }

    public Optional<String> getRedirectUrl () {
      return redirectUrl;
    }

    public Optional<String> getMessage () {
      return message;
    }

    public Optional<Runnable> getAction () {
      return action;
    }

    public Optional<Consumer<String>> getConsumer () {
      return consumer;
    }
  }


  // MiddlewareResponse Class
  public static class MiddlewareResponse {
    private final boolean proceed;
    private final Optional<String> redirectUrl;
    private final Optional<String> message;
    private final Optional<Runnable> action;
    private final Optional<Consumer<String>> consumer;

    private MiddlewareResponse (boolean proceed, String redirectUrl, String message, Runnable action, Consumer<String> consumer) {
      this.proceed = proceed;
      this.redirectUrl = Optional.ofNullable(redirectUrl);
      this.message = Optional.ofNullable(message);
      this.action = Optional.ofNullable(action);
      this.consumer = Optional.ofNullable(consumer);
    }

    public static MiddlewareResponse proceed () {
      return new MiddlewareResponse(true, null, null, null, null);
    }

    public static MiddlewareResponse block (String message, Runnable action) {
      return new MiddlewareResponse(false, null, message, action, null);
    }

    public static MiddlewareResponse block (String message, Consumer<String> action) {
      return new MiddlewareResponse(false, null, message, null, action);
    }

    public static MiddlewareResponse redirect (String redirectUrl, String message, Runnable action) {
      return new MiddlewareResponse(false, redirectUrl, message, action, null);
    }

    public static MiddlewareResponse redirect (String redirectUrl, Runnable action) {
      return new MiddlewareResponse(false, redirectUrl, null, action, null);
    }

    public static MiddlewareResponse redirect (String redirectUrl) {
      return new MiddlewareResponse(false, redirectUrl, null, null, null);
    }

    public static MiddlewareResponse redirect (String redirectUrl, String message, Consumer<String> action) {
      return new MiddlewareResponse(false, redirectUrl, message, null, action);
    }

    public static MiddlewareResponse redirect (String redirectUrl, String message) {
      return new MiddlewareResponse(false, redirectUrl, message, null, null);
    }

    public boolean shouldProceed () {
      return proceed;
    }

    public boolean shouldNotProceed () {
      return !shouldProceed();
    }

    public Optional<String> getRedirectUrl () {
      return redirectUrl;
    }

    public Optional<String> getMessage () {
      return message;
    }

    public Optional<Runnable> getAction () {
      return action;
    }

    public Optional<Consumer<String>> getConsumer () {
      return consumer;
    }
  }


  // Custom Exceptions

  public static class NavigationException extends RuntimeException {
    public NavigationException (String message) {
      super(message);
    }

    public NavigationException (String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class RouteNotFoundException extends NavigationException {
    public RouteNotFoundException (String message) {
      super(message);
    }
  }

  public static class ResourceNotFoundException extends NavigationException {
    public ResourceNotFoundException (String message) {
      super(message);
    }
  }
}
