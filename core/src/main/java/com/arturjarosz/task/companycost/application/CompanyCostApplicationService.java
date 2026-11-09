package com.arturjarosz.task.companycost.application;

import com.arturjarosz.task.dto.CompanyCostAmountDto;
import com.arturjarosz.task.dto.CompanyCostChargeDto;
import com.arturjarosz.task.dto.CompanyCostDto;
import com.arturjarosz.task.dto.CompanyCostPaymentDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Company costs outside projects, their amount history and their paid charges. Every method validates its input and
 * throws an exception when the cost, an amount or a payment does not exist or a change is not allowed.
 */
public interface CompanyCostApplicationService {

    /**
     * All company costs, by name. With a period (startDate and endDate, both or neither), only the costs with a charge
     * due in it or a payment made in it, each with its period figures.
     */
    List<CompanyCostDto> getCompanyCosts(LocalDate startDate, LocalDate endDate);

    CompanyCostDto getCompanyCost(Long companyCostId);

    /**
     * Creates a one-time or recurring cost. A one-time cost with paid is paid at once; a recurring cost with
     * markPastChargesAsPaid gets every charge before today paid on its charge date. Nothing else is paid.
     */
    CompanyCostDto createCompanyCost(CompanyCostDto companyCostDto);

    /**
     * Updates a cost; its kind cannot change. A change that would leave a paid charge outside the charges of the cost
     * is refused.
     */
    CompanyCostDto updateCompanyCost(Long companyCostId, CompanyCostDto companyCostDto);

    /**
     * Removes the cost with its amounts and payments.
     */
    void deleteCompanyCost(Long companyCostId);

    /**
     * Charges due in the given year, newest first, paid or not.
     */
    List<CompanyCostChargeDto> getCharges(Long companyCostId, Integer year);

    CompanyCostDto createAmount(Long companyCostId, CompanyCostAmountDto amountDto);

    CompanyCostDto updateAmount(Long companyCostId, Long amountId, CompanyCostAmountDto amountDto);

    CompanyCostDto deleteAmount(Long companyCostId, Long amountId);

    /**
     * Marks a charge as paid, always at its scheduled amount.
     */
    CompanyCostPaymentDto createPayment(Long companyCostId, CompanyCostPaymentDto paymentDto);

    /**
     * Changes the payment date of a paid charge.
     */
    CompanyCostPaymentDto updatePayment(Long companyCostId, Long paymentId, CompanyCostPaymentDto paymentDto);

    /**
     * Marks a paid charge as not paid.
     */
    void deletePayment(Long companyCostId, Long paymentId);
}
