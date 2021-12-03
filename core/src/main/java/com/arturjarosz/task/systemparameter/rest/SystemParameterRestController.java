package com.arturjarosz.task.systemparameter.rest;

import com.arturjarosz.task.systemparameter.application.SystemParameterService;
import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("systemParameters")
public class SystemParameterRestController {
    private final SystemParameterService systemParameterService;

    @Autowired
    public SystemParameterRestController(SystemParameterService systemParameterService) {
        this.systemParameterService = systemParameterService;
    }

    @PutMapping("{systemParameterId}")
    public ResponseEntity<SystemParameterDto> updateSystemParameter(
            @PathVariable("systemParameterId") Long systemParameterId,
            @RequestBody SystemParameterDto systemParameterDto) {
        return new ResponseEntity<>(
                this.systemParameterService.updateSystemParameter(systemParameterId, systemParameterDto),
                HttpStatus.OK);
    }

    @GetMapping("{systemParameterId}")
    public ResponseEntity<SystemParameterDto> getSystemParameter(
            @PathVariable("systemParameterId") Long systemParameterId) {
        return new ResponseEntity<>(this.systemParameterService.getSystemParameter(systemParameterId), HttpStatus.OK);
    }

    public ResponseEntity<List<SystemParameterDto>> getSystemParameters() {
        return new ResponseEntity<>(this.systemParameterService.getSystemParameters(), HttpStatus.OK);
    }
}
