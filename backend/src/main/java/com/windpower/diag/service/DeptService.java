package com.windpower.diag.service;

import com.windpower.diag.entity.SysDept;
import java.util.List;

public interface DeptService {
    List<SysDept> getDeptTree();
    List<SysDept> listAll();
    SysDept getById(Long id);
    void create(SysDept dept);
    void update(SysDept dept);
    void delete(Long id);
    /** 获取指定部门及其所有下级部门ID */
    List<Long> getDeptAndChildIds(Long deptId);
}
