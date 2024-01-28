package com.eden.navigatorfx.routing.utils;

public class ThreadUtils {
    public static void sleep(long duration){
        try{
            Thread.sleep(duration);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }
}
