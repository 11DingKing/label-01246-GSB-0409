package com.windpower.diag.service;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.FaultRecord;
import java.util.Map;

public interface FaultService {
    PageResult<FaultRecord> page(int current, int size, String faultType, String faultLevel, Integer status, Long turbineId);
    FaultRecord getById(Long id);
    Map<String, Object> getStatistics();
    boolean save(FaultRecord faultRecord);
}
