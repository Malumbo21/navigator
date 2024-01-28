package com.eden.navigatorfx.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

public class NavigationFailedException extends NavigatorException {
    public NavigationFailedException(String route,Throwable cause){
        super("Failed to navigate to '%s' due to exception '%s'".formatted(route,cause.getMessage()),cause);
    }
}
