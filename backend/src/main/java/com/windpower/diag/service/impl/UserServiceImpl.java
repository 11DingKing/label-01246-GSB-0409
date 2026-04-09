package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windpower.diag.common.BusinessException;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.SysUser;
import com.windpower.diag.entity.SysUserRole;
import com.windpower.diag.mapper.SysUserMapper;
import com.windpower.diag.mapper.SysUserRoleMapper;
import com.windpower.diag.service.DataScopeService;
import com.windpower.diag.service.UserService;
import com.windpower.diag.util.PasswordEncoder;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Ds
    private SysUserMapper userMapper;
    @Ds
    private SysUserRoleMapper userRoleMapper;
    @Inject
    private PasswordEncoder passwordEncoder;
    @Inject
    private DataScopeService dataScopeService;

    @Override
    public PageResult<SysUser> page(int current, int size, String username, String realName, Integer status, Long operatorUserId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (realName != null && !realName.isEmpty()) {
            wrapper.like(SysUser::getRealName, realName);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        // 数据范围权限过滤
        if (operatorUserId != null) {
            if (dataScopeService.isSelfOnly(operatorUserId)) {
                // 仅本人数据
                wrapper.eq(SysUser::getId, operatorUserId);
            } else {
                List<Long> visibleDeptIds = dataScopeService.getVisibleDeptIds(operatorUserId);
                if (visibleDeptIds != null) {
                    // 按部门过滤
                    if (visibleDeptIds.isEmpty()) {
                        wrapper.eq(SysUser::getId, operatorUserId); // 无可见部门，只能看自己
                    } else {
                        wrapper.in(SysUser::getDeptId, visibleDeptIds);
                    }
                }
                // visibleDeptIds == null 表示全部数据，不加过滤
            }
        }

        wrapper.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> page = userMapper.selectPage(new Page<>(current, size), wrapper);
        // 清除密码字段
        page.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(page);
    }

    @Override
    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void create(SysUser user) {
        // 空字符串转 null，避免唯一索引冲突
        if (user.getEmail() != null && user.getEmail().isBlank()) {
            user.setEmail(null);
        }
        if (user.getPhone() != null && user.getPhone().isBlank()) {
            user.setPhone(null);
        }
        if (user.getRealName() != null && user.getRealName().isBlank()) {
            user.setRealName(null);
        }
        // 检查用户名唯一性
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 检查邮箱唯一性
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(SysUser::getEmail, user.getEmail());
            if (userMapper.selectCount(emailWrapper) > 0) {
                throw new BusinessException("邮箱已被使用");
            }
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        user.setAccountLocked(0);
        user.setLoginFailCount(0);
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                if (e.getMessage().contains("uk_email")) {
                    throw new BusinessException("邮箱已被使用");
                }
                throw new BusinessException("用户名已存在");
            }
            throw e;
        }
        log.info("创建用户: {}", user.getUsername());
    }

    @Override
    public void update(SysUser user) {
        SysUser existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        // 空字符串转 null
        if (user.getEmail() != null && user.getEmail().isBlank()) {
            user.setEmail(null);
        }
        if (user.getPhone() != null && user.getPhone().isBlank()) {
            user.setPhone(null);
        }
        if (user.getRealName() != null && user.getRealName().isBlank()) {
            user.setRealName(null);
        }
        // 不允许修改密码和用户名
        user.setPassword(null);
        user.setUsername(null);
        userMapper.updateById(user);
        log.info("更新用户: id={}", user.getId());
    }

    @Override
    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException("不允许删除超级管理员");
        }
        userMapper.deleteById(id);
        // 删除用户角色关联
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, id);
        userRoleMapper.delete(wrapper);
        log.info("删除用户: id={}", id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("更新用户状态: id={}, status={}", id, status);
    }

    @Override
    public void updateLock(Long id, Integer locked) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setAccountLocked(locked);
        if (locked == 0) {
            user.setLoginFailCount(0);
        }
        userMapper.updateById(user);
        log.info("更新用户锁定状态: id={}, locked={}", id, locked);
    }

    @Override
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 先删除原有角色
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);
        // 批量插入新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                userRoleMapper.insert(new SysUserRole(userId, roleId));
            }
        }
        log.info("分配用户角色: userId={}, roleIds={}", userId, roleIds);
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        return userRoleMapper.selectList(wrapper).stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }
}
