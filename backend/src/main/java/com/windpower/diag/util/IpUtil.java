package com.windpower.diag.util;

import org.noear.solon.core.handle.Context;

public class IpUtil {

    private IpUtil() {}

    public static String getClientIp(Context ctx) {
        if (ctx == null) {
            return "unknown";
        }
        String ip = ctx.header("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个为真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index).trim();
            }
            return ip.trim();
        }
        ip = ctx.header("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return ctx.realIp();
    }
}
