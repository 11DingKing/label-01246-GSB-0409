package com.windpower.diag.controller;

import com.windpower.diag.aop.OperationLog;
import com.windpower.diag.common.Result;
import com.windpower.diag.entity.SysPermission;
import com.windpower.diag.service.PermissionService;
import org.noear.solon.annotation.*;

import java.util.List;

@Controller
@Mapping("/api/permission")
public class PermissionController {

    @Inject
    private PermissionService permissionService;

    @Get
    @Mapping("/tree")
    public Result<List<SysPermission>> tree() {
        return Result.ok(permissionService.getPermissionTree());
    }

    @Post
    @Mapping("/")
    @OperationLog(module = "权限管理", operation = "新增权限")
    public Result<Void> create(@Body SysPermission permission) {
        if (permission.getPermCode() == null || permission.getPermCode().isBlank()) {
            return Result.fail(400, "权限编码不能为空");
        }
        if (permission.getPermName() == null || permission.getPermName().isBlank()) {
            return Result.fail(400, "权限名称不能为空");
        }
        permissionService.create(permission);
        return Result.ok("创建成功", null);
    }

    @Put
    @Mapping("/{id}")
    @OperationLog(module = "权限管理", operation = "修改权限")
    public Result<Void> update(@Path Long id, @Body SysPermission permission) {
        permission.setId(id);
        permissionService.update(permission);
        return Result.ok("更新成功", null);
    }

    @Delete
    @Mapping("/{id}")
    @OperationLog(module = "权限管理", operation = "删除权限")
    public Result<Void> delete(@Path Long id) {
        permissionService.delete(id);
        return Result.ok("删除成功", null);
    }
}
