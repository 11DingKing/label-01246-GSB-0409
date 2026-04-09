package com.windpower.diag.aop;

import com.windpower.diag.entity.SysOperationLog;
import com.windpower.diag.mapper.SysOperationLogMapper;
import com.windpower.diag.util.IpUtil;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.handle.Context;
import org.noear.solon.data.annotation.Ds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
public class OperationLogAspect implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    @Ds
    private SysOperationLogMapper operationLogMapper;

    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        OperationLog annotation = inv.method().getAnnotation(OperationLog.class);
        if (annotation == null) {
            return inv.invoke();
        }

        long startTime = System.currentTimeMillis();
        SysOperationLog opLog = new SysOperationLog();
        opLog.setModule(annotation.module());
        opLog.setOperation(annotation.operation());
        opLog.setMethod(inv.method().getMethod().getDeclaringClass().getName() + "." + inv.method().getMethod().getName());
        opLog.setCreatedAt(LocalDateTime.now());

        Context ctx = Context.current();
        if (ctx != null) {
            opLog.setUrl(ctx.path());
            opLog.setIp(IpUtil.getClientIp(ctx));
            try {
                Object userId = ctx.attr("userId");
                if (userId instanceof Long) {
                    opLog.setUserId((Long) userId);
                } else if (userId instanceof Number) {
                    opLog.setUserId(((Number) userId).longValue());
                }
                Object username = ctx.attr("username");
                if (username != null) {
                    opLog.setUsername(String.valueOf(username));
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败", e);
            }
        }

        try {
            Object result = inv.invoke();
            opLog.setStatus(1);
            opLog.setDuration((int) (System.currentTimeMillis() - startTime));
            saveLog(opLog);
            return result;
        } catch (Throwable e) {
            opLog.setStatus(0);
            opLog.setErrorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "unknown");
            opLog.setDuration((int) (System.currentTimeMillis() - startTime));
            saveLog(opLog);
            throw e;
        }
    }

    private void saveLog(SysOperationLog opLog) {
        try {
            operationLogMapper.insert(opLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
