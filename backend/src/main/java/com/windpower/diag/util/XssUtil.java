package com.windpower.diag.util;

/**
 * XSS 防护工具类
 * 对用户输入进行 HTML 实体转义，防止 XSS 攻击
 */
public class XssUtil {

    private XssUtil() {}

    /**
     * 对字符串进行 HTML 实体转义
     * 转义 < > " ' & 五个关键字符
     */
    public static String escape(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#x27;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 清除所有 HTML 标签（更激进的策略）
     */
    public static String stripTags(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.replaceAll("<[^>]*>", "");
    }

    /**
     * 检测字符串是否包含潜在的 XSS 攻击内容
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        String lower = input.toLowerCase();
        return lower.contains("<script") ||
               lower.contains("javascript:") ||
               lower.contains("onerror") ||
               lower.contains("onload") ||
               lower.contains("onclick") ||
               lower.contains("onmouseover") ||
               lower.contains("onfocus") ||
               lower.contains("eval(") ||
               lower.contains("expression(") ||
               lower.contains("vbscript:");
    }
}
