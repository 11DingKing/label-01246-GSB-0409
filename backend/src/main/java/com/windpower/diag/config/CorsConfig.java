package com.windpower.diag.config;

import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

@Component
public class CorsConfig implements Filter {

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        ctx.headerSet("Access-Control-Allow-Origin", "*");
        ctx.headerSet("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        ctx.headerSet("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,X-CSRF-Token");
        ctx.headerSet("Access-Control-Expose-Headers", "X-CSRF-Token");
        ctx.headerSet("Access-Control-Max-Age", "3600");

        // 设置响应编码为 UTF-8，防止中文乱码
        ctx.contentType("application/json;charset=UTF-8");

        if ("OPTIONS".equalsIgnoreCase(ctx.method())) {
            ctx.status(204);
            return;
        }
        chain.doFilter(ctx);
    }
}
