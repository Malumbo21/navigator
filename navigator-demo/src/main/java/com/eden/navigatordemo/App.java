package com.eden.navigatordemo;

import atlantafx.base.theme.PrimerDark;
import com.eden.navigatordemo.controllers.AccessDeniedPage;
import com.eden.navigatordemo.controllers.JavaUi;
import com.eden.navigatordemo.utils.AuthService;
import javafx.application.Application;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.eden.navigatorfx.v2.Navigator;

import static com.eden.navigatorfx.v2.Navigator.*;

public class App extends Application {

  @Override
  public void start(Stage primaryStage) {
    // Initialize the Navigator
    Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

    ResourceLoader resourceLoader = (String path) -> getClass().getResource(path);

    Navigator.bind(resourceLoader, "Navigator Demo", primaryStage, StageStyle.DECORATED, 800, 600)
             .baseScene("/views/login.fxml");
    Navigator.defineRoutes(
        route("/login", "/views/login.fxml"),
        layout("/views/LayoutPanel.fxml")
            .children(
                route("/dashboard", "/views/dashboard.fxml"),
                route("/settings", "/views/settings.fxml")
                    .asDialog(),
                route("/admin", "/views/admin.fxml")
                    .withGuard(request -> {
                      if (AuthService.isAdmin()) {
                        return RouteGuardResponse.allow();
                      } else {
                        return RouteGuardResponse.block("Access Denied");
                      }
                    })
                /*.redirectTo("/access-denied")*/,
                route("/profile", "/views/profile.fxml"),
                route("/java-demo", JavaUi::new),
                route("/access-denied", AccessDeniedPage::new)
            )

    );
    Navigator.middleware(request -> {
      if (AuthService.isAuthenticated() || request.url().equals("/login")) {
        return Navigator.MiddlewareResponse.proceed();
      }
      return Navigator.MiddlewareResponse.redirect("/login", "You are not authorized");
    });
  }

  public static void main(String[] args) {
    launch(args);
  }
}