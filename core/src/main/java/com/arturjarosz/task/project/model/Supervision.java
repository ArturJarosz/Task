package com.arturjarosz.task.project.model;

import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@SequenceGenerator(name = "supervision_sequence_generator", sequenceName = "supervision_sequence", allocationSize = 1)
@Table(name = "SUPERVISION")
public class Supervision extends AbstractEntity {
    private static final long serialVersionUID = -1180515376945392460L;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "BASE_NET_RATE", nullable = false))
    private Money baseNetRate;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "BASE_GROSS_RATE", nullable = false))
    private Money baseGrossNet;

    @Column(name = "HAS_INVOICE")
    private boolean hasInvoice;

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

    // TODO: TA-185 create and connect SUPERVISION_VISIT

    protected Supervision() {
        // Needed by Hibernate
    }

    public Supervision(boolean hasInvoice, BigDecimal baseNetRate, BigDecimal hourlyNetRate, BigDecimal visitNetRate) {
        this.financialData = new FinancialData(new Money(0), hasInvoice, true);
        this.baseNetRate = new Money(baseNetRate);
        //TODO: implement gross
        this.baseGrossNet = this.baseNetRate;
        this.hourlyNetRate = new Money(hourlyNetRate);
        this.visitNetRate = new Money(visitNetRate);
    }

    public BigDecimal getBaseNetRate() {
        return this.baseNetRate.getValue();
    }

    public BigDecimal getBaseGrossNet() {
        return this.baseGrossNet.getValue();
    }

    public boolean isHasInvoice() {
        return this.hasInvoice;
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
}
