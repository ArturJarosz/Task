package com.arturjarosz.task.user.application;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;

import java.util.List;

public interface UserApplicationService {

    /**
     * Returns the {@link com.arturjarosz.task.user.model.User} matching the email from the current JWT token.
     * If no user is found, an empty {@link UserDto} is returned.
     */
    UserDto getCurrentUser();

    /**
     * Creates a new {@link com.arturjarosz.task.user.model.User} from given {@link UserDto}.
     */
    UserDto createUser(UserDto userDto);

    /**
     * Returns configurations the {@link com.arturjarosz.task.user.model.User} matching the current JWT token has
     * defined. Returns an empty list when there is no such user.
     */
    List<UserConfigurationDto> getCurrentUserConfigurations();

    /**
     * Creates or updates configuration with given name of the {@link com.arturjarosz.task.user.model.User} matching
     * the current JWT token. Throws {@link com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException}
     * when there is no such user.
     */
    UserConfigurationDto updateCurrentUserConfiguration(UserConfigurationNameDto name,
            UserConfigurationDto userConfigurationDto);
}
