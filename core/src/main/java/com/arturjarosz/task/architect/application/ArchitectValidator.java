package com.arturjarosz.task.architect.application;

import com.arturjarosz.task.architect.domain.ArchitectExceptionCodes;
import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates architect related dtos and Architect domain model.
 */
@Component
@RequiredArgsConstructor
public class ArchitectValidator {
    @NonNull
    private final ArchitectRepository architectRepository;
    @NonNull
    private final ProjectQueryService projectQueryService;

    public static void validateArchitectDto(ArchitectDto architectDto) {
        assertIsTrue(architectDto != null, createMessageCode(ExceptionCodes.NULL, ArchitectExceptionCodes.ARCHITECT));
        validateName(architectDto.getFirstName(), ArchitectExceptionCodes.FIRST_NAME);
        validateName(architectDto.getLastName(), ArchitectExceptionCodes.LAST_NAME);
    }

    private static void validateName(String name, String nameExceptionCode) {
        assertIsTrue(name != null,
                createMessageCode(ExceptionCodes.NULL, ArchitectExceptionCodes.ARCHITECT, nameExceptionCode));
        assertNotEmpty(name,
                createMessageCode(ExceptionCodes.EMPTY, ArchitectExceptionCodes.ARCHITECT, nameExceptionCode));
    }

    public static void validateArchitectExistence(Optional<Architect> maybeArchitect, Long architectId) {
        assertEntityPresent(maybeArchitect.isPresent(),
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXIST, ArchitectExceptionCodes.ARCHITECT),
                architectId);
    }

    public void validateArchitectExistence(Long architectId) {
        var architect = this.architectRepository.findById(architectId);
        validateArchitectExistence(architect, architectId);
    }

    public void validateArchitectHasNoProjects(Long architectId) {
        List<Project> projectList = this.projectQueryService.getProjectsForArchitect(architectId);
        assertIsTrue(projectList.isEmpty(),
                createMessageCode(ExceptionCodes.NOT_VALID, ArchitectExceptionCodes.ARCHITECT,
                        ArchitectExceptionCodes.PROJECTS));
    }

}
