package com.windpower.diag.controller;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.common.Result;
import com.windpower.diag.entity.FaultRecord;
import com.windpower.diag.service.FaultService;
import org.noear.solon.annotation.*;

import java.util.Map;

@Controller
@Mapping("/api/fault")
public class FaultController {

    @Inject
    private FaultService faultService;

    @Get
    @Mapping("/page")
    public Result<PageResult<FaultRecord>> page(
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            @Param(required = false) String faultType,
            @Param(required = false) String faultLevel,
            @Param(required = false) Integer status,
            @Param(required = false) Long turbineId) {
        return Result.ok(faultService.page(current, size, faultType, faultLevel, status, turbineId));
    }

    @Get
    @Mapping("/{id}")
    public Result<FaultRecord> detail(@Path Long id) {
        return Result.ok(faultService.getById(id));
    }

    @Get
    @Mapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        return Result.ok(faultService.getStatistics());
    }
}
