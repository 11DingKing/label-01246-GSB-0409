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
     * 发送故障告警邮件
     * @param toEmails 收件人邮箱列表
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendFaultAlarmEmail(List<String> toEmails, String subject, String content);
}
