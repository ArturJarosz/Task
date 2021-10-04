package com.arturjarosz.task.supervision.model;

import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.supervision.application.dto.SupervisionDto;
import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "supervision_sequence", allocationSize = 1)
@Table(name = "SUPERVISION")
public class Supervision extends AbstractAggregateRoot {
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

    @Column(name = "HOURS_COUNT")
    private int hoursCount;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "FINANCIAL_DATA_ID", referencedColumnName = "ID")
    private FinancialData financialData;

    private String note;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "SUPERVISION_ID")
    private Set<SupervisionVisit> supervisionVisits;

    @Column(name = "PROJECT_ID")
    private Long projectId;

    protected Supervision() {
        // Needed by Hibernate
    }

    public Supervision(SupervisionDto supervisionDto) {
        this.projectId = supervisionDto.getProjectId();
        this.baseNetRate = new Money(supervisionDto.getBaseNetRate());
        this.hourlyNetRate = new Money(supervisionDto.getHourlyNetRate());
        this.visitNetRate = new Money(supervisionDto.getVisitNetRate());
        this.note = supervisionDto.getNote();
        this.supervisionVisits = new HashSet<>();
        this.financialData = new FinancialData(new Money(0), supervisionDto.isHasInvoice(), true);
    }

    public void update(SupervisionDto supervisionDto) {
        this.baseNetRate = new Money(supervisionDto.getBaseNetRate());
        this.hourlyNetRate = new Money(supervisionDto.getHourlyNetRate());
        this.visitNetRate = new Money(supervisionDto.getVisitNetRate());
        this.financialData.setHasInvoice(supervisionDto.isHasInvoice());
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

    public int getHoursCount() {
        return this.hoursCount;
    }

    public FinancialData getFinancialData() {
        return this.financialData;
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

    public Set<SupervisionVisit> getSupervisionVisits() {
        return this.supervisionVisits;
    }

    public Long getProjectId() {
        return projectId;
    }

    public SupervisionVisit updateSupervisionVisit(Long supervisionVisitId,
                                                   SupervisionVisitDto supervisionVisitDto) {
        SupervisionVisit supervisionVisit = this.supervisionVisits.stream()
                .filter(sv -> sv.getId().equals(supervisionVisitId)).findFirst().orElse(null);
        return supervisionVisit.update(supervisionVisitDto);
    }
}
