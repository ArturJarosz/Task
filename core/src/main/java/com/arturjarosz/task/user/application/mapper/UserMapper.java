package com.arturjarosz.task.user.application.mapper;

import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.user.model.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserDto mapToDto(User user);

    User mapFromDto(UserDto userDto);
}
