package com.arturjarosz.task.contractor.application;

import com.arturjarosz.task.dto.ContractorCategoryDto;
import com.arturjarosz.task.contractor.domain.ContractorExceptionCodes;
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository;
import com.arturjarosz.task.contractor.model.ContractorCategory;
import com.arturjarosz.task.contractor.query.ContractorQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsFalse;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates contractor category dtos and ContractorCategory entities.
 */
@Component
@RequiredArgsConstructor
public class ContractorCategoryValidator {

    public static final String CONTRACTOR_CATEGORY_WITH_CONTRACTORS_MESSAGE_CODE = createMessageCode(ExceptionCodes.NOT_VALID,
            ContractorExceptionCodes.CONTRACTOR_CATEGORY, ContractorExceptionCodes.CONTRACTORS);
    public static final String CONTRACTOR_CATEGORY_NAME_UNIQUE_MESSAGE_CODE = createMessageCode(ExceptionCodes.NOT_VALID,
            ContractorExceptionCodes.CONTRACTOR_CATEGORY, ContractorExceptionCodes.NAME, ContractorExceptionCodes.UNIQUE);

    private final ContractorCategoryRepository contractorCategoryRepository;
    private final ContractorQueryService contractorQueryService;

    public void validateCreateContractorCategoryDto(ContractorCategoryDto contractorCategoryDto) {
        this.validateContractorCategoryDto(contractorCategoryDto, null);
    }

    public void validateUpdateContractorCategoryDto(ContractorCategoryDto contractorCategoryDto, Long contractorCategoryId) {
        this.validateContractorCategoryDto(contractorCategoryDto, contractorCategoryId);
    }

    /**
     * Validates the name of a new Contractor category (contractorCategoryId null) or of the renamed one with
     * contractorCategoryId. Names are compared without surrounding whitespace and regardless of letter case.
     */
    private void validateContractorCategoryDto(ContractorCategoryDto contractorCategoryDto, Long contractorCategoryId) {
        assertNotNull(contractorCategoryDto, createMessageCode(ExceptionCodes.NULL, ContractorExceptionCodes.CONTRACTOR_CATEGORY));
        assertNotNull(contractorCategoryDto.getName(), createMessageCode(ExceptionCodes.NULL,
                ContractorExceptionCodes.CONTRACTOR_CATEGORY, ContractorExceptionCodes.NAME));
        var name = contractorCategoryDto.getName().strip();
        assertNotEmpty(name, createMessageCode(ExceptionCodes.EMPTY, ContractorExceptionCodes.CONTRACTOR_CATEGORY,
                ContractorExceptionCodes.NAME));
        assertIsTrue(name.length() <= ContractorCategory.NAME_MAX_LENGTH,
                createMessageCode(ExceptionCodes.NOT_VALID, ContractorExceptionCodes.CONTRACTOR_CATEGORY,
                        ContractorExceptionCodes.NAME, ContractorExceptionCodes.TOO_LONG), ContractorCategory.NAME_MAX_LENGTH);
        var nameTaken = contractorCategoryId == null
                ? this.contractorCategoryRepository.existsByNameIgnoreCase(name)
                : this.contractorCategoryRepository.existsByNameIgnoreCaseAndIdNot(name, contractorCategoryId);
        assertIsFalse(nameTaken, CONTRACTOR_CATEGORY_NAME_UNIQUE_MESSAGE_CODE, name);
    }

    public void validateContractorCategoryExistence(Optional<ContractorCategory> maybeContractorCategory, Long contractorCategoryId) {
        assertEntityPresent(maybeContractorCategory.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ContractorExceptionCodes.CONTRACTOR_CATEGORY), contractorCategoryId);
    }

    public void validateContractorCategoryExistence(Long contractorCategoryId) {
        this.validateContractorCategoryExistence(this.contractorCategoryRepository.findById(contractorCategoryId), contractorCategoryId);
    }

    public void validateContractorCategoryHasNoContractors(Long contractorCategoryId) {
        var contractorsCount = this.contractorQueryService.getNumberOfContractorsByCategoryIds(List.of(contractorCategoryId));
        assertIsTrue(contractorsCount.get(contractorCategoryId) == 0, CONTRACTOR_CATEGORY_WITH_CONTRACTORS_MESSAGE_CODE);
    }
}
