package com.arturjarosz.task.user.application;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;

import java.util.List;

public interface UserApplicationService {

    /**
     * Returns the {@link com.arturjarosz.task.user.model.User} matching the subject of the current JWT token, creating
     * it from the token claims on the first login. An empty {@link UserDto} is returned when the token does not belong
     * to a person.
     */
    UserDto getCurrentUser();

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
}
