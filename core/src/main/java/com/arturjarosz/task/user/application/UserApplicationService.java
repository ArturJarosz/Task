package com.arturjarosz.task.user.application;

import com.arturjarosz.task.dto.UserDto;

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
}
