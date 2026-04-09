package com.windpower.diag.event;

import java.util.List;

public class FaultAlarmEvent {
    private List<String> adminEmails;
    private String subject;
    private String content;

    public FaultAlarmEvent() {
    }

    public FaultAlarmEvent(List<String> adminEmails, String subject, String content) {
        this.adminEmails = adminEmails;
        this.subject = subject;
        this.content = content;
    }

    public List<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
