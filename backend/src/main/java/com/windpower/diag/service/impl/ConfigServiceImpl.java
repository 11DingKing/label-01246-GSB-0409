package com.windpower.diag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.windpower.diag.entity.SysConfig;
import com.windpower.diag.mapper.SysConfigMapper;
import com.windpower.diag.service.ConfigService;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Ds;

import java.util.List;

@Component
public class ConfigServiceImpl implements ConfigService {

    @Ds
    private SysConfigMapper configMapper;

    @Override
    public List<SysConfig> listByGroup(String group) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (group != null && !group.isEmpty()) {
            wrapper.eq(SysConfig::getConfigGroup, group);
        }
        wrapper.orderByAsc(SysConfig::getSortOrder);
        return configMapper.selectList(wrapper);
    }

    @Override
    public List<SysConfig> listAll() {
        return configMapper.selectList(new LambdaQueryWrapper<SysConfig>().orderByAsc(SysConfig::getConfigGroup).orderByAsc(SysConfig::getSortOrder));
    }

    @Override
    public void update(Long id, String configValue) {
        SysConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new IllegalArgumentException("配置项不存在");
        }
        config.setConfigValue(configValue);
        configMapper.updateById(config);
    }

    @Override
    public void batchUpdate(List<SysConfig> configs) {
        for (SysConfig c : configs) {
            if (c.getId() != null) {
                SysConfig existing = configMapper.selectById(c.getId());
                if (existing != null) {
                    existing.setConfigValue(c.getConfigValue());
                    configMapper.updateById(existing);
                }
            }
        }
    }
}
