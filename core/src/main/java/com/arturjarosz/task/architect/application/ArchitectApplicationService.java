package com.arturjarosz.task.architect.application;

import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.dto.ArchitectDto;

import java.util.List;

public interface ArchitectApplicationService {

    /**
     * Creates {@link Architect} from given {@link ArchitectDto}.
     * When not all data provided, then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException}
     * is thrown.
     *
     * @param architectDto dto with necessary data to creat new architect.
     * @return CreatedEntityDto with newly created Architect id.
     */
    ArchitectDto createArchitect(ArchitectDto architectDto);

    /**
     * Removes {@link Architect} of given I.
     * If entity with given architectId does not exist then
     * {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is thrown.
     */
    void removeArchitect(Long architectId);

    /**
     * Loads all architect data by given id.
     * If entity with given architectId does not exist then
     * {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is thrown.
     */
    ArchitectDto getArchitect(Long architectId);

    /**
     * Updates Architect of given architectId with data provided in architectDto.
     * If entity with given architectId does not exist then
     * {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is thrown.
     */
    ArchitectDto updateArchitect(Long architectId, ArchitectDto architectDto);

    /**
     * Loads list of basic architect data or all existing architects.
     */
    List<ArchitectDto> getArchitects();
}
