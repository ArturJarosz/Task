package com.arturjarosz.task.architect.application.impl;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.architect.application.ArchitectValidator;
import com.arturjarosz.task.architect.application.mapper.ArchitectDtoMapper;
import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.arturjarosz.task.architect.application.ArchitectValidator.validateArchitectDto;
import static com.arturjarosz.task.architect.application.ArchitectValidator.validateArchitectExistence;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class ArchitectApplicationServiceImpl implements ArchitectApplicationService {
    @NonNull
    private final ArchitectRepository architectRepository;
    @NonNull
    private final ArchitectValidator architectValidator;

    @Transactional
    @Override
    public ArchitectDto createArchitect(ArchitectDto architectDto) {
        LOG.debug("creating architect");
        validateArchitectDto(architectDto);
        var architect = ArchitectDtoMapper.INSTANCE.architectDtoToArchitect(architectDto);
        architect = this.architectRepository.save(architect);
        LOG.debug("architect created");
        return ArchitectDtoMapper.INSTANCE.architectToArchitectDto(architect);
    }

    @Transactional
    @Override
    public void removeArchitect(Long architectId) {
        LOG.debug("removing architect");

        this.architectValidator.validateArchitectExistence(architectId);
        this.architectValidator.validateArchitectHasNoProjects(architectId);
        this.architectRepository.deleteById(architectId);

        LOG.debug("architect with id {} removed", architectId);
    }

    @Override
    public ArchitectDto getArchitect(Long architectId) {
        var maybeArchitect = this.architectRepository.findById(architectId);
        validateArchitectExistence(maybeArchitect, architectId);

        LOG.debug("architect with id {} loaded", architectId);
        return ArchitectDtoMapper.INSTANCE.architectToArchitectDto(maybeArchitect.orElseThrow(
                ResourceNotFoundException::new));
    }

    @Transactional
    @Override
    public ArchitectDto updateArchitect(Long architectId, ArchitectDto architectDto) {
        LOG.debug("updating architect with id {}", architectId);

        var maybeArchitect = this.architectRepository.findById(architectId);
        validateArchitectExistence(maybeArchitect, architectId);
        validateArchitectDto(architectDto);
        var architect = maybeArchitect.orElseThrow(
                ResourceNotFoundException::new);
        architect.updateArchitectName(architectDto.getFirstName(), architectDto.getLastName());
        architect = this.architectRepository.save(architect);

        LOG.debug("architect with id {} updated", architectId);

        return ArchitectDtoMapper.INSTANCE.architectToArchitectDto(architect);
    }

    @Override
    public List<ArchitectDto> getArchitects() {
        return this.architectRepository.findAll()
                .stream()
                .map(ArchitectDtoMapper.INSTANCE::architectToArchitectDto)
                .toList();
    }
}
