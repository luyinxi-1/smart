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

    public static Integer isPartyNumberToInteger(String param) {
        switch (param) {
            case "是":
                return 1;
            case "否":
                return 0;
            default:
                return null;
        }
    }

    public static Integer educationalBackgroundToInteger(String param) {
        switch (param) {
            case "博士":
                return 2;
            case "硕士":
                return 1;
            case "本科":
                return 0;
            default:
                return null;
        }
    }
}
