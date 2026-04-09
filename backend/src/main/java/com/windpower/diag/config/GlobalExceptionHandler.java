package com.windpower.diag.config;

import com.windpower.diag.common.BusinessException;
import com.windpower.diag.common.Result;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GlobalExceptionHandler implements Filter {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        try {
            chain.doFilter(ctx);
        } catch (BusinessException e) {
            log.warn("业务异常: {}", e.getMessage());
            ctx.status(200);
            ctx.render(Result.fail(e.getCode(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("参数校验异常: {}", e.getMessage());
            ctx.status(200);
            ctx.render(Result.fail(400, e.getMessage()));
        } catch (Throwable e) {
            log.error("系统异常", e);
            ctx.status(200);
            // 开发阶段返回详细错误信息，便于排查
            String detail = e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : "unknown");
            // 如果有 cause，也附加上
            if (e.getCause() != null) {
                detail += " | Cause: " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage();
            }
            ctx.render(Result.fail(500, detail));
        }
    }
}
