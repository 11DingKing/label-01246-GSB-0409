package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windpower.diag.entity.SysRole;
import com.windpower.diag.entity.SysRoleDataScope;
import com.windpower.diag.entity.SysUser;
import com.windpower.diag.entity.SysUserRole;
import com.windpower.diag.mapper.SysRoleDataScopeMapper;
import com.windpower.diag.mapper.SysRoleMapper;
import com.windpower.diag.mapper.SysUserMapper;
import com.windpower.diag.mapper.SysUserRoleMapper;
import com.windpower.diag.service.DataScopeService;
import com.windpower.diag.service.DeptService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataScopeServiceImpl implements DataScopeService {

    @Ds
    private SysUserMapper userMapper;
    @Ds
    private SysUserRoleMapper userRoleMapper;
    @Ds
    private SysRoleMapper roleMapper;
    @Ds
    private SysRoleDataScopeMapper roleDataScopeMapper;
    @Inject
    private DeptService deptService;

    @Override
    public int getUserDataScope(Long userId) {
        List<Long> roleIds = getUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return 4; // 无角色默认仅本人
        }

        // 优先从 sys_role_data_scope 表查询
        LambdaQueryWrapper<SysRoleDataScope> scopeWrapper = new LambdaQueryWrapper<>();
        scopeWrapper.in(SysRoleDataScope::getRoleId, roleIds);
        List<SysRoleDataScope> scopes = roleDataScopeMapper.selectList(scopeWrapper);

        if (!scopes.isEmpty()) {
            // 取最小值（范围最大）：1=全部 > 2=本部门 > 3=本部门及下级 > 4=仅本人
            return scopes.stream()
                    .mapToInt(SysRoleDataScope::getScopeType)
                    .min()
                    .orElse(4);
        }

        // 回退：从角色表的 data_scope 字段查询
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(SysRole::getId, roleIds);
        List<SysRole> roles = roleMapper.selectList(roleWrapper);
        return roles.stream()
                .map(SysRole::getDataScope)
                .filter(ds -> ds != null)
                .mapToInt(Integer::intValue)
                .min()
                .orElse(4);
    }

    @Override
    public List<Long> getVisibleDeptIds(Long userId) {
        int scope = getUserDataScope(userId);
        if (scope == 1) {
            return null; // 全部数据，不限制
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getDeptId() == null) {
            return new ArrayList<>(); // 无部门，返回空
        }

        if (scope == 2) {
            // 本部门数据
            List<Long> ids = new ArrayList<>();
            ids.add(user.getDeptId());
            return ids;
        }

        if (scope == 3) {
            // 本部门及下级
            return deptService.getDeptAndChildIds(user.getDeptId());
        }

        // scope == 4 仅本人，返回空列表（由调用方处理userId过滤）
        return new ArrayList<>();
    }

    @Override
    public boolean isSelfOnly(Long userId) {
        return getUserDataScope(userId) == 4;
    }

    private List<Long> getUserRoleIds(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        return userRoleMapper.selectList(wrapper).stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }
}
