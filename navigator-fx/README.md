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

**Navigator** is a powerful JavaFX scene navigation library designed to simplify and enhance scene management in JavaFX applications. It provides an intuitive and flexible API for defining routes, handling scene transitions, passing data, and managing navigation history. With support for middleware, route guards, animated transitions, and more, Navigator empowers developers to build sophisticated JavaFX applications with ease.

---

## Features

- **Simplified Scene Navigation**: Easily navigate between scenes using route URLs.
- **Route Definitions**: Define routes with associated scenes or view creators.
- **Nested Routes**: Organize routes hierarchically for complex applications.
- **Data Passing**: Pass arbitrary data objects between scenes.
- **Query Parameters**: Use query parameters in URLs and access them in scenes.
- **Controller Integration**: Seamlessly inject navigation requests into controllers.
- **Back Navigation**: Support back navigation with navigation history management.
- **Middleware and Route Guards**: Control navigation flow with middleware and guards.
- **Animated Transitions**: Enhance user experience with animated scene transitions.
- **Dialog Support**: Display routes in modal dialogs or new stages.
- **Performance Optimizations**: Improve navigation performance with scene caching.
- **History Management**: Manage navigation history size and clear history when needed.
- **Fluent API**: Use method chaining for cleaner and more readable code.
- **Error Handling**: Handle navigation errors with custom handlers.
- **Support for FXML and Java-based UIs**: Work with both FXML scenes and programmatically created views.

---

## Installation

To use the Navigator library in your JavaFX project, you can include the source files or package it as a library. Here's how to include it in your project:

1. **Add the Source Files**:
  - Include `Navigator.java`, `NavRequest.java`, and `BaseController.java` in your project source code.

2. **Package as a Library (Optional)**:
  - Compile the source files into a JAR file.
  - Add the JAR file to your project's build path.

---

## Getting Started

### **Initialization**

Begin by initializing the `Navigator` in your main application class:

```java
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Navigator.bind(this, "My Application", primaryStage, StageStyle.DECORATED, 1024, 768)
                 .baseScene("/views/base.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

- **Parameters**:
  - `this`: Reference to the main application class.
  - `"My Application"`: The application name or primary stage title.
  - `primaryStage`: The primary `Stage` instance.
  - `StageStyle.DECORATED`: The style of the primary stage.
  - `1024`, `768`: Width and height of the primary stage.
  - `"/views/base.fxml"`: Path to the base FXML file for the initial scene.

---

## Defining Routes

Use the `when` methods to define routes and associate them with scenes or view creators.

```java
Navigator.when("/login", "/views/login.fxml")
         .when("/signup", "/views/signup.fxml", "Signup Stage", 800, 600)
         .when("/dashboard", request -> new DashboardUI(), nav -> {
             nav.when("/user", "/views/user.fxml")
                .when("/products", req -> new ProductsUI());
         });
```

- **Defining a Route with FXML**:
  - `when(String url, String fxmlPath)`: Associates a URL with an FXML file.

- **Defining a Route with Custom Stage Details**:
  - `withStage(String stageName, double width, double height)`: Sets custom stage properties for the route.

- **Defining a Route with a View Creator Function**:
  - `when(String url, Function<NavRequest, Parent> viewCreator)`: Uses a function to create the scene.

- **Nested Routes**:
  - Define nested routes by providing a `Consumer<Navigator>` to the `when` method.

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

- **Displaying Routes in Dialogs**:

```java
Navigator.when("/settings", "/views/settings.fxml")
         .asDialog(true);

Navigator.navigateTo("/settings");
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

Navigator.when("/data", request -> {
    Optional<List<Integer>> dataList = request.data();
    dataList.ifPresent(list -> {
        // Use the data
    });
    return new DataView();
});
```

### **Using Query Parameters**

Include query parameters in URLs and access them in the target scene.

```java
Navigator.navigateTo("/search?query=JavaFX&sort=recent");

Navigator.when("/search", request -> {
    String query = request.query("query");
    String sort = request.query("sort");
    // Use query parameters
    return new SearchView();
});
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

## Nested Routes

Organize routes hierarchically for better structure.

```java
Navigator.when("/dashboard", request -> new DashboardUI(), nav -> {
    nav.when("/user", "/views/user.fxml")
       .when("/products", req -> new ProductsUI());
});
```

- **Parent Route**:
  - `/dashboard` with its own view creator.

- **Child Routes**:
  - `/dashboard/user`
  - `/dashboard/products`

---

## Middleware and Route Guards

Control navigation flow using middleware functions and route guards.

### **Global Middleware**

```java
Navigator.use(request -> {
    if (AuthService.isAuthenticated()) {
        return MiddlewareResponse.proceed();
    } else {
        return MiddlewareResponse.redirect("/login");
    }
});
```

- **Define Middleware**:
  - Use `Navigator.use(Middleware middleware)` to add middleware.
  - Middleware can proceed, redirect, or block navigation.

### **Route Guards**

```java
Navigator.when("/admin", "/views/admin.fxml")
         .withGuard(request -> AuthService.isAdmin())
         .redirectTo("/access-denied");
```

- **Add a Guard to a Route**:
  - Use `withGuard(RouteGuard guard)` to add a guard.
  - Guards can allow or block navigation.
  - Use `redirectTo(String url)` to specify a redirect URL when blocked.

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

- **Define a Route to Display in a Dialog**:

```java
Navigator.when("/settings", "/views/settings.fxml")
         .asDialog(true);
```

