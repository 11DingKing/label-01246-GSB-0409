package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windpower.diag.common.BusinessException;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.SysRole;
import com.windpower.diag.entity.SysRoleDataScope;
import com.windpower.diag.entity.SysRolePermission;
import com.windpower.diag.mapper.SysRoleDataScopeMapper;
import com.windpower.diag.mapper.SysRoleMapper;
import com.windpower.diag.mapper.SysRolePermissionMapper;
import com.windpower.diag.service.RoleService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleServiceImpl implements RoleService {
    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Ds
    private SysRoleMapper roleMapper;
    @Ds
    private SysRolePermissionMapper rolePermissionMapper;
    @Ds
    private SysRoleDataScopeMapper roleDataScopeMapper;

    @Override
    public PageResult<SysRole> page(int current, int size, String roleName, Integer status) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getSortOrder);
        Page<SysRole> page = roleMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<SysRole> listAll() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1);
        wrapper.orderByAsc(SysRole::getSortOrder);
        return roleMapper.selectList(wrapper);
    }

    @Override
    public SysRole getById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        // 查询角色权限ID
        role.setPermissionIds(getPermissionIds(id));
        // 查询数据范围
        if (role.getDataScope() == null) {
            role.setDataScope(getDataScope(id));
        }
        return role;
    }

    @Override
    public void create(SysRole role) {
        // 空字符串转 null
        if (role.getDescription() != null && role.getDescription().isBlank()) {
            role.setDescription(null);
        }
        // 设置默认值
        role.setStatus(1);
        if (role.getSortOrder() == null) {
            role.setSortOrder(0);
        }
        // 清除不需要插入的字段
        role.setId(null);
        role.setCreatedAt(null);
        role.setUpdatedAt(null);
        role.setDeleted(null);
        try {
            roleMapper.insert(role);
        } catch (Exception e) {
            log.error("创建角色失败: roleCode={}, error={}", role.getRoleCode(), e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                throw new BusinessException("角色编码已存在");
            }
            throw e;
        }
        log.info("创建角色: {}", role.getRoleCode());
    }

    @Override
    public void update(SysRole role) {
        SysRole existing = roleMapper.selectById(role.getId());
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        roleMapper.updateById(role);
        log.info("更新角色: id={}", role.getId());
    }

    @Override
    public void delete(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if ("ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException("不允许删除管理员角色");
        }
        roleMapper.deleteById(id);
        // 删除角色权限关联
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, id);
        rolePermissionMapper.delete(wrapper);
        log.info("删除角色: id={}", id);
    }

    @Override
    public void assignPermissions(Long roleId, List<Long> permIds) {
        // 先删除原有权限
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(wrapper);
        // 批量插入新权限
        if (permIds != null && !permIds.isEmpty()) {
            for (Long permId : permIds) {
                rolePermissionMapper.insert(new SysRolePermission(roleId, permId));
            }
        }
        log.info("分配角色权限: roleId={}, permIds={}", roleId, permIds);
    }

    @Override
    public List<Long> getPermissionIds(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        return rolePermissionMapper.selectList(wrapper).stream()
                .map(SysRolePermission::getPermId)
                .collect(Collectors.toList());
    }

    @Override
    public void updateDataScope(Long roleId, Integer scopeType) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        // 更新角色表的 data_scope 字段
        SysRole updateRole = new SysRole();
        updateRole.setId(roleId);
        updateRole.setDataScope(scopeType);
        roleMapper.updateById(updateRole);

        // 更新 sys_role_data_scope 表
        LambdaQueryWrapper<SysRoleDataScope> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleDataScope::getRoleId, roleId);
        SysRoleDataScope existing = roleDataScopeMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setScopeType(scopeType);
            roleDataScopeMapper.updateById(existing);
        } else {
            roleDataScopeMapper.insert(new SysRoleDataScope(roleId, scopeType));
        }
        log.info("更新角色数据范围: roleId={}, scopeType={}", roleId, scopeType);
    }

    @Override
    public Integer getDataScope(Long roleId) {
        LambdaQueryWrapper<SysRoleDataScope> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleDataScope::getRoleId, roleId);
        SysRoleDataScope scope = roleDataScopeMapper.selectOne(wrapper);
        return scope != null ? scope.getScopeType() : 1; // 默认全部数据
    }
}
