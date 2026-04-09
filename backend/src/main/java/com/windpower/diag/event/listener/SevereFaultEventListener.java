package com.windpower.diag.event.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windpower.diag.entity.FaultRecord;
import com.windpower.diag.entity.SysUser;
import com.windpower.diag.entity.SysUserRole;
import com.windpower.diag.entity.WindTurbine;
import com.windpower.diag.event.SevereFaultEvent;
import com.windpower.diag.mapper.SysUserMapper;
import com.windpower.diag.mapper.SysUserRoleMapper;
import com.windpower.diag.mapper.WindTurbineMapper;
import com.windpower.diag.service.EmailService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.event.EventListener;
import org.noear.solon.data.annotation.Ds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 严重故障事件监听器
 * 负责发送告警邮件
 */
@Component
public class SevereFaultEventListener implements EventListener<SevereFaultEvent> {

    private static final Logger log = LoggerFactory.getLogger(SevereFaultEventListener.class);

    @Ds
    private WindTurbineMapper turbineMapper;
    @Ds
    private SysUserMapper userMapper;
    @Ds
    private SysUserRoleMapper userRoleMapper;
    @Inject
    private EmailService emailService;

    @Override
    public void onEvent(SevereFaultEvent event) {
        FaultRecord faultRecord = event.getFaultRecord();
        log.info("接收到严重故障事件: faultCode={}", faultRecord.getFaultCode());

        try {
            sendFaultAlert(faultRecord);
        } catch (Exception e) {
            log.error("处理严重故障事件失败: faultCode={}", faultRecord.getFaultCode(), e);
        }
    }

    /**
     * 发送严重故障告警邮件
     */
    private void sendFaultAlert(FaultRecord faultRecord) {
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
