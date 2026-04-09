package com.windpower.diag.interceptor;

import com.windpower.diag.util.XssUtil;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XSS 全局过滤器
 * 对 JSON 请求体中的字符串值进行 HTML 实体转义
 * 同时设置安全响应头
 */
@Component
public class XssFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(XssFilter.class);

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        // 设置 XSS 防护相关的安全响应头
        ctx.headerSet("X-Content-Type-Options", "nosniff");
        ctx.headerSet("X-XSS-Protection", "1; mode=block");
        ctx.headerSet("X-Frame-Options", "DENY");
        ctx.headerSet("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:");

        // 对 POST/PUT 请求的 JSON body 进行 XSS 过滤
        if (isWriteMethod(ctx.method()) && isJsonRequest(ctx)) {
            String body = ctx.body();
            if (body != null && !body.isEmpty()) {
                String sanitized = sanitizeJsonBody(body);
                if (!body.equals(sanitized)) {
                    log.debug("XSS filter sanitized request body for: {}", ctx.path());
                    ctx.bodyNew(sanitized);
                }
            }
        }

        // 对 URL 查询参数进行 XSS 检测
        for (String key : ctx.paramMap().keySet()) {
            String value = ctx.param(key);
            if (value != null && XssUtil.containsXss(value)) {
                log.warn("XSS attack detected in query param: key={}, path={}", key, ctx.path());
                ctx.paramMap().put(key, XssUtil.escape(value));
            }
        }

        chain.doFilter(ctx);
    }

    private boolean isWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method) ||
               "PUT".equalsIgnoreCase(method) ||
               "PATCH".equalsIgnoreCase(method);
    }

    private boolean isJsonRequest(Context ctx) {
        String contentType = ctx.contentType();
        return contentType != null && contentType.contains("application/json");
    }

    /**
     * 对 JSON 字符串中的所有字符串值进行 XSS 转义
     * 简单实现：扫描 JSON 中的字符串值并转义危险字符
     */
    private String sanitizeJsonBody(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        StringBuilder result = new StringBuilder(json.length() + 32);
        boolean inString = false;
        boolean isKey = true; // 跟踪当前是 key 还是 value
        boolean escaped = false;
        StringBuilder currentString = new StringBuilder();

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                if (inString) {
                    currentString.append(c);
                } else {
                    result.append(c);
                }
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                if (inString) {
                    currentString.append(c);
                } else {
                    result.append(c);
                }
                continue;
            }

            if (c == '"' && !escaped) {
                if (inString) {
                    // 字符串结束
                    String str = currentString.toString();
                    if (!isKey && XssUtil.containsXss(str)) {
                        str = XssUtil.escape(str);
                    }
                    result.append('"').append(str).append('"');
                    currentString.setLength(0);
                    inString = false;
                } else {
                    // 字符串开始
                    inString = true;
                    currentString.setLength(0);
                }
                continue;
            }

            if (inString) {
                currentString.append(c);
            } else {
                result.append(c);
                if (c == ':') {
                    isKey = false;
                } else if (c == ',' || c == '{' || c == '[') {
                    isKey = (c == ',' || c == '{');
                }
            }
        }

        return result.toString();
    }
}
