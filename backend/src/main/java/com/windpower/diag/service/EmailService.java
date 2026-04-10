package com.windpower.diag.service;

import java.util.List;

public interface EmailService {
    /**
     * 发送邮箱验证邮件
     * @param toEmail 收件人邮箱
     * @param username 用户名
     * @param verifyToken 验证token
     */
    void sendVerifyEmail(String toEmail, String username, String verifyToken);

    /**
     * 批量发送严重故障告警邮件
     * @param toEmails 收件人邮箱列表
     * @param turbineCode 风机编号
     * @param faultType 故障类型
     * @param faultLevel 故障等级
     * @param faultTime 发生时间
     */
    void sendFaultAlertEmail(List<String> toEmails, String turbineCode, String faultType, String faultLevel, String faultTime);
}
