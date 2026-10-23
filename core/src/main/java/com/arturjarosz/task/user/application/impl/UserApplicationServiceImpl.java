package com.arturjarosz.task.user.application.impl;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.user.application.UserApplicationService;
import com.arturjarosz.task.user.application.mapper.UserMapper;
import com.arturjarosz.task.user.domain.UserConfigurationValidator;
import com.arturjarosz.task.user.domain.UserExceptionCodes;
import com.arturjarosz.task.user.infrastructure.repository.UserRepository;
import com.arturjarosz.task.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class UserApplicationServiceImpl implements UserApplicationService {

    private static final String SUB_CLAIM = "sub";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto getCurrentUser() {
        LOG.info("Getting current user.");

        var maybeUser = this.findCurrentUser();
        if (maybeUser.isPresent()) {
            LOG.info("User found for authId {}.", maybeUser.get().getAuthId());
            return this.userMapper.mapToDto(maybeUser.get());
        }

        LOG.info("No user found for current authentication.");
        return new UserDto();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserConfigurationDto> getCurrentUserConfigurations() {
        LOG.info("Getting configurations of current user.");

        return this.findCurrentUser()
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
        var user = this.findCurrentUser().orElseThrow(() -> new ResourceNotFoundException(
                createMessageCode(ExceptionCodes.NOT_EXIST, UserExceptionCodes.USER)));
        var configuration = user.setConfiguration(configurationName, userConfigurationDto.getValue());

        LOG.info("Configuration {} of current user updated.", name);
        return this.userMapper.mapToDto(configuration);
    }

    private Optional<User> findCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String authId = jwtAuth.getToken().getClaimAsString(SUB_CLAIM);
            if (authId != null) {
                return this.userRepository.findByAuthId(authId);
            }
        }
        return Optional.empty();
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        LOG.info("Creating user.");

        var user = this.userMapper.mapFromDto(userDto);

        // Set authId from JWT token's 'sub' claim
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String authId = jwtAuth.getToken().getClaimAsString(SUB_CLAIM);
            if (authId != null) {
                user.setAuthId(authId);
                LOG.info("Setting authId {} for new user.", authId);
            }
        }

        user = this.userRepository.save(user);

        LOG.info("User created.");
        return this.userMapper.mapToDto(user);
    }
}
