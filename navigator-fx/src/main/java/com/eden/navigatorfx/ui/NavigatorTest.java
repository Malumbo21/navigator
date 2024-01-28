package com.eden.navigatorfx.ui;

import atlantafx.base.theme.PrimerDark;
import com.eden.navigatorfx.Navigator;
import com.eden.navigatorfx.routing.utils.NavigationTransition;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.stage.Stage;

import static com.eden.navigatorfx.routing.ParentRoute.parent;

public class NavigatorTest extends Application {

    @Override
    public void start(Stage stage) throws Exception {
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

    @Override
    public void stop() throws Exception {
        System.out.println("Closing now");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}