package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.contractor.application.ContractorApplicationService;
import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.ContractorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(3)
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
        return SampleDataFileReader.readList(FILE_NAME, ContractorDto.class);
    }
}
