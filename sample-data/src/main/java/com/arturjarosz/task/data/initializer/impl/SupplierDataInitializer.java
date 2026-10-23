package com.arturjarosz.task.data.initializer.impl;

import com.arturjarosz.task.data.initializer.DataInitializer;
import com.arturjarosz.task.data.initializer.SampleDataFileReader;
import com.arturjarosz.task.dto.SupplierDto;
import com.arturjarosz.task.supplier.application.SupplierApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(4)
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
        return SampleDataFileReader.readList(FILE_NAME, SupplierDto.class);
    }
}
