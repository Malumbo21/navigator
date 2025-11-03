package com.eden.navigatordemo.controllers;

import com.eden.navigatordemo.utils.User;
import com.eden.navigatorfx.v2.BaseController;
import com.eden.navigatorfx.v2.NavRequest;
import com.eden.navigatorfx.v2.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Optional;

public class ProfileController extends BaseController {

    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;

    @Override
    protected void onRequest(NavRequest request) {
        // Get the user data passed during navigation
        Optional<User> userOpt = request.data();

        userOpt.ifPresent(user -> {
            nameLabel.setText("Name: " + user.name());
            usernameLabel.setText("Username: " + user.username());
        });
    }

    @FXML
    private void goToDashboard() {
        Navigator.navigateTo("/dashboard")
                .withTransition(Navigator.TransitionType.SLIDE_RIGHT);
    }
}

