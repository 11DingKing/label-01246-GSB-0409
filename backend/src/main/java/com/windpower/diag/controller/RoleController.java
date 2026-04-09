package com.windpower.diag.controller;

import com.windpower.diag.aop.OperationLog;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.common.Result;
import com.windpower.diag.entity.SysRole;
import com.windpower.diag.service.RoleService;
import org.noear.solon.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@Mapping("/api/role")
public class RoleController {

    @Inject
    private RoleService roleService;

    @Get
    @Mapping("/page")
    public Result<PageResult<SysRole>> page(
            @Param(defaultValue = "1") int current,
            @Param(defaultValue = "10") int size,
            @Param(required = false) String roleName,
            @Param(required = false) Integer status) {
        return Result.ok(roleService.page(current, size, roleName, status));
    }

    @Get
    @Mapping("/list")
    public Result<List<SysRole>> listAll() {
        return Result.ok(roleService.listAll());
    }

    @Get
    @Mapping("/{id}")
    public Result<SysRole> getById(@Path Long id) {
        return Result.ok(roleService.getById(id));
    }

    @Post
    @Mapping("/")
    @OperationLog(module = "角色管理", operation = "新增角色")
    public Result<Void> create(@Body SysRole role) {
        if (role.getRoleCode() == null || role.getRoleCode().isBlank()) {
            return Result.fail(400, "角色编码不能为空");
        }
        if (role.getRoleName() == null || role.getRoleName().isBlank()) {
            return Result.fail(400, "角色名称不能为空");
        }
        roleService.create(role);
        return Result.ok("创建成功", null);
    }

    @Put
    @Mapping("/{id}")
    @OperationLog(module = "角色管理", operation = "修改角色")
    public Result<Void> update(@Path Long id, @Body SysRole role) {
        role.setId(id);
        roleService.update(role);
        return Result.ok("更新成功", null);
    }

    @Delete
    @Mapping("/{id}")
    @OperationLog(module = "角色管理", operation = "删除角色")
    public Result<Void> delete(@Path Long id) {
        roleService.delete(id);
        return Result.ok("删除成功", null);
    }

    @Put
    @Mapping("/{id}/permissions")
    @OperationLog(module = "角色管理", operation = "分配权限")
    public Result<Void> assignPermissions(@Path Long id, @Body Map<String, List<Long>> params) {
        List<Long> permIds = params.get("permIds");
        roleService.assignPermissions(id, permIds);
        return Result.ok("权限分配成功", null);
    }

    @Get
    @Mapping("/{id}/permissions")
    public Result<List<Long>> getPermissionIds(@Path Long id) {
        return Result.ok(roleService.getPermissionIds(id));
    }

    @Put
    @Mapping("/{id}/data-scope")
    @OperationLog(module = "角色管理", operation = "配置数据范围")
    public Result<Void> updateDataScope(@Path Long id, @Body Map<String, Integer> params) {
        Integer scopeType = params.get("scopeType");
        if (scopeType == null || scopeType < 1 || scopeType > 5) {
            return Result.fail(400, "数据范围类型无效（1-全部 2-本部门 3-本部门及下级 4-仅本人 5-自定义）");
        }
        roleService.updateDataScope(id, scopeType);
        return Result.ok("数据范围配置成功", null);
    }

    @Get
    @Mapping("/{id}/data-scope")
    public Result<Integer> getDataScope(@Path Long id) {
        return Result.ok(roleService.getDataScope(id));
    }
}
