package com.eden.navigatordemo.controllers;

import com.eden.navigatordemo.utils.AuthService;
import com.eden.navigatorfx.v2.Navigator;
import com.eden.navigatorfx.v2.TextUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private Button submitButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;



    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (AuthService.authenticate(username, password)) {
            // Navigate to the dashboard with a fade transition
            Navigator.navigateTo("/dashboard")
                    .withTransition(Navigator.TransitionType.FADE);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Credentials");
            alert.setHeaderText("Please try again with a different username or password");
            alert.showAndWait();

        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TextUtils.formSubmit(submitButton, usernameField, passwordField);
    }
}
