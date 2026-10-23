package com.arturjarosz.task.calendar.domain.validator;

import com.arturjarosz.task.calendar.domain.GoogleCalendarService;
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
 * Validates the Google Calendar credentials, which have to be empty or a Google service account JSON key. The key
 * cannot be removed while the sync is turned on.
 */
@Component
public class GoogleCalendarCredentialsValidator extends AbstractSystemParameterValidator implements SystemParameterValidator {
    private final SystemParameterQueryService systemParameterQueryService;
    private final GoogleCalendarService googleCalendarService;

    @Autowired
    public GoogleCalendarCredentialsValidator(SystemParameterQueryService systemParameterQueryService,
            GoogleCalendarService googleCalendarService) {
        this.systemParameterQueryService = systemParameterQueryService;
        this.googleCalendarService = googleCalendarService;
    }

    @Override
    public void validate(String name) {
        super.validate(name);
        this.validateValue(this.systemParameterQueryService.getValueByName(name));
    }

    @Override
    public void validateOnUpdate(SystemParameterDto systemParameterDto) {
        var value = systemParameterDto.getValue();
        if (value == null || value.isBlank()) {
            assertIsTrue(!Boolean.parseBoolean(
                            this.systemParameterQueryService.getValueByName(SystemParameters.GOOGLE_CALENDAR_SYNC)),
                    createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                            SystemParameterExceptionCodes.GOOGLE_CALENDAR_CREDENTIALS,
                            SystemParameterExceptionCodes.GOOGLE_CALENDAR_SYNC));
        }
        this.validateValue(value);
    }

    private void validateValue(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        assertIsTrue(this.googleCalendarService.readServiceAccountEmail(value).isPresent(),
                createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                        SystemParameterExceptionCodes.GOOGLE_CALENDAR_CREDENTIALS, SystemParameterExceptionCodes.VALUE));
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.GOOGLE_CALENDAR_CREDENTIALS;
    }
}
