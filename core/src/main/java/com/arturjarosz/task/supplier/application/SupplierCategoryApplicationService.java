package com.arturjarosz.task.supplier.application;

import com.arturjarosz.task.dto.SupplierCategoryDto;

import java.util.List;

public interface SupplierCategoryApplicationService {

    /**
     * Create new SupplierCategory with name from SupplierCategoryDto. If the name is missing, too long or already used by
     * another SupplierCategory, an exception is thrown.
     */
    SupplierCategoryDto createSupplierCategory(SupplierCategoryDto supplierCategoryDto);

    /**
     * Rename SupplierCategory of given supplierCategoryId. If SupplierCategory does not exist or the name is not correct, an
     * exception is thrown.
     */
    SupplierCategoryDto updateSupplierCategory(Long supplierCategoryId, SupplierCategoryDto supplierCategoryDto);

    /**
     * Remove SupplierCategory with given supplierCategoryId. If SupplierCategory does not exist or any Supplier has that
     * category, an exception is thrown.
     */
    void deleteSupplierCategory(Long supplierCategoryId);

    /**
     * Get SupplierCategory of given supplierCategoryId with the number of its Suppliers. If SupplierCategory does not exist,
     * an exception is thrown.
     */
    SupplierCategoryDto getSupplierCategory(Long supplierCategoryId);

    /**
     * Get all SupplierCategories sorted by name, each with the number of its Suppliers.
     */
    List<SupplierCategoryDto> getSupplierCategories();
}
