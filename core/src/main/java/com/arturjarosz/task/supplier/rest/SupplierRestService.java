package com.arturjarosz.task.supplier.rest;

import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import com.arturjarosz.task.supplier.application.SupplierApplicationService;
import com.arturjarosz.task.supplier.application.dto.SupplierDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("suppliers")
public class SupplierRestService {

    private final SupplierApplicationService supplierApplicationService;

    public SupplierRestService(SupplierApplicationService supplierApplicationService) {
        this.supplierApplicationService = supplierApplicationService;
    }

    @PostMapping("")
    public ResponseEntity<CreatedEntityDto> createSupplier(@RequestBody SupplierDto supplierDto) {
        return new ResponseEntity<>(this.supplierApplicationService.createSupplier(supplierDto), HttpStatus.CREATED);
    }

    @PutMapping("{supplierId}")
    public ResponseEntity<Void> updateSupplier(@PathVariable("supplierId") Long supplierId,
                                               @RequestBody SupplierDto supplierDto) {
        this.supplierApplicationService.updateSupplier(supplierId, supplierDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("{supplierId}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable("supplierId") Long supplierId) {
        this.supplierApplicationService.deleteSupplier(supplierId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{supplierId}")
    public ResponseEntity<SupplierDto> getSupplier(@PathVariable("supplierId") Long supplierId) {
        return new ResponseEntity<>(this.supplierApplicationService.getSupplier(supplierId), HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<SupplierDto>> getBasicSuppliers() {
        return new ResponseEntity<>(this.supplierApplicationService.getBasicSuppliers(), HttpStatus.OK);
    }
}
