package com.windpower.diag.service;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.FaultRecord;
import java.util.Map;

public interface FaultService {
    PageResult<FaultRecord> page(int current, int size, String faultType, String faultLevel, Integer status, Long turbineId);
    FaultRecord getById(Long id);
    Map<String, Object> getStatistics();
    /**
     * 保存故障记录，如果是严重故障(level >= 3)会自动触发邮件告警
     * @param faultRecord 故障记录
     */
    void save(FaultRecord faultRecord);
}
