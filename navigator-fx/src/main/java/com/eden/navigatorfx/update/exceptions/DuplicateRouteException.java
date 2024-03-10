package com.eden.navigatorfx.update.exceptions;

public class DuplicateRouteException extends NavigationException{

    public DuplicateRouteException(String url) {
        super("Route '"+url+"' alreaady exists");
    }

}
