package com.arturjarosz.task.user.application.impl;

import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.user.application.UserApplicationService;
import com.arturjarosz.task.user.application.mapper.UserMapper;
import com.arturjarosz.task.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class UserApplicationServiceImpl implements UserApplicationService {

    private static final String SUB_CLAIM = "sub";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto getCurrentUser() {
        LOG.debug("Getting current user.");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String authId = jwtAuth.getToken().getClaimAsString(SUB_CLAIM);
            if (authId != null) {
                var maybeUser = this.userRepository.findByAuthId(authId);
                if (maybeUser.isPresent()) {
                    LOG.debug("User found for authId {}.", authId);
                    return this.userMapper.mapToDto(maybeUser.get());
                }
            }
        }

        LOG.debug("No user found for current authentication.");
        return new UserDto();
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        LOG.debug("Creating user.");

        var user = this.userMapper.mapFromDto(userDto);

        // Set authId from JWT token's 'sub' claim
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String authId = jwtAuth.getToken().getClaimAsString(SUB_CLAIM);
            if (authId != null) {
                user.setAuthId(authId);
                LOG.debug("Setting authId {} for new user.", authId);
            }
        }

        user = this.userRepository.save(user);

        LOG.debug("User created.");
        return this.userMapper.mapToDto(user);
    }
}
