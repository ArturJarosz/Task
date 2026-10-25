package com.arturjarosz.task.calendar.domain.validator;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.properties.GoogleCalendarProperties;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.validator.AbstractSystemParameterValidator;
import com.arturjarosz.task.systemparameter.domain.validator.SystemParameterValidator;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

/**
 * Validates intervals of the Google Calendar synchronization jobs, which are whole numbers of minutes between 1 and
 * 1440 (one day).
 */
public abstract class AbstractGoogleCalendarIntervalValidator extends AbstractSystemParameterValidator implements SystemParameterValidator {
    private static final int MIN_MINUTES = GoogleCalendarProperties.MIN_INTERVAL;
    private static final int MAX_MINUTES = GoogleCalendarProperties.MAX_INTERVAL;
    private static final String WHOLE_NUMBER_PATTERN = "\\d{1,4}";

    private final SystemParameterQueryService systemParameterQueryService;

    protected AbstractGoogleCalendarIntervalValidator(SystemParameterQueryService systemParameterQueryService) {
        this.systemParameterQueryService = systemParameterQueryService;
    }

    /**
     * Returns exception code part identifying validated interval.
     */
    protected abstract String getIntervalExceptionCode();

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
        var messageCode = createMessageCode(ExceptionCodes.NOT_VALID, SystemParameterExceptionCodes.SYSTEM_PARAMETER,
                this.getIntervalExceptionCode(), SystemParameterExceptionCodes.VALUE);
        assertIsTrue(value != null && value.matches(WHOLE_NUMBER_PATTERN), messageCode, MIN_MINUTES, MAX_MINUTES);
        var minutes = Integer.parseInt(value);
        assertIsTrue(minutes >= MIN_MINUTES && minutes <= MAX_MINUTES, messageCode, MIN_MINUTES, MAX_MINUTES);
    }
}
