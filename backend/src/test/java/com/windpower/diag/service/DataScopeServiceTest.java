package com.windpower.diag.service;

import com.windpower.diag.entity.SysRole;
import com.windpower.diag.entity.SysRoleDataScope;
import com.windpower.diag.entity.SysUser;
import com.windpower.diag.entity.SysUserRole;
import com.windpower.diag.mapper.SysRoleDataScopeMapper;
import com.windpower.diag.mapper.SysRoleMapper;
import com.windpower.diag.mapper.SysUserMapper;
import com.windpower.diag.mapper.SysUserRoleMapper;
import com.windpower.diag.service.impl.DataScopeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DataScopeService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("数据范围权限服务测试")
class DataScopeServiceTest {

    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysRoleDataScopeMapper roleDataScopeMapper;
    @Mock
    private DeptService deptService;

    @InjectMocks
    private DataScopeServiceImpl dataScopeService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("getUserDataScope - 无角色默认仅本人")
    void testGetUserDataScopeNoRoles() {
        when(userRoleMapper.selectList(any())).thenReturn(Collections.emptyList());
        assertEquals(4, dataScopeService.getUserDataScope(1L));
    }

    @Test
    @DisplayName("getUserDataScope - 从 data_scope 表取最大范围")
    void testGetUserDataScopeFromTable() {
        SysUserRole ur1 = new SysUserRole(1L, 1L);
        SysUserRole ur2 = new SysUserRole(1L, 2L);
        when(userRoleMapper.selectList(any())).thenReturn(Arrays.asList(ur1, ur2));

        SysRoleDataScope scope1 = new SysRoleDataScope(1L, 1); // 全部数据
        SysRoleDataScope scope2 = new SysRoleDataScope(2L, 3); // 本部门及下级
        when(roleDataScopeMapper.selectList(any())).thenReturn(Arrays.asList(scope1, scope2));

        // 应取最小值（范围最大）= 1
        assertEquals(1, dataScopeService.getUserDataScope(1L));
    }

    @Test
    @DisplayName("getUserDataScope - 回退到角色表 data_scope 字段")
    void testGetUserDataScopeFallbackToRole() {
        SysUserRole ur = new SysUserRole(1L, 2L);
        when(userRoleMapper.selectList(any())).thenReturn(Collections.singletonList(ur));
        when(roleDataScopeMapper.selectList(any())).thenReturn(Collections.emptyList());

        SysRole role = new SysRole();
        role.setId(2L);
        role.setDataScope(2);
        when(roleMapper.selectList(any())).thenReturn(Collections.singletonList(role));

        assertEquals(2, dataScopeService.getUserDataScope(1L));
    }

    @Test
    @DisplayName("getVisibleDeptIds - 全部数据返回 null")
    void testGetVisibleDeptIdsAll() {
        SysUserRole ur = new SysUserRole(1L, 1L);
        when(userRoleMapper.selectList(any())).thenReturn(Collections.singletonList(ur));
        SysRoleDataScope scope = new SysRoleDataScope(1L, 1);
        when(roleDataScopeMapper.selectList(any())).thenReturn(Collections.singletonList(scope));

        assertNull(dataScopeService.getVisibleDeptIds(1L));
    }

    @Test
    @DisplayName("getVisibleDeptIds - 本部门数据返回单个部门 ID")
    void testGetVisibleDeptIdsSelfDept() {
        SysUserRole ur = new SysUserRole(1L, 2L);
        when(userRoleMapper.selectList(any())).thenReturn(Collections.singletonList(ur));
        SysRoleDataScope scope = new SysRoleDataScope(2L, 2);
        when(roleDataScopeMapper.selectList(any())).thenReturn(Collections.singletonList(scope));

        SysUser user = new SysUser();
        user.setId(1L);
        user.setDeptId(5L);
        when(userMapper.selectById(1L)).thenReturn(user);

        List<Long> ids = dataScopeService.getVisibleDeptIds(1L);
        assertNotNull(ids);
        assertEquals(1, ids.size());
        assertEquals(5L, ids.get(0));
    }

    @Test
    @DisplayName("getVisibleDeptIds - 本部门及下级")
    void testGetVisibleDeptIdsDeptAndChildren() {
        SysUserRole ur = new SysUserRole(1L, 3L);
        when(userRoleMapper.selectList(any())).thenReturn(Collections.singletonList(ur));
        SysRoleDataScope scope = new SysRoleDataScope(3L, 3);
        when(roleDataScopeMapper.selectList(any())).thenReturn(Collections.singletonList(scope));

        SysUser user = new SysUser();
        user.setId(1L);
        user.setDeptId(5L);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(deptService.getDeptAndChildIds(5L)).thenReturn(Arrays.asList(5L, 6L, 7L));

        List<Long> ids = dataScopeService.getVisibleDeptIds(1L);
        assertNotNull(ids);
        assertEquals(3, ids.size());
        assertTrue(ids.containsAll(Arrays.asList(5L, 6L, 7L)));
    }

    @Test
    @DisplayName("getVisibleDeptIds - 仅本人返回空列表")
    void testGetVisibleDeptIdsSelfOnly() {
        SysUserRole ur = new SysUserRole(1L, 4L);
        when(userRoleMapper.selectList(any())).thenReturn(Collections.singletonList(ur));
        SysRoleDataScope scope = new SysRoleDataScope(4L, 4);
        when(roleDataScopeMapper.selectList(any())).thenReturn(Collections.singletonList(scope));

        SysUser user = new SysUser();
        user.setId(1L);
        user.setDeptId(5L);
        when(userMapper.selectById(1L)).thenReturn(user);

        List<Long> ids = dataScopeService.getVisibleDeptIds(1L);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    @DisplayName("getVisibleDeptIds - 用户无部门返回空列表")
    void testGetVisibleDeptIdsNoDept() {
        SysUserRole ur = new SysUserRole(1L, 2L);
        when(userRoleMapper.selectList(any())).thenReturn(Collections.singletonList(ur));
        SysRoleDataScope scope = new SysRoleDataScope(2L, 2);
        when(roleDataScopeMapper.selectList(any())).thenReturn(Collections.singletonList(scope));

        SysUser user = new SysUser();
        user.setId(1L);
        user.setDeptId(null);
        when(userMapper.selectById(1L)).thenReturn(user);

        List<Long> ids = dataScopeService.getVisibleDeptIds(1L);
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    @DisplayName("isSelfOnly - 仅本人数据范围")
    void testIsSelfOnly() {
        SysUserRole ur = new SysUserRole(1L, 4L);
        when(userRoleMapper.selectList(any())).thenReturn(Collections.singletonList(ur));
        SysRoleDataScope scope = new SysRoleDataScope(4L, 4);
        when(roleDataScopeMapper.selectList(any())).thenReturn(Collections.singletonList(scope));

        assertTrue(dataScopeService.isSelfOnly(1L));
    }

    @Test
    @DisplayName("isSelfOnly - 非仅本人")
    void testIsSelfOnlyFalse() {
        SysUserRole ur = new SysUserRole(1L, 1L);
        when(userRoleMapper.selectList(any())).thenReturn(Collections.singletonList(ur));
        SysRoleDataScope scope = new SysRoleDataScope(1L, 1);
        when(roleDataScopeMapper.selectList(any())).thenReturn(Collections.singletonList(scope));

        assertFalse(dataScopeService.isSelfOnly(1L));
    }
}
