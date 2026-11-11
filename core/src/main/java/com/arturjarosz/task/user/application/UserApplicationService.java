package com.arturjarosz.task.user.application;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.dto.UserRolesDto;
import com.arturjarosz.task.dto.UserSummaryDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserApplicationService {

    /**
     * Returns the {@link com.arturjarosz.task.user.model.User} matching the subject of the current JWT token, creating
     * it from the token claims on the first login, with the permissions the user has. An empty {@link UserDto} is
     * returned when the token does not belong to a person.
     */
    UserDto getCurrentUser();

    /**
     * Returns everyone who has logged in at least once, sorted by id.
     */
    List<UserSummaryDto> getUsers();

    /**
     * Returns users with given ids by user id. Ids of not existing users are skipped.
     */
    Map<Long, UserSummaryDto> getUserSummaries(Collection<Long> userIds);

    /**
     * Returns configurations the {@link com.arturjarosz.task.user.model.User} matching the current JWT token has
     * defined. Returns an empty list when there is no such user.
     */
    List<UserConfigurationDto> getCurrentUserConfigurations();

    /**
     * Creates or updates configuration with given name of the {@link com.arturjarosz.task.user.model.User} matching
     * the current JWT token, creating the user when it does not exist yet. Throws
     * {@link com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException} when the token does not belong to a
     * person.
     */
    UserConfigurationDto updateCurrentUserConfiguration(UserConfigurationNameDto name,
            UserConfigurationDto userConfigurationDto);

    /**
     * Replaces the roles of the user with given id with the roles listed in userRolesDto.
     */
    UserSummaryDto updateUserRoles(Long userId, UserRolesDto userRolesDto);
}
