package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.Settings;
import com.tanhua.dubbo.mapper.SettingsMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SettingsApiImpl implements SettingsApi {

    @Autowired
    private SettingsMapper settingsMapper;

    /**
     * 根据id查询设置表:tb_settings
     * @param userId
     * @return
     */
    @Override
    public Settings queryByUserId(Long userId) {
        QueryWrapper<Settings> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        Settings settings = settingsMapper.selectOne(queryWrapper);
        return settings;
    }

    /**
     * 通知设置 - 保存
     * @param settings
     */
    @Override
    public void saveSettings(Settings settings) {
            settingsMapper.insert(settings);
    }

    /**
     * 通知设置 - 保存 - 更新
     * @param settings
     */
    @Override
    public void updateSettings(Settings settings) {
            settingsMapper.updateById(settings);
    }
}
