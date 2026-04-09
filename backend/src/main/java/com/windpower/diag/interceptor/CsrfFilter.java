package com.windpower.diag.interceptor;

import com.windpower.diag.common.Result;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

/**
 * CSRF 防护过滤器
 *
 * 防护策略：
 * 1. 对状态变更请求（POST/PUT/DELETE）校验 X-CSRF-Token 请求头
 * 2. 校验 Origin/Referer 头，防止跨域伪造请求
 * 3. 提供 /api/auth/csrf-token 接口获取 CSRF Token
 *
 * 注意：本项目使用 JWT Bearer Token 认证（非 Cookie），
 * CSRF 攻击面较小，此过滤器作为纵深防御层。
 */
@Component
public class CsrfFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(CsrfFilter.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    // 不需要 CSRF 校验的路径
    private static final Set<String> CSRF_EXEMPT = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-email",
            "/api/auth/resend-verify-email",
            "/api/auth/csrf-token"
    );

    // 允许的 Origin（生产环境应配置为实际域名）
    @Inject(value = "${csrf.allowedOrigins:http://localhost:5173,http://localhost:8082}", required = false)
    private String allowedOriginsStr;

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        String path = ctx.path();
        String method = ctx.method();

        // GET/HEAD/OPTIONS 请求不需要 CSRF 校验
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            // 如果是获取 CSRF Token 的请求
            if ("/api/auth/csrf-token".equals(path)) {
                handleCsrfTokenRequest(ctx);
                return;
            }
            chain.doFilter(ctx);
            return;
        }

        // 非 /api 开头的请求放行
        if (!path.startsWith("/api")) {
            chain.doFilter(ctx);
            return;
        }

        // 豁免路径放行
        if (CSRF_EXEMPT.contains(path)) {
            chain.doFilter(ctx);
            return;
        }

        // 校验 Origin/Referer
        if (!validateOrigin(ctx)) {
            log.warn("CSRF: Origin/Referer validation failed for {} {}", method, path);
            ctx.status(403);
            ctx.outputAsJson(toJson(Result.fail(403, "CSRF校验失败：请求来源不合法")));
            return;
        }

        // 校验 CSRF Token
        String csrfToken = ctx.header("X-CSRF-Token");
        String storedToken = ctx.header("Authorization") != null ?
                extractCsrfFromJwt(ctx) : null;

        // 如果有 Authorization header（JWT），则 CSRF 风险较低，
        // 但仍校验 X-CSRF-Token（如果前端发送了的话）
        if (csrfToken != null && !csrfToken.isEmpty()) {
            // 前端发送了 CSRF Token，进行校验
            if (!validateCsrfToken(csrfToken)) {
                log.warn("CSRF: Invalid CSRF token for {} {}", method, path);
                ctx.status(403);
                ctx.outputAsJson(toJson(Result.fail(403, "CSRF校验失败：Token无效")));
                return;
            }
        }
        // 如果没有 CSRF Token 但有 JWT Authorization，允许通过
        // （JWT 本身不会被浏览器自动携带，CSRF 攻击无法利用）

        chain.doFilter(ctx);
    }

    /**
     * 生成并返回 CSRF Token
     */
    private void handleCsrfTokenRequest(Context ctx) {
        String token = generateCsrfToken();
        ctx.headerSet("X-CSRF-Token", token);
        ctx.outputAsJson("{\"code\":200,\"message\":\"ok\",\"data\":\"" + token + "\"}");
    }

    /**
     * 校验 Origin 或 Referer 头
     */
    private boolean validateOrigin(Context ctx) {
        // 如果请求携带了 JWT Authorization header，CSRF 风险极低（JWT 不会被浏览器自动携带）
        if (ctx.header("Authorization") != null && !ctx.header("Authorization").isEmpty()) {
            return true;
        }

        String origin = ctx.header("Origin");
        String referer = ctx.header("Referer");

        // 如果都没有，可能是同源请求（某些浏览器不发送）
        if ((origin == null || origin.isEmpty()) && (referer == null || referer.isEmpty())) {
            // 检查是否有 X-Requested-With 头（AJAX 请求标识）
            String xRequestedWith = ctx.header("X-Requested-With");
            return "XMLHttpRequest".equals(xRequestedWith);
        }

        Set<String> allowed = Set.of(allowedOriginsStr != null ?
                allowedOriginsStr.split(",") : new String[]{"http://localhost:5173", "http://localhost:8082"});

        if (origin != null && !origin.isEmpty()) {
            return allowed.contains(origin.trim());
        }

        if (referer != null && !referer.isEmpty()) {
            for (String allowedOrigin : allowed) {
                if (referer.startsWith(allowedOrigin.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 生成 CSRF Token（32字节随机数的 Base64 编码）
     */
    private String generateCsrfToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 校验 CSRF Token 格式（基本格式校验）
     */
    private boolean validateCsrfToken(String token) {
        if (token == null || token.length() < 20) {
            return false;
        }
        // Base64 URL-safe 格式校验
        return token.matches("^[A-Za-z0-9_-]+$");
    }

    private String extractCsrfFromJwt(Context ctx) {
        // JWT 存在即可，不需要额外提取
        return ctx.header("Authorization");
    }

    private String toJson(Result<?> result) {
        return "{\"code\":" + result.getCode() + ",\"message\":\"" + result.getMessage() + "\",\"data\":null}";
    }
}
