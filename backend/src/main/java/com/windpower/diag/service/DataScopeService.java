package com.windpower.diag.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;

/**
 * 数据范围权限服务
 * 根据当前用户的角色数据范围配置，自动过滤查询条件
 */
public interface DataScopeService {
    /**
     * 获取用户的数据范围类型（取角色中最大范围）
     * 1-全部数据 2-本部门数据 3-本部门及下级 4-仅本人数据
     */
    int getUserDataScope(Long userId);

    /**
     * 获取用户可见的部门ID列表（根据数据范围）
     * 返回null表示可查看全部数据
     */
    List<Long> getVisibleDeptIds(Long userId);

    /**
     * 判断用户是否只能查看自己的数据
     */
    boolean isSelfOnly(Long userId);
}
