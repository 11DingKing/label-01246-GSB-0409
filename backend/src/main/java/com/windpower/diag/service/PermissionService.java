package com.windpower.diag.service;

import com.windpower.diag.entity.SysPermission;
import java.util.List;

public interface PermissionService {
    List<SysPermission> getPermissionTree();
    List<SysPermission> getPermissionsByRoleIds(List<Long> roleIds);
    void create(SysPermission permission);
    void update(SysPermission permission);
    void delete(Long id);
    List<SysPermission> getPermissionsByIds(List<Long> ids);
}
