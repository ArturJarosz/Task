package com.arturjarosz.task.supplier.application;

import com.arturjarosz.task.supplier.application.dto.SupplierDto;

import java.util.List;

public interface SupplierApplicationService {

    /**
     * Create new Supplier based on data form SupplierDto. If data provided in SupplierDto are not correct
     * an exception will be thrown.
     */
    SupplierDto createSupplier(SupplierDto supplierDto);

    /**
     * Update Supplier of given supplierId with data from SupplierDto. If Supplier does not exist or
     * data provided in dto are not correct an exception will be thrown.
     */
    void updateSupplier(Long supplierId, SupplierDto supplierDto);

    /**
     * Remove Supplier with given supplierId. If Supplier does not exist, or has Supply, new exception
     * is thrown.
     */
    void deleteSupplier(Long supplierId);

    /**
     * Get Supplier data in SupplierDto of given supplierId. If Supplier does not exist,
     * new exception is thrown.
     */
    SupplierDto getSupplier(Long supplierId);

    /**
     * Get list of SupplierDto with basic Supplier data.
     */
    List<SupplierDto> getBasicSuppliers();
}
