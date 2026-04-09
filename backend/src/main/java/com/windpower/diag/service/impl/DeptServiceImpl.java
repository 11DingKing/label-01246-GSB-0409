package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windpower.diag.common.BusinessException;
import com.windpower.diag.entity.SysDept;
import com.windpower.diag.mapper.SysDeptMapper;
import com.windpower.diag.service.DeptService;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Ds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeptServiceImpl implements DeptService {
    private static final Logger log = LoggerFactory.getLogger(DeptServiceImpl.class);

    @Ds
    private SysDeptMapper deptMapper;

    @Override
    public List<SysDept> getDeptTree() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getStatus, 1);
        wrapper.orderByAsc(SysDept::getSortOrder);
        List<SysDept> allDepts = deptMapper.selectList(wrapper);
        return buildTree(allDepts, 0L);
    }

    private List<SysDept> buildTree(List<SysDept> allDepts, Long parentId) {
        List<SysDept> tree = new ArrayList<>();
        for (SysDept dept : allDepts) {
            if (parentId.equals(dept.getParentId())) {
                dept.setChildren(buildTree(allDepts, dept.getId()));
                tree.add(dept);
            }
        }
        return tree;
    }

    @Override
    public List<SysDept> listAll() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getStatus, 1);
        wrapper.orderByAsc(SysDept::getSortOrder);
        return deptMapper.selectList(wrapper);
    }

    @Override
    public SysDept getById(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return dept;
    }

    @Override
    public void create(SysDept dept) {
        dept.setStatus(1);
        if (dept.getSortOrder() == null) dept.setSortOrder(0);
        dept.setId(null);
        deptMapper.insert(dept);
        log.info("创建部门: {}", dept.getDeptName());
    }

    @Override
    public void update(SysDept dept) {
        SysDept existing = deptMapper.selectById(dept.getId());
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }
        deptMapper.updateById(dept);
        log.info("更新部门: id={}", dept.getId());
    }

    @Override
    public void delete(Long id) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getParentId, id);
        if (deptMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("存在子部门，无法删除");
        }
        deptMapper.deleteById(id);
        log.info("删除部门: id={}", id);
    }

    @Override
    public List<Long> getDeptAndChildIds(Long deptId) {
        List<Long> result = new ArrayList<>();
        result.add(deptId);
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDept::getStatus, 1);
        List<SysDept> allDepts = deptMapper.selectList(wrapper);
        collectChildIds(allDepts, deptId, result);
        return result;
    }

    private void collectChildIds(List<SysDept> allDepts, Long parentId, List<Long> result) {
        for (SysDept dept : allDepts) {
            if (parentId.equals(dept.getParentId())) {
                result.add(dept.getId());
                collectChildIds(allDepts, dept.getId(), result);
            }
        }
    }
}
