package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates brand color of the application theme, which has to be hex color in #rrggbb format.
 */
@Component
public class ThemeColorValidator extends AbstractSystemParameterValidator implements SystemParameterValidator {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#[0-9a-fA-F]{6}");

    private final SystemParameterQueryService systemParameterQueryService;

    @Autowired
    public ThemeColorValidator(SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterQueryService = systemParameterQueryService;
    }

    @Override
    public void validate(String name) {
        super.validate(name);
        var systemProperty = this.systemParameterQueryService.getSystemPropertyByName(name);
        assertNotNull(systemProperty,
                createMessageCode(ExceptionCodes.NOT_EXIST, SystemParameterExceptionCodes.SYSTEM_PARAMETER));
        this.validateValue(systemProperty.getValue());
    }

    @Override
    public void validateOnUpdate(SystemParameterDto systemParameterDto) {
        this.validateValue(systemParameterDto.getValue());
    }

    private void validateValue(String value) {
        assertIsTrue(value != null && HEX_COLOR_PATTERN.matcher(value).matches(),
                createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        SystemParameterExceptionCodes.THEME_COLOR, SystemParameterExceptionCodes.VALUE));
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.THEME_COLOR;
    }
}
