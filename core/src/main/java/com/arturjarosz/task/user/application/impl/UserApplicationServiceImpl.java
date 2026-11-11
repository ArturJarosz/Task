package com.arturjarosz.task.user.application.impl;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.dto.UserRolesDto;
import com.arturjarosz.task.dto.UserSummaryDto;
import com.arturjarosz.task.role.application.RoleValidator;
import com.arturjarosz.task.role.application.mapper.PermissionMapper;
import com.arturjarosz.task.security.permission.PermissionProvider;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.user.application.CurrentUserProvider;
import com.arturjarosz.task.user.application.UserApplicationService;
import com.arturjarosz.task.user.application.mapper.UserMapper;
import com.arturjarosz.task.user.domain.UserConfigurationValidator;
import com.arturjarosz.task.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.arturjarosz.task.user.application.UserValidator.validateUserExistence;
import static com.arturjarosz.task.user.application.UserValidator.validateUserRolesDto;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class UserApplicationServiceImpl implements UserApplicationService {

    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PermissionProvider permissionProvider;
    private final RoleValidator roleValidator;
    private final PermissionMapper permissionMapper;

    @Transactional
    @Override
    public UserDto getCurrentUser() {
        LOG.info("Getting current user.");

        var maybeUser = this.currentUserProvider.findOrCreateCurrentUser();
        if (maybeUser.isPresent()) {
            LOG.info("Returning user for authId {}.", maybeUser.get().getAuthId());
            var userDto = this.userMapper.mapToDto(maybeUser.get());
            var permissions = this.currentUserProvider.findCurrentPrincipal()
                    .map(this.permissionProvider::getPermissions)
                    .orElse(Set.of());
            userDto.setPermissions(this.permissionMapper.mapToDtos(permissions));
            return userDto;
        }

        LOG.info("No user for current authentication.");
        return new UserDto();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserSummaryDto> getUsers() {
        LOG.info("Getting all users.");

        return this.userRepository.findAll(Sort.by("id")).stream()
                .map(this.userMapper::mapToSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Long, UserSummaryDto> getUserSummaries(Collection<Long> userIds) {
        LOG.info("Getting {} users.", userIds.size());

        return this.userRepository.findAllById(userIds).stream()
                .map(this.userMapper::mapToSummaryDto)
                .collect(Collectors.toMap(UserSummaryDto::getId, Function.identity()));
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
        var user = this.currentUserProvider.getOrCreateCurrentUser();
        var configuration = user.setConfiguration(configurationName, userConfigurationDto.getValue());

        LOG.info("Configuration {} of current user updated.", name);
        return this.userMapper.mapToDto(configuration);
    }

    @Transactional
    @Override
    public UserSummaryDto updateUserRoles(Long userId, UserRolesDto userRolesDto) {
        LOG.info("Updating roles of user with id {}.", userId);

        validateUserRolesDto(userRolesDto);
        var maybeUser = this.userRepository.findById(userId);
        validateUserExistence(maybeUser, userId);
        // locks the roles, so none of them can be removed before the user is saved with it
        userRolesDto.getRoleIds().forEach(this.roleValidator::validateExistence);
        var user = maybeUser.orElseThrow(ResourceNotFoundException::new);
        user.assignRoles(userRolesDto.getRoleIds());
        user = this.userRepository.saveAndFlush(user);

        LOG.info("Roles of user with id {} updated.", userId);
        return this.userMapper.mapToSummaryDto(user);
    }
}
