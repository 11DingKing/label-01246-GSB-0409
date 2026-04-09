package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.FaultRecord;
import com.windpower.diag.mapper.FaultRecordMapper;
import com.windpower.diag.service.FaultService;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Ds;

import java.util.*;

@Component
public class FaultServiceImpl implements FaultService {

    @Ds
    private FaultRecordMapper faultRecordMapper;

    @Override
    public PageResult<FaultRecord> page(int current, int size, String faultType, String faultLevel, Integer status, Long turbineId) {
        IPage<FaultRecord> page = faultRecordMapper.selectPageWithTurbine(
                new Page<>(current, size), faultType, faultLevel, status, turbineId);
        return PageResult.of(page);
    }

    @Override
    public FaultRecord getById(Long id) {
        return faultRecordMapper.selectById(id);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<FaultRecord> all = faultRecordMapper.selectList(null);

        long total = all.size();
        long pending = all.stream().filter(f -> f.getStatus() == 1).count();
        long processing = all.stream().filter(f -> f.getStatus() == 2).count();
        long resolved = all.stream().filter(f -> f.getStatus() == 3).count();
        long closed = all.stream().filter(f -> f.getStatus() == 4).count();

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("processing", processing);
        stats.put("resolved", resolved);
        stats.put("closed", closed);

        // 按故障类型统计
        Map<String, Long> typeCount = new LinkedHashMap<>();
        for (FaultRecord f : all) {
            typeCount.merge(f.getFaultType() != null ? f.getFaultType() : "未知", 1L, Long::sum);
        }
        stats.put("typeDistribution", typeCount);

        // 按故障等级统计
        Map<String, Long> levelCount = new LinkedHashMap<>();
        for (FaultRecord f : all) {
            levelCount.merge(f.getFaultLevel() != null ? f.getFaultLevel() : "未知", 1L, Long::sum);
        }
        stats.put("levelDistribution", levelCount);

        return stats;
    }
}
