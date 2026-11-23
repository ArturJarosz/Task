package com.arturjarosz.task.notification.query.impl;

import com.arturjarosz.task.notification.model.Notification;
import com.arturjarosz.task.notification.model.NotificationType;
import com.arturjarosz.task.notification.model.QNotification;
import com.arturjarosz.task.notification.query.NotificationQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import java.util.List;

@Finder
public class NotificationQueryServiceImpl extends AbstractQueryService<QNotification>
        implements NotificationQueryService {

    private static final QNotification NOTIFICATION = QNotification.notification;

    public NotificationQueryServiceImpl() {
        super(NOTIFICATION);
    }

    @Override
    public List<Notification> getNotifications(long userId, boolean unreadOnly, NotificationType type, int page,
            int size) {
        return this.query().selectFrom(NOTIFICATION).where(filter(userId, unreadOnly, type))
                .orderBy(NOTIFICATION.dueDate.asc(), NOTIFICATION.dueDateTime.asc().nullsFirst(), NOTIFICATION.id.asc())
                .offset((long) page * size).limit(size).fetch();
    }

    @Override
    public long countNotifications(long userId, boolean unreadOnly, NotificationType type) {
        var count = this.query().select(NOTIFICATION.count()).from(NOTIFICATION)
                .where(filter(userId, unreadOnly, type)).fetchOne();
        return count != null ? count : 0;
    }

    private static Predicate filter(long userId, boolean unreadOnly, NotificationType type) {
        var filter = new BooleanBuilder(NOTIFICATION.userId.eq(userId));
        if (unreadOnly) {
            filter.and(NOTIFICATION.readDateTime.isNull());
        }
        if (type != null) {
            filter.and(NOTIFICATION.type.eq(type));
        }
        return filter;
    }
}
