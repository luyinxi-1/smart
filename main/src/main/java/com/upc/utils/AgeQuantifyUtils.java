package com.upc.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class AgeQuantifyUtils {
    /**
     * 根据身份证号计算出生日期
     * 【注意】表中的出生日期类型为varchar类型
     * @param idNumber 身份证号
     * @return 出生日期
     */
    public static String getBirthDateFromIdNumber(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            return null;
        }
        String birthYear = idNumber.substring(6, 10);
        String birthMonth = idNumber.substring(10, 12);
        String birthDay = idNumber.substring(12, 14);
        LocalDate birthDate = LocalDate.of(Integer.parseInt(birthYear), Integer.parseInt(birthMonth), Integer.parseInt(birthDay));
        return birthDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 根据身份证号计算年龄
     *
     * @param idNumber 身份证号
     * @return 年龄
     */
    public static int getAgeFromIdNumber(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            return 0;
        }
        LocalDate birthDate = LocalDate.of(Integer.parseInt(idNumber.substring(6, 10)), Integer.parseInt(idNumber.substring(10, 12)), Integer.parseInt(idNumber.substring(12, 14)));
        LocalDate nowDate = LocalDate.now();
        Period period = Period.between(birthDate, nowDate);
        return period.getYears();
    }

    /**
     * 根据身份证号计算性别
     * @param idNumber 身份证号
     * @return 女0，男1
     */
    public static int getGenderFromIdNumber(String idNumber) {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            return -1; // 或者抛出异常，视情况而定
        }
        // 获取身份证号第17位，即性别信息
        char genderChar = idNumber.charAt(16);
        // 判断性别奇偶
        int genderCode = Integer.parseInt(String.valueOf(genderChar));
        return genderCode % 2 == 0 ? 0 : 1; // 奇数表示男性（1），偶数表示女性（0）
    }

}