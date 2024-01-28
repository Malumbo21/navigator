package com.eden.navigatorfx.exceptions;

public class DuplicateRouteException extends NavigatorException{
    public DuplicateRouteException(String route){
        super("Route '%s' has already been declared".formatted(route));
    }

}
