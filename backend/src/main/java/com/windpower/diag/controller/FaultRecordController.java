package com.windpower.diag.controller;

import com.windpower.diag.entity.FaultRecord;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.result.Result;
import com.windpower.diag.service.FaultService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;

import java.util.Map;

@Valid
@Controller
@Mapping("/api/fault")
public class FaultRecordController {

    @Inject
    private FaultService faultService;

    @Get
    @Mapping("/page")
    public Result<PageResult<FaultRecord>> page(
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            String faultType, String faultLevel, Integer status, Long turbineId) {
        PageResult<FaultRecord> page = faultService.page(current, size, faultType, faultLevel, status, turbineId);
        return Result.success(page);
    }

    @Get
    @Mapping("/{id}")
    public Result<FaultRecord> getById(Long id) {
        FaultRecord faultRecord = faultService.getById(id);
        return Result.success(faultRecord);
    }

    @Get
    @Mapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = faultService.getStatistics();
        return Result.success(statistics);
    }

    @Post
    @Mapping
    public Result<Boolean> save(@Body FaultRecord faultRecord) {
        boolean success = faultService.save(faultRecord);
        return success ? Result.success(true) : Result.error("保存失败");
    }
}
