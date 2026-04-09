package com.windpower.diag.listener;

import com.windpower.diag.event.FaultAlarmEvent;
import com.windpower.diag.service.EmailService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.event.EventListener;
import org.noear.solon.scheduling.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class FaultAlarmListener implements EventListener<FaultAlarmEvent> {
    private static final Logger log = LoggerFactory.getLogger(FaultAlarmListener.class);

    @Inject
    private EmailService emailService;

    @Override
    @Async
    public void onEvent(FaultAlarmEvent event) {
        log.info("开始异步处理故障告警邮件: subject={}", event.getSubject());
        try {
            emailService.sendFaultAlarmEmail(
                    event.getAdminEmails(),
                    event.getSubject(),
                    event.getContent()
            );
            log.info("故障告警邮件发送完成: recipients={}", event.getAdminEmails().size());
        } catch (Exception e) {
            log.error("故障告警邮件发送失败: error={}", e.getMessage(), e);
        }
    }
}
