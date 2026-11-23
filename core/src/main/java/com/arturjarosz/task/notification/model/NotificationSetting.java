package com.arturjarosz.task.notification.model;

import com.arturjarosz.task.notification.domain.NotificationPreference;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A notification type a user has changed; types without one use the defaults of {@link NotificationType}.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "notification_setting_sequence", allocationSize = 1)
@Table(name = "NOTIFICATION_SETTING")
@Getter
public class NotificationSetting extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = -3527981447106388104L;

    @Column(name = "USER_ID", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, updatable = false)
    private NotificationType type;

    @Column(name = "ENABLED", nullable = false)
    private boolean enabled;

    // Days before the due date of each reminder. Loaded in batches, as the nightly job reads the settings of every user
    @ElementCollection
    @CollectionTable(name = "NOTIFICATION_SETTING_REMINDER", joinColumns = @JoinColumn(name = "NOTIFICATION_SETTING_ID"))
    @Column(name = "DAYS_BEFORE", nullable = false)
    @BatchSize(size = 50)
    private Set<Integer> daysBefore = new HashSet<>();

    protected NotificationSetting() {
        // needed by JPA
    }

    public NotificationSetting(Long userId, NotificationType type, NotificationPreference preference) {
        this.userId = userId;
        this.type = type;
        this.update(preference);
    }

    public void update(NotificationPreference preference) {
        this.enabled = preference.enabled();
        this.daysBefore.retainAll(preference.daysBefore());
        this.daysBefore.addAll(preference.daysBefore());
    }

    public NotificationPreference toPreference() {
        return new NotificationPreference(this.enabled, new ArrayList<>(this.daysBefore));
    }
}
