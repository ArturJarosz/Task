package com.arturjarosz.task.notification.application;

import com.arturjarosz.task.dto.NotificationSettingDto;

import java.util.List;

/**
 * Notification settings of the current user.
 */
public interface NotificationSettingsApplicationService {

    List<NotificationSettingDto> getNotificationSettings();

    List<NotificationSettingDto> updateNotificationSettings(List<NotificationSettingDto> settingDtos);
}
