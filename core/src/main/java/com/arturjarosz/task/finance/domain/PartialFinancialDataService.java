package com.arturjarosz.task.finance.domain;

import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.model.PartialFinancialData;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;

public interface PartialFinancialDataService<T extends PartialFinancialData> {

    PartialFinancialDataType getType();

    FinancialValueDto getPartialFinancialData(long projectId);

    SummationStrategy getSummationStrategy();

}
