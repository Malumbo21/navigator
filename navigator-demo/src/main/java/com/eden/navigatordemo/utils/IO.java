package com.eden.navigatordemo.utils;

public class IO {
    public static void println(Object out){
        System.out.println(out);
    }

    public static void printf(String formattedTxt,Object... values){
        System.out.printf(formattedTxt+"\n",values);
    }
}
