package com.arturjarosz.task.project.application;

import com.arturjarosz.task.cooperator.domain.CooperatorExceptionCodes;
import com.arturjarosz.task.cooperator.query.CooperatorQueryService;
import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class SupplyValidator {

    private final CooperatorQueryService cooperatorQueryService;
    private final ProjectQueryService projectQueryService;

    @Autowired
    public SupplyValidator(CooperatorQueryService cooperatorQueryService, ProjectQueryService projectQueryService) {
        this.cooperatorQueryService = cooperatorQueryService;
        this.projectQueryService = projectQueryService;
    }


    public void validateSupplierExistence(Long supplierId) {
        assertIsTrue(this.cooperatorQueryService.supplierWithIdExists(supplierId),
                createMessageCode(ExceptionCodes.NOT_EXIST, CooperatorExceptionCodes.SUPPLIER), supplierId);
    }

    public void validateCreateSupplyDto(SupplyDto supplyDto) {
        assertNotNull(supplyDto, createMessageCode(ExceptionCodes.NULL, SupplyExceptionCodes.SUPPLY));
        assertNotNull(supplyDto.getSupplierId(),
                createMessageCode(ExceptionCodes.NULL, SupplyExceptionCodes.SUPPLY, SupplyExceptionCodes.SUPPLIER));
        this.validateSupplyName(supplyDto.getName());
        this.validateSupplyValue(supplyDto.getValue());
    }

    private void validateSupplyName(String name) {
        assertNotNull(name,
                createMessageCode(ExceptionCodes.NULL, SupplyExceptionCodes.SUPPLY, SupplyExceptionCodes.NAME));
        assertIsTrue(!name.isBlank(),
                createMessageCode(ExceptionCodes.EMPTY, SupplyExceptionCodes.SUPPLY, SupplyExceptionCodes.NAME));
    }

    private void validateSupplyValue(BigDecimal value) {
        assertNotNull(value,
                createMessageCode(ExceptionCodes.NULL, SupplyExceptionCodes.SUPPLY, SupplyExceptionCodes.VALUE));
        assertIsTrue(value.doubleValue() >= 0,
                createMessageCode(ExceptionCodes.NEGATIVE, SupplyExceptionCodes.SUPPLY, SupplyExceptionCodes.VALUE));
    }

    public void validateSupplyOnProjectExistence(Long projectId, Long supplyId) {
        assertNotNull(this.projectQueryService.getSupplyForProject(projectId, supplyId),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                        SupplyExceptionCodes.SUPPLY), projectId, supplyId);
    }

    public void validateUpdateSupplyDto(SupplyDto supplyDto) {
        assertNotNull(supplyDto, createMessageCode(ExceptionCodes.NULL, SupplyExceptionCodes.SUPPLY));
        this.validateSupplyName(supplyDto.getName());
        this.validateSupplyValue(supplyDto.getValue());
    }
}
