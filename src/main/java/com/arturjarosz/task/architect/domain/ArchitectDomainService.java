package com.arturjarosz.task.architect.domain;

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface ArchitectDomainService {

    CreatedEntityDto createArchitect(ArchitectBasicDto architectBasicDto);

    void removeArchitect(Long architectId);

    ArchitectDto getArchitect(Long architectId);

    void updateArchitect(Long architectId, ArchitectDto architectDto);

    List<ArchitectBasicDto> getArchitects();
}
