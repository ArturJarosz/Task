package com.arturjarosz.task.user.application.impl;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.dto.UserSummaryDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class UserApplicationServiceImpl implements UserApplicationService {

    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

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
}
