package com.arturjarosz.task.configuration.rest;

import com.arturjarosz.task.configuration.application.ConfigurationService;
import com.arturjarosz.task.dto.ApplicationConfigurationDto;
import com.arturjarosz.task.rest.ConfigurationApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ConfigurationRestController implements ConfigurationApi {

    @NonNull
    private final ConfigurationService configurationService;

    @Override
    public ResponseEntity<ApplicationConfigurationDto> getConfiguration() {
        return new ResponseEntity<>(this.configurationService.getApplicationConfiguration(), HttpStatus.OK);
    }
}
