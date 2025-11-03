package com.eden.navigatordemo.controllers;

import com.eden.navigatorfx.v2.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class AdminController {

    @FXML
    public void initialize() {
        // Initialization logic if needed
    }

    public void back(ActionEvent actionEvent) {
        Navigator.back();
    }
}
