package com.eden.navigatordemo.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class SettingsController {
    @FXML
    private void closeDialog(ActionEvent event) {
        // Close the dialog window
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
//<input type="text" readonly="" value="VL8987749" class="uppercase" isdatepicker="true">
