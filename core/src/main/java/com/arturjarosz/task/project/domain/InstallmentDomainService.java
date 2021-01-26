package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.model.Stage;

import java.time.LocalDate;

public interface InstallmentDomainService {
    public void updateInstallment(Stage stage, Double value, LocalDate payDate, String description);

    void payForInstallment(Stage stage, LocalDate payDate);
}
