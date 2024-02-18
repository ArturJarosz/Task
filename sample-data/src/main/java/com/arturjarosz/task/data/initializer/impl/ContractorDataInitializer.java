package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.dto.ContractorDto;
import com.arturjarosz.task.exception.SampleDataInitializingException;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
public class ContractorDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/contractorsSample.json";
    private final ContractorApplicationService contractorApplicationService;

    @Autowired
    public ContractorDataInitializer(ContractorApplicationService contractorApplicationService) {
        this.contractorApplicationService = contractorApplicationService;
    }

    @Override
    public void initializeData() {
        LOG.info("Starting importing contractors.");
        this.importContractorsFromFile();
        LOG.info("Contractors added to the database.");
    }

    private void importContractorsFromFile() {
        List<ContractorDto> contractorDtos = this.prepareContractors();
        contractorDtos.forEach(this.contractorApplicationService::createContractor);
    }

    private List<ContractorDto> prepareContractors() {
        var mapper = new ObjectMapper();
        BaseValidator.assertNotEmpty(FILE_NAME, "File name cannot be empty.");
        try (InputStream inputStream = ContractorDataInitializer.class.getClassLoader().getResourceAsStream(
                FILE_NAME)) {
            return mapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new SampleDataInitializingException(
                    String.format("There was a problem with adding contractors from %1$s file", FILE_NAME), e);
        }
    }
}
