package com.eden.navigatorfx.ui;
import java.net.URL;
import java.util.ResourceBundle;

import com.eden.navigatorfx.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class HomeController implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    void navigateToSecond(ActionEvent event) {
        Navigator.navigateTo("second","Hey there");
    }


    public void navigateToLayout(ActionEvent actionEvent) {
        Navigator.navigateTo("layout");
    }
}
