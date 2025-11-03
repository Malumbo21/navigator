package com.eden.navigatordemo.controllers;

import com.eden.navigatordemo.utils.AuthService;
import com.eden.navigatorfx.v2.Navigator;
import com.eden.navigatorfx.v2.layout.NavLayout;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class LayoutController implements NavLayout, Initializable {
    @FXML
    public ScrollPane contentPanel;
    @FXML
    private BorderPane root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void setContent(Parent content) {
        contentPanel.setContent(content);
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), content);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    public void navigateHome(ActionEvent event) {
        Navigator.navigateTo("/dashboard");
    }

    public void navigateToAdmin(ActionEvent event) {
        Navigator.navigateTo("/admin");
    }

    public void navigateProfile(ActionEvent event) {
        Navigator.navigateTo("/profile",AuthService.getCurrentUser());
    }

    public void navigateJavaExmp(ActionEvent event) {
        Navigator.navigateTo("/java-demo");
    }

    public void openSettings(ActionEvent event) {
        Navigator.navigateTo("/settings");
    }

    public void logOut(ActionEvent event) {
        AuthService.logout();
        Navigator.navigateTo("/login");
    }

    public void toggleMenu(ActionEvent actionEvent) {
    }
}
