package com.arturjarosz.task.finance.application.validator;

import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.finance.application.SupplyExceptionCodes;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.supplier.query.SupplierQueryService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@RequiredArgsConstructor
@Component
public class SupplyValidator {

    @NonNull
    private final SupplierQueryService supplierQueryService;
    @NonNull
    private final FinancialDataQueryService financialDataQueryService;

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
        assertIsTrue(this.financialDataQueryService.doesSupplyForProjectExists(projectId, supplyId),
                createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.PROJECT,
                        SupplyExceptionCodes.SUPPLY), supplyId, projectId);
    }

    public void validateUpdateSupplyDto(SupplyDto supplyDto) {
        assertNotNull(supplyDto, createMessageCode(ExceptionCodes.NULL, SupplyExceptionCodes.SUPPLY));
        this.validateSupplyName(supplyDto.getName());
        this.validateSupplyValue(supplyDto.getValue());
    }
}
