package com.arturjarosz.task.systemparameter.domain.validator;

import com.arturjarosz.task.systemparameter.domain.SystemParameterExceptionCodes;
import com.arturjarosz.task.systemparameter.domain.SystemParameters;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PastEventsShownValidator extends AbstractEventsShownValidator {

    @Autowired
    public PastEventsShownValidator(SystemParameterQueryService systemParameterQueryService) {
        super(systemParameterQueryService);
    }

    @Override
    protected String getEventsShownExceptionCode() {
        return SystemParameterExceptionCodes.PAST_EVENTS_SHOWN;
    }

    @Override
    public String getSystemParameterName() {
        return SystemParameters.PAST_EVENTS_SHOWN;
    }
}
