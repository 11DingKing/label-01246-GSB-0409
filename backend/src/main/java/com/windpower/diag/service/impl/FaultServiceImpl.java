package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.FaultRecord;
import com.windpower.diag.entity.WindTurbine;
import com.windpower.diag.mapper.FaultRecordMapper;
import com.windpower.diag.mapper.SysUserMapper;
import com.windpower.diag.mapper.WindTurbineMapper;
import com.windpower.diag.service.DataScopeService;
import com.windpower.diag.service.EmailService;
import com.windpower.diag.service.FaultService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class FaultServiceImpl implements FaultService {

    @Ds
    private FaultRecordMapper faultRecordMapper;
    @Ds
    private WindTurbineMapper windTurbineMapper;
    @Ds
    private SysUserMapper sysUserMapper;
    @Inject
    private DataScopeService dataScopeService;
    @Inject
    private EmailService emailService;

    @Override
    public PageResult<FaultRecord> page(int current, int size, String faultType, String faultLevel, Integer status, Long turbineId, Long userId) {
        List<Long> visibleDeptIds = dataScopeService.getVisibleDeptIds(userId);
        IPage<FaultRecord> page = faultRecordMapper.selectPageWithTurbine(
                new Page<>(current, size), faultType, faultLevel, status, turbineId, visibleDeptIds);
        return PageResult.of(page);
    }

    @Override
    public FaultRecord getById(Long id) {
        return faultRecordMapper.selectById(id);
    }

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        List<Long> visibleDeptIds = dataScopeService.getVisibleDeptIds(userId);
        LambdaQueryWrapper<WindTurbine> turbineWrapper = new LambdaQueryWrapper<>();
        if (visibleDeptIds != null) {
            if (visibleDeptIds.isEmpty()) {
                turbineWrapper.in(WindTurbine::getDeptId, -1);
            } else {
                turbineWrapper.in(WindTurbine::getDeptId, visibleDeptIds);
            }
        }
        List<Long> visibleTurbineIds = windTurbineMapper.selectList(turbineWrapper).stream()
                .map(WindTurbine::getId).toList();
        LambdaQueryWrapper<FaultRecord> faultWrapper = new LambdaQueryWrapper<>();
        if (!visibleTurbineIds.isEmpty()) {
            faultWrapper.in(FaultRecord::getTurbineId, visibleTurbineIds);
        } else if (visibleDeptIds != null) {
            faultWrapper.in(FaultRecord::getTurbineId, -1);
        }
        List<FaultRecord> all = faultRecordMapper.selectList(faultWrapper);

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

    @Override
    public FaultRecord create(FaultRecord faultRecord, Long userId) {
        faultRecord.setCreatedAt(LocalDateTime.now());
        faultRecord.setStatus(1);
        faultRecordMapper.insert(faultRecord);

        int levelNum = getLevelNumber(faultRecord.getFaultLevel());
        if (levelNum >= 3) {
            WindTurbine turbine = windTurbineMapper.selectById(faultRecord.getTurbineId());
            if (turbine != null && turbine.getDeptId() != null) {
                List<String> adminEmails = sysUserMapper.selectAdminEmailsByDeptId(turbine.getDeptId());
                if (!adminEmails.isEmpty()) {
                    String faultTime = faultRecord.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    emailService.sendFaultAlertEmail(adminEmails, turbine.getTurbineCode(),
                            faultRecord.getFaultType(), faultRecord.getFaultLevel(), faultTime);
                }
            }
        }
        return faultRecord;
    }

    private int getLevelNumber(String level) {
        if (level == null) return 0;
        return switch (level) {
            case "LOW" -> 1;
            case "MEDIUM" -> 2;
            case "HIGH" -> 3;
            case "CRITICAL" -> 4;
            default -> 0;
        };
    }
}
