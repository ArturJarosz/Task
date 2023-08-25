package com.arturjarosz.task.supplier.rest;

import com.arturjarosz.task.dto.SupplierDto;
import com.arturjarosz.task.rest.SupplierApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import com.arturjarosz.task.supplier.application.SupplierApplicationService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class SupplierRestController implements SupplierApi {

    @NonNull
    private final SupplierApplicationService supplierApplicationService;

    @Override
    public ResponseEntity<SupplierDto> createSupplier(SupplierDto supplierDto) {
        var supplier = this.supplierApplicationService.createSupplier(supplierDto);
        var headers = new HttpHeadersBuilder().withLocation("/suppliers/{supplierId}",
                supplier.getId()).build();
        return new ResponseEntity<>(supplier, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SupplierDto> updateSupplier(SupplierDto supplierDto, Long supplierId) {
        var updatedSupplier = this.supplierApplicationService.updateSupplier(supplierId, supplierDto);
        return new ResponseEntity<>(updatedSupplier, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteSupplier(Long supplierId) {
        this.supplierApplicationService.deleteSupplier(supplierId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupplierDto> getSupplier(Long supplierId) {
        return new ResponseEntity<>(this.supplierApplicationService.getSupplier(supplierId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<SupplierDto>> getSuppliers() {
        return new ResponseEntity<>(this.supplierApplicationService.getBasicSuppliers(), HttpStatus.OK);
    }
}
