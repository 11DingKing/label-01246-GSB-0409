package com.windpower.diag.service;

import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.SysRole;
import java.util.List;

public interface RoleService {
    PageResult<SysRole> page(int current, int size, String roleName, Integer status);
    List<SysRole> listAll();
    SysRole getById(Long id);
    void create(SysRole role);
    void update(SysRole role);
    void delete(Long id);
    void assignPermissions(Long roleId, List<Long> permIds);
    List<Long> getPermissionIds(Long roleId);
    void updateDataScope(Long roleId, Integer scopeType);
    Integer getDataScope(Long roleId);
}
