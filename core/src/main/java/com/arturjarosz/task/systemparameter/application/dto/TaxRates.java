package com.arturjarosz.task.systemparameter.application.dto;

import java.math.BigDecimal;

/**
 * Tax rates used for financial calculations, expressed as fractions (e.g. 0.23 for 23%).
 */
public record TaxRates(BigDecimal vatTax, BigDecimal incomeTax) {
}
