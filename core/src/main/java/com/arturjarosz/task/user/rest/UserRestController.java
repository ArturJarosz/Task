package com.arturjarosz.task.user.rest;

import com.arturjarosz.task.dto.UserDto;
import com.arturjarosz.task.rest.UserApi;
import com.arturjarosz.task.user.application.UserApplicationService;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserRestController implements UserApi {
    private static final String USERS_API = "/users";

    @NonNull
    private final UserApplicationService userApplicationService;

    @Override
    public ResponseEntity<UserDto> createUser(UserDto userDto) {
        var createdUserDto = this.userApplicationService.createUser(userDto);
        var headers = new HttpHeadersBuilder()
                .withLocation("%s/{id}".formatted(USERS_API), createdUserDto.getId())
                .build();
        return new ResponseEntity<>(createdUserDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<UserDto> getCurrentUser() {
        return new ResponseEntity<>(this.userApplicationService.getCurrentUser(), HttpStatus.OK);
    }
}
