package com.windpower.diag.controller;

import com.windpower.diag.aop.OperationLog;
import com.windpower.diag.common.Result;
import com.windpower.diag.entity.SysDept;
import com.windpower.diag.service.DeptService;
import org.noear.solon.annotation.*;

import java.util.List;

@Controller
@Mapping("/api/dept")
public class DeptController {

    @Inject
    private DeptService deptService;

    @Get
    @Mapping("/tree")
    public Result<List<SysDept>> tree() {
        return Result.ok(deptService.getDeptTree());
    }

    @Get
    @Mapping("/list")
    public Result<List<SysDept>> list() {
        return Result.ok(deptService.listAll());
    }

    @Get
    @Mapping("/{id}")
    public Result<SysDept> getById(@Path Long id) {
        return Result.ok(deptService.getById(id));
    }

    @Post
    @Mapping("/")
    @OperationLog(module = "部门管理", operation = "新增部门")
    public Result<Void> create(@Body SysDept dept) {
        if (dept.getDeptName() == null || dept.getDeptName().isBlank()) {
            return Result.fail(400, "部门名称不能为空");
        }
        deptService.create(dept);
        return Result.ok("创建成功", null);
    }

    @Put
    @Mapping("/{id}")
    @OperationLog(module = "部门管理", operation = "修改部门")
    public Result<Void> update(@Path Long id, @Body SysDept dept) {
        dept.setId(id);
        deptService.update(dept);
        return Result.ok("更新成功", null);
    }

    @Delete
    @Mapping("/{id}")
    @OperationLog(module = "部门管理", operation = "删除部门")
    public Result<Void> delete(@Path Long id) {
        deptService.delete(id);
        return Result.ok("删除成功", null);
    }
}
