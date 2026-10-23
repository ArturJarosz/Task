package com.arturjarosz.task.systemparameter.application;

import com.arturjarosz.task.systemparameter.application.dto.TaxRates;

public interface TaxRatesProvider {

    /**
     * Loads current tax rates stored as system parameters.
     */
    TaxRates getTaxRates();
}
