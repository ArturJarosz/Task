package com.arturjarosz.task.companycost.rest;

import com.arturjarosz.task.companycost.application.CompanyCostApplicationService;
import com.arturjarosz.task.dto.CompanyCostAmountDto;
import com.arturjarosz.task.dto.CompanyCostChargeDto;
import com.arturjarosz.task.dto.CompanyCostDto;
import com.arturjarosz.task.dto.CompanyCostPaymentDto;
import com.arturjarosz.task.rest.CompanyCostApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class CompanyCostRestController implements CompanyCostApi {

    @NonNull
    private final CompanyCostApplicationService companyCostApplicationService;

    @Override
    public ResponseEntity<List<CompanyCostDto>> getCompanyCosts(LocalDate startDate, LocalDate endDate) {
        return new ResponseEntity<>(this.companyCostApplicationService.getCompanyCosts(startDate, endDate),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CompanyCostDto> getCompanyCost(Long companyCostId) {
        return new ResponseEntity<>(this.companyCostApplicationService.getCompanyCost(companyCostId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CompanyCostDto> createCompanyCost(CompanyCostDto companyCostDto) {
        var companyCost = this.companyCostApplicationService.createCompanyCost(companyCostDto);
        var headers = new HttpHeadersBuilder().withLocation("/company-costs/{companyCostId}", companyCost.getId())
                .build();
        return new ResponseEntity<>(companyCost, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CompanyCostDto> updateCompanyCost(CompanyCostDto companyCostDto, Long companyCostId) {
        return new ResponseEntity<>(this.companyCostApplicationService.updateCompanyCost(companyCostId,
                companyCostDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteCompanyCost(Long companyCostId) {
        this.companyCostApplicationService.deleteCompanyCost(companyCostId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CompanyCostChargeDto>> getCompanyCostCharges(Long companyCostId, Integer year) {
        return new ResponseEntity<>(this.companyCostApplicationService.getCharges(companyCostId, year),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CompanyCostDto> createCompanyCostAmount(CompanyCostAmountDto companyCostAmountDto,
            Long companyCostId) {
        var companyCost = this.companyCostApplicationService.createAmount(companyCostId, companyCostAmountDto);
        var headers = new HttpHeadersBuilder().withLocation("/company-costs/{companyCostId}", companyCostId).build();
        return new ResponseEntity<>(companyCost, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CompanyCostDto> updateCompanyCostAmount(CompanyCostAmountDto companyCostAmountDto,
            Long companyCostId, Long amountId) {
        return new ResponseEntity<>(this.companyCostApplicationService.updateAmount(companyCostId, amountId,
                companyCostAmountDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CompanyCostDto> deleteCompanyCostAmount(Long companyCostId, Long amountId) {
        return new ResponseEntity<>(this.companyCostApplicationService.deleteAmount(companyCostId, amountId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CompanyCostPaymentDto> createCompanyCostPayment(CompanyCostPaymentDto companyCostPaymentDto,
            Long companyCostId) {
        var payment = this.companyCostApplicationService.createPayment(companyCostId, companyCostPaymentDto);
        var headers = new HttpHeadersBuilder()
                .withLocation("/company-costs/{companyCostId}/payments/{paymentId}", companyCostId, payment.getId())
                .build();
        return new ResponseEntity<>(payment, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CompanyCostPaymentDto> updateCompanyCostPayment(CompanyCostPaymentDto companyCostPaymentDto,
            Long companyCostId, Long paymentId) {
        return new ResponseEntity<>(this.companyCostApplicationService.updatePayment(companyCostId, paymentId,
                companyCostPaymentDto), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteCompanyCostPayment(Long companyCostId, Long paymentId) {
        this.companyCostApplicationService.deletePayment(companyCostId, paymentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
