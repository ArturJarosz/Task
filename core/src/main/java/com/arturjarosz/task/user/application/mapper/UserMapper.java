package com.arturjarosz.task.user.application.mapper;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.dto.UserSummaryDto;
import com.arturjarosz.task.user.model.UserConfiguration;
import com.arturjarosz.task.user.model.UserConfigurationName;
import com.arturjarosz.task.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    // permissions do not come from the user, they are resolved by the PermissionProvider
    @Mapping(target = "permissions", ignore = true)
    UserDto mapToDto(User user);

    UserSummaryDto mapToSummaryDto(User user);

    UserConfigurationDto mapToDto(UserConfiguration userConfiguration);

    UserConfigurationName mapConfigurationName(UserConfigurationNameDto userConfigurationNameDto);
}
