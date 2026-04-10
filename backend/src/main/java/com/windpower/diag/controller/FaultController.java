package com.windpower.diag.controller;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.common.Result;
import com.windpower.diag.entity.FaultRecord;
import com.windpower.diag.service.FaultService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;

import java.util.Map;

@Controller
@Mapping("/api/fault")
public class FaultController {

    @Inject
    private FaultService faultService;

    @Get
    @Mapping("/page")
    public Result<PageResult<FaultRecord>> page(
            Context ctx,
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            @Param(required = false) String faultType,
            @Param(required = false) String faultLevel,
            @Param(required = false) Integer status,
            @Param(required = false) Long turbineId) {
        Long userId = (Long) ctx.attr("userId");
        return Result.ok(faultService.page(current, size, faultType, faultLevel, status, turbineId, userId));
    }

    @Get
    @Mapping("/{id}")
    public Result<FaultRecord> detail(@Path Long id) {
        return Result.ok(faultService.getById(id));
    }

    @Get
    @Mapping("/statistics")
    public Result<Map<String, Object>> statistics(Context ctx) {
        Long userId = (Long) ctx.attr("userId");
        return Result.ok(faultService.getStatistics(userId));
    }

    @Post
    @Mapping
    public Result<FaultRecord> create(Context ctx, @Body FaultRecord faultRecord) {
        Long userId = (Long) ctx.attr("userId");
        return Result.ok(faultService.create(faultRecord, userId));
    }
}
