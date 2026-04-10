package com.windpower.diag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.windpower.diag.entity.FaultRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface FaultRecordMapper extends BaseMapper<FaultRecord> {

    @SelectProvider(type = FaultRecordSqlProvider.class, method = "selectPageWithTurbineSql")
    IPage<FaultRecord> selectPageWithTurbine(Page<FaultRecord> page,
                                              @Param("faultType") String faultType,
                                              @Param("faultLevel") String faultLevel,
                                              @Param("status") Integer status,
                                              @Param("turbineId") Long turbineId,
                                              @Param("deptIdList") List<Long> deptIdList);

    class FaultRecordSqlProvider {
        public String selectPageWithTurbineSql(Map<String, Object> params) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT f.*, t.turbine_name, t.turbine_code FROM fault_record f ");
            sql.append("LEFT JOIN wind_turbine t ON f.turbine_id = t.id WHERE 1=1 ");
            
            if (params.get("faultType") != null && !params.get("faultType").toString().trim().isEmpty()) {
                sql.append("AND f.fault_type = #{faultType} ");
            }
            if (params.get("faultLevel") != null && !params.get("faultLevel").toString().trim().isEmpty()) {
                sql.append("AND f.fault_level = #{faultLevel} ");
            }
            if (params.get("status") != null) {
                sql.append("AND f.status = #{status} ");
            }
            if (params.get("turbineId") != null) {
                sql.append("AND f.turbine_id = #{turbineId} ");
            }
            
            @SuppressWarnings("unchecked")
            List<Long> deptIdList = (List<Long>) params.get("deptIdList");
            if (deptIdList != null && !deptIdList.isEmpty()) {
                sql.append("AND t.dept_id IN (");
                for (int i = 0; i < deptIdList.size(); i++) {
                    if (i > 0) sql.append(",");
                    sql.append("#{deptIdList[");
                    sql.append(i);
                    sql.append("]}");
                }
                sql.append(") ");
            } else if (deptIdList != null) {
                sql.append("AND 1=0 ");
            }
            
            sql.append("ORDER BY f.created_at DESC");
            return sql.toString();
        }
    }
}
