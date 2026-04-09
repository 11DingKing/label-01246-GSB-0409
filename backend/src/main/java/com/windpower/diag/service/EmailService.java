package com.windpower.diag.service;

public interface EmailService {
    /**
     * 发送邮箱验证邮件
     * @param toEmail 收件人邮箱
     * @param username 用户名
     * @param verifyToken 验证token
     */
    void sendVerifyEmail(String toEmail, String username, String verifyToken);
}
