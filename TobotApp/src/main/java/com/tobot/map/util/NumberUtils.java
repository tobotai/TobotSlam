package com.tobot.map.util;

import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * @author houdeming
 * @date 2018/11/28
 */
public class NumberUtils {
    /**
     * 中文的数字转int
     *
     * @param str
     * @return
     */
    public static int chineseNum2Int(String str) {
        int result = 0;
        if (!TextUtils.isEmpty(str)) {
            // 存放一个单位的数字如：十万
            int temp = 1;
            // 判断是否有chArr
            int count = 0;
            // 是否有数字
            int strNum = 0;
            char[] cnArr = new char[]{'一', '二', '三', '四', '五', '六', '七', '八', '九', '两'};
            char[] chArr = new char[]{'十', '百', '千', '万', '亿'};
            int length = str.length();
            int cnArrLength = cnArr.length;
            int chArrLength = chArr.length;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                char c = str.charAt(i);
                // 是数字
                if (Character.isDigit(c)) {
                    builder.append(c);
                } else {
                    // 判断是否是chArr
                    boolean b = true;
                    // 非单位，即数字
                    for (int j = 0; j < cnArrLength; j++) {
                        if (c == cnArr[j]) {
                            // 添加下一个单位之前，先把上一个单位值添加到结果中
                            if (0 != count) {
                                result += temp;
                                count = 0;
                            }

                            // 下标+1，就是对应的值
                            temp = j == cnArrLength - 1 ? 2 : j + 1;
                            b = false;
                            strNum = 1;
                            break;
                        }
                    }

                    // 单位{'十','百','千','万','亿'}
                    if (b) {
                        for (int j = 0; j < chArrLength; j++) {
                            if (c == chArr[j]) {
                                switch (j) {
                                    case 0:
                                        temp *= 10;
                                        break;
                                    case 1:
                                        temp *= 100;
                                        break;
                                    case 2:
                                        temp *= 1000;
                                        break;
                                    case 3:
                                        temp *= 10000;
                                        break;
                                    case 4:
                                        temp *= 100000000;
                                        break;
                                    default:
                                        break;
                                }

                                count++;
                                strNum = 1;
                            }
                        }
                    }

                    // 遍历到最后一个字符
                    if (i == str.length() - 1) {
                        if (strNum != 0) {
                            result += temp;
                        }
                    }
                }
            }

            String tempDistance = builder.toString();
            if (!TextUtils.isEmpty(tempDistance)) {
                if (TextUtils.isDigitsOnly(tempDistance)) {
                    result = Integer.parseInt(tempDistance);
                }
            }
        }

        return result;
    }

    /**
     * 判断是数字型
     *
     * @param str
     * @return
     */
    public static boolean isDigits(String str) {
        if (!TextUtils.isEmpty(str)) {
            String regex = "-[0-9]+(.[0-9]+)?|[0-9]+(.[0-9]+)?";
            return str.matches(regex);
        }

        return false;
    }

    /**
     * 是否为浮点数
     *
     * @param str
     * @return
     */
    public static boolean isDoubleOrFloat(String str) {
        if (!TextUtils.isEmpty(str)) {
            Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
            return pattern.matcher(str).matches();
        }

        return false;
    }
}
