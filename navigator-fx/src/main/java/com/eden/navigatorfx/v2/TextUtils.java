package com.eden.navigatorfx.v2;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;

public class TextUtils {
    public static void formSubmit(Button submitButton, TextInputControl... fields) {
        for (var field : fields) {
            field.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    submitButton.fire();
                }
            });
        }
    }
}
