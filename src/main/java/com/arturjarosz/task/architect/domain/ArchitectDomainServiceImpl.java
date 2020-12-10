package com.arturjarosz.task.architect.domain;

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.architect.application.mapper.ArchitectDtoMapper;
import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;
import java.util.stream.Collectors;

import static com.arturjarosz.task.architect.domain.ArchitectValidator.validateArchitectDto;
import static com.arturjarosz.task.architect.domain.ArchitectValidator.validateBasicArchitectDto;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;

@DomainService
public class ArchitectDomainServiceImpl implements ArchitectDomainService {

    private ArchitectRepository architectRepository;

    public ArchitectDomainServiceImpl(ArchitectRepository architectRepository) {
        this.architectRepository = architectRepository;
    }

    @Override
    public CreatedEntityDto createArchitect(
            ArchitectBasicDto architectBasicDto) {
        validateBasicArchitectDto(architectBasicDto);
        Architect architect = ArchitectDtoMapper.INSTANCE.architectBasicDtoToArchitect(architectBasicDto);
        this.architectRepository.save(architect);
        return new CreatedEntityDto(architect.getId());
    }

    @Override
    public void removeArchitect(Long architectId) {
        this.architectRepository.remove(architectId);
    }

    @Override
    public ArchitectDto getArchitect(Long architectId) {
        Architect architect = this.architectRepository.load(architectId);
        assertIsTrue(architect != null,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXISTS, ArchitectExceptionCodes.ARCHITECT),
                architectId);
        return ArchitectDtoMapper.INSTANCE.architectToArchitectDto(architect);
    }

    @Override
    public void updateArchitect(Long architectId, ArchitectDto architectDto) {
        Architect architect = this.architectRepository.load(architectId);
        assertIsTrue(architect != null,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXISTS, ArchitectExceptionCodes.ARCHITECT),
                architectId);
        validateArchitectDto(architectDto);
        architect.updateArchitectName(architectDto.getFirstName(), architectDto.getLastName());
        this.architectRepository.save(architect);
    }

    @Override
    public List<ArchitectBasicDto> getClients() {
        return this.architectRepository.loadAll().stream()
                .map(ArchitectDtoMapper.INSTANCE::architectToArchitectBasicDto)
                .collect(Collectors.toList());
    }
}
