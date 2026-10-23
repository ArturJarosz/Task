package com.arturjarosz.task.user.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.user.model.ThemeMode;
import com.arturjarosz.task.user.model.UserConfigurationName;

import java.util.Arrays;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Checks values of user configurations; a new {@link UserConfigurationName} needs its own case here.
 */
public final class UserConfigurationValidator {

    private UserConfigurationValidator() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }

    public static void validateConfiguration(UserConfigurationName name, String value) {
        assertNotNull(name, createMessageCode(ExceptionCodes.NULL, UserExceptionCodes.USER,
                UserExceptionCodes.CONFIGURATION, UserExceptionCodes.NAME));
        assertNotNull(value, createMessageCode(ExceptionCodes.NULL, UserExceptionCodes.USER,
                UserExceptionCodes.CONFIGURATION, UserExceptionCodes.VALUE));
        switch (name) {
            case THEME_MODE -> assertIsTrue(isEnumValue(value, ThemeMode.class),
                    createMessageCode(ExceptionCodes.NOT_VALID, UserExceptionCodes.USER,
                            UserExceptionCodes.CONFIGURATION, UserExceptionCodes.THEME_MODE), value);
        }
    }

    private static <E extends Enum<E>> boolean isEnumValue(String value, Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).anyMatch(constant -> constant.name().equals(value));
    }
}
