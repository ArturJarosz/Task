package com.arturjarosz.task.notification.application.mapper;

import com.arturjarosz.task.dto.NotificationDto;
import com.arturjarosz.task.dto.NotificationSettingDto;
import com.arturjarosz.task.notification.domain.NotificationPreference;
import com.arturjarosz.task.notification.model.Notification;
import com.arturjarosz.task.notification.model.NotificationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface NotificationMapper {

    @Mapping(source = "read", target = "read")
    NotificationDto mapToDto(Notification notification);

    @Mapping(source = "type", target = "type")
    @Mapping(source = "preference.enabled", target = "enabled")
    @Mapping(source = "preference.daysBefore", target = "daysBefore")
    NotificationSettingDto mapToSettingDto(NotificationType type, NotificationPreference preference);
}
