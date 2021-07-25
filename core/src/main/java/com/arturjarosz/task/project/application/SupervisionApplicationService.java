package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.SupervisionDto;

public interface SupervisionApplicationService {

    SupervisionDto createSupervision(Long projectId, SupervisionDto supervisionDto);

    SupervisionDto updateSupervision();

    void deleteSupervision();
}
