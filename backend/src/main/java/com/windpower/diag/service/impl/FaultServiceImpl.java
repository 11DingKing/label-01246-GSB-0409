package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.FaultRecord;
import com.windpower.diag.entity.SysUser;
import com.windpower.diag.entity.SysUserRole;
import com.windpower.diag.entity.WindTurbine;
import com.windpower.diag.mapper.FaultRecordMapper;
import com.windpower.diag.mapper.SysUserMapper;
import com.windpower.diag.mapper.SysUserRoleMapper;
import com.windpower.diag.mapper.WindTurbineMapper;
import com.windpower.diag.service.EmailService;
import com.windpower.diag.service.FaultService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class FaultServiceImpl implements FaultService {
    private static final Logger log = LoggerFactory.getLogger(FaultServiceImpl.class);

    @Ds
    private FaultRecordMapper faultRecordMapper;
    @Ds
    private WindTurbineMapper turbineMapper;
    @Ds
    private SysUserMapper userMapper;
    @Ds
    private SysUserRoleMapper userRoleMapper;
    @Inject
    private EmailService emailService;

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
    public void save(FaultRecord faultRecord) {
        // 设置创建时间
        faultRecord.setCreatedAt(LocalDateTime.now());
        faultRecord.setUpdatedAt(LocalDateTime.now());

        // 保存故障记录
        faultRecordMapper.insert(faultRecord);
        log.info("故障记录已保存: faultCode={}, level={}", faultRecord.getFaultCode(), faultRecord.getFaultLevel());

        // 检查是否为严重故障 (level >= 3: HIGH=3, CRITICAL=4)
        if (isSevereFault(faultRecord.getFaultLevel())) {
            log.info("检测到严重故障，准备发送告警邮件: faultCode={}", faultRecord.getFaultCode());
            sendFaultAlert(faultRecord);
        }
    }

    /**
     * 判断是否为严重故障
     * 故障等级: LOW=1, MEDIUM=2, HIGH=3, CRITICAL=4
     * level >= 3 视为严重故障
     */
    private boolean isSevereFault(String faultLevel) {
        if (faultLevel == null) {
            return false;
        }
        return "HIGH".equals(faultLevel) || "CRITICAL".equals(faultLevel);
    }

    /**
     * 发送严重故障告警邮件
     */
    private void sendFaultAlert(FaultRecord faultRecord) {
        try {
            // 获取风机信息
            WindTurbine turbine = turbineMapper.selectById(faultRecord.getTurbineId());
            if (turbine == null) {
                log.warn("无法发送告警邮件：风机不存在, turbineId={}", faultRecord.getTurbineId());
                return;
            }

            // 获取风机所属部门的所有管理员邮箱
            List<String> adminEmails = getDeptAdminEmails(turbine.getDeptId());
            if (adminEmails.isEmpty()) {
                log.warn("无法发送告警邮件：部门 {} 没有管理员邮箱", turbine.getDeptId());
                return;
            }

            // 发送告警邮件
            emailService.sendFaultAlertEmail(adminEmails, faultRecord, turbine);
        } catch (Exception e) {
            log.error("发送严重故障告警邮件失败: faultCode={}", faultRecord.getFaultCode(), e);
        }
    }

    /**
     * 获取指定部门的所有管理员邮箱
     */
    private List<String> getDeptAdminEmails(Long deptId) {
        if (deptId == null) {
            return new ArrayList<>();
        }

        // 查询该部门下所有用户
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getDeptId, deptId);
        userWrapper.eq(SysUser::getStatus, 1); // 只查询正常状态用户
        List<SysUser> users = userMapper.selectList(userWrapper);

        if (users.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取用户ID列表
        List<Long> userIds = users.stream()
                .map(SysUser::getId)
                .collect(Collectors.toList());

        // 查询这些用户中哪些是管理员（role_code = 'admin'）
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.in(SysUserRole::getUserId, userIds);
        List<SysUserRole> userRoles = userRoleMapper.selectList(urWrapper);

        // 获取管理员用户ID
        Set<Long> adminUserIds = userRoles.stream()
                .filter(ur -> {
                    // 这里简化处理，假设 role_id 为 1 是管理员
                    // 实际项目中应该查询 sys_role 表确认 role_code
                    return ur.getRoleId() != null && ur.getRoleId() == 1L;
                })
                .map(SysUserRole::getUserId)
                .collect(Collectors.toSet());

        // 返回管理员邮箱列表
        return users.stream()
                .filter(u -> adminUserIds.contains(u.getId()))
                .map(SysUser::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .collect(Collectors.toList());
    }
}
