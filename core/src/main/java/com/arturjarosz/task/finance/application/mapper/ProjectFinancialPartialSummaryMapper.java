package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.model.ProjectFinancialPartialSummary;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface ProjectFinancialPartialSummaryMapper {

    TotalProjectFinancialSummaryDto map(ProjectFinancialPartialSummary projectFinancialPartialSummary);
}
