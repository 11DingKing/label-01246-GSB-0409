package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.SysLoginLog;
import com.windpower.diag.entity.SysOperationLog;
import com.windpower.diag.mapper.SysLoginLogMapper;
import com.windpower.diag.mapper.SysOperationLogMapper;
import com.windpower.diag.service.LogService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LogServiceImpl implements LogService {
    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);

    @Ds
    private SysOperationLogMapper operationLogMapper;
    @Ds
    private SysLoginLogMapper loginLogMapper;

    @Override
    public PageResult<SysOperationLog> pageOperationLog(int current, int size, String username, String module) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysOperationLog::getUsername, username);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.like(SysOperationLog::getModule, module);
        }
        wrapper.orderByDesc(SysOperationLog::getCreatedAt);
        Page<SysOperationLog> page = operationLogMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public PageResult<SysLoginLog> pageLoginLog(int current, int size, String username, Integer status) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysLoginLog::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysLoginLog::getStatus, status);
        }
        wrapper.orderByDesc(SysLoginLog::getCreatedAt);
        Page<SysLoginLog> page = loginLogMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public void saveLoginLog(SysLoginLog loginLog) {
        try {
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("保存登录日志失败", e);
        }
    }

    @Override
    public void saveOperationLog(SysOperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
}
