package com.windpower.diag.service;

import com.windpower.diag.entity.FaultRecord;
import com.windpower.diag.entity.WindTurbine;

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
     * 发送严重故障告警邮件
     * @param toEmails 收件人邮箱列表
     * @param faultRecord 故障记录
     * @param turbine 风机信息
     */
    void sendFaultAlertEmail(List<String> toEmails, FaultRecord faultRecord, WindTurbine turbine);
}
