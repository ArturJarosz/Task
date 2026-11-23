package com.arturjarosz.task.notification.rest;

import com.arturjarosz.task.dto.NotificationCountDto;
import com.arturjarosz.task.dto.NotificationReadDto;
import com.arturjarosz.task.dto.NotificationSettingDto;
import com.arturjarosz.task.dto.NotificationTypeDto;
import com.arturjarosz.task.dto.NotificationsDto;
import com.arturjarosz.task.notification.application.NotificationApplicationService;
import com.arturjarosz.task.notification.application.NotificationSettingsApplicationService;
import com.arturjarosz.task.rest.NotificationApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class NotificationRestController implements NotificationApi {

    @NonNull
    private final NotificationApplicationService notificationApplicationService;
    @NonNull
    private final NotificationSettingsApplicationService notificationSettingsApplicationService;

    @Override
    public ResponseEntity<NotificationsDto> getNotifications(Boolean unread, NotificationTypeDto type, Integer page,
            Integer size) {
        return new ResponseEntity<>(this.notificationApplicationService.getNotifications(unread, type, page, size),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<NotificationCountDto> getNotificationCount() {
        return new ResponseEntity<>(this.notificationApplicationService.getNotificationCount(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateNotificationRead(NotificationReadDto notificationReadDto, Long notificationId) {
        this.notificationApplicationService.updateNotificationRead(notificationId, notificationReadDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> markAllNotificationsRead() {
        this.notificationApplicationService.markAllNotificationsRead();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<NotificationSettingDto>> getNotificationSettings() {
        return new ResponseEntity<>(this.notificationSettingsApplicationService.getNotificationSettings(),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<NotificationSettingDto>> updateNotificationSettings(
            List<NotificationSettingDto> notificationSettingDtos) {
        return new ResponseEntity<>(
                this.notificationSettingsApplicationService.updateNotificationSettings(notificationSettingDtos),
                HttpStatus.OK);
    }
}
