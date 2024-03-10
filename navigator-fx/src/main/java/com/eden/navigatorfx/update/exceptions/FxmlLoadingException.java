package com.eden.navigatorfx.update.exceptions;

public class FxmlLoadingException extends NavigationException {

    private final String path;

    public FxmlLoadingException(String path, Throwable cause) {
        super("Failed to load FXML: " + path, cause);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}