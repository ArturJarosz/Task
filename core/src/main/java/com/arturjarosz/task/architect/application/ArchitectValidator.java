package com.arturjarosz.task.architect.application;

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.architect.domain.ArchitectExceptionCodes;
import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates architect related dtos and Architect domain model.
 */
@Component
public class ArchitectValidator {

    private final ArchitectRepository architectRepository;

    @Autowired
    public ArchitectValidator(ArchitectRepository architectRepository) {
        this.architectRepository = architectRepository;
    }

    public static void validateBasicArchitectDto(ArchitectBasicDto architectBasicDto) {
        assertIsTrue(architectBasicDto != null,
                createMessageCode(ExceptionCodes.NULL, ArchitectExceptionCodes.ARCHITECT));
        validateName(architectBasicDto.getFirstName(), ArchitectExceptionCodes.FIRST_NAME);
        validateName(architectBasicDto.getLastName(), ArchitectExceptionCodes.LAST_NAME);
    }

    public static void validateArchitectDto(ArchitectDto architectDto) {
        assertIsTrue(architectDto != null,
                createMessageCode(ExceptionCodes.NULL, ArchitectExceptionCodes.ARCHITECT));
        validateName(architectDto.getFirstName(), ArchitectExceptionCodes.FIRST_NAME);
        validateName(architectDto.getLastName(), ArchitectExceptionCodes.LAST_NAME);
    }

    private static void validateName(String name, String nameExceptionCode) {
        assertIsTrue(name != null,
                createMessageCode(ExceptionCodes.NULL, ArchitectExceptionCodes.ARCHITECT, nameExceptionCode));
        assertNotEmpty(name,
                createMessageCode(ExceptionCodes.EMPTY, ArchitectExceptionCodes.ARCHITECT, nameExceptionCode));
    }

    public static void validateArchitectExistence(Architect architect, Long architectId) {
        assertIsTrue(architect != null,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXISTS, ArchitectExceptionCodes.ARCHITECT),
                architectId);
    }

    public void validateArchitectExistence(Long architectId) {
        Architect architect = this.architectRepository.load(architectId);
        validateArchitectExistence(architect, architectId);
    }

}
