package com.arturjarosz.task.systemparameter.rest;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.rest.SystemParameterApi;
import com.arturjarosz.task.systemparameter.application.SystemParameterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class SystemParameterRestController implements SystemParameterApi {

    @NonNull
    private final SystemParameterService systemParameterService;

    @Override
    public ResponseEntity<SystemParameterDto> updateSystemParameter(SystemParameterDto systemParameterDto,
            Long systemParameterId) {
        return new ResponseEntity<>(
                this.systemParameterService.updateSystemParameter(systemParameterId, systemParameterDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SystemParameterDto> getSystemParameter(Long systemParameterId) {
        return new ResponseEntity<>(this.systemParameterService.getSystemParameter(systemParameterId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<SystemParameterDto>> getSystemParameters() {
        return new ResponseEntity<>(this.systemParameterService.getSystemParameters(), HttpStatus.OK);
    }
}
