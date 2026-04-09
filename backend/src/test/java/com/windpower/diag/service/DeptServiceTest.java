package com.windpower.diag.service;

import com.windpower.diag.common.BusinessException;
import com.windpower.diag.entity.SysDept;
import com.windpower.diag.mapper.SysDeptMapper;
import com.windpower.diag.service.impl.DeptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DeptService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("部门服务测试")
class DeptServiceTest {

    @Mock
    private SysDeptMapper deptMapper;

    @InjectMocks
    private DeptServiceImpl deptService;

    private SysDept rootDept;
    private SysDept childDept1;
    private SysDept childDept2;
    private SysDept grandChildDept;

    @BeforeEach
    void setUp() {
        rootDept = new SysDept();
        rootDept.setId(1L);
        rootDept.setParentId(0L);
        rootDept.setDeptName("总公司");
        rootDept.setSortOrder(1);
        rootDept.setStatus(1);

        childDept1 = new SysDept();
        childDept1.setId(2L);
        childDept1.setParentId(1L);
        childDept1.setDeptName("运维部");
        childDept1.setSortOrder(1);
        childDept1.setStatus(1);

        childDept2 = new SysDept();
        childDept2.setId(3L);
        childDept2.setParentId(1L);
        childDept2.setDeptName("数据分析部");
        childDept2.setSortOrder(2);
        childDept2.setStatus(1);

        grandChildDept = new SysDept();
        grandChildDept.setId(4L);
        grandChildDept.setParentId(2L);
        grandChildDept.setDeptName("运维一组");
        grandChildDept.setSortOrder(1);
        grandChildDept.setStatus(1);
    }

    @Test
    @DisplayName("getDeptTree - 构建正确的树形结构")
    void testGetDeptTree() {
        when(deptMapper.selectList(any())).thenReturn(
                Arrays.asList(rootDept, childDept1, childDept2, grandChildDept));

        List<SysDept> tree = deptService.getDeptTree();

        assertNotNull(tree);
        assertEquals(1, tree.size());
        assertEquals("总公司", tree.get(0).getDeptName());
        assertEquals(2, tree.get(0).getChildren().size());

        // 运维部下有运维一组
        SysDept ops = tree.get(0).getChildren().stream()
                .filter(d -> "运维部".equals(d.getDeptName()))
                .findFirst().orElse(null);
        assertNotNull(ops);
        assertEquals(1, ops.getChildren().size());
        assertEquals("运维一组", ops.getChildren().get(0).getDeptName());
    }

    @Test
    @DisplayName("getDeptAndChildIds - 获取部门及所有下级 ID")
    void testGetDeptAndChildIds() {
        when(deptMapper.selectList(any())).thenReturn(
                Arrays.asList(rootDept, childDept1, childDept2, grandChildDept));

        // 从总公司开始，应包含所有部门
        List<Long> ids = deptService.getDeptAndChildIds(1L);
        assertEquals(4, ids.size());
        assertTrue(ids.containsAll(Arrays.asList(1L, 2L, 3L, 4L)));

        // 从运维部开始，应包含运维部和运维一组
        List<Long> opsIds = deptService.getDeptAndChildIds(2L);
        assertEquals(2, opsIds.size());
        assertTrue(opsIds.containsAll(Arrays.asList(2L, 4L)));

        // 叶子节点只包含自己
        List<Long> leafIds = deptService.getDeptAndChildIds(3L);
        assertEquals(1, leafIds.size());
        assertTrue(leafIds.contains(3L));
    }

    @Test
    @DisplayName("getById - 部门存在时返回")
    void testGetByIdExists() {
        when(deptMapper.selectById(1L)).thenReturn(rootDept);
        SysDept result = deptService.getById(1L);
        assertEquals("总公司", result.getDeptName());
    }

    @Test
    @DisplayName("getById - 部门不存在时抛异常")
    void testGetByIdNotExists() {
        when(deptMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> deptService.getById(999L));
    }

    @Test
    @DisplayName("delete - 存在子部门时无法删除")
    void testDeleteWithChildren() {
        when(deptMapper.selectCount(any())).thenReturn(2L);
        assertThrows(BusinessException.class, () -> deptService.delete(1L));
    }

    @Test
    @DisplayName("delete - 无子部门时正常删除")
    void testDeleteNoChildren() {
        when(deptMapper.selectCount(any())).thenReturn(0L);
        when(deptMapper.deleteById(3L)).thenReturn(1);
        assertDoesNotThrow(() -> deptService.delete(3L));
        verify(deptMapper).deleteById(3L);
    }

    @Test
    @DisplayName("create - 正常创建部门")
    void testCreate() {
        SysDept newDept = new SysDept();
        newDept.setDeptName("新部门");
        newDept.setParentId(1L);
        when(deptMapper.insert(any(SysDept.class))).thenReturn(1);

        assertDoesNotThrow(() -> deptService.create(newDept));
        assertEquals(1, newDept.getStatus());
        verify(deptMapper).insert(any(SysDept.class));
    }
}
