package com.windpower.diag.service;

import com.windpower.diag.entity.SysConfig;
import java.util.List;

public interface ConfigService {
    List<SysConfig> listByGroup(String group);
    List<SysConfig> listAll();
    void update(Long id, String configValue);
    void batchUpdate(List<SysConfig> configs);
}
