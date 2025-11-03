package com.eden.navigatorfx.v2;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;

public abstract class BaseController {

    private final ObjectProperty<NavRequest> requestProperty = new SimpleObjectProperty<>();

    public BaseController() {
        // Constructor remains empty or can include essential setup
    }

    @FXML
    public void initialize() {
        // Set up the listener to handle NavRequest changes after FXML fields are initialized
        requestProperty.addListener((obs, oldRequest, newRequest) -> {
            onRequest(newRequest);
        });

        // Call an optional method for additional initialization in child classes
        onInitialize();
    }

    /**
     * Method to be called by Navigator to inject the NavRequest.
     */
    public void setRequest(NavRequest request) {
        requestProperty.set(request);
    }

    /**
     * Abstract method that child controllers must implement to handle the NavRequest.
     */
    protected abstract void onRequest(NavRequest request);

    /**
     * Optional method for additional initialization in child controllers.
     */
    protected void onInitialize() {
        // Can be overridden by child classes if needed
    }
}