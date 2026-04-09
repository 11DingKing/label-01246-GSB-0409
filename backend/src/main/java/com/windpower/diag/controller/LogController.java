package com.windpower.diag.controller;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.common.Result;
import com.windpower.diag.entity.SysLoginLog;
import com.windpower.diag.entity.SysOperationLog;
import com.windpower.diag.service.LogService;
import org.noear.solon.annotation.*;

@Controller
@Mapping("/api/log")
public class LogController {

    @Inject
    private LogService logService;

    @Get
    @Mapping("/operation/page")
    public Result<PageResult<SysOperationLog>> operationLogPage(
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            @Param(required = false) String username,
            @Param(required = false) String module) {
        return Result.ok(logService.pageOperationLog(current, size, username, module));
    }

    @Get
    @Mapping("/login/page")
    public Result<PageResult<SysLoginLog>> loginLogPage(
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            @Param(required = false) String username,
            @Param(required = false) Integer status) {
        return Result.ok(logService.pageLoginLog(current, size, username, status));
    }
}
