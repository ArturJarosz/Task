package com.arturjarosz.task.architect.application;

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.architect.domain.ArchitectDomainService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import javax.transaction.Transactional;
import java.util.List;

@ApplicationService
public class ArchitectApplicationServiceImpl implements ArchitectApplicationService {

    private ArchitectDomainService architectDomainService;

    public ArchitectApplicationServiceImpl(ArchitectDomainService architectDomainService) {
        this.architectDomainService = architectDomainService;
    }

    @Transactional
    @Override
    public CreatedEntityDto createClient(ArchitectBasicDto architectBasicDto) {
        return this.architectDomainService.createArchitect(architectBasicDto);
    }

    @Transactional
    @Override
    public void deleteArchitect(Long architectId) {
        this.architectDomainService.removeArchitect(architectId);
    }

    @Override
    public ArchitectDto getArchitect(Long architectId) {
        return this.architectDomainService.getArchitect(architectId);
    }

    @Transactional
    @Override
    public void updateArchitect(Long architectId, ArchitectDto architectDto) {
        this.architectDomainService.updateArchitect(architectId, architectDto);
    }

    @Override
    public List<ArchitectBasicDto> getBasicClients() {
        return this.architectDomainService.getClients();
    }
}
