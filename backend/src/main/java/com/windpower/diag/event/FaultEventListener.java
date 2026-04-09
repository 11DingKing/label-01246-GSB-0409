package com.windpower.diag.event;

import com.windpower.diag.mapper.SysUserMapper;
import com.windpower.diag.service.EmailService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.event.EventListener;

import java.util.List;

@Component
public class FaultEventListener implements EventListener<FaultEvent> {
    @Inject
    private SysUserMapper sysUserMapper;

    @Inject
    private EmailService emailService;

    @Override
    public void onEvent(FaultEvent event) {
        if (event.getDeptId() != null) {
            List<String> adminEmails = sysUserMapper.selectAdminEmailsByDeptId(event.getDeptId());
            if (!adminEmails.isEmpty()) {
                emailService.sendFaultAlertEmail(
                        adminEmails,
                        event.getTurbineCode(),
                        event.getFaultType(),
                        event.getFaultLevel(),
                        event.getFaultTime()
                );
            }
        }
    }
}
