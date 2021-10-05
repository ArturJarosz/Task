package com.arturjarosz.task.supervision.domain;

import com.arturjarosz.task.supervision.model.Supervision;

public interface SupervisionCalculationService {

    /**
     * Recalculated FinancialData related to Supervision.
     * @param supervision
     */
    void recalculateSupervision(Supervision supervision);
}
