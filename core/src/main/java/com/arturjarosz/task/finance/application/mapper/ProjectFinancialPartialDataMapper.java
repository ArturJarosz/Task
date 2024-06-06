package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.common.mapper.MoneyMapper;
import com.arturjarosz.task.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.model.ProjectFinancialPartialData;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN, uses = {MoneyMapper.class})
public interface ProjectFinancialPartialDataMapper {

    TotalProjectFinancialSummaryDto map(ProjectFinancialPartialData projectFinancialPartialData);
}
