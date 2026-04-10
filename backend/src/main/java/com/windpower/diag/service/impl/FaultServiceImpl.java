package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.FaultRecord;
import com.windpower.diag.entity.WindTurbine;
import com.windpower.diag.event.FaultAlarmEvent;
import com.windpower.diag.mapper.FaultRecordMapper;
import com.windpower.diag.mapper.SysUserMapper;
import com.windpower.diag.mapper.WindTurbineMapper;
import com.windpower.diag.service.FaultService;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Ds;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class FaultServiceImpl implements FaultService {
    private static final Logger log = LoggerFactory.getLogger(FaultServiceImpl.class);

    @Ds
    private FaultRecordMapper faultRecordMapper;

    @Ds
    private WindTurbineMapper windTurbineMapper;

    @Ds
    private SysUserMapper sysUserMapper;

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

    @Override
    @Transaction(rollbackFor = Exception.class)
    public boolean save(FaultRecord faultRecord) {
        faultRecord.setCreateTime(LocalDateTime.now());
        int rows = faultRecordMapper.insert(faultRecord);
        if (rows > 0) {
            checkAndSendAlarm(faultRecord);
        }
        return rows > 0;
    }

    private void checkAndSendAlarm(FaultRecord faultRecord) {
        int level = parseLevel(faultRecord.getFaultLevel());
        if (level < 3) {
            return;
        }
        try {
            WindTurbine turbine = windTurbineMapper.selectById(faultRecord.getTurbineId());
            if (turbine == null || turbine.getDeptId() == null) {
                log.warn("风机信息不完整，无法发送告警邮件: faultRecordId={}", faultRecord.getId());
                return;
            }

            List<String> adminEmails = sysUserMapper.selectAdminEmailsByDeptId(turbine.getDeptId());
            if (adminEmails == null || adminEmails.isEmpty()) {
                log.warn("部门[{}]无管理员邮箱，无法发送告警邮件", turbine.getDeptId());
                return;
            }

            String subject = "【严重故障告警】风机编号: " + turbine.getTurbineCode();
            String content = buildAlarmContent(turbine, faultRecord);
            EventBus.publish(new FaultAlarmEvent(adminEmails, subject, content));
            log.info("严重故障事件已发布: turbineCode={}, adminEmailCount={}", turbine.getTurbineCode(), adminEmails.size());
        } catch (Exception e) {
            log.error("发送告警邮件失败: error={}", e.getMessage(), e);
        }
    }

    private int parseLevel(String faultLevel) {
        if (faultLevel == null) return 0;
        return switch (faultLevel.toUpperCase()) {
            case "LOW" -> 1;
            case "MEDIUM" -> 2;
            case "HIGH" -> 3;
            case "CRITICAL" -> 4;
            default -> 0;
        };
    }

    private String buildAlarmContent(WindTurbine turbine, FaultRecord faultRecord) {
        return String.format(
                "<p><strong>风机编号：</strong>%s</p>" +
                "<p><strong>风机名称：</strong>%s</p>" +
                "<p><strong>故障类型：</strong>%s</p>" +
                "<p><strong>故障等级：</strong>%s</p>" +
                "<p><strong>故障时间：</strong>%s</p>" +
                "<p><strong>故障描述：</strong>%s</p>",
                turbine.getTurbineCode(),
                turbine.getTurbineName() != null ? turbine.getTurbineName() : "--",
                faultRecord.getFaultType() != null ? faultRecord.getFaultType() : "--",
                faultRecord.getFaultLevel() != null ? faultRecord.getFaultLevel() : "--",
                faultRecord.getFaultTime() != null ? faultRecord.getFaultTime().toString() : "--",
                faultRecord.getDescription() != null ? faultRecord.getDescription() : "暂无描述"
        );
    }
}
