package com.windpower.diag.controller;

import com.windpower.diag.common.Result;
import com.windpower.diag.entity.SysConfig;
import com.windpower.diag.service.ConfigService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import com.windpower.diag.aop.OperationLog;

import java.util.List;

@Controller
@Mapping("/api/config")
public class ConfigController {

    @Inject
    private ConfigService configService;

    @Get
    @Mapping("/list")
    public Result<List<SysConfig>> list(@Param(required = false) String group) {
        if (group != null && !group.isEmpty()) {
            return Result.ok(configService.listByGroup(group));
        }
        return Result.ok(configService.listAll());
    }

    @Put
    @Mapping("/{id}")
    @OperationLog(module = "系统配置", operation = "修改配置项")
    public Result<Void> update(@Path Long id, Context ctx) {
        String configValue = ctx.param("configValue");
        configService.update(id, configValue);
        return Result.ok();
    }

    @Put
    @Mapping("/batch")
    @OperationLog(module = "系统配置", operation = "批量修改配置")
    public Result<Void> batchUpdate(@Body List<SysConfig> configs) {
        configService.batchUpdate(configs);
        return Result.ok();
    }
}
