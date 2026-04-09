package com.windpower.diag.controller;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.common.Result;
import com.windpower.diag.entity.WindTurbine;
import com.windpower.diag.service.TurbineService;
import org.noear.solon.annotation.*;

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
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            @Param(required = false) String turbineCode,
            @Param(required = false) Integer status) {
        return Result.ok(turbineService.page(current, size, turbineCode, status));
    }

    @Get
    @Mapping("/list")
    public Result<List<WindTurbine>> list() {
        return Result.ok(turbineService.listAll());
    }

    @Get
    @Mapping("/{id}")
    public Result<WindTurbine> detail(@Path Long id) {
        return Result.ok(turbineService.getById(id));
    }

    @Get
    @Mapping("/monitor")
    public Result<Map<String, Object>> monitorData(@Param(required = false) Long turbineId) {
        return Result.ok(turbineService.getMonitorData(turbineId));
    }

    @Get
    @Mapping("/dashboard-stats")
    public Result<Map<String, Object>> dashboardStats() {
        return Result.ok(turbineService.getDashboardStats());
    }
}
