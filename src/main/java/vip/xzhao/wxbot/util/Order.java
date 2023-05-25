package vip.xzhao.wxbot.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Order {
    public static String extractDigit(String str) {
        Pattern pattern = Pattern.compile("(?<=订单号：)\\d+(?:-\\d+)*");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }
}
