package com.windpower.diag.service;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.SysUser;
import java.util.List;

public interface UserService {
    PageResult<SysUser> page(int current, int size, String username, String realName, Integer status, Long operatorUserId);
    SysUser getById(Long id);
    void create(SysUser user);
    void update(SysUser user);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void updateLock(Long id, Integer locked);
    void assignRoles(Long userId, List<Long> roleIds);
    List<Long> getRoleIds(Long userId);
}
