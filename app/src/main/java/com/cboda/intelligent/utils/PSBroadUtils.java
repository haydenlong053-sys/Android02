package com.cboda.intelligent.utils;

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/6/5.
 */
public class PSBroadUtils {
    public static boolean isLetterDigit(String str) {
//        boolean isDigit = false;//定义一个boolean值，用来表示是否包含数字
//        boolean isLetter = false;//定义一个boolean值，用来表示是否包含字母
//        for (int i = 0; i < str.length(); i++) {
//            if (Character.isDigit(str.charAt(i))) {   //用char包装类中的判断数字的方法判断每一个字符
//                isDigit = true;
//            } else if (Character.isLetter(str.charAt(i))) {  //用char包装类中的判断字母的方法判断每一个字符
//                isLetter = true;
//            }
//        }
//        AppBroadUtil.Companion.debug("密码","::: "+isDigit+"  "+isLetter);

//        String regex = "^[a-zA-Z0-9]+$";
        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,16}$";
//        boolean isRight = isDigit && isLetter && str.matches(regex);
        boolean isRight = str.matches(regex);

        return isRight;
    }
}
