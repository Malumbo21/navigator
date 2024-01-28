package com.eden.navigatorfx.exceptions;

public abstract class NavigatorException extends RuntimeException{

    public NavigatorException(String message) {
        super(message);
    }

    public NavigatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public NavigatorException(Throwable cause) {
        super(cause);
    }

    public NavigatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
