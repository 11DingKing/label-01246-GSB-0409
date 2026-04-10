package com.windpower.diag.service.impl;

import com.windpower.diag.service.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

@Component
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Inject("${mail.host}")
    private String host;
    @Inject("${mail.port}")
    private int port;
    @Inject("${mail.username}")
    private String username;
    @Inject("${mail.password}")
    private String password;
    @Inject("${mail.ssl}")
    private boolean ssl;
    @Inject("${mail.from}")
    private String from;
    @Inject("${mail.fromName}")
    private String fromName;
    @Inject("${email-verify.baseUrl}")
    private String baseUrl;
    @Inject("${email-verify.expireMinutes}")
    private int expireMinutes;

    @Override
    public void sendVerifyEmail(String toEmail, String userName, String verifyToken) {
        String verifyUrl = baseUrl + "/api/auth/verify-email?token=" + verifyToken;
        String subject = "【风力发电机故障诊断系统】邮箱验证";
        String htmlContent = buildVerifyHtml(userName, verifyUrl, expireMinutes);

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", String.valueOf(port));
            props.put("mail.smtp.auth", "true");
            if (ssl) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            }

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, fromName, "UTF-8"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlContent, "text/html;charset=UTF-8");

            Transport.send(message);
            log.info("验证邮件已发送: toEmail={}, userName={}", toEmail, userName);
        } catch (Exception e) {
            log.error("发送验证邮件失败: toEmail={}, error={}", toEmail, e.getMessage(), e);
            // 不抛异常，避免阻塞注册流程；记录日志即可
        }
    }

    @Override
    public void sendFaultAlertEmail(List<String> toEmails, String turbineCode, String faultType, String faultLevel, String faultTime) {
        if (toEmails == null || toEmails.isEmpty()) {
            return;
        }
        String subject = "【严重故障告警】风力发电机发生严重等级故障";
        String htmlContent = buildFaultAlertHtml(turbineCode, faultType, faultLevel, faultTime);

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", String.valueOf(port));
            props.put("mail.smtp.auth", "true");
            if (ssl) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", String.valueOf(port));
            }

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, fromName, "UTF-8"));
            for (String email : toEmails) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            }
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlContent, "text/html;charset=UTF-8");

            Transport.send(message);
            log.info("故障告警邮件已发送: toEmails={}, turbineCode={}", toEmails, turbineCode);
        } catch (Exception e) {
            log.error("发送故障告警邮件失败: toEmails={}, error={}", toEmails, e.getMessage(), e);
        }
    }

    private String buildVerifyHtml(String userName, String verifyUrl, int expireMinutes) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;'>"
                + "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:8px;padding:40px;box-shadow:0 2px 8px rgba(0,0,0,0.1);'>"
                + "<h2 style='color:#165DFF;text-align:center;'>风力发电机故障诊断系统</h2>"
                + "<p>您好，<strong>" + userName + "</strong>：</p>"
                + "<p>感谢您注册风力发电机故障诊断系统。请点击下方按钮验证您的邮箱地址：</p>"
                + "<div style='text-align:center;margin:30px 0;'>"
                + "<a href='" + verifyUrl + "' style='display:inline-block;padding:12px 40px;background:#165DFF;color:#fff;text-decoration:none;border-radius:6px;font-size:16px;'>验证邮箱</a>"
                + "</div>"
                + "<p style='color:#999;font-size:13px;'>如果按钮无法点击，请复制以下链接到浏览器打开：</p>"
                + "<p style='color:#165DFF;font-size:13px;word-break:break-all;'>" + verifyUrl + "</p>"
                + "<p style='color:#999;font-size:13px;'>此链接有效期为 " + expireMinutes + " 分钟。如非本人操作，请忽略此邮件。</p>"
                + "<hr style='border:none;border-top:1px solid #eee;margin:20px 0;'>"
                + "<p style='color:#ccc;font-size:12px;text-align:center;'>此邮件由系统自动发送，请勿回复</p>"
                + "</div></body></html>";
    }

    private String buildFaultAlertHtml(String turbineCode, String faultType, String faultLevel, String faultTime) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:Arial,sans-serif;background:#fff5f5;padding:20px;'>"
                + "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:8px;padding:40px;box-shadow:0 2px 8px rgba(245,63,63,0.2);border:1px solid #ffcccc;'>"
                + "<h2 style='color:#F53F3F;text-align:center;'>⚠️ 严重故障告警</h2>"
                + "<p style='text-align:center;color:#666;margin-bottom:30px;'>风力发电机发生严重等级故障，请立即处理</p>"
                + "<table style='width:100%;border-collapse:collapse;'>"
                + "<tr><td style='padding:12px;border-bottom:1px solid #eee;background:#fff5f5;color:#F53F3F;font-weight:bold;'>风机编号</td><td style='padding:12px;border-bottom:1px solid #eee;'>" + turbineCode + "</td></tr>"
                + "<tr><td style='padding:12px;border-bottom:1px solid #eee;background:#fff5f5;color:#F53F3F;font-weight:bold;'>故障类型</td><td style='padding:12px;border-bottom:1px solid #eee;'>" + faultType + "</td></tr>"
                + "<tr><td style='padding:12px;border-bottom:1px solid #eee;background:#fff5f5;color:#F53F3F;font-weight:bold;'>故障等级</td><td style='padding:12px;border-bottom:1px solid #eee;'><span style='background:#F53F3F;color:white;padding:2px 8px;border-radius:4px;'>" + faultLevel + "</span></td></tr>"
                + "<tr><td style='padding:12px;border-bottom:1px solid #eee;background:#fff5f5;color:#F53F3F;font-weight:bold;'>发生时间</td><td style='padding:12px;border-bottom:1px solid #eee;'>" + faultTime + "</td></tr>"
                + "</table>"
                + "<p style='margin-top:30px;padding:15px;background:#fff5f5;border-radius:6px;color:#F53F3F;text-align:center;'>请立即登录系统查看故障详情并安排检修人员！</p>"
                + "<hr style='border:none;border-top:1px solid #eee;margin:20px 0;'>"
                + "<p style='color:#ccc;font-size:12px;text-align:center;'>此邮件由系统自动发送，请勿回复</p>"
                + "</div></body></html>";
    }
}
