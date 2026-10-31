package com.arturjarosz.task.user.application.mapper;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.user.model.UserConfiguration;
import com.arturjarosz.task.user.model.UserConfigurationName;
import com.arturjarosz.task.user.model.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserDto mapToDto(User user);

    UserConfigurationDto mapToDto(UserConfiguration userConfiguration);

    UserConfigurationName mapConfigurationName(UserConfigurationNameDto userConfigurationNameDto);
}
