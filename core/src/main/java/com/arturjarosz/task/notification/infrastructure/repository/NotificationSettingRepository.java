package com.arturjarosz.task.notification.infrastructure.repository;

import com.arturjarosz.task.notification.model.NotificationSetting;
import com.arturjarosz.task.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    List<NotificationSetting> findByUserId(Long userId);

    List<NotificationSetting> findByUserIdInAndType(Collection<Long> userIds, NotificationType type);
}
