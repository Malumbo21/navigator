open module com.eden.navigatordemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires eden.navigatorfx;
    requires atlantafx.base;
    requires java.compiler;


//    opens com.eden.navigatordemo to javafx.fxml, eden.navigatorfx;
    exports com.eden.navigatordemo;
    exports com.eden.navigatordemo.controllers;
}