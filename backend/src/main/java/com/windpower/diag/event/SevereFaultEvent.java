package com.windpower.diag.event;

import com.windpower.diag.entity.FaultRecord;

/**
 * 严重故障事件
 * 当故障等级 >= 3 (HIGH, CRITICAL) 时触发
 */
public class SevereFaultEvent {

    private final FaultRecord faultRecord;

    public SevereFaultEvent(FaultRecord faultRecord) {
        this.faultRecord = faultRecord;
    }

    public FaultRecord getFaultRecord() {
        return faultRecord;
    }
}
