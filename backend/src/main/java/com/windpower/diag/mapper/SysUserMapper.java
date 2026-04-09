package com.windpower.diag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.windpower.diag.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @Select("SELECT u.email FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE u.dept_id = #{deptId} AND r.role_key = 'ADMIN' " +
            "AND u.email IS NOT NULL AND u.email <> ''")
    List<String> selectAdminEmailsByDeptId(Long deptId);
}
