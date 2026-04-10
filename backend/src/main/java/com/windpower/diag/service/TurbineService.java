package com.windpower.diag.service;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.WindTurbine;
import java.util.List;
import java.util.Map;

public interface TurbineService {
    PageResult<WindTurbine> page(int current, int size, String turbineCode, Integer status, Long operatorUserId);
    List<WindTurbine> listAll(Long operatorUserId);
    WindTurbine getById(Long id);
    Map<String, Object> getMonitorData(Long turbineId);
    Map<String, Object> getDashboardStats();
}
