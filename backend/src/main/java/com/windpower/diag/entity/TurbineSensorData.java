package com.windpower.diag.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("turbine_sensor_data")
public class TurbineSensorData implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long turbineId;
    private BigDecimal windSpeed;
    private BigDecimal rotorSpeed;
    private BigDecimal power;
    private BigDecimal nacelleTemp;
    private BigDecimal bearingTemp;
    private BigDecimal gearboxTemp;
    private BigDecimal vibrationX;
    private BigDecimal vibrationY;
    private BigDecimal vibrationZ;
    private BigDecimal pitchAngle;
    private BigDecimal yawAngle;
    private LocalDateTime recordTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTurbineId() { return turbineId; }
    public void setTurbineId(Long turbineId) { this.turbineId = turbineId; }
    public BigDecimal getWindSpeed() { return windSpeed; }
    public void setWindSpeed(BigDecimal windSpeed) { this.windSpeed = windSpeed; }
    public BigDecimal getRotorSpeed() { return rotorSpeed; }
    public void setRotorSpeed(BigDecimal rotorSpeed) { this.rotorSpeed = rotorSpeed; }
    public BigDecimal getPower() { return power; }
    public void setPower(BigDecimal power) { this.power = power; }
    public BigDecimal getNacelleTemp() { return nacelleTemp; }
    public void setNacelleTemp(BigDecimal nacelleTemp) { this.nacelleTemp = nacelleTemp; }
    public BigDecimal getBearingTemp() { return bearingTemp; }
    public void setBearingTemp(BigDecimal bearingTemp) { this.bearingTemp = bearingTemp; }
    public BigDecimal getGearboxTemp() { return gearboxTemp; }
    public void setGearboxTemp(BigDecimal gearboxTemp) { this.gearboxTemp = gearboxTemp; }
    public BigDecimal getVibrationX() { return vibrationX; }
    public void setVibrationX(BigDecimal vibrationX) { this.vibrationX = vibrationX; }
    public BigDecimal getVibrationY() { return vibrationY; }
    public void setVibrationY(BigDecimal vibrationY) { this.vibrationY = vibrationY; }
    public BigDecimal getVibrationZ() { return vibrationZ; }
    public void setVibrationZ(BigDecimal vibrationZ) { this.vibrationZ = vibrationZ; }
    public BigDecimal getPitchAngle() { return pitchAngle; }
    public void setPitchAngle(BigDecimal pitchAngle) { this.pitchAngle = pitchAngle; }
    public BigDecimal getYawAngle() { return yawAngle; }
    public void setYawAngle(BigDecimal yawAngle) { this.yawAngle = yawAngle; }
    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }
}
