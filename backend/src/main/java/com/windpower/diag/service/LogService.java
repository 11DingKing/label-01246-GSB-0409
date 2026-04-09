package com.windpower.diag.service;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.SysLoginLog;
import com.windpower.diag.entity.SysOperationLog;

public interface LogService {
    PageResult<SysOperationLog> pageOperationLog(int current, int size, String username, String module);
    PageResult<SysLoginLog> pageLoginLog(int current, int size, String username, Integer status);
    void saveLoginLog(SysLoginLog log);
    void saveOperationLog(SysOperationLog log);
}
