package com.windpower.diag.controller;

import com.windpower.diag.aop.OperationLog;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.common.Result;
import com.windpower.diag.entity.SysUser;
import com.windpower.diag.service.UserService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;

import java.util.List;
import java.util.Map;

@Controller
@Mapping("/api/user")
public class UserController {

    @Inject
    private UserService userService;

    @Get
    @Mapping("/page")
    public Result<PageResult<SysUser>> page(
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            @Param(required = false) String username,
            @Param(required = false) String realName,
            @Param(required = false) Integer status,
            Context ctx) {
        Long operatorUserId = (Long) ctx.attr("userId");
        return Result.ok(userService.page(current, size, username, realName, status, operatorUserId));
    }

    @Get
    @Mapping("/{id}")
    public Result<SysUser> getById(@Path Long id) {
        SysUser user = userService.getById(id);
        user.setRoles(null); // 单独查角色用其他接口
        return Result.ok(user);
    }

    @Post
    @Mapping("/")
    @OperationLog(module = "用户管理", operation = "新增用户")
    public Result<Void> create(@Body SysUser user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return Result.fail(400, "用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return Result.fail(400, "密码不能为空");
        }
        userService.create(user);
        return Result.ok("创建成功", null);
    }

    @Put
    @Mapping("/{id}")
    @OperationLog(module = "用户管理", operation = "修改用户")
    public Result<Void> update(@Path Long id, @Body SysUser user) {
        user.setId(id);
        userService.update(user);
        return Result.ok("更新成功", null);
    }

    @Delete
    @Mapping("/{id}")
    @OperationLog(module = "用户管理", operation = "删除用户")
    public Result<Void> delete(@Path Long id) {
        userService.delete(id);
        return Result.ok("删除成功", null);
    }

    @Put
    @Mapping("/{id}/status")
    @OperationLog(module = "用户管理", operation = "修改用户状态")
    public Result<Void> updateStatus(@Path Long id, @Body Map<String, Integer> params) {
        Integer status = params.get("status");
        if (status == null) {
            return Result.fail(400, "状态不能为空");
        }
        userService.updateStatus(id, status);
        return Result.ok("状态更新成功", null);
    }

    @Put
    @Mapping("/{id}/lock")
    @OperationLog(module = "用户管理", operation = "锁定/解锁用户")
    public Result<Void> updateLock(@Path Long id, @Body Map<String, Integer> params) {
        Integer locked = params.get("locked");
        if (locked == null) {
            return Result.fail(400, "锁定状态不能为空");
        }
        userService.updateLock(id, locked);
        return Result.ok("操作成功", null);
    }

    @Put
    @Mapping("/{id}/roles")
    @OperationLog(module = "用户管理", operation = "分配角色")
    public Result<Void> assignRoles(@Path Long id, @Body Map<String, List<Long>> params) {
        List<Long> roleIds = params.get("roleIds");
        userService.assignRoles(id, roleIds);
        return Result.ok("角色分配成功", null);
    }

    @Get
    @Mapping("/{id}/roles")
    public Result<List<Long>> getRoleIds(@Path Long id) {
        return Result.ok(userService.getRoleIds(id));
    }
}
