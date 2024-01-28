module com.eden.navigatorfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires fr.brouillard.oss.cssfx;
    requires org.jetbrains.annotations;


    opens com.eden.navigatorfx to javafx.fxml;
    exports com.eden.navigatorfx;
    exports com.eden.navigatorfx.ui;
    opens com.eden.navigatorfx.ui to javafx.fxml;

    exports com.eden.navigatorfx.routing;
    opens com.eden.navigatorfx.routing to javafx.fxml;

    exports com.eden.navigatorfx.routing.utils;
    opens com.eden.navigatorfx.routing.utils to javafx.fxml;
}