package com.eden.navigatorfx.update.exceptions;

public class MissingViewCreatorException extends NavigationException {

    private final String url;

    public MissingViewCreatorException(String url) {
        super("No view creator found for URL: " + url);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}