package com.arturjarosz.task.user.application;

import com.arturjarosz.task.dto.UserRolesDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.user.domain.UserExceptionCodes;
import com.arturjarosz.task.user.model.User;

import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates user related dtos and User domain model.
 */
public final class UserValidator {

    private UserValidator() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }

    public static void validateUserExistence(Optional<User> maybeUser, Long userId) {
        assertEntityPresent(maybeUser.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, UserExceptionCodes.USER, UserExceptionCodes.ID), userId);
    }

    public static void validateUserRolesDto(UserRolesDto userRolesDto) {
        assertIsTrue(userRolesDto != null && userRolesDto.getRoleIds() != null
                        && !userRolesDto.getRoleIds().contains(null),
                createMessageCode(ExceptionCodes.NULL, UserExceptionCodes.USER, UserExceptionCodes.ROLES));
    }
}
