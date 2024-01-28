package com.eden.navigatorfx.exceptions;

public class RouteNotFoundException extends NavigatorException{
    public RouteNotFoundException(String route){
        super("Route '%s' has not been found".formatted(route));
    }

}
