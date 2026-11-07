package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpcomingEventsShownValidator extends AbstractEventsShownValidator {

    @Autowired
    public UpcomingEventsShownValidator(SystemParameterQueryService systemParameterQueryService) {
        super(systemParameterQueryService);
    }

    @Override
    protected String getEventsShownExceptionCode() {
        return SystemParameterExceptionCodes.UPCOMING_EVENTS_SHOWN;
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.UPCOMING_EVENTS_SHOWN;
    }
}
