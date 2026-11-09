package com.arturjarosz.task.companycost.model;

import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A cost of the company outside any project (a salary, a subscription, rent). A one-time cost has one charge on its
 * date; a recurring one has a charge every month, quarter or year from its start month until its end month, if it has
 * one. The net value of each charge comes from the amount valid in its month (a one-time cost has exactly one amount),
 * and every paid charge is a payment. Months are stored as their first day.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "company_cost_sequence", allocationSize = 1)
@Table(name = "COMPANY_COST")
public class CompanyCost extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = -2829516270418396310L;

    @Getter
    @Column(name = "NAME", nullable = false)
    private String name;

    @Getter
    @Column(name = "CATEGORY_ID", nullable = false)
    private Long categoryId;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "KIND", nullable = false, updatable = false)
    private CompanyCostKind kind;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "REPEATS")
    private CompanyCostRepeats repeats;

    @Getter
    @Column(name = "START_MONTH")
    private LocalDate startMonth;

    @Getter
    @Column(name = "END_MONTH")
    private LocalDate endMonth;

    @Getter
    @Column(name = "CHARGE_DAY")
    private Integer chargeDay;

    @Getter
    @Column(name = "DATE")
    private LocalDate date;

    @Getter
    @Column(name = "HAS_INVOICE", nullable = false)
    private boolean hasInvoice;

    @Getter
    @Column(name = "NOTE")
    private String note;

    // Subselects load the amounts and the payments of a whole list of costs in one query each, not one per cost
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "COMPANY_COST_ID", nullable = false)
    private Set<CompanyCostAmount> amounts = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "COMPANY_COST_ID", nullable = false)
    private Set<CompanyCostPayment> payments = new HashSet<>();

    protected CompanyCost() {
        // needed by JPA
    }

    private CompanyCost(CompanyCostKind kind, String name, Long categoryId, boolean hasInvoice, String note) {
        this.kind = kind;
        this.update(name, categoryId, hasInvoice, note);
    }

    public static CompanyCost oneTime(String name, Long categoryId, LocalDate date, boolean hasInvoice, String note,
            BigDecimal netValue) {
        var companyCost = new CompanyCost(CompanyCostKind.ONE_TIME, name, categoryId, hasInvoice, note);
        companyCost.date = date;
        companyCost.amounts.add(new CompanyCostAmount(monthOf(date), netValue));
        return companyCost;
    }

    public static CompanyCost recurring(String name, Long categoryId, CompanyCostRepeats repeats,
            LocalDate startMonth, LocalDate endMonth, int chargeDay, boolean hasInvoice, String note,
            BigDecimal netValue) {
        var companyCost = new CompanyCost(CompanyCostKind.RECURRING, name, categoryId, hasInvoice, note);
        companyCost.setSchedule(repeats, startMonth, endMonth, chargeDay);
        companyCost.amounts.add(new CompanyCostAmount(companyCost.startMonth, netValue));
        return companyCost;
    }

    public static LocalDate monthOf(LocalDate date) {
        return date == null ? null : date.withDayOfMonth(1);
    }

    public boolean isRecurring() {
        return this.kind == CompanyCostKind.RECURRING;
    }

    public void update(String name, Long categoryId, boolean hasInvoice, String note) {
        this.name = name;
        this.categoryId = categoryId;
        this.hasInvoice = hasInvoice;
        this.note = note;
    }

    /**
     * Moves the charge of a one-time cost and sets its value. Its payment, if any, is not changed.
     */
    public void updateOneTime(LocalDate date, BigDecimal netValue) {
        this.date = date;
        this.getFirstAmount().update(monthOf(date), netValue);
    }

    /**
     * Changes the schedule of a recurring cost. The amount valid in the new start month moves to it (the first amount
     * when the start moves earlier), and amounts starting before the new start month or after the new end month are
     * removed, so every charge keeps the amount it had. Payments are not changed.
     */
    public void updateRecurring(CompanyCostRepeats repeats, LocalDate startMonth, LocalDate endMonth, int chargeDay) {
        this.setSchedule(repeats, startMonth, endMonth, chargeDay);
        var startAmount = this.findAmountValidIn(this.startMonth).orElseGet(this::getFirstAmount);
        this.amounts.removeIf(amount -> amount != startAmount && (amount.getValidFrom().isBefore(this.startMonth)
                || this.endMonth != null && amount.getValidFrom().isAfter(this.endMonth)));
        startAmount.update(this.startMonth, startAmount.getNetValue());
    }

    /**
     * Sets the net value from the month of validFrom on; an amount already starting in that month gets the new value.
     */
    public CompanyCostAmount addAmount(LocalDate validFrom, BigDecimal netValue) {
        var month = monthOf(validFrom);
        var existingAmount = this.amounts.stream().filter(amount -> amount.getValidFrom().equals(month)).findFirst();
        if (existingAmount.isPresent()) {
            existingAmount.get().update(month, netValue);
            return existingAmount.get();
        }
        var amount = new CompanyCostAmount(month, netValue);
        this.amounts.add(amount);
        return amount;
    }

    public void updateAmount(Long amountId, LocalDate validFrom, BigDecimal netValue) {
        this.findAmount(amountId).orElseThrow(ResourceNotFoundException::new).update(monthOf(validFrom), netValue);
    }

    public void removeAmount(Long amountId) {
        this.amounts.removeIf(amount -> amount.getId().equals(amountId));
    }

    public CompanyCostPayment addPayment(LocalDate chargeMonth, LocalDate paymentDate, BigDecimal netValue) {
        var payment = new CompanyCostPayment(monthOf(chargeMonth), paymentDate, netValue);
        this.payments.add(payment);
        return payment;
    }

    public CompanyCostPayment changePaymentDate(Long paymentId, LocalDate paymentDate) {
        var payment = this.findPayment(paymentId).orElseThrow(ResourceNotFoundException::new);
        payment.changePaymentDate(paymentDate);
        return payment;
    }

    public void removePayment(Long paymentId) {
        this.payments.removeIf(payment -> payment.getId().equals(paymentId));
    }

    /**
     * Amounts in the order they start.
     */
    public List<CompanyCostAmount> getAmounts() {
        return this.amounts.stream().sorted(Comparator.comparing(CompanyCostAmount::getValidFrom)).toList();
    }

    public CompanyCostAmount getFirstAmount() {
        return this.getAmounts().getFirst();
    }

    /**
     * Payments in the order of their charges.
     */
    public List<CompanyCostPayment> getPayments() {
        return this.payments.stream().sorted(Comparator.comparing(CompanyCostPayment::getChargeMonth)).toList();
    }

    public Optional<CompanyCostAmount> findAmount(Long amountId) {
        return this.amounts.stream().filter(amount -> amount.getId().equals(amountId)).findFirst();
    }

    public Optional<CompanyCostPayment> findPayment(Long paymentId) {
        return this.payments.stream().filter(payment -> payment.getId().equals(paymentId)).findFirst();
    }

    public Optional<CompanyCostPayment> findPaymentOfCharge(LocalDate chargeMonth) {
        var month = monthOf(chargeMonth);
        return this.payments.stream().filter(payment -> payment.getChargeMonth().equals(month)).findFirst();
    }

    private Optional<CompanyCostAmount> findAmountValidIn(LocalDate month) {
        return this.getAmounts().stream().filter(amount -> !amount.getValidFrom().isAfter(month))
                .reduce((earlier, later) -> later);
    }

    private void setSchedule(CompanyCostRepeats repeats, LocalDate startMonth, LocalDate endMonth, int chargeDay) {
        this.repeats = repeats;
        this.startMonth = monthOf(startMonth);
        this.endMonth = monthOf(endMonth);
        this.chargeDay = chargeDay;
    }
}
