package com.arturjarosz.task.calendar.domain.validator;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.domain.validator.AbstractSystemParameterValidator;
import com.arturjarosz.task.systemparameter.domain.validator.SystemParameterValidator;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates the switch of the Google Calendar sync, which has to be true or false. The sync can be turned on only
 * when the service account key is stored.
 */
@Component
public class GoogleCalendarSyncValidator extends AbstractSystemParameterValidator implements SystemParameterValidator {
    private final SystemParameterQueryService systemParameterQueryService;

    @Autowired
    public GoogleCalendarSyncValidator(SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterQueryService = systemParameterQueryService;
    }

    @Override
    public void validate(String name) {
        super.validate(name);
        this.validateValue(this.systemParameterQueryService.getValueByName(name));
    }

    @Override
    public void validateOnUpdate(SystemParameterDto systemParameterDto) {
        this.validateValue(systemParameterDto.getValue());
    }

    private void validateValue(String value) {
        assertIsTrue("true".equals(value) || "false".equals(value),
                createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        SystemParameterExceptionCodes.GOOGLE_CALENDAR_SYNC, SystemParameterExceptionCodes.VALUE));
        if (Boolean.parseBoolean(value)) {
            var credentials = this.systemParameterQueryService.getValueByName(
                    SystemParameters.GOOGLE_CALENDAR_CREDENTIALS);
            assertIsTrue(credentials != null && !credentials.isBlank(),
                    createMessageCode(ExceptionCodes.EMPTY, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                            SystemParameterExceptionCodes.GOOGLE_CALENDAR_CREDENTIALS));
        }
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.GOOGLE_CALENDAR_SYNC;
    }
}
