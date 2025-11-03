package com.eden.navigatordemo.controllers;
import com.eden.navigatordemo.utils.AuthService;
import com.eden.navigatorfx.v2.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class DashboardController {

    @FXML
    private void openSettings() {
        Navigator.navigateTo("/settings");
    }

    @FXML
    private void openAdmin() {
        Navigator.navigateTo("/admin")
                .onError(Navigator.NavigationException.class, error -> {
                    // Handle navigation errors (e.g., access denied)
                    System.err.println("Access Denied: " + error.getMessage());
                    // Optionally, navigate to an access-denied page
                    // Navigator.navigateTo("/access-denied");
                });
    }

    @FXML
    private void handleLogout() {
        // Perform logout logic
        AuthService.logout();
        // Navigate back to login
        Navigator.navigateTo("/login");
    }

    public void openJavaDemo(ActionEvent actionEvent) {
        Navigator.navigateTo("/java-demo");
    }
}
