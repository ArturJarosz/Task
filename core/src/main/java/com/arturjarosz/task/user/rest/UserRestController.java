package com.arturjarosz.task.user.rest;

import com.arturjarosz.task.dto.UserConfigurationDto;
import com.arturjarosz.task.dto.UserConfigurationNameDto;
import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.rest.UserApi;
import com.arturjarosz.task.user.application.UserApplicationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class UserRestController implements UserApi {
    @NonNull
    private final UserApplicationService userApplicationService;

    @Override
    public ResponseEntity<UserDto> getCurrentUser() {
        return new ResponseEntity<>(this.userApplicationService.getCurrentUser(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<UserConfigurationDto>> getCurrentUserConfigurations() {
        return new ResponseEntity<>(this.userApplicationService.getCurrentUserConfigurations(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserConfigurationDto> updateCurrentUserConfiguration(
            UserConfigurationDto userConfigurationDto, UserConfigurationNameDto name) {
        return new ResponseEntity<>(this.userApplicationService.updateCurrentUserConfiguration(name,
                userConfigurationDto), HttpStatus.OK);
    }
}
