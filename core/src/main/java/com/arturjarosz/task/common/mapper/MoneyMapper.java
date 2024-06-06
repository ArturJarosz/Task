package com.arturjarosz.task.common.mapper;

import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MoneyMapper {
    default Double map(Money source) {
        return source.getValue().doubleValue();
    }
}
