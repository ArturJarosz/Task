package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.finance.application.ProjectFinancialSummaryService;
import com.arturjarosz.task.finance.application.dto.TotalProjectFinancialSummaryDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class FinancialDataRestController {

    @NonNull
    private final ProjectFinancialSummaryService projectFinancialSummaryService;

    @GetMapping("projects/{projectId}/totalFinancialSummary")
    public ResponseEntity<TotalProjectFinancialSummaryDto> getTotalProjectFinancialSummary(
            @PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.projectFinancialSummaryService.getTotalProjectFinancialSummary(projectId),
                HttpStatus.OK);
    }
}
