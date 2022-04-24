package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.finance.application.dto.TotalProjectFinancialDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FinancialDataRestController {

    private final ProjectFinancialDataService projectFinancialDataService;

    @Autowired
    public FinancialDataRestController(ProjectFinancialDataService projectFinancialDataService) {
        this.projectFinancialDataService = projectFinancialDataService;
    }

    @GetMapping("projects/{projectId}/totalFinancialData")
    public ResponseEntity<TotalProjectFinancialDataDto> getTotalProjectFinancialData(
            @PathVariable("projectId") Long projectId) {
        return new ResponseEntity<>(this.projectFinancialDataService.getTotalProjectFinancialData(projectId),
                HttpStatus.OK);
    }
}
