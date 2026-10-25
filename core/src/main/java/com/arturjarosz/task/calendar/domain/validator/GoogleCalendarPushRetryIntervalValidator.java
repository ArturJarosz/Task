package com.arturjarosz.task.calendar.domain.validator;

import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleCalendarPushRetryIntervalValidator extends AbstractGoogleCalendarIntervalValidator {

    @Autowired
    public GoogleCalendarPushRetryIntervalValidator(SystemParameterQueryService systemParameterQueryService) {
        super(systemParameterQueryService);
    }

    @Override
    protected String getIntervalExceptionCode() {
        return SystemParameterExceptionCodes.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL;
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.GOOGLE_CALENDAR_PUSH_RETRY_INTERVAL;
    }
}
