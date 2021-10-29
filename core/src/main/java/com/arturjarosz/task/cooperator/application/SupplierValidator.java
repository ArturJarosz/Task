package com.arturjarosz.task.cooperator.application;

import com.arturjarosz.task.cooperator.application.dto.SupplierDto;
import com.arturjarosz.task.cooperator.domain.CooperatorExceptionCodes;
import com.arturjarosz.task.cooperator.infrastructure.CooperatorRepository;
import com.arturjarosz.task.cooperator.model.Cooperator;
import com.arturjarosz.task.cooperator.model.CooperatorType;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class SupplierValidator {

    private final CooperatorRepository cooperatorRepository;

    public SupplierValidator(CooperatorRepository cooperatorRepository) {
        this.cooperatorRepository = cooperatorRepository;
    }

    public static void validateCreateSupplierDto(SupplierDto supplierDto) {
        assertNotNull(supplierDto, createMessageCode(ExceptionCodes.NULL, CooperatorExceptionCodes.SUPPLIER));
        assertNotNull(supplierDto.getName(),
                createMessageCode(ExceptionCodes.NULL, CooperatorExceptionCodes.SUPPLIER,
                        CooperatorExceptionCodes.NAME));
        assertNotEmpty(supplierDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, CooperatorExceptionCodes.SUPPLIER,
                        CooperatorExceptionCodes.NAME));
    }

    public static void validateUpdateSupplierDto(SupplierDto supplierDto) {
        assertNotNull(supplierDto, createMessageCode(ExceptionCodes.NULL, CooperatorExceptionCodes.SUPPLIER));
        assertNotNull(supplierDto.getName(),
                createMessageCode(ExceptionCodes.NULL, CooperatorExceptionCodes.SUPPLIER,
                        CooperatorExceptionCodes.NAME));
        assertNotEmpty(supplierDto.getName(),
                createMessageCode(ExceptionCodes.EMPTY, CooperatorExceptionCodes.SUPPLIER,
                        CooperatorExceptionCodes.NAME));
    }

    public void validateSupplierExistence(Long supplierId) {
        Cooperator cooperator = this.cooperatorRepository.load(supplierId);
        assertNotNull(cooperator, createMessageCode(ExceptionCodes.NOT_EXIST, CooperatorExceptionCodes.SUPPLIER));
        assertIsTrue(cooperator.getType().equals(CooperatorType.SUPPLIER),
                createMessageCode(ExceptionCodes.NOT_EXIST, CooperatorExceptionCodes.SUPPLIER));
    }

    public void validateSupplierHasNoSupply(Long supplierId) {
        //TODO: to implemented when SupplierJob is ready
    }
}
