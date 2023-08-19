package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.application.ProjectFinancialSummaryService;
import com.arturjarosz.task.rest.FinancialDataApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class FinancialDataRestController implements FinancialDataApi {

    @NonNull
    private final ProjectFinancialSummaryService projectFinancialSummaryService;

    public ResponseEntity<TotalProjectFinancialSummaryDto> getTotalProjectFinancialSummary(
            @PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.projectFinancialSummaryService.getTotalProjectFinancialSummary(projectId),
                HttpStatus.OK);
    }
}
