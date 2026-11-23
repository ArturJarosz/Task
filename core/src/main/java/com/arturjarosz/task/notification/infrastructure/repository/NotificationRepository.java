package com.arturjarosz.task.notification.infrastructure.repository;

import com.arturjarosz.task.notification.model.Notification;
import com.arturjarosz.task.notification.model.NotificationItemType;
import com.arturjarosz.task.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByItemTypeAndItemId(NotificationItemType itemType, Long itemId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    List<Notification> findByProjectId(Long projectId);

    @Modifying
    @Query("update Notification n set n.readDateTime = :now where n.userId = :userId and n.readDateTime is null")
    int markAllRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from Notification n where n.readDateTime < :readBefore and n.type <> :keptType")
    int deleteReadBefore(@Param("readBefore") LocalDateTime readBefore, @Param("keptType") NotificationType keptType);
}
