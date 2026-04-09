package com.windpower.diag.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;

@TableName("sys_role_permission")
public class SysRolePermission implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long permId;

    public SysRolePermission() {}
    public SysRolePermission(Long roleId, Long permId) {
        this.roleId = roleId;
        this.permId = permId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getPermId() { return permId; }
    public void setPermId(Long permId) { this.permId = permId; }
}
