package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.windpower.diag.common.PageResult;
import com.windpower.diag.entity.TurbineSensorData;
import com.windpower.diag.entity.WindTurbine;
import com.windpower.diag.mapper.TurbineSensorDataMapper;
import com.windpower.diag.mapper.WindTurbineMapper;
import com.windpower.diag.service.DataScopeService;
import com.windpower.diag.service.TurbineService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Ds;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class TurbineServiceImpl implements TurbineService {

    @Ds
    private WindTurbineMapper turbineMapper;
    @Ds
    private TurbineSensorDataMapper sensorDataMapper;
    @Inject
    private DataScopeService dataScopeService;

    @Override
    public PageResult<WindTurbine> page(int current, int size, String turbineCode, Integer status, Long userId) {
        LambdaQueryWrapper<WindTurbine> wrapper = new LambdaQueryWrapper<>();
        if (turbineCode != null && !turbineCode.isEmpty()) {
            wrapper.like(WindTurbine::getTurbineCode, turbineCode);
        }
        if (status != null) {
            wrapper.eq(WindTurbine::getStatus, status);
        }
        // 数据权限过滤
        applyDataScope(wrapper, userId);
        wrapper.orderByAsc(WindTurbine::getTurbineCode);
        Page<WindTurbine> page = turbineMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<WindTurbine> listAll(Long userId) {
        LambdaQueryWrapper<WindTurbine> wrapper = new LambdaQueryWrapper<>();
        applyDataScope(wrapper, userId);
        wrapper.orderByAsc(WindTurbine::getTurbineCode);
        return turbineMapper.selectList(wrapper);
    }

    @Override
    public WindTurbine getById(Long id) {
        return turbineMapper.selectById(id);
    }

    @Override
    public Map<String, Object> getMonitorData(Long turbineId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        // 先过滤风机数据范围
        LambdaQueryWrapper<WindTurbine> turbineWrapper = new LambdaQueryWrapper<>();
        applyDataScope(turbineWrapper, userId);
        if (turbineId != null) {
            turbineWrapper.eq(WindTurbine::getId, turbineId);
        }
        List<Long> visibleTurbineIds = turbineMapper.selectList(turbineWrapper).stream()
                .map(WindTurbine::getId)
                .toList();
        // 获取最近24小时的传感器数据
        LambdaQueryWrapper<TurbineSensorData> wrapper = new LambdaQueryWrapper<>();
        if (!visibleTurbineIds.isEmpty()) {
            wrapper.in(TurbineSensorData::getTurbineId, visibleTurbineIds);
        } else {
            wrapper.in(TurbineSensorData::getTurbineId, -1); // 无权限，不匹配任何数据
        }
        wrapper.ge(TurbineSensorData::getRecordTime, LocalDateTime.now().minusHours(24));
        wrapper.orderByAsc(TurbineSensorData::getRecordTime);
        List<TurbineSensorData> sensorList = sensorDataMapper.selectList(wrapper);
        result.put("sensorData", sensorList);

        // 获取最新一条数据
        LambdaQueryWrapper<TurbineSensorData> latestWrapper = new LambdaQueryWrapper<>();
        if (!visibleTurbineIds.isEmpty()) {
            latestWrapper.in(TurbineSensorData::getTurbineId, visibleTurbineIds);
        } else {
            latestWrapper.in(TurbineSensorData::getTurbineId, -1);
        }
        latestWrapper.orderByDesc(TurbineSensorData::getRecordTime).last("LIMIT 1");
        TurbineSensorData latest = sensorDataMapper.selectOne(latestWrapper);
        result.put("latest", latest);

        return result;
    }

    @Override
    public Map<String, Object> getDashboardStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        LambdaQueryWrapper<WindTurbine> turbineWrapper = new LambdaQueryWrapper<>();
        applyDataScope(turbineWrapper, userId);
        List<WindTurbine> allTurbines = turbineMapper.selectList(turbineWrapper);

        long total = allTurbines.size();
        long running = allTurbines.stream().filter(t -> t.getStatus() == 1).count();
        long warning = allTurbines.stream().filter(t -> t.getStatus() == 2).count();
        long fault = allTurbines.stream().filter(t -> t.getStatus() == 3).count();
        long maintenance = allTurbines.stream().filter(t -> t.getStatus() == 4).count();
        long offline = allTurbines.stream().filter(t -> t.getStatus() == 5).count();

        stats.put("totalTurbines", total);
        stats.put("runningCount", running);
        stats.put("warningCount", warning);
        stats.put("faultCount", fault);
        stats.put("maintenanceCount", maintenance);
        stats.put("offlineCount", offline);

        // 设备状态分布
        List<Map<String, Object>> statusDist = new ArrayList<>();
        statusDist.add(Map.of("name", "正常运行", "value", running));
        statusDist.add(Map.of("name", "告警", "value", warning));
        statusDist.add(Map.of("name", "故障", "value", fault));
        statusDist.add(Map.of("name", "停机维护", "value", maintenance));
        statusDist.add(Map.of("name", "离线", "value", offline));
        stats.put("statusDistribution", statusDist);

        // 总装机容量
        BigDecimal totalCapacity = allTurbines.stream()
                .map(WindTurbine::getRatedPower)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalCapacity", totalCapacity.setScale(1, RoundingMode.HALF_UP));

        // 最近24小时传感器趋势（取所有设备平均值）
        LambdaQueryWrapper<TurbineSensorData> sensorWrapper = new LambdaQueryWrapper<>();
        // 添加数据权限过滤 - 只筛选可见风机的传感器数据
        List<Long> turbineIdsForStats = allTurbines.stream().map(WindTurbine::getId).toList();
        if (!turbineIdsForStats.isEmpty()) {
            sensorWrapper.in(TurbineSensorData::getTurbineId, turbineIdsForStats);
        } else {
            sensorWrapper.in(TurbineSensorData::getTurbineId, -1);
        }
        sensorWrapper.ge(TurbineSensorData::getRecordTime, LocalDateTime.now().minusHours(24));
        sensorWrapper.orderByAsc(TurbineSensorData::getRecordTime);
        List<TurbineSensorData> recentData = sensorDataMapper.selectList(sensorWrapper);

        List<Map<String, Object>> trendData = new ArrayList<>();
        for (TurbineSensorData d : recentData) {
            Map<String, Object> point = new HashMap<>();
            point.put("time", d.getRecordTime());
            point.put("windSpeed", d.getWindSpeed());
            point.put("power", d.getPower());
            point.put("rotorSpeed", d.getRotorSpeed());
            trendData.add(point);
        }
        stats.put("trendData", trendData);

        return stats;
    }

    private void applyDataScope(LambdaQueryWrapper<WindTurbine> wrapper, Long userId) {
        if (userId == null) {
            return;
        }
        java.util.List<Long> visibleDeptIds = dataScopeService.getVisibleDeptIds(userId);
        if (visibleDeptIds != null) {
            if (visibleDeptIds.isEmpty()) {
                wrapper.in(WindTurbine::getDeptId, -1); // 空列表，不匹配任何部门
            } else {
                wrapper.in(WindTurbine::getDeptId, visibleDeptIds);
            }
        }
        // visibleDeptIds 为 null 表示全部数据（管理员），不添加过滤条件
    }
}
