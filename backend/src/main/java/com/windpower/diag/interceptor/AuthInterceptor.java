package com.windpower.diag.interceptor;

import com.windpower.diag.common.Result;
import com.windpower.diag.util.JwtUtil;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Component
public class AuthInterceptor implements Filter {
    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-email",
            "/api/auth/resend-verify-email",
            "/api/auth/csrf-token"
    );

    @Inject
    private JwtUtil jwtUtil;

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        String path = ctx.path();

        // 放行白名单和OPTIONS请求
        if (WHITE_LIST.contains(path) || "OPTIONS".equalsIgnoreCase(ctx.method())) {
            chain.doFilter(ctx);
            return;
        }

        // 非 /api 开头的请求放行（静态资源等）
        if (!path.startsWith("/api")) {
            chain.doFilter(ctx);
            return;
        }

        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.status(401);
            ctx.outputAsJson(toJson(Result.fail(401, "未登录或Token已过期")));
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            ctx.status(401);
            ctx.outputAsJson(toJson(Result.fail(401, "Token无效或已过期")));
            return;
        }

        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        ctx.attrSet("userId", userId);
        ctx.attrSet("username", username);

        chain.doFilter(ctx);
    }

    private String toJson(Result<?> result) {
        return "{\"code\":" + result.getCode() + ",\"message\":\"" + result.getMessage() + "\",\"data\":null}";
    }
}
