package com.windpower.diag.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("fault_record")
public class FaultRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long turbineId;
    private String faultCode;
    private String faultType;
    private String faultLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String description;
    private String diagnosis;
    private String solution;
    private Integer status; // 1-待处理 2-处理中 3-已解决 4-已关闭
    private String handler;
    private LocalDateTime faultTime;
    private LocalDateTime resolveTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String turbineName;
    @TableField(exist = false)
    private String turbineCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTurbineId() { return turbineId; }
    public void setTurbineId(Long turbineId) { this.turbineId = turbineId; }
    public String getFaultCode() { return faultCode; }
    public void setFaultCode(String faultCode) { this.faultCode = faultCode; }
    public String getFaultType() { return faultType; }
    public void setFaultType(String faultType) { this.faultType = faultType; }
    public String getFaultLevel() { return faultLevel; }
    public void setFaultLevel(String faultLevel) { this.faultLevel = faultLevel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
    public LocalDateTime getFaultTime() { return faultTime; }
    public void setFaultTime(LocalDateTime faultTime) { this.faultTime = faultTime; }
    public LocalDateTime getResolveTime() { return resolveTime; }
    public void setResolveTime(LocalDateTime resolveTime) { this.resolveTime = resolveTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getTurbineName() { return turbineName; }
    public void setTurbineName(String turbineName) { this.turbineName = turbineName; }
    public String getTurbineCode() { return turbineCode; }
    public void setTurbineCode(String turbineCode) { this.turbineCode = turbineCode; }
}
