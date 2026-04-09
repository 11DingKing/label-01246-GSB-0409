package com.windpower.diag.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("sys_role_data_scope")
public class SysRoleDataScope implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Integer scopeType; // 1-全部数据 2-本部门数据 3-本部门及下级 4-仅本人数据 5-自定义
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SysRoleDataScope() {}
    public SysRoleDataScope(Long roleId, Integer scopeType) {
        this.roleId = roleId;
        this.scopeType = scopeType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Integer getScopeType() { return scopeType; }
    public void setScopeType(Integer scopeType) { this.scopeType = scopeType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
