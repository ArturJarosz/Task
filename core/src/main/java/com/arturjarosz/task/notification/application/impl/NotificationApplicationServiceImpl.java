package com.arturjarosz.task.notification.application.impl;

import com.arturjarosz.task.dto.NotificationCountDto;
import com.arturjarosz.task.dto.NotificationReadDto;
import com.arturjarosz.task.dto.NotificationTypeDto;
import com.arturjarosz.task.dto.NotificationsDto;
import com.arturjarosz.task.notification.application.NotificationApplicationService;
import com.arturjarosz.task.notification.application.mapper.NotificationMapper;
import com.arturjarosz.task.notification.infrastructure.repository.NotificationRepository;
import com.arturjarosz.task.notification.model.NotificationType;
import com.arturjarosz.task.notification.query.NotificationQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.user.application.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static com.arturjarosz.task.notification.application.NotificationValidator.validateNotificationExistence;
import static com.arturjarosz.task.notification.application.NotificationValidator.validateNotificationReadDto;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class NotificationApplicationServiceImpl implements NotificationApplicationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final Clock clock;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationMapper notificationMapper;
    private final NotificationQueryService notificationQueryService;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    @Override
    public NotificationsDto getNotifications(Boolean unread, NotificationTypeDto type, Integer page, Integer size) {
        LOG.info("Getting notifications of current user.");

        var unreadOnly = Boolean.TRUE.equals(unread);
        var notificationType = type != null ? NotificationType.valueOf(type.name()) : null;
        var pageNumber = page != null ? Math.max(page, 0) : 0;
        var pageSize = size != null ? Math.clamp(size, 1, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        // Someone who has never used the app has no local user yet, and so no notifications
        return this.currentUserProvider.findCurrentUser()
                .map(user -> new NotificationsDto()
                        .notifications(this.notificationQueryService.getNotifications(user.getId(), unreadOnly,
                                notificationType, pageNumber, pageSize).stream()
                                .map(this.notificationMapper::mapToDto).toList())
                        .total(this.notificationQueryService.countNotifications(user.getId(), unreadOnly,
                                notificationType)))
                .orElseGet(() -> new NotificationsDto().notifications(List.of()).total(0L));
    }

    @Transactional(readOnly = true)
    @Override
    public NotificationCountDto getNotificationCount() {
        var unread = this.currentUserProvider.findCurrentUser()
                .map(user -> this.notificationQueryService.countNotifications(user.getId(), true, null))
                .orElse(0L);
        return new NotificationCountDto().unread(unread);
    }

    @Transactional
    @Override
    public void updateNotificationRead(Long notificationId, NotificationReadDto notificationReadDto) {
        LOG.info("Marking notification with id {} of current user.", notificationId);

        validateNotificationReadDto(notificationReadDto);
        var user = this.currentUserProvider.getOrCreateCurrentUser();
        var maybeNotification = this.notificationRepository.findByIdAndUserId(notificationId, user.getId());
        validateNotificationExistence(maybeNotification, notificationId);
        var notification = maybeNotification.orElseThrow(ResourceNotFoundException::new);
        if (Boolean.TRUE.equals(notificationReadDto.getRead())) {
            notification.markRead(LocalDateTime.now(this.clock));
        } else {
            notification.markUnread();
        }

        LOG.info("Notification with id {} marked as {}.", notificationId,
                notification.isRead() ? "read" : "unread");
    }

    @Transactional
    @Override
    public void markAllNotificationsRead() {
        var user = this.currentUserProvider.getOrCreateCurrentUser();
        var marked = this.notificationRepository.markAllRead(user.getId(), LocalDateTime.now(this.clock));

        LOG.info("{} notifications of current user marked as read.", marked);
    }
}
