package com.arturjarosz.task.finance.model;

import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;

public record SupplierSupplyDataDto(SupplyDto supply, FinancialDataDto financialData) {
}
