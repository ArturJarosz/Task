package com.arturjarosz.task.architect.application.impl;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.architect.application.ArchitectValidator;
import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.architect.application.mapper.ArchitectDtoMapper;
import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.arturjarosz.task.architect.application.ArchitectValidator.validateArchitectDto;
import static com.arturjarosz.task.architect.application.ArchitectValidator.validateArchitectExistence;
import static com.arturjarosz.task.architect.application.ArchitectValidator.validateBasicArchitectDto;

@ApplicationService
public class ArchitectApplicationServiceImpl implements ArchitectApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(ArchitectApplicationServiceImpl.class);

    private final ArchitectRepository architectRepository;
    private final ArchitectValidator architectValidator;

    public ArchitectApplicationServiceImpl(ArchitectRepository architectRepository,
                                           ArchitectValidator architectValidator) {
        this.architectRepository = architectRepository;
        this.architectValidator = architectValidator;
    }

    @Transactional
    @Override
    public ArchitectDto createArchitect(ArchitectBasicDto architectBasicDto) {
        LOG.debug("creating architect");

        validateBasicArchitectDto(architectBasicDto);
        Architect architect = ArchitectDtoMapper.INSTANCE.architectBasicDtoToArchitect(architectBasicDto);
        architect = this.architectRepository.save(architect);
        LOG.debug("architect created");
        return ArchitectDtoMapper.INSTANCE.architectToArchitectDto(architect);
    }

    @Transactional
    @Override
    public void removeArchitect(Long architectId) {
        LOG.debug("removing architect");

        Architect architect = this.architectRepository.load(architectId);
        validateArchitectExistence(architect, architectId);
        this.architectValidator.validateArchitectHasNoProjects(architectId);
        this.architectRepository.remove(architectId);

        LOG.debug("architect with id {} removed", architectId);
    }

    @Override
    public ArchitectDto getArchitect(Long architectId) {
        Architect architect = this.architectRepository.load(architectId);
        validateArchitectExistence(architect, architectId);

        LOG.debug("architect with id {} loaded", architectId);
        return ArchitectDtoMapper.INSTANCE.architectToArchitectDto(architect);
    }

    @Transactional
    @Override
    public ArchitectDto updateArchitect(Long architectId, ArchitectDto architectDto) {
        LOG.debug("updating architect with id {}", architectId);

        Architect architect = this.architectRepository.load(architectId);
        validateArchitectExistence(architect, architectId);
        validateArchitectDto(architectDto);
        architect.updateArchitectName(architectDto.getFirstName(), architectDto.getLastName());

        architect = this.architectRepository.save(architect);
        LOG.debug("architect with id {} updated", architectId);
        return ArchitectDtoMapper.INSTANCE.architectToArchitectDto(architect);
    }

    @Override
    public List<ArchitectBasicDto> getBasicArchitects() {
        return this.architectRepository.loadAll().stream()
                .map(ArchitectDtoMapper.INSTANCE::architectToArchitectBasicDto).toList();
    }
}
