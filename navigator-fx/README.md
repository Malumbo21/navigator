# Navigator Library for JavaFX

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Installation](#installation)
- [Getting Started](#getting-started)
- [Defining Routes](#defining-routes)
- [Navigating Between Scenes](#navigating-between-scenes)
- [Handling Navigation Errors](#handling-navigation-errors)
- [Passing Data and Query Parameters](#passing-data-and-query-parameters)
- [Controller Integration](#controller-integration)
- [Back Navigation](#back-navigation)
- [Nested Routes](#nested-routes)
- [Middleware and Route Guards](#middleware-and-route-guards)
- [Performance Optimizations (Scene Caching)](#performance-optimizations-scene-caching)
- [Animated Transitions](#animated-transitions)
- [Dialog Support](#dialog-support)
- [History Management](#history-management)
- [API Reference](#api-reference)
- [Examples](#examples)
- [Notes and Considerations](#notes-and-considerations)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgements](#acknowledgements)

---

## Introduction

**Navigator** is a powerful JavaFX scene navigation library designed to simplify and enhance scene management in JavaFX applications. It provides an intuitive and flexible API for defining routes, handling scene transitions, passing data, and managing navigation history. With support for layouts, middleware, route guards, lifecycle hooks, animated transitions, plugins, and more, Navigator empowers developers to build sophisticated JavaFX applications with ease.

The library uses a singleton pattern with static methods for easy access throughout your application, and features a first-class layout system that allows you to structure your application with shared layouts and nested routes.

---

## Features

- **Simplified Scene Navigation**: Easily navigate between scenes using route URLs.
- **Declarative Route Definitions**: Define routes with `route()` and `layout()` methods.
- **First-Class Layout System**: Create shared layouts with the `NavLayout` interface and `RouteLayout` class.
- **Layout Caching**: Layouts are cached and reused across child route navigation for optimal performance.
- **Data Passing**: Pass arbitrary data objects between scenes.
- **Query Parameters**: Use query parameters in URLs and access them in scenes.
- **Controller Integration**: Seamlessly inject navigation requests into controllers via `BaseController`.
- **Lifecycle Hooks**: Control navigation flow with `beforeEnter`, `afterEnter`, `beforeExit`, and `afterExit` hooks.
- **Back Navigation**: Support back navigation with navigation history management.
- **Middleware System**: Control navigation flow globally with chainable middleware functions.
- **Route Guards**: Protect routes with guard functions that can allow, block, or redirect.
- **Plugin Architecture**: Modularize your application with the plugin system.
- **Named Routes**: Navigate to routes by name instead of URL.
- **Route Metadata**: Attach arbitrary metadata to routes for custom logic.
- **Route-Specific Stylesheets**: Apply CSS stylesheets per route.
- **Animated Transitions**: Enhance user experience with fade and slide transitions.
- **Dialog Support**: Display routes in modal or non-modal dialogs.
- **Performance Optimizations**: Improve navigation performance with scene and layout caching.
- **History Management**: Manage navigation history size and clear history when needed.
- **Fluent API**: Use method chaining for cleaner and more readable code.
- **Error Handling**: Handle navigation errors with custom error handlers.
- **Support for FXML and Java-based UIs**: Work with both FXML scenes and programmatically created views.
- **Flexible Resource Loading**: Custom resource loading strategy supports any classpath location.
- **SharedState Utility**: Thread-safe state management across routes.

---

## Installation

To use the Navigator library in your JavaFX project:

### **Maven**

Add the Navigator library as a dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.eden</groupId>
    <artifactId>navigator-fx</artifactId>
    <version>2.0.0</version>
</dependency>
```

### **Manual Installation**

1. **Build from Source**:
   ```bash
   mvn clean install
   ```

2. **Add to Your Project**:
   - Add the generated JAR file to your project's build path.

### **Module System**

If using Java modules, add to your `module-info.java`:

```java
module your.module {
    requires navigator.fx;
}
```

---

## Getting Started

### **Initialization**

Begin by initializing the `Navigator` in your main application class. Navigator uses a singleton pattern and must be bound before use:

```java
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Bind Navigator with a resource loader
        Navigator.bind(this, "My Application", primaryStage, StageStyle.DECORATED, 1024, 768)
                 .baseScene("/views/login.fxml");

        // Define your routes
        Navigator.defineRoutes(
            route("/login", "/views/login.fxml"),
            route("/dashboard", "/views/dashboard.fxml")
        );

        // Add global middleware (optional)
        Navigator.middleware(request -> {
            // Your authentication logic
            return Navigator.MiddlewareResponse.proceed();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

- **Bind Parameters**:
  - `this`: Reference to the main application class (used for resource loading).
  - `"My Application"`: The application name or primary stage title.
  - `primaryStage`: The primary `Stage` instance.
  - `StageStyle.DECORATED`: The style of the primary stage.
  - `1024`, `768`: Width and height of the primary stage.

- **Alternative Resource Loaders**:
  ```java
  // Using a custom ResourceLoader
  Navigator.ResourceLoader loader = path -> getClass().getResource(path);
  Navigator.bind(loader, "My Application", primaryStage, StageStyle.DECORATED, 1024, 768);

  // Using a class reference
  Navigator.bind(MainApp.class, "My Application", primaryStage, StageStyle.DECORATED, 1024, 768);
  ```

---

## Defining Routes

Routes are defined using the `route()` and `layout()` static methods. Use `defineRoutes()` to register multiple routes at once.

### **Basic Route Definition**

```java
import static com.eden.navigatorfx.v2.Navigator.*;

// Simple FXML route
route("/login", "/views/login.fxml")

// Route with custom dimensions
route("/signup", "/views/signup.fxml", 800, 600)

// Route with programmatic view creator
route("/dashboard", request -> new DashboardUI())

// Route with method reference
route("/profile", ProfileView::new)
```

### **Route Configuration**

Routes support extensive configuration through method chaining:

```java
route("/admin", "/views/admin.fxml")
    .withName("admin")                              // Named route
    .withGuard(request -> /* guard logic */)        // Route guard
    .redirectTo("/access-denied")                   // Fallback redirect
    .asDialog()                                     // Display as modal dialog
    .withStage("Admin Panel", 1200, 800)            // Custom stage properties
    .withStylesheet("/css/admin.css")               // Route-specific CSS
    .meta("requiresAdmin", true)                    // Metadata
    .beforeEnter(req -> /* logic */)                // Lifecycle hooks
    .afterExit(req -> /* cleanup */)
```

### **Registering Routes**

Use `defineRoutes()` to register multiple routes:

```java
Navigator.defineRoutes(
    route("/login", "/views/login.fxml"),
    route("/dashboard", "/views/dashboard.fxml"),
    route("/settings", "/views/settings.fxml")
        .asDialog(),
    route("/profile", ProfileController::new)
);
```

---

## Layout System

Navigator provides a first-class layout system that allows you to create shared layouts for multiple routes. Layouts are cached and reused for optimal performance.

### **Creating a Layout**

Layouts must implement the `NavLayout` interface:

```java
import com.eden.navigatorfx.v2.layout.NavLayout;

public class MainLayoutController implements NavLayout {
    @FXML private BorderPane contentArea;

    @Override
    public void setContent(Parent content) {
        contentArea.setCenter(content);
    }
}
```

### **Defining Layout Routes**

Use the `layout()` method to define routes with a shared layout:

```java
Navigator.defineRoutes(
    route("/login", "/views/login.fxml"),  // No layout

    layout("/views/MainLayout.fxml")
        .children(
            route("/dashboard", "/views/dashboard.fxml"),
            route("/profile", "/views/profile.fxml"),
            route("/settings", "/views/settings.fxml")
        )
);
```

All child routes (`/dashboard`, `/profile`, `/settings`) will be rendered inside the layout defined by `MainLayout.fxml`.

### **Programmatic Layouts**

You can also create layouts programmatically:

```java
// Using a layout creator function
layout(request -> {
    BorderPane layout = new BorderPane();
    // Configure layout...
    return layout;
}).children(
    route("/dashboard", "/views/dashboard.fxml"),
    route("/profile", "/views/profile.fxml")
);

// Using a Parent and ContentConsumer
BorderPane layoutRoot = new BorderPane();
layout(layoutRoot, (layout, content) -> {
    ((BorderPane) layout).setCenter(content);
}).children(
    route("/dashboard", "/views/dashboard.fxml")
);
```

### **Layout Caching**

- Layouts are automatically cached and reused across child route navigation
- The same layout instance persists when navigating between child routes
- Layout is only recreated when navigating away and back to a different layout group
- This provides excellent performance and maintains layout state

### **How It Works**

1. When navigating to a route with a layout, Navigator loads the layout if not cached
2. The route's view is loaded and passed to the layout's `setContent()` method
3. The layout becomes the scene root
4. When navigating between routes in the same layout, only the content changes
5. The layout controller persists and maintains its state

---

## Navigating Between Scenes

Use the `navigateTo` method to navigate to a route.

```java
Navigator.navigateTo("/login")
         .onError(Navigator.NavigationException.class, error -> {
             // Handle navigation errors
             System.err.println("Navigation Error: " + error.getMessage());
         });
```

- **With Data**:

```java
User user = new User("John Doe");
Navigator.navigateTo("/dashboard", user)
         .onError(Navigator.NavigationException.class, error -> {
             // Handle errors
         });
```

- **With Animated Transitions**:

```java
Navigator.navigateTo("/dashboard")
         .withTransition(Navigator.TransitionType.FADE);
```

- **Navigating to Named Routes**:

```java
route("/admin", "/views/admin.fxml").withName("adminPanel");

// Navigate by name
Navigator.navigateToNamed("adminPanel");
Navigator.navigateToNamed("adminPanel", userData);
```

---

## Handling Navigation Errors

Chain the `onError` method after `navigateTo` to handle exceptions.

```java
Navigator.navigateTo("/nonexistent")
         .onError(Navigator.RouteNotFoundException.class, error -> {
             // Handle route not found error
             System.err.println("Route not found: " + error.getMessage());
             // Redirect to an error page
             Navigator.navigateTo("/error");
         });
```

---

## Passing Data and Query Parameters

### **Passing Data**

Pass data objects during navigation, accessible via `NavRequest`.

```java
List<Integer> data = List.of(1, 2, 3);
Navigator.navigateTo("/data", data);

// In your view creator
route("/data", request -> {
    Optional<List<Integer>> dataList = request.data();
    dataList.ifPresent(list -> {
        // Use the data
        System.out.println("Received: " + list);
    });
    return new DataView();
});

// Or in your controller
public class DataController extends BaseController {
    @Override
    protected void onRequest(NavRequest request) {
        Optional<List<Integer>> dataList = request.data();
        // Use the data
    }
}
```

### **Using Query Parameters**

Include query parameters in URLs and access them in the target scene.

```java
Navigator.navigateTo("/search?query=JavaFX&sort=recent");

// In your view creator
route("/search", request -> {
    String query = request.query("query");
    String sort = request.query("sort");
    // Use query parameters
    return new SearchView(query, sort);
});

// Or in your controller
public class SearchController extends BaseController {
    @Override
    protected void onRequest(NavRequest request) {
        String query = request.query("query");
        String sort = request.query("sort");
        // Perform search...
    }
}
```

---

## Controller Integration

Controllers can receive the `NavRequest` by extending `BaseController`.

### **Creating a Controller**

```java
public class MyController extends BaseController {

    @FXML private TextField someField;

    @Override
    protected void onRequest(NavRequest request) {
        String param = request.query("param");
        someField.setText(param != null ? param : "Default Value");
    }

    @Override
    protected void onInitialize() {
        // Additional initialization if needed
    }
}
```

- **Extend `BaseController`**:
  - Inherit methods for handling `NavRequest`.

- **Override `onRequest`**:
  - Implement logic to handle the navigation request.

- **Override `onInitialize` (Optional)**:
  - Perform additional initialization after FXML fields are injected.

---

## Back Navigation

Use `Navigator.back()` to navigate to the previous scene.

```java
if (Navigator.canGoBack()) {
    Navigator.back();
}
```

- **Check if Back Navigation is Possible**:
  - `Navigator.canGoBack()` returns `true` if there is a previous scene.

- **Clear Navigation History**:

```java
Navigator.clearHistory();
```

---

## Lifecycle Hooks

Routes support four lifecycle hooks that allow you to execute logic at different stages of navigation:

```java
route("/dashboard", "/views/dashboard.fxml")
    .beforeEnter(request -> {
        // Called before entering the route
        System.out.println("About to enter dashboard");
    })
    .afterEnter(request -> {
        // Called after entering the route
        System.out.println("Entered dashboard");
    })
    .beforeExit(request -> {
        // Called before leaving the route
        System.out.println("About to leave dashboard");
    })
    .afterExit(request -> {
        // Called after leaving the route
        System.out.println("Left dashboard");
    });
```

**Execution Order:**
1. Previous route's `beforeExit`
2. Route guard evaluation (if any)
3. New route's `beforeEnter`
4. View loading and scene update
5. New route's `afterEnter`
6. Previous route's `afterExit`

---

## Middleware and Route Guards

Control navigation flow using middleware functions and route guards.

### **Global Middleware**

Middleware executes before every navigation and can control whether navigation proceeds:

```java
Navigator.middleware(request -> {
    if (AuthService.isAuthenticated() || request.url().equals("/login")) {
        return Navigator.MiddlewareResponse.proceed();
    }
    return Navigator.MiddlewareResponse.redirect("/login", "Authentication required");
});
```

**MiddlewareResponse Options:**
- `proceed()`: Allow navigation to continue
- `redirect(url)`: Redirect to a different route
- `redirect(url, message)`: Redirect with a message
- `block(message)`: Block navigation entirely
- `block(message, action)`: Block with custom action

**Chaining Middleware:**
```java
Navigator.middleware(authMiddleware)
         .middleware(loggingMiddleware)
         .middleware(permissionMiddleware);
```

### **Route Guards**

Guards protect specific routes and can allow, block, or redirect:

```java
route("/admin", "/views/admin.fxml")
    .withGuard(request -> {
        if (AuthService.isAdmin()) {
            return RouteGuardResponse.allow();
        }
        return RouteGuardResponse.block("Access Denied: Admin only");
    })
    .redirectTo("/access-denied");
```

**RouteGuardResponse Options:**
- `allow()`: Permit navigation
- `block()`: Deny navigation
- `block(message)`: Deny with a message
- `block(message, action)`: Deny with custom action
- `redirect(url)`: Redirect to alternate route
- `redirect(url, message)`: Redirect with message

**Simplified Guards:**
```java
route("/admin", "/views/admin.fxml")
    .withGuardPredicate(request -> AuthService.isAdmin())
    .redirectTo("/access-denied");
```

---

## Plugin System

Navigator supports a plugin architecture for modularizing your application.

### **Creating a Plugin**

Implement the `Plugin` interface:

```java
import com.eden.navigatorfx.v2.Plugin;
import com.eden.navigatorfx.v2.Navigator;

public class AdminPlugin implements Plugin {
    @Override
    public String id() {
        return "admin-plugin";
    }

    @Override
    public String name() {
        return "Admin Module";
    }

    @Override
    public void registerRoutes(Navigator navigator) {
        navigator.defineRoutes(
            route("/admin", "/views/admin.fxml"),
            route("/admin/users", "/views/users.fxml"),
            route("/admin/logs", "/views/logs.fxml")
        );
    }

    @Override
    public void initialize() {
        // Plugin initialization logic
        System.out.println("Admin plugin initialized");
    }

    @Override
    public void shutdown() {
        // Cleanup logic
        System.out.println("Admin plugin shutdown");
    }
}
```

### **Registering Plugins**

```java
Navigator.register(
    new AdminPlugin(),
    new ReportsPlugin(),
    new AnalyticsPlugin()
);
```

### **Unregistering Plugins**

```java
Navigator.unregister(adminPlugin);
Navigator.shutdownPlugins(); // Shutdown all plugins
```

---

## Route Metadata and Stylesheets

### **Route Metadata**

Attach arbitrary metadata to routes for custom logic:

```java
route("/dashboard", "/views/dashboard.fxml")
    .meta("requiresAuth", true)
    .meta("title", "Dashboard")
    .meta("roles", List.of("admin", "user"));

// Access metadata
Navigator.currentRoute().ifPresent(route -> {
    Optional<Boolean> requiresAuth = route.meta("requiresAuth");
    Optional<String> title = route.meta("title");
    Optional<List<String>> roles = route.meta("roles");
});
```

### **Route-Specific Stylesheets**

Apply CSS stylesheets to specific routes:

```java
route("/dashboard", "/views/dashboard.fxml")
    .withStylesheet("/css/dashboard.css", "/css/charts.css");

route("/admin", "/views/admin.fxml")
    .withStylesheet("/css/admin-theme.css");
```

Stylesheets are automatically applied when navigating to the route.

---

## Performance Optimizations (Scene Caching)

Improve navigation performance by caching scenes.

- **Enable or Disable Caching**:

```java
Navigator.setCachingEnabled(true);
```

- **Clear Scene Cache**:

```java
Navigator.clearCache();
```

- **How it Works**:
  - When caching is enabled, scenes are stored after the first load.
  - Subsequent navigations to the same route use the cached scene.
  - Be cautious with memory usage; clear cache if necessary.

---

## Animated Transitions

Enhance user experience with animated transitions between scenes.

- **Available Transition Types**:
  - `NONE`
  - `FADE`
  - `SLIDE_LEFT`
  - `SLIDE_RIGHT`

- **Using Transitions**:

```java
Navigator.navigateTo("/profile")
         .withTransition(Navigator.TransitionType.SLIDE_LEFT);
```

- **Set Default Transition**:
  - Currently, transitions are specified per navigation.
  - Future enhancements may include setting default transitions.

---

## Dialog Support

Display routes in modal dialogs or new stages.

### **Defining Dialog Routes**

```java
// Modal dialog (blocks interaction with main window)
route("/settings", "/views/settings.fxml")
    .asDialog();

// Non-modal dialog
route("/help", "/views/help.fxml")
    .asDialog(false);

// Dialog with custom size
route("/preferences", "/views/preferences.fxml")
    .asDialog()
    .withStage("Preferences", 600, 400);
```

### **Navigating to Dialogs**

```java
Navigator.navigateTo("/settings");
```

**Dialog Characteristics:**
- Opens in a new `Stage` window
- Can be modal (blocks main window) or non-modal
- Ownership is set to the primary stage
- Dialog routes are not cached by default
- Custom stage properties can be specified

---

## History Management

Manage navigation history to control memory usage and back navigation behavior.

- **Set Maximum History Size**:

```java
Navigator.setMaxHistorySize(10);
```

- **Clear Navigation History**:

```java
Navigator.clearHistory();
```

- **How it Works**:
  - The navigation stack maintains a history of visited routes.
  - When the maximum size is reached, the oldest entries are removed.
  - Clearing history removes all entries, disabling back navigation.

---

## API Reference

### **Navigator Class**

#### **Initialization Methods**
- `bind(ResourceLoader, String appName, Stage primaryStage, StageStyle style, double width, double height)`: Initialize Navigator
- `bind(Object baseRef, String appName, Stage primaryStage, StageStyle style, double width, double height)`: Initialize with object reference
- `bind(Class<?> baseClass, String appName, Stage primaryStage, StageStyle style, double width, double height)`: Initialize with class reference
- `baseScene(String fxmlPath)`: Set the initial scene

#### **Route Definition Methods**
- `route(String url, String fxmlPath)`: Create FXML route
- `route(String url, String fxmlPath, double width, double height)`: Create FXML route with dimensions
- `route(String url, Function<NavRequest, Parent> viewCreator)`: Create route with view creator
- `route(String url, Function<NavRequest, Parent> viewCreator, double width, double height)`: Create route with view creator and dimensions
- `layout(String layoutPath)`: Create layout from FXML
- `layout(Function<NavRequest, Parent> layoutCreator)`: Create layout with creator function
- `layout(Parent layoutRoot, ContentConsumer contentConsumer)`: Create layout with custom content setter
- `defineRoutes(Route... routes)`: Register multiple routes

#### **Navigation Methods**
- `navigateTo(String url)`: Navigate to a route
- `navigateTo(String url, Object data)`: Navigate with data
- `navigateToNamed(String name)`: Navigate by route name
- `navigateToNamed(String name, Object data)`: Navigate by name with data
- `back()`: Navigate to previous route
- `canGoBack()`: Check if back navigation is possible

#### **Middleware and Plugins**
- `middleware(Middleware middleware)`: Add global middleware
- `register(Plugin... plugins)`: Register plugins
- `unregister(Plugin plugin)`: Unregister a plugin
- `shutdownPlugins()`: Shutdown all plugins

#### **History Management**
- `setMaxHistorySize(int size)`: Set maximum history size
- `clearHistory()`: Clear navigation history

#### **Scene Caching**
- `setCachingEnabled(boolean enabled)`: Enable or disable caching
- `clearCache()`: Clear the scene cache

#### **Current Route**
- `currentRoute()`: Get current route as Optional
- `currentRouteProperty()`: Get current route property for binding

### **Route Class**

#### **Configuration Methods**
- `withName(String name)`: Set route name for named navigation
- `withGuard(RouteGuard guard)`: Add route guard
- `withGuardPredicate(Predicate<NavRequest> predicate)`: Add simplified guard
- `redirectTo(String url)`: Set redirect URL for guard
- `asDialog()`: Display route in modal dialog
- `asDialog(boolean modal)`: Display route in dialog with modality
- `withStage(String stageName, double width, double height)`: Set custom stage properties
- `withLayout(String layoutPath)`: Associate layout with route
- `withLayout(Function<NavRequest, Parent> layoutCreator)`: Associate layout creator
- `withStylesheet(String... stylesheets)`: Add route-specific CSS
- `meta(String key, Object value)`: Set metadata
- `meta(String key)`: Get metadata by key
- `beforeEnter(Consumer<NavRequest>)`: Set beforeEnter hook
- `afterEnter(Consumer<NavRequest>)`: Set afterEnter hook
- `beforeExit(Consumer<NavRequest>)`: Set beforeExit hook
- `afterExit(Consumer<NavRequest>)`: Set afterExit hook

### **RouteLayout Class**

- `children(Route route, Route... routes)`: Add child routes to layout

### **NavRequest Class**

- `url()`: Get the requested URL
- `query()`: Get all query parameters as Map
- `query(String key)`: Get specific query parameter
- `path()`: Get all path parameters (not fully implemented)
- `path(String key)`: Get specific path parameter
- `data()`: Get typed data object as Optional

### **NavLayout Interface**

```java
public interface NavLayout {
    void setContent(Parent content);
}
```

### **Middleware Interface**

```java
@FunctionalInterface
public interface Middleware {
    MiddlewareResponse beforeNavigate(NavRequest request);
}
```

#### **MiddlewareResponse**
- `proceed()`: Continue navigation
- `redirect(String url)`: Redirect to another route
- `redirect(String url, String message)`: Redirect with message
- `block(String message)`: Block navigation
- `block(String message, Runnable action)`: Block with action

### **RouteGuard Interface**

```java
@FunctionalInterface
public interface RouteGuard {
    RouteGuardResponse allowNavigate(NavRequest request);
}
```

#### **RouteGuardResponse**
- `allow()`: Permit navigation
- `block()`: Deny navigation
- `block(String message)`: Deny with message
- `block(String message, Runnable action)`: Deny with action
- `redirect(String url)`: Redirect to alternate route
- `redirect(String url, String message)`: Redirect with message

### **Plugin Interface**

```java
public interface Plugin {
    String id();
    String name();
    void registerRoutes(Navigator navigator);
    void initialize();
    default void shutdown() {}
}
```

### **SharedState Class**

- `put(String key, Object value)`: Store state value
- `get(String key)`: Get state value as Optional
- `remove(String key)`: Remove state value
- `clear()`: Clear all state
- `containsKey(String key)`: Check if key exists

---

## Examples

### **Complete Application Example**

```java
import com.eden.navigatorfx.v2.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static com.eden.navigatorfx.v2.Navigator.*;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize Navigator
        Navigator.bind(this, "My Application", primaryStage, StageStyle.DECORATED, 1024, 768)
                 .baseScene("/views/login.fxml");

        // Define routes with layouts
        Navigator.defineRoutes(
            // Public routes (no layout)
            route("/login", "/views/login.fxml")
                .beforeEnter(req -> System.out.println("Entering login")),

            // Routes with shared layout
            layout("/views/MainLayout.fxml")
                .children(
                    route("/dashboard", "/views/dashboard.fxml")
                        .withName("home")
                        .withStylesheet("/css/dashboard.css")
                        .meta("title", "Dashboard")
                        .beforeEnter(req -> loadDashboardData())
                        .afterExit(req -> cleanupDashboardData()),

                    route("/profile", "/views/profile.fxml")
                        .withName("userProfile"),

                    route("/settings", "/views/settings.fxml")
                        .asDialog()
                        .withStage("Settings", 600, 400),

                    route("/admin", "/views/admin.fxml")
                        .withGuard(request -> {
                            if (AuthService.isAdmin()) {
                                return RouteGuardResponse.allow();
                            }
                            return RouteGuardResponse.block("Admin access required");
                        })
                        .redirectTo("/access-denied"),

                    route("/products", ProductListView::new),

                    route("/access-denied", "/views/access-denied.fxml")
                )
        );

        // Add global authentication middleware
        Navigator.middleware(request -> {
            if (AuthService.isAuthenticated() || request.url().equals("/login")) {
                return Navigator.MiddlewareResponse.proceed();
            }
            return Navigator.MiddlewareResponse.redirect("/login", "Please login first");
        });

        // Register plugins
        Navigator.register(
            new AdminPlugin(),
            new ReportsPlugin()
        );
    }

    private void loadDashboardData() {
        // Load data for dashboard
    }

    private void cleanupDashboardData() {
        // Cleanup dashboard resources
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### **Layout Controller Example**

```java
import com.eden.navigatorfx.v2.layout.NavLayout;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class MainLayoutController implements NavLayout {

    @FXML private BorderPane contentArea;
    @FXML private Label titleLabel;

    @Override
    public void setContent(Parent content) {
        contentArea.setCenter(content);

        // Update title based on current route
        Navigator.currentRoute().ifPresent(route -> {
            route.meta("title").ifPresent(title ->
                titleLabel.setText((String) title)
            );
        });
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        Navigator.navigateTo("/login");
    }

    @FXML
    private void handleBack() {
        if (Navigator.canGoBack()) {
            Navigator.back();
        }
    }
}
```

### **Route Controller with NavRequest**

```java
import com.eden.navigatorfx.v2.BaseController;
import com.eden.navigatorfx.v2.NavRequest;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;

public class SearchController extends BaseController {

    @FXML private TextField searchField;
    @FXML private ListView<Result> resultsList;

    @Override
    protected void onRequest(NavRequest request) {
        // Handle query parameters
        String query = request.query("query");
        String category = request.query("category");

        if (query != null) {
            searchField.setText(query);
            performSearch(query, category);
        }

        // Handle passed data
        request.data().ifPresent(data -> {
            if (data instanceof SearchFilter filter) {
                applyFilter(filter);
            }
        });
    }

    @Override
    protected void onInitialize() {
        // Additional setup after FXML injection
        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.length() > 2) {
                performSearch(newVal, null);
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        // Navigate with query parameter
        Navigator.navigateTo("/search?query=" + query);
    }

    private void performSearch(String query, String category) {
        // Search implementation
    }

    private void applyFilter(SearchFilter filter) {
        // Apply filter logic
    }
}
```

### **Programmatic View Example**

```java
import com.eden.navigatorfx.v2.NavRequest;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ProductListView {

    public ProductListView(NavRequest request) {
        // Constructor receives NavRequest
    }

    public Parent createView(NavRequest request) {
        VBox root = new VBox(10);

        Label title = new Label("Products");
        Button refreshButton = new Button("Refresh");

        refreshButton.setOnAction(e -> {
            // Navigate with data
            Navigator.navigateTo("/products", new RefreshData());
        });

        // Access route metadata
        Navigator.currentRoute().ifPresent(route -> {
            route.meta("sortBy").ifPresent(sort -> {
                // Apply sorting
            });
        });

        root.getChildren().addAll(title, refreshButton);
        return root;
    }
}
```

### **Plugin Example**

```java
import com.eden.navigatorfx.v2.Plugin;
import com.eden.navigatorfx.v2.Navigator;

public class ReportsPlugin implements Plugin {

    @Override
    public String id() {
        return "reports-plugin";
    }

    @Override
    public String name() {
        return "Reports Module";
    }

    @Override
    public void registerRoutes(Navigator navigator) {
        navigator.defineRoutes(
            route("/reports", "/views/reports/main.fxml")
                .meta("plugin", "reports"),
            route("/reports/sales", "/views/reports/sales.fxml"),
            route("/reports/analytics", "/views/reports/analytics.fxml")
        );
    }

    @Override
    public void initialize() {
        System.out.println("Reports plugin initialized");
        // Setup plugin resources
    }

    @Override
    public void shutdown() {
        System.out.println("Reports plugin shutdown");
        // Cleanup plugin resources
    }
}
```

---

## Notes and Considerations

### **Thread Safety**
- All UI updates occur on the JavaFX Application Thread using `Platform.runLater()`
- Navigator is safe to call from any thread
- Callbacks and hooks execute on the JavaFX Application Thread

### **Layout System**
- Layouts must implement the `NavLayout` interface
- Layouts are cached and reused across child route navigation
- The same layout instance persists when navigating between child routes
- Layout caching improves performance but maintains controller state

### **Error Handling**
- Use the `onError()` method to handle exceptions during navigation
- Unhandled exceptions may terminate the application
- NavigationTask provides typed error handling for specific exceptions

### **Middleware and Guards**
- Middleware executes before every navigation globally
- Route guards execute per-route after middleware
- Both can proceed, block, or redirect navigation
- Guards can provide user feedback through messages and actions

### **Scene Caching**
- Enabled by default for performance optimization
- Be mindful of memory usage when caching many scenes
- Controllers receive `NavRequest` on re-navigation even with cached scenes
- Clear cache if scenes are no longer needed or to force reload

### **Lifecycle Hooks**
- Four hooks per route: beforeEnter, afterEnter, beforeExit, afterExit
- Hooks execute in a specific order during navigation
- Use hooks for data loading, cleanup, analytics, etc.
- Hooks have access to NavRequest for context

### **Dialog Support**
- Dialogs open in new Stage windows
- Dialog routes are not cached by default
- Modal dialogs block interaction with main window
- Properly manage dialog lifecycle to prevent resource leaks

### **Resource Loading**
- Flexible ResourceLoader supports custom loading strategies
- FXML files must be on the classpath
- Resources loaded via thread context class loader with fallbacks
- Custom loaders can load from filesystem, JAR, or network

### **Performance**
- Layout caching reduces layout recreation overhead
- Scene caching improves navigation speed
- Transitions may impact performance on slower systems
- Consider disabling animations for better performance

### **Current Limitations**
- Dynamic path parameters (e.g., `/user/{id}`) not fully implemented
- Route matching is exact (no regex or wildcards)
- Single primary scene (dialogs use separate stages)
- No nested route outlets (single layout level)
- No route lazy loading or preloading strategies

### **Best Practices**
- Use layouts to avoid repeating common UI elements
- Leverage middleware for cross-cutting concerns (auth, logging)
- Use route guards for fine-grained access control
- Store shared state in SharedState for cross-route communication
- Clean up resources in beforeExit/afterExit hooks
- Use named routes for refactoring-friendly navigation
- Attach metadata to routes for custom logic

---

## Contributing

Contributions are welcome! To contribute:

1. **Fork the Repository**: Create your own fork of the project.
2. **Create a Branch**: For your feature or bug fix.
3. **Commit Changes**: Make your changes and commit.
4. **Push to Branch**: Push your changes to your fork.
5. **Submit a Pull Request**: Describe your changes and submit.

---

## License

This project is licensed under the MIT License.

---

## Acknowledgements

- **JavaFX Community**: For providing a robust framework for building rich client applications.
- **OpenAi**: For assistance in refining the initial library design.
- **ClaudeCode**: For building the readme because documentation sucks and is hard.

---

**We hope the Navigator library enhances your JavaFX development experience. If you have any questions or need further assistance, feel free to reach out or submit an issue on the repository. Happy coding!**