- **Navigate to the Dialog Route**:

```java
Navigator.navigateTo("/settings");
```

- **Modal and Non-Modal Dialogs**:
  - Use `asDialog(true)` for modal dialogs (`Modality.APPLICATION_MODAL`).
  - Use `asDialog(false)` for non-modal dialogs.

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

- **Initialization Methods**:
  - `bind(...)`: Initialize the Navigator.
  - `baseScene(String fxmlPath)`: Set the initial scene.

- **Route Definitions**:
  - `when(String url, ...)`: Define a route.
  - `withGuard(RouteGuard guard)`: Add a guard to a route.
  - `redirectTo(String url)`: Specify a redirect URL for a guard.
  - `asDialog(boolean modal)`: Configure a route to display in a dialog.
  - `withStage(String stageName, double width, double height)`: Set custom stage properties.

- **Navigation Methods**:
  - `navigateTo(String url)`: Navigate to a route.
  - `navigateTo(String url, Object data)`: Navigate with data.
  - `back()`: Navigate back.
  - `canGoBack()`: Check if back navigation is possible.

- **Middleware**:
  - `use(Middleware middleware)`: Add a global middleware function.

- **History Management**:
  - `setMaxHistorySize(int size)`: Set maximum history size.
  - `clearHistory()`: Clear navigation history.

- **Scene Caching**:
  - `setCachingEnabled(boolean enabled)`: Enable or disable caching.
  - `clearCache()`: Clear the scene cache.

### **NavRequest Class**

- **Methods**:
  - `url()`: Get the requested URL.
  - `query()`: Get all query parameters.
  - `query(String key)`: Get a specific query parameter.
  - `path()`: Get path parameters (future enhancement).
  - `path(String key)`: Get a specific path parameter.
  - `data()`: Get the data object passed during navigation.

### **Middleware Interface**

```java
@FunctionalInterface
public interface Middleware {
    MiddlewareResponse beforeNavigate(NavRequest request);
}
```

- **MiddlewareResponse**:
  - `proceed()`: Continue navigation.
  - `redirect(String url)`: Redirect to another route.
  - `shouldProceed()`: Check if navigation should proceed.
  - `getRedirectUrl()`: Get the redirect URL if set.

### **RouteGuard Interface**

```java
@FunctionalInterface
public interface RouteGuard {
    boolean allowNavigate(NavRequest request);
}
```

- Return `true` to allow navigation or `false` to block it.

---

## Examples

### **Full Example**

```java
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Navigator.bind(this, "My Application", primaryStage, StageStyle.DECORATED, 1024, 768)
                 .baseScene("/views/base.fxml")
                 // Global middleware to check authentication
                 .use(request -> {
                     if (AuthService.isAuthenticated()) {
                         return MiddlewareResponse.proceed();
                     } else {
                         return MiddlewareResponse.redirect("/login");
                     }
                 })
                 // Define routes
                 .when("/login", "/views/login.fxml")
                 .when("/dashboard", request -> new DashboardUI(), nav -> {
                     nav.when("/user", "/views/user.fxml")
                        .when("/products", req -> new ProductsUI())
                        .when("/admin", "/views/admin.fxml")
                        .withGuard(req -> AuthService.isAdmin())
                        .redirectTo("/access-denied");
                 });

        // Navigate to the dashboard with a transition
        Navigator.navigateTo("/dashboard")
                 .withTransition(Navigator.TransitionType.FADE)
                 .onError(Navigator.NavigationException.class, error -> {
                     System.err.println("Navigation Error: " + error.getMessage());
                 });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### **Controller Handling NavRequest**

```java
public class SearchController extends BaseController {

    @FXML private TextField searchField;
    @FXML private ListView<Result> resultsList;

    @Override
    protected void onRequest(NavRequest request) {
        String query = request.query("query");
        if (query != null) {
            searchField.setText(query);
            performSearch(query);
        }
    }

    private void performSearch(String query) {
        // Search logic
    }
}
```

---

## Notes and Considerations

- **Thread Safety**:
  - All UI updates occur on the JavaFX Application Thread using `Platform.runLater()`.

- **Error Handling**:
  - Use the `onError()` method to handle exceptions during navigation.
  - Unhandled exceptions may terminate the application.

- **Middleware and Guards**:
  - Middleware functions can control navigation flow globally.
  - Route guards control access to specific routes.

- **Scene Caching**:
  - Be mindful of memory usage when caching scenes.
  - Clear the cache if scenes are no longer needed.

- **Animated Transitions**:
  - Transitions enhance user experience but may impact performance.
  - Choose transition types that suit your application's needs.

- **Dialog Support**:
  - Use dialogs for settings, forms, or additional information.
  - Manage dialog stages appropriately to prevent resource leaks.

- **Dynamic Path Parameters**:
  - Currently, route matching is exact. Support for dynamic path parameters (e.g., `/user/{id}`) can be added by enhancing the route matching logic.

- **Resource Loading**:
  - Ensure FXML files are correctly placed relative to the classpath.

- **Logging**:
  - Integrate a logging framework for better error logging.

- **Extensibility**:
  - The library can be extended with features like custom transitions, additional middleware capabilities, and more.

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
- **OpenAI**: For assistance in refining the library design.
- **Contributors**: Thank you to all who have contributed to the development and improvement of this library.

---

**We hope the Navigator library enhances your JavaFX development experience. If you have any questions or need further assistance, feel free to reach out or submit an issue on the repository. Happy coding!**