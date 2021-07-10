package com.tanhua.dubbo.api.db;

import com.tanhua.domain.db.Settings;

public interface SettingsApi {

    /**
     * 根据id查询设置表:tb_settings
     * @param userId
     * @return
     */
    Settings queryByUserId(Long userId);

    /**
     * 通知设置 - 保存
     * @param settings
     */
    void saveSettings(Settings settings);

    /**
     * 通知设置 - 保存
     * @param settings
     */
    void updateSettings(Settings settings);
}
