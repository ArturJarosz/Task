package com.arturjarosz.task.companycost.application.impl;

import com.arturjarosz.task.companycost.application.CompanyCostApplicationService;
import com.arturjarosz.task.companycost.application.CompanyCostAssembler;
import com.arturjarosz.task.companycost.application.CompanyCostValidator;
import com.arturjarosz.task.companycost.domain.ChargePeriod;
import com.arturjarosz.task.companycost.domain.ChargeSchedule;
import com.arturjarosz.task.companycost.infrastructure.CompanyCostRepository;
import com.arturjarosz.task.companycost.model.CompanyCost;
import com.arturjarosz.task.dto.CompanyCostAmountDto;
import com.arturjarosz.task.dto.CompanyCostChargeDto;
import com.arturjarosz.task.dto.CompanyCostDto;
import com.arturjarosz.task.dto.CompanyCostKindDto;
import com.arturjarosz.task.dto.CompanyCostPaymentDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class CompanyCostApplicationServiceImpl implements CompanyCostApplicationService {

    private final CompanyCostRepository companyCostRepository;
    private final CompanyCostValidator companyCostValidator;
    private final CompanyCostAssembler companyCostAssembler;

    @Transactional(readOnly = true)
    @Override
    public List<CompanyCostDto> getCompanyCosts(LocalDate startDate, LocalDate endDate) {
        LOG.info("Loading company costs.");
        this.companyCostValidator.validatePeriod(startDate, endDate);
        var today = ChargeSchedule.today();
        var period = startDate == null ? null : new ChargePeriod(startDate, endDate);
        // With a period, the costs that charged or paid in it: a payment in the period is money spent in it, as the
        // financial report counts it, even when its charge was due before
        return this.companyCostRepository.findAll(Sort.by("name", "id")).stream()
                .map(companyCost -> this.companyCostAssembler.toDto(companyCost, today, period))
                .filter(dto -> period == null || dto.getPeriodCharges() > 0 || dto.getPeriodPaidNetValue().signum() > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CompanyCostDto getCompanyCost(Long companyCostId) {
        LOG.info("Loading company cost with id {}.", companyCostId);
        return this.companyCostAssembler.toDto(this.loadCompanyCost(companyCostId), ChargeSchedule.today());
    }

    @Transactional
    @Override
    public CompanyCostDto createCompanyCost(CompanyCostDto companyCostDto) {
        LOG.info("Creating company cost.");
        var today = ChargeSchedule.today();
        this.companyCostValidator.validateCreateDto(companyCostDto, today);
        var name = companyCostDto.getName().strip();
        var note = StringUtils.trimToNull(companyCostDto.getNote());
        var hasInvoice = Boolean.TRUE.equals(companyCostDto.getHasInvoice());
        CompanyCost companyCost;
        if (companyCostDto.getKind() == CompanyCostKindDto.ONE_TIME) {
            companyCost = CompanyCost.oneTime(name, companyCostDto.getCategoryId(), companyCostDto.getDate(),
                    hasInvoice, note, companyCostDto.getNetValue());
            if (Boolean.TRUE.equals(companyCostDto.getPaid())) {
                companyCost.addPayment(companyCostDto.getDate(), companyCostDto.getPaymentDate(),
                        companyCostDto.getNetValue());
            }
        } else {
            companyCost = CompanyCost.recurring(name, companyCostDto.getCategoryId(),
                    CompanyCostValidator.toRepeats(companyCostDto), companyCostDto.getStartMonth(),
                    companyCostDto.getEndMonth(), companyCostDto.getChargeDay(), hasInvoice, note,
                    companyCostDto.getNetValue());
            if (Boolean.TRUE.equals(companyCostDto.getMarkPastChargesAsPaid())) {
                var schedule = ChargeSchedule.of(companyCost);
                var newCompanyCost = companyCost;
                schedule.chargeMonthsBefore(today).forEach(month -> newCompanyCost.addPayment(month,
                        schedule.chargeDate(month), schedule.netValue(month)));
            }
        }
        companyCost = this.companyCostRepository.save(companyCost);
        LOG.info("Company cost with id {} created.", companyCost.getId());
        return this.companyCostAssembler.toDto(companyCost, today);
    }

    @Transactional
    @Override
    public CompanyCostDto updateCompanyCost(Long companyCostId, CompanyCostDto companyCostDto) {
        LOG.info("Updating company cost with id {}.", companyCostId);
        var companyCost = this.loadCompanyCost(companyCostId);
        this.companyCostValidator.validateUpdateDto(companyCostDto, companyCost);
        companyCost.update(companyCostDto.getName().strip(), companyCostDto.getCategoryId(),
                Boolean.TRUE.equals(companyCostDto.getHasInvoice()), StringUtils.trimToNull(companyCostDto.getNote()));
        if (companyCost.isRecurring()) {
            companyCost.updateRecurring(CompanyCostValidator.toRepeats(companyCostDto), companyCostDto.getStartMonth(),
                    companyCostDto.getEndMonth(), companyCostDto.getChargeDay());
        } else {
            companyCost.updateOneTime(companyCostDto.getDate(), companyCostDto.getNetValue());
        }
        companyCost = this.companyCostRepository.save(companyCost);
        LOG.info("Company cost with id {} updated.", companyCostId);
        return this.companyCostAssembler.toDto(companyCost, ChargeSchedule.today());
    }

    @Transactional
    @Override
    public void deleteCompanyCost(Long companyCostId) {
        LOG.info("Deleting company cost with id {}.", companyCostId);
        this.companyCostRepository.delete(this.loadCompanyCost(companyCostId));
        LOG.info("Company cost with id {} deleted.", companyCostId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompanyCostChargeDto> getCharges(Long companyCostId, Integer year) {
        LOG.info("Loading charges of company cost with id {} in {}.", companyCostId, year);
        var companyCost = this.loadCompanyCost(companyCostId);
        this.companyCostValidator.validateChargesYear(year);
        return this.companyCostAssembler.toChargeDtos(companyCost, year, ChargeSchedule.today());
    }

    @Transactional
    @Override
    public CompanyCostDto createAmount(Long companyCostId, CompanyCostAmountDto amountDto) {
        LOG.info("Changing the amount of company cost with id {}.", companyCostId);
        var companyCost = this.loadCompanyCost(companyCostId);
        this.companyCostValidator.validateNewAmountDto(amountDto, companyCost);
        companyCost.addAmount(amountDto.getValidFrom(), amountDto.getNetValue());
        return this.saveAndAssemble(companyCost);
    }

    @Transactional
    @Override
    public CompanyCostDto updateAmount(Long companyCostId, Long amountId, CompanyCostAmountDto amountDto) {
        LOG.info("Updating amount with id {} of company cost with id {}.", amountId, companyCostId);
        var companyCost = this.loadCompanyCost(companyCostId);
        this.companyCostValidator.validateUpdatedAmountDto(amountDto, companyCost, amountId);
        companyCost.updateAmount(amountId, amountDto.getValidFrom(), amountDto.getNetValue());
        return this.saveAndAssemble(companyCost);
    }

    @Transactional
    @Override
    public CompanyCostDto deleteAmount(Long companyCostId, Long amountId) {
        LOG.info("Deleting amount with id {} of company cost with id {}.", amountId, companyCostId);
        var companyCost = this.loadCompanyCost(companyCostId);
        this.companyCostValidator.validateAmountRemoval(companyCost, amountId);
        companyCost.removeAmount(amountId);
        return this.saveAndAssemble(companyCost);
    }

    @Transactional
    @Override
    public CompanyCostPaymentDto createPayment(Long companyCostId, CompanyCostPaymentDto paymentDto) {
        LOG.info("Paying a charge of company cost with id {}.", companyCostId);
        var companyCost = this.loadCompanyCost(companyCostId);
        this.companyCostValidator.validateNewPaymentDto(paymentDto, companyCost, ChargeSchedule.today());
        var chargeMonth = CompanyCost.monthOf(paymentDto.getChargeMonth());
        companyCost.addPayment(chargeMonth, paymentDto.getPaymentDate(),
                ChargeSchedule.of(companyCost).netValue(chargeMonth));
        // flushed, so the new payment has its id; saving merges it into a new instance, so it is looked up again
        companyCost = this.companyCostRepository.saveAndFlush(companyCost);
        var payment = companyCost.findPaymentOfCharge(chargeMonth).orElseThrow(ResourceNotFoundException::new);
        LOG.info("Payment with id {} created.", payment.getId());
        return this.companyCostAssembler.toPaymentDto(companyCost, payment);
    }

    @Transactional
    @Override
    public CompanyCostPaymentDto updatePayment(Long companyCostId, Long paymentId,
            CompanyCostPaymentDto paymentDto) {
        LOG.info("Updating payment with id {} of company cost with id {}.", paymentId, companyCostId);
        var companyCost = this.loadCompanyCost(companyCostId);
        this.companyCostValidator.validateUpdatedPaymentDto(paymentDto, companyCost, paymentId,
                ChargeSchedule.today());
        var payment = companyCost.changePaymentDate(paymentId, paymentDto.getPaymentDate());
        companyCost = this.companyCostRepository.save(companyCost);
        return this.companyCostAssembler.toPaymentDto(companyCost, payment);
    }

    @Transactional
    @Override
    public void deletePayment(Long companyCostId, Long paymentId) {
        LOG.info("Deleting payment with id {} of company cost with id {}.", paymentId, companyCostId);
        var companyCost = this.loadCompanyCost(companyCostId);
        this.companyCostValidator.validatePaymentExistence(companyCost, paymentId);
        companyCost.removePayment(paymentId);
        this.companyCostRepository.save(companyCost);
    }

    private CompanyCost loadCompanyCost(Long companyCostId) {
        var maybeCompanyCost = this.companyCostRepository.findById(companyCostId);
        this.companyCostValidator.validateExistence(maybeCompanyCost, companyCostId);
        return maybeCompanyCost.orElseThrow(ResourceNotFoundException::new);
    }

    private CompanyCostDto saveAndAssemble(CompanyCost companyCost) {
        // flushed, so a new amount has its id in the dto
        var savedCompanyCost = this.companyCostRepository.saveAndFlush(companyCost);
        return this.companyCostAssembler.toDto(savedCompanyCost, ChargeSchedule.today());
    }
}
