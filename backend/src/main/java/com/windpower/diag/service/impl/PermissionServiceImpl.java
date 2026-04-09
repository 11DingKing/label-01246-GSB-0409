package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windpower.diag.common.BusinessException;
import com.windpower.diag.entity.SysPermission;
import com.windpower.diag.entity.SysRolePermission;
import com.windpower.diag.mapper.SysPermissionMapper;
import com.windpower.diag.mapper.SysRolePermissionMapper;
import com.windpower.diag.service.PermissionService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionServiceImpl implements PermissionService {
    private static final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Ds
    private SysPermissionMapper permissionMapper;
    @Ds
    private SysRolePermissionMapper rolePermissionMapper;

    @Override
    public List<SysPermission> getPermissionTree() {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getStatus, 1);
        wrapper.orderByAsc(SysPermission::getSortOrder);
        List<SysPermission> allPerms = permissionMapper.selectList(wrapper);
        return buildTree(allPerms, 0L);
    }

    private List<SysPermission> buildTree(List<SysPermission> allPerms, Long parentId) {
        List<SysPermission> tree = new ArrayList<>();
        for (SysPermission perm : allPerms) {
            if (parentId.equals(perm.getParentId())) {
                perm.setChildren(buildTree(allPerms, perm.getId()));
                tree.add(perm);
            }
        }
        return tree;
    }

    @Override
    public List<SysPermission> getPermissionsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 查询角色权限关联
        LambdaQueryWrapper<SysRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.in(SysRolePermission::getRoleId, roleIds);
        List<Long> permIds = rolePermissionMapper.selectList(rpWrapper).stream()
                .map(SysRolePermission::getPermId)
                .distinct()
                .collect(Collectors.toList());
        if (permIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 查询权限详情
        LambdaQueryWrapper<SysPermission> pWrapper = new LambdaQueryWrapper<>();
        pWrapper.in(SysPermission::getId, permIds);
        pWrapper.eq(SysPermission::getStatus, 1);
        pWrapper.orderByAsc(SysPermission::getSortOrder);
        return permissionMapper.selectList(pWrapper);
    }

    @Override
    public void create(SysPermission permission) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getPermCode, permission.getPermCode());
        if (permissionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("权限编码已存在");
        }
        permission.setStatus(1);
        permissionMapper.insert(permission);
        log.info("创建权限: {}", permission.getPermCode());
    }

    @Override
    public void update(SysPermission permission) {
        SysPermission existing = permissionMapper.selectById(permission.getId());
        if (existing == null) {
            throw new BusinessException("权限不存在");
        }
        permissionMapper.updateById(permission);
        log.info("更新权限: id={}", permission.getId());
    }

    @Override
    public void delete(Long id) {
        // 检查是否有子权限
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getParentId, id);
        if (permissionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("存在子权限，无法删除");
        }
        permissionMapper.deleteById(id);
        // 删除角色权限关联
        LambdaQueryWrapper<SysRolePermission> rpWrapper = new LambdaQueryWrapper<>();
        rpWrapper.eq(SysRolePermission::getPermId, id);
        rolePermissionMapper.delete(rpWrapper);
        log.info("删除权限: id={}", id);
    }

    @Override
    public List<SysPermission> getPermissionsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysPermission::getId, ids);
        wrapper.eq(SysPermission::getStatus, 1);
        wrapper.orderByAsc(SysPermission::getSortOrder);
        return permissionMapper.selectList(wrapper);
    }
}
