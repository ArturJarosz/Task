package com.arturjarosz.task.supervision.model;

import com.arturjarosz.task.dto.SupervisionDto;
import com.arturjarosz.task.dto.SupervisionVisitDto;
import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.finance.model.PartialFinancialData;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;
import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "supervision_sequence", allocationSize = 1)
@Table(name = "SUPERVISION")
public class Supervision extends AbstractAggregateRoot implements PartialFinancialData {
    @Serial
    private static final long serialVersionUID = -1180515376945392460L;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "BASE_NET_RATE", nullable = false))
    private Money baseNetRate;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "HOURLY_NET_RATE", nullable = false))
    private Money hourlyNetRate;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "VISIT_NET_RATE", nullable = false))
    private Money visitNetRate;

    @Getter
    @Column(name = "HOURS_COUNT")
    private int hoursCount;

    @Getter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "FINANCIAL_DATA_ID", referencedColumnName = "ID")
    private FinancialData financialData;

    private String note;

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "SUPERVISION_ID")
    private Set<SupervisionVisit> supervisionVisits;

    @Getter
    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    protected Supervision() {
        // needed by JPA
    }

    public Supervision(SupervisionDto supervisionDto) {
        this.projectId = supervisionDto.getProjectId();
        this.baseNetRate = new Money(supervisionDto.getBaseNetRate());
        this.hourlyNetRate = new Money(supervisionDto.getHourlyNetRate());
        this.visitNetRate = new Money(supervisionDto.getVisitNetRate());
        this.note = supervisionDto.getNote();
        this.supervisionVisits = new HashSet<>();
        this.financialData = new FinancialData(new Money(supervisionDto.getBaseNetRate()),
                supervisionDto.getHasInvoice(), true);
    }

    public void update(SupervisionDto supervisionDto) {
        this.baseNetRate = new Money(supervisionDto.getBaseNetRate());
        this.hourlyNetRate = new Money(supervisionDto.getHourlyNetRate());
        this.visitNetRate = new Money(supervisionDto.getVisitNetRate());
        this.financialData.setHasInvoice(supervisionDto.getHasInvoice());
        this.note = supervisionDto.getNote();
    }

    public BigDecimal getBaseNetRate() {
        return this.baseNetRate.getValue();
    }

    public BigDecimal getHourlyNetRate() {
        return this.hourlyNetRate.getValue();
    }

    public BigDecimal getVisitNetRate() {
        return this.visitNetRate.getValue();
    }

    public void setHoursCount(int hoursCount) {
        this.hoursCount = hoursCount;
    }

    public void addSupervisionVisit(SupervisionVisit supervisionVisit) {
        if (this.supervisionVisits == null) {
            this.supervisionVisits = new HashSet<>();
        }
        this.supervisionVisits.add(supervisionVisit);
    }

    public void removeSupervisionVisit(Long supervisionVisitId) {
        this.supervisionVisits.removeIf(supervisionVisit -> supervisionVisit.getId().equals(supervisionVisitId));
    }

    public SupervisionVisit updateSupervisionVisit(Long supervisionVisitId,
            SupervisionVisitDto supervisionVisitDto) {
        SupervisionVisit supervisionVisit = this.supervisionVisits.stream()
                .filter(sv -> sv.getId().equals(supervisionVisitId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
        return supervisionVisit.update(supervisionVisitDto);
    }
}
