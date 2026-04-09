package com.windpower.diag.controller;

import com.windpower.diag.common.Result;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HealthController {

    @Get
    @Mapping("/")
    public Result<Map<String, String>> health() {
        Map<String, String> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "wind-power-diag");
        return Result.ok(data);
    }
}
