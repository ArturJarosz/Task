package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.dto.FinancialReportDto;
import com.arturjarosz.task.dto.PeriodTypeDto;

import java.time.LocalDate;

public interface FinancialReportApplicationService {
    FinancialReportDto getFinancialReport(LocalDate startDate, LocalDate endDate, PeriodTypeDto periodType);
}
