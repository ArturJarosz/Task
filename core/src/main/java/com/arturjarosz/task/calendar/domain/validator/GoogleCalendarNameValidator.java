package com.arturjarosz.task.calendar.domain.validator;

import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleCalendarNameValidator extends AbstractGoogleCalendarNameValidator {

    @Autowired
    public GoogleCalendarNameValidator(SystemParameterQueryService systemParameterQueryService) {
        super(systemParameterQueryService);
    }

    @Override
    protected String getNameExceptionCode() {
        return SystemParameterExceptionCodes.GOOGLE_CALENDAR_NAME;
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.GOOGLE_CALENDAR_NAME;
    }
}
