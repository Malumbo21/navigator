package com.eden.navigatordemo.controllers;

import com.eden.navigatorfx.v2.NavRequest;
import com.eden.navigatorfx.v2.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AccessDeniedPage extends VBox {
    private NavRequest request;

    public AccessDeniedPage(NavRequest request) {
        this.request = request;
        setSpacing(10);
        setPadding(new Insets(10, 10, 10, 10));
        setAlignment(Pos.CENTER);
        Label title = new Label("Access Denied");
        title.setStyle("""
                        -fx-text-fill: white;
                        -fx-font-weight: bold;
                        -fx-font-size: 25px;
                        -fx-font-style: italic;
                        """);
        Button backButton = new Button("<- Back");
        backButton.setOnAction(e-> Navigator.back());
        getChildren().addAll(
                title,
                backButton
        );
    }


}
