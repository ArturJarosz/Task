package com.arturjarosz.task.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "revinfo_seqence", allocationSize = 1)
@Table(name = "REVINFO")
@Data
public class RevInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence_generator")
    private Integer rev;

    @Column(name = "REVTSTMP")
    private Instant revtstmp;
}
