package com.arturjarosz.task.user.application.impl;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.user.application.CurrentUserProvider;
import com.arturjarosz.task.user.application.UserApplicationService;
import com.arturjarosz.task.user.application.mapper.UserMapper;
import com.arturjarosz.task.user.domain.UserConfigurationValidator;
import com.arturjarosz.task.user.domain.UserExceptionCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class UserApplicationServiceImpl implements UserApplicationService {

    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserDto getCurrentUser() {
        LOG.info("Getting current user.");

        var maybeUser = this.currentUserProvider.findOrCreateCurrentUser();
        if (maybeUser.isPresent()) {
            LOG.info("Returning user for authId {}.", maybeUser.get().getAuthId());
            return this.userMapper.mapToDto(maybeUser.get());
        }

        LOG.info("No user for current authentication.");
        return new UserDto();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserConfigurationDto> getCurrentUserConfigurations() {
        LOG.info("Getting configurations of current user.");

        return this.currentUserProvider.findCurrentUser()
                .map(user -> user.getConfigurations().stream()
                        .sorted(Comparator.comparing(configuration -> configuration.getName().name()))
                        .map(this.userMapper::mapToDto)
                        .toList())
                .orElse(List.of());
    }

    @Transactional
    @Override
    public UserConfigurationDto updateCurrentUserConfiguration(UserConfigurationNameDto name,
            UserConfigurationDto userConfigurationDto) {
        LOG.info("Updating configuration {} of current user.", name);

        var configurationName = this.userMapper.mapConfigurationName(name);
        UserConfigurationValidator.validateConfiguration(configurationName, userConfigurationDto.getValue());
        var user = this.currentUserProvider.findOrCreateCurrentUser().orElseThrow(() -> new ResourceNotFoundException(
                createMessageCode(ExceptionCodes.NOT_EXIST, UserExceptionCodes.USER)));
        var configuration = user.setConfiguration(configurationName, userConfigurationDto.getValue());

        LOG.info("Configuration {} of current user updated.", name);
        return this.userMapper.mapToDto(configuration);
    }
}
