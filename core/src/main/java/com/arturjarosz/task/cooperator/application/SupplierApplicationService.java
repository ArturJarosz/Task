package com.arturjarosz.task.cooperator.application;

import com.arturjarosz.task.cooperator.application.dto.SupplierDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface SupplierApplicationService {

    /**
     * Create new Supplier based on data form SupplierDto. If data provided in SupplierDto are not correct
     * an exception will be thrown.
     *
     * @param supplierDto
     * @return
     */
    CreatedEntityDto createSupplier(SupplierDto supplierDto);

    /**
     * Update Supplier of given supplierId with data from SupplierDto. If Supplier does not exists or
     * data provided in dto are not correct an exception will be thrown.
     *
     * @param supplierId
     * @param supplierDto
     */
    void updateSupplier(Long supplierId, SupplierDto supplierDto);

    /**
     * Remove Supplier with given supplierId. If Supplier does not exist, or has Supply, new exception
     * is thrown.
     *
     * @param supplierId
     */
    void deleteSupplier(Long supplierId);

    /**
     * Get Supplier data in SupplierDto of given supplierId. If Supplier does not exist,
     * new exception is thrown.
     *
     * @param supplierId
     * @return
     */
    SupplierDto getSupplier(Long supplierId);

    /**
     * Get list of SupplierDto with basic Supplier data.
     *
     * @return
     */
    List<SupplierDto> getBasicSuppliers();
}
