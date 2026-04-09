package com.windpower.diag.controller;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.common.Result;
import com.windpower.diag.entity.WindTurbine;
import com.windpower.diag.service.TurbineService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;

import java.util.List;
import java.util.Map;

@Controller
@Mapping("/api/turbine")
public class TurbineController {

    @Inject
    private TurbineService turbineService;

    @Get
    @Mapping("/page")
    public Result<PageResult<WindTurbine>> page(
            Context ctx,
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            @Param(required = false) String turbineCode,
            @Param(required = false) Integer status) {
        Long userId = ctx.attr("userId", Long.class);
        return Result.ok(turbineService.page(current, size, turbineCode, status, userId));
    }

    @Get
    @Mapping("/list")
    public Result<List<WindTurbine>> list(Context ctx) {
        Long userId = ctx.attr("userId", Long.class);
        return Result.ok(turbineService.listAll(userId));
    }

    @Get
    @Mapping("/{id}")
    public Result<WindTurbine> detail(@Path Long id) {
        return Result.ok(turbineService.getById(id));
    }

    @Get
    @Mapping("/monitor")
    public Result<Map<String, Object>> monitorData(Context ctx, @Param(required = false) Long turbineId) {
        Long userId = ctx.attr("userId", Long.class);
        return Result.ok(turbineService.getMonitorData(turbineId, userId));
    }

    @Get
    @Mapping("/dashboard-stats")
    public Result<Map<String, Object>> dashboardStats(Context ctx) {
        Long userId = ctx.attr("userId", Long.class);
        return Result.ok(turbineService.getDashboardStats(userId));
    }
}
