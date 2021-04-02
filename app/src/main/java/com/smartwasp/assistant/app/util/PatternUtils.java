package com.smartwasp.assistant.app.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by luotao on 2017/11/15.
 */

public class PatternUtils {
    public static Pattern NET = Pattern.compile("(http://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn)[^\u4e00-\u9fa5\\s]*");

    /**
     * 校验只能是数字,英文字母和中文
     * @param s
     * @return 是否没有特殊字符
     */
    public static boolean digitalEnZh(String s) {
        Pattern p = Pattern.compile("^[\u4E00-\u9FA50-9a-zA-Z_!@#$&*+=.|]+$");
        Matcher m = p.matcher(s);
        return m.matches();
    }

    /**
     * 验证邮箱
     *
     * @param email email 邮箱
     * @return 是否为合格的邮箱
     */
    public static boolean checkEmail(String email) {
        boolean flag;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码，11位数字，1开通，第二位数必须是3456789这些数字之一 *
     * @param mobileNumber 手机号码
     * @return 是否为中国大陆11位手机号码
     */
    public static boolean checkMobileNumber(String mobileNumber) {
        return mobileNumber.length() >= 9 && mobileNumber.length() <=20;
//        boolean flag;
//        try {
//            // Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
//            Pattern regex = Pattern.compile("^1[345789]\\d{9}$");
//            Matcher matcher = regex.matcher(mobileNumber);
//            flag = matcher.matches();
//        } catch (Exception e) {
//            e.printStackTrace();
//            flag = false;
//
//        }
//        return flag;
    }
    private static boolean isMatch(String regex, String orginal){
        if (orginal == null || orginal.trim().equals("")) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher isNum = pattern.matcher(orginal);
        return isNum.matches();
    }

    public static boolean isPositiveInteger(String orginal) {
        return isMatch("^\\+{0,1}[1-9]\\d*", orginal);
    }

    public static boolean isNegativeInteger(String orginal) {
        return isMatch("^-[1-9]\\d*", orginal);
    }

    public static boolean isWholeNumber(String orginal) {
        return isMatch("[+-]{0,1}0", orginal) || isPositiveInteger(orginal) || isNegativeInteger(orginal);
    }

    public static boolean isPositiveDecimal(String orginal){
        return isMatch("\\+{0,1}[0]\\.[1-9]*|\\+{0,1}[1-9]\\d*\\.\\d*", orginal);
    }

    public static boolean isNegativeDecimal(String orginal){
        return isMatch("^-[0]\\.[1-9]*|^-[1-9]\\d*\\.\\d*", orginal);
    }

    public static boolean isDecimal(String orginal){
        return isMatch("[-+]{0,1}\\d+\\.\\d*|[-+]{0,1}\\d*\\.\\d+", orginal);
    }

    public static boolean isRealNumber(String orginal){
        return isWholeNumber(orginal) || isDecimal(orginal);
    }

    public static boolean isBase64(String str) {
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return Pattern.matches(base64Pattern, str);
    }
}
