package com.windpower.diag.event;

public class FaultEvent {
    private Long faultRecordId;
    private Long turbineId;
    private String turbineCode;
    private String faultType;
    private String faultLevel;
    private String faultTime;
    private Long deptId;

    public FaultEvent() {
    }

    public FaultEvent(Long faultRecordId, Long turbineId, String turbineCode, String faultType, String faultLevel, String faultTime, Long deptId) {
        this.faultRecordId = faultRecordId;
        this.turbineId = turbineId;
        this.turbineCode = turbineCode;
        this.faultType = faultType;
        this.faultLevel = faultLevel;
        this.faultTime = faultTime;
        this.deptId = deptId;
    }

    public Long getFaultRecordId() {
        return faultRecordId;
    }

    public void setFaultRecordId(Long faultRecordId) {
        this.faultRecordId = faultRecordId;
    }

    public Long getTurbineId() {
        return turbineId;
    }

    public void setTurbineId(Long turbineId) {
        this.turbineId = turbineId;
    }

    public String getTurbineCode() {
        return turbineCode;
    }

    public void setTurbineCode(String turbineCode) {
        this.turbineCode = turbineCode;
    }

    public String getFaultType() {
        return faultType;
    }

    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }

    public String getFaultLevel() {
        return faultLevel;
    }

    public void setFaultLevel(String faultLevel) {
        this.faultLevel = faultLevel;
    }

    public String getFaultTime() {
        return faultTime;
    }

    public void setFaultTime(String faultTime) {
        this.faultTime = faultTime;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }
}
