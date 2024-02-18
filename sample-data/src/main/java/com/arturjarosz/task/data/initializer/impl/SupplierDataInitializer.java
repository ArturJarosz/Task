package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.dto.SupplierDto;
import com.arturjarosz.task.exception.SampleDataInitializingException;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.supplier.application.SupplierApplicationService;
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
public class SupplierDataInitializer implements DataInitializer {
    private static final String FILE_NAME = "sampleData/supplierSample.json";
    private final SupplierApplicationService supplierApplicationService;

    @Autowired
    public SupplierDataInitializer(SupplierApplicationService supplierApplicationService) {
        this.supplierApplicationService = supplierApplicationService;
    }

    @Override
    public void initializeData() {
        LOG.info("Starting importing suppliers.");
        this.importSuppliersFromFile();
        LOG.info("Suppliers added to the database");
    }

    private void importSuppliersFromFile() {
        List<SupplierDto> supplierDtos = this.prepareSuppliers();
        supplierDtos.forEach(this.supplierApplicationService::createSupplier);
    }

    private List<SupplierDto> prepareSuppliers() {
        var mapper = new ObjectMapper();
        BaseValidator.assertNotEmpty(FILE_NAME, "File name cannot be empty");
        try (InputStream inputStream = SupplierDataInitializer.class.getClassLoader().getResourceAsStream(FILE_NAME)) {
            return mapper.readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new SampleDataInitializingException(
                    String.format("There was a problem with adding suppliers from %1$s file", FILE_NAME), e);
        }
    }
}
