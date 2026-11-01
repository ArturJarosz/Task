package com.arturjarosz.task.supplier.rest;

import com.arturjarosz.task.dto.SupplierCategoryDto;
import com.arturjarosz.task.supplier.application.SupplierCategoryApplicationService;
import com.arturjarosz.task.rest.SupplierCategoryApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class SupplierCategoryRestController implements SupplierCategoryApi {

    @NonNull
    private final SupplierCategoryApplicationService supplierCategoryApplicationService;

    @Override
    public ResponseEntity<SupplierCategoryDto> createSupplierCategory(SupplierCategoryDto supplierCategoryDto) {
        var supplierCategory = this.supplierCategoryApplicationService.createSupplierCategory(supplierCategoryDto);
        var headers = new HttpHeadersBuilder().withLocation("/supplier-categories/{supplierCategoryId}", supplierCategory.getId())
                .build();
        return new ResponseEntity<>(supplierCategory, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<SupplierCategoryDto> updateSupplierCategory(SupplierCategoryDto supplierCategoryDto,
            Long supplierCategoryId) {
        return new ResponseEntity<>(
                this.supplierCategoryApplicationService.updateSupplierCategory(supplierCategoryId, supplierCategoryDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteSupplierCategory(Long supplierCategoryId) {
        this.supplierCategoryApplicationService.deleteSupplierCategory(supplierCategoryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupplierCategoryDto> getSupplierCategory(Long supplierCategoryId) {
        return new ResponseEntity<>(this.supplierCategoryApplicationService.getSupplierCategory(supplierCategoryId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<SupplierCategoryDto>> getSupplierCategories() {
        return new ResponseEntity<>(this.supplierCategoryApplicationService.getSupplierCategories(), HttpStatus.OK);
    }
}
