package com.arturjarosz.task.contract.application;

import com.arturjarosz.task.contract.application.dto.ContractDto;
import com.arturjarosz.task.contract.domain.ContractExceptionCodes;
import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class ContractValidator {

    public void validateOffer(ContractDto contractDto) {
        assertNotNull(contractDto, createMessageCode(ExceptionCodes.NULL, ContractExceptionCodes.PROJECT,
                ContractExceptionCodes.CONTRACT));
        assertNotNull(contractDto.getOfferValue(),
                createMessageCode(ExceptionCodes.NULL, ContractExceptionCodes.CONTRACT, ContractExceptionCodes.OFFER,
                        ContractExceptionCodes.VALUE));
        assertIsTrue(contractDto.getOfferValue() >= 0.0,
                createMessageCode(ExceptionCodes.NEGATIVE, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.OFFER, ContractExceptionCodes.VALUE));
        if (contractDto.getDeadline() != null) {
            assertIsTrue(contractDto.getDeadline().isAfter(LocalDate.now()),
                    createMessageCode(ExceptionCodes.NOT_VALID, ContractExceptionCodes.CONTRACT,
                            ContractExceptionCodes.DEADLINE));
        }
    }

    public void validateContractExistence(Optional<Contract> maybeContract, Long contractId) {
        assertIsTrue(maybeContract.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, ContractExceptionCodes.CONTRACT), contractId);
    }

    public void validateSignContractDto(ContractDto contractDto) {
        assertNotNull(contractDto, createMessageCode(ExceptionCodes.NULL, ContractExceptionCodes.PROJECT,
                ContractExceptionCodes.CONTRACT));
        assertNotNull(contractDto.getOfferValue(),
                createMessageCode(ExceptionCodes.NULL, ContractExceptionCodes.CONTRACT, ContractExceptionCodes.OFFER,
                        ContractExceptionCodes.VALUE));
        assertIsTrue(contractDto.getOfferValue() >= 0.0,
                createMessageCode(ExceptionCodes.NEGATIVE, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.OFFER, ContractExceptionCodes.VALUE));
        assertNotNull(contractDto.getSigningDate(),
                createMessageCode(ExceptionCodes.NULL, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.SIGNING_DATE));
        assertIsTrue(!contractDto.getSigningDate().isAfter(LocalDate.now()),
                createMessageCode(ExceptionCodes.NOT_VALID, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.SIGNING_DATE));
        assertNotNull(contractDto.getDeadline(), createMessageCode(ExceptionCodes.NULL, ContractExceptionCodes.CONTRACT,
                ContractExceptionCodes.DEADLINE));
        assertIsTrue(contractDto.getDeadline().isAfter(LocalDate.now()),
                createMessageCode(ExceptionCodes.NOT_VALID, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.DEADLINE));
        assertNotNull(contractDto.getStartDate(),
                createMessageCode(ExceptionCodes.NULL, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.START_DATE));
        assertIsTrue(!contractDto.getStartDate().isBefore(contractDto.getSigningDate()),
                createMessageCode(ExceptionCodes.NOT_VALID, ContractExceptionCodes.CONTRACT,
                        ContractExceptionCodes.START_DATE));
    }

    public void validateTerminateContractDto(ContractDto contractDto) {
        this.validateCompleteContractDto(contractDto);
    }

    public void validateCompleteContractDto(ContractDto contractDto) {
        assertNotNull(contractDto, createMessageCode(ExceptionCodes.NULL, ContractExceptionCodes.PROJECT,
                ContractExceptionCodes.CONTRACT));
        assertNotNull(contractDto.getEndDate(), createMessageCode(ExceptionCodes.NULL, ContractExceptionCodes.CONTRACT,
                ContractExceptionCodes.END_DATE));
    }
}
