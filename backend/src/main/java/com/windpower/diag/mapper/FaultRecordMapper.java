package com.windpower.diag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.windpower.diag.entity.FaultRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FaultRecordMapper extends BaseMapper<FaultRecord> {

    @Select("<script>" +
            "SELECT f.*, t.turbine_name, t.turbine_code FROM fault_record f " +
            "LEFT JOIN wind_turbine t ON f.turbine_id = t.id " +
            "<where>" +
            "  <if test='faultType != null and faultType != \"\"> AND f.fault_type = #{faultType}</if>" +
            "  <if test='faultLevel != null and faultLevel != \"\"> AND f.fault_level = #{faultLevel}</if>" +
            "  <if test='status != null'> AND f.status = #{status}</if>" +
            "  <if test='turbineId != null'> AND f.turbine_id = #{turbineId}</if>" +
            "  <if test='deptIdList != null and !deptIdList.isEmpty()"> AND t.dept_id IN <foreach collection='deptIdList' item='id' open='(' separator=',' close=')'>#{id}</foreach></if>" +
            "  <if test='deptIdList != null and deptIdList.isEmpty()'> AND 1=0</if>" +
            "</where>" +
            " ORDER BY f.created_at DESC" +
            "</script>")
    IPage<FaultRecord> selectPageWithTurbine(Page<FaultRecord> page,
                                              @Param("faultType") String faultType,
                                              @Param("faultLevel") String faultLevel,
                                              @Param("status") Integer status,
                                              @Param("turbineId") Long turbineId,
                                              @Param("deptIdList") List<Long> deptIdList);
}
