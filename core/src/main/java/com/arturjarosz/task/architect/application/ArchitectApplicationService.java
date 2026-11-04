package com.arturjarosz.task.architect.application;

import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.dto.ArchitectUserLinkDto;
import com.arturjarosz.task.dto.EntityProjectsSummaryDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * Loads all architect data by given id, without the linked user, so it can be embedded in other responses.
     * If entity with given architectId does not exist then
     * {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is thrown.
     */
    ArchitectDto getArchitect(Long architectId);

    /**
     * Same as {@link #getArchitect(Long)}, with the user linked with the architect.
     */
    ArchitectDto getArchitectWithLinkedUser(Long architectId);

    /**
     * Loads basic data (without projects counts and value) of {@link Architect}s with given architectIds in one
     * query, by architect id. Ids of not existing architects are skipped.
     */
    Map<Long, ArchitectDto> getArchitectsBasicData(Collection<Long> architectIds);

    /**
     * Updates Architect of given architectId with data provided in architectDto.
     * If entity with given architectId does not exist then
     * {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is thrown.
     */
    ArchitectDto updateArchitect(Long architectId, ArchitectDto architectDto);

    /**
     * Loads list of basic architect data or all existing architects, sorted by id.
     */
    List<ArchitectDto> getArchitects();

    EntityProjectsSummaryDto getProjectsSummary(Long architectId);

    /**
     * Links {@link Architect} of given architectId with the user from given {@link ArchitectUserLinkDto}, replacing
     * the user linked so far. If the architect or the user does not exist then
     * {@link com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException} is thrown. If the user is linked
     * with another architect then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is
     * thrown.
     */
    ArchitectDto linkUser(Long architectId, ArchitectUserLinkDto architectUserLinkDto);

    /**
     * Removes the link between {@link Architect} of given architectId and its user. The architect and everything
     * assigned to it stay. If the architect does not exist then
     * {@link com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException} is thrown.
     */
    ArchitectDto unlinkUser(Long architectId);
}
