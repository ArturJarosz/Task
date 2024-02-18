package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.exception.SampleDataInitializingException;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository;
import com.arturjarosz.task.systemparameter.model.SystemParameter;
import com.arturjarosz.task.systemparameter.model.SystemParameterType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SystemParametersDataInitializer implements DataInitializer {
    private static final String SYSTEM_PARAMETERS_PATH = "initialSystemParameters.json";

    private final SystemParameterRepository systemParameterRepository;

    @Autowired
    public SystemParametersDataInitializer(SystemParameterRepository systemParameterRepository) {
        this.systemParameterRepository = systemParameterRepository;
    }

    @Override
    public void initializeData() {
        LOG.info("Start importing system parameters.");
        this.importSystemParametersFromFile();
        LOG.info("System parameters added to the database.");
    }

    private void importSystemParametersFromFile() {
        List<SystemParameter> systemParameters = this.prepareSystemParameters();
        this.systemParameterRepository.saveAll(systemParameters);
    }

    private List<SystemParameter> prepareSystemParameters() {
        var mapper = new ObjectMapper();
        BaseValidator.assertNotEmpty(SystemParametersDataInitializer.SYSTEM_PARAMETERS_PATH,
                "File name cannot be empty.");
        try (InputStream inputStream = SystemParametersDataInitializer.class.getClassLoader()
                .getResourceAsStream(SystemParametersDataInitializer.SYSTEM_PARAMETERS_PATH)) {
            JsonNode jsonNode = mapper.readTree(inputStream);
            ArrayNode systemParametersNodes = (ArrayNode) jsonNode;
            List<SystemParameter> systemParameters = new ArrayList<>();
            systemParametersNodes.forEach(systemParameterNode -> {
                SystemParameter systemParameter = new SystemParameter(systemParameterNode.get("name").asText(),
                        systemParameterNode.get("value").asText(), systemParameterNode.get("defaultValue").asText(),
                        SystemParameterType.valueOf(systemParameterNode.get("type").asText()),
                        systemParameterNode.get("singleValue").booleanValue());
                systemParameters.add(systemParameter);
            });
            return systemParameters;
        } catch (Exception e) {
            throw new SampleDataInitializingException(
                    String.format("There was a problem with adding system parameter from %1$s file",
                            SystemParametersDataInitializer.SYSTEM_PARAMETERS_PATH), e);
        }
    }
}
