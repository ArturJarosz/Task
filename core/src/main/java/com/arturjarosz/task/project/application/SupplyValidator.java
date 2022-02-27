package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.supplier.query.SupplierQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class SupplyValidator {

    private final SupplierQueryService supplierQueryService;
    private final ProjectQueryService projectQueryService;

    @Autowired
    public SupplyValidator(SupplierQueryService supplierQueryService, ProjectQueryService projectQueryService) {
        this.supplierQueryService = supplierQueryService;
        this.projectQueryService = projectQueryService;
    }


    public void validateSupplierExistence(Long supplierId) {
        assertIsTrue(this.supplierQueryService.supplierWithIdExists(supplierId),
                createMessageCode(ExceptionCodes.NOT_EXIST, SupplyExceptionCodes.SUPPLIER), supplierId);
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
