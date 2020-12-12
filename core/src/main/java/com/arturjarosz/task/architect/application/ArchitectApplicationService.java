package com.arturjarosz.task.architect.application;

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface ArchitectApplicationService {

    CreatedEntityDto createClient(ArchitectBasicDto architectBasicDto);

    void deleteArchitect(Long architectId);

    ArchitectDto getArchitect(Long architectId);

    void updateArchitect(Long architectId, ArchitectDto architectDto);

    List<ArchitectBasicDto> getBasicClients();
}
