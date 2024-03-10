<p align="center"><img width="200" height="200" src="./navigator-icon.png"></p>

# Navigator
[![release](http://github-release-version.herokuapp.com/github/Marcotrombino/Navigator/release.svg?style=flat)](https://github.com/Marcotrombino/Navigator/releases/latest)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A simple JavaFX router to switch between application scenes

[Example](#example)

## Download
Get latest release [here](https://github.com/Marcotrombino/Navigator/releases/latest)

## Supported versions
- Java 17+ 
### Advantages
You can switch between your scenes from <b>anywhere</b> through a simple method, without worrying about annoying Stage settings.

## Usage
### 1. Bind
Add Navigator as project dependency and import it from its package:

```java
  
```
Connect Navigator to your application stage: call `bind()` from your main class `start()` method (if you use IntelliJ IDEA) or similar:
```java
Navigator.bind(this, primaryStage);
```
  ##### You can optionally set application title and size (width, height):
```java
Navigator.bind(this, primaryStage, "MyApplication", 800, 600);
```
### 2. Set routes
Define your Application routes with a <b>label identifier</b> and its corresponding <b>.fxml</b> screen file:
```java
Navigator.when("login", "myloginscreen.fxml");
Navigator.when("profile", "myprofilescreen.fxml");
// ... others
```
##### You can optionally specify the route title and size (width, height):
```java
Navigator.when("login", "myloginscreen.fxml", "My login screen", 1000, 500);
```

##### You can optionally specify the sub routes with and without title and size (width, height):
## /product is a borderpane and has its center replaced by its child routes
```java

Navigator.when(parent("layout", "com/eden/navigatorfx/ui/layout.fxml")
                .when("layout_one", "com/eden/navigatorfx/ui/l_1.fxml")
                .when("layout_two", "com/eden/navigatorfx/ui/l_2.fxml")
                .when("layout_three", "com/eden/navigatorfx/ui/l_3.fxml")
);

```
### 3. Switch
Switch routes from anywhere (controllers, services, etc):
```java
Navigator.navigateTo("login");     // switch to myloginscreen.fxml
```


### Passing and retrieving data between routes
Your application could need to pass some data to another route and then retrieve those data:
#### Send data from the current scene
`navigateTo()` accepts two parameters: a <b>route identifier</b> and a <b>`Object`</b>:
##### (Multiples data could be stored on an appropriate Collection)
```java
Navigator.navigateTo("profile", "johndoe22");     // switch to myprofilescreen.fxml passing an username
```
#### Get data from the destination scene
`getData()` returns an Optional of <b>`<T>`</b> which will be cast to appropriate data type:
```java

var value = Navigator.<T>getData();     // returns optional auto cast to <T>

```

## Example
#### Without Navigator
A common JavaFX project starter:
```java
package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
```

#### Using Navigator 
```java
package sample;

import javafx.application.Application;
import javafx.stage.Stage;
import sample.Navigator;                                 // import Navigator
import com.eden.navigatorfx.routing.utils.NavigationTransition;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
    
        Navigator.bind(this, stage, "Navigation FX", 600, 400);
        Navigator.when("home", "com/eden/navigatorfx/ui/home.fxml");
        Navigator.when("second", "com/eden/navigatorfx/ui/second.fxml");
        Navigator.when(parent("layout", "com/eden/navigatorfx/ui/layout.fxml")
                        .when("layout_one", "com/eden/navigatorfx/ui/l_1.fxml")
                        .when("layout_two", "com/eden/navigatorfx/ui/l_2.fxml")
                        .when("layout_three", "com/eden/navigatorfx/ui/l_3.fxml")
        );
        Navigator.setAnimationType(NavigationTransition.FADE, 300);

        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        CSSFX.start();
        Navigator.start("home");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
```

### Switch animation
You can also set an animation type when you switch between routes:
```java
Navigator.setAnimationType("fade");
```
##### You can optionally specify the animation duration (ms):
```java
Navigator.setAnimationType(NavigationTransition.FADE, 300);
```
#### animationType
| AnimationType  | Default duration |
| ------------- | ------------- |
| `fade` | 800  |
