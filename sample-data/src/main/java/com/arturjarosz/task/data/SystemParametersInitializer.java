package com.arturjarosz.task.data;

import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository;
import com.arturjarosz.task.systemparameter.model.SystemParameter;
import com.arturjarosz.task.systemparameter.model.SystemParameterType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SystemParametersInitializer implements DataInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemParametersInitializer.class);
    private static final String SYSTEM_PARAMETERS_PATH = "initialSystemParameters.json";

    private final SystemParameterRepository systemParameterRepository;

    @Autowired
    public SystemParametersInitializer(SystemParameterRepository systemParameterRepository) {
        this.systemParameterRepository = systemParameterRepository;
    }

    @Override
    public void initializeData() {
        LOGGER.info("Start importing system parameters.");
        this.importSystemParametersFromFile();
        LOGGER.info("System parameters added to the database.");
    }

    private void importSystemParametersFromFile() {
        List<SystemParameter> systemParameters = this.prepareSystemParameters(SYSTEM_PARAMETERS_PATH);
        systemParameters.forEach(this.systemParameterRepository::save);
    }

    private List<SystemParameter> prepareSystemParameters(String filename) {
        var mapper = new ObjectMapper();
        BaseValidator.assertNotEmpty(filename, "File name cannot be empty.");
        try (InputStream inputStream = SystemParametersInitializer.class.getClassLoader()
                .getResourceAsStream(filename)) {
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
        } catch (IOException e) {
            throw new UncheckedIOException(
                    String.format("There was a problem with adding system parameter from %1$s file", filename), e);
        }
    }
}
