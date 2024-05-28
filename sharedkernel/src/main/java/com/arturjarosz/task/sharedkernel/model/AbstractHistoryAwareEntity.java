package com.arturjarosz.task.sharedkernel.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractHistoryAwareEntity extends AbstractEntity {

    @Setter
    @Getter
    @CreatedDate
    @Column(name = "CREATED_DATETIME", nullable = false, updatable = false)
    private LocalDateTime createdDateTime;

    @Setter
    @Getter
    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATETIME")
    private LocalDateTime lastModifiedDateTime;
}
