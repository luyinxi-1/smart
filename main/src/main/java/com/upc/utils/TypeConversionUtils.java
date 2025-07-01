package com.upc.utils;


public class TypeConversionUtils {
    public static String sexToString(Integer param) {
        switch (param) {
            case 0:
                return "女";
            case 1:
                return "男";
            default:
                return null;
        }
    }

}
