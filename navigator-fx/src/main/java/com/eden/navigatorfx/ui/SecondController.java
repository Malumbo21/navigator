package com.eden.navigatorfx.ui;

import com.eden.navigatorfx.Navigator;
import com.eden.navigatorfx.routing.utils.ThreadUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.application.Platform.runLater;

public class SecondController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        ThreadUtils.sleep(5000);
        Navigator.currentRequest().<String>data().ifPresent(data -> {
            runLater(()->{
                Alert alert = new Alert(Alert.AlertType.INFORMATION, data);
                alert.setHeaderText("Message");
                alert.show();
            });
        });
    }

    @FXML
    void navigateToFirst(ActionEvent event) {
        Navigator.navigateTo("home");
    }


}
