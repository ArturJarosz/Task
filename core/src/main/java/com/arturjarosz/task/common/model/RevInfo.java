package com.arturjarosz.task.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.LocalDateTime;

@Entity
@RevisionEntity
@SequenceGenerator(name = "sequence_generator", sequenceName = "revinfo_seqence", allocationSize = 1)
@Table(name = "REVINFO")
@Data
public class RevInfo {

    @Id
    @RevisionNumber
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    private Integer rev;

    @RevisionTimestamp
    @Column(name = "REVTSTMP")
    private LocalDateTime revtstmp;
}
