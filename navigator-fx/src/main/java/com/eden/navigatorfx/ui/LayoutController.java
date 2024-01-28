package com.eden.navigatorfx.ui;

import com.eden.navigatorfx.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class LayoutController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void goTo1(ActionEvent actionEvent) {
        Navigator.navigateTo("layout/layout_one");
    }

    public void goTo2(ActionEvent actionEvent) {
        Navigator.navigateTo("layout/layout_two");
    }

    public void goTo3(ActionEvent actionEvent) {
        Navigator.navigateTo("layout/layout_three");
    }

    public void back(ActionEvent actionEvent) {
        Navigator.navigateTo("home");
    }
}
