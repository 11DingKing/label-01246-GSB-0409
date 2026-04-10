package com.windpower.diag.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("wind_turbine")
public class WindTurbine implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String turbineCode;
    private String turbineName;
    private String model;
    private BigDecimal ratedPower;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer status; // 1-正常运行 2-告警 3-故障 4-停机维护 5-离线
    private Long deptId; // 所属部门ID
    private LocalDateTime installDate;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTurbineCode() { return turbineCode; }
    public void setTurbineCode(String turbineCode) { this.turbineCode = turbineCode; }
    public String getTurbineName() { return turbineName; }
    public void setTurbineName(String turbineName) { this.turbineName = turbineName; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public BigDecimal getRatedPower() { return ratedPower; }
    public void setRatedPower(BigDecimal ratedPower) { this.ratedPower = ratedPower; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public LocalDateTime getInstallDate() { return installDate; }
    public void setInstallDate(LocalDateTime installDate) { this.installDate = installDate; }
    public LocalDateTime getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
