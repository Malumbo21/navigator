package com.eden.navigatordemo.controllers;

import atlantafx.base.controls.Tile;
import com.eden.navigatordemo.utils.AuthService;
import com.eden.navigatordemo.utils.User;
import com.eden.navigatorfx.v2.NavRequest;
import com.eden.navigatorfx.v2.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class JavaUi extends VBox {
    private final NavRequest request;

    public JavaUi(NavRequest request) {
        this.request = request;

        setSpacing(10);
        setPadding(new Insets(10, 10, 10, 10));
        setAlignment(Pos.CENTER);
        var user = AuthService.getCurrentUser();
        var graphic = new VBox();
        graphic.setAlignment(Pos.CENTER);
        graphic.setPrefSize(200, 200);
        graphic.setBackground(Background.fill(Color.DARKGRAY));
        graphic.setStyle("-fx-border-radius: 10px");
        Label label = new Label((user.name().charAt(1) + "").toUpperCase());
        label.setStyle("-fx-text-fill: white;");
        label.setFont(Font.font("System", FontWeight.BOLD, 30));
        graphic.getChildren().addAll(
                label
        );
        Button backButton = new Button("<- Back");
        backButton.setOnAction(e-> Navigator.back());
        getChildren().addAll(
                backButton,
                new Tile(user.name(), "Current logged in user", graphic)
        );
    }

}
