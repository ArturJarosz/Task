package com.arturjarosz.task.architect.domain;

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

public class ArchitectValidator {

    public static void validateBasicArchitectDto(ArchitectBasicDto architectBasicDto) {
        assertIsTrue(architectBasicDto != null,
                createMessageCode(ExceptionCodes.IS_NULL, ArchitectExceptionCodes.ARCHITECT));
        validateName(architectBasicDto.getFirstName(), ArchitectExceptionCodes.FIRST_NAME);
        validateName(architectBasicDto.getLastName(), ArchitectExceptionCodes.LAST_NAME);
    }

    public static void validateArchitectDto(ArchitectDto architectDto) {
        assertIsTrue(architectDto != null,
                createMessageCode(ExceptionCodes.IS_NULL, ArchitectExceptionCodes.ARCHITECT));
        validateName(architectDto.getFirstName(), ArchitectExceptionCodes.FIRST_NAME);
        validateName(architectDto.getLastName(), ArchitectExceptionCodes.LAST_NAME);
    }

    private static void validateName(String name, String nameExceptionCode) {
        assertIsTrue(name != null,
                createMessageCode(ExceptionCodes.IS_NULL, ArchitectExceptionCodes.ARCHITECT, nameExceptionCode));
        assertNotEmpty(name,
                createMessageCode(ExceptionCodes.EMPTY, ArchitectExceptionCodes.ARCHITECT, nameExceptionCode));
    }

}
