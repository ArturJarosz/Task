package com.arturjarosz.task.supplier.query;

import com.arturjarosz.task.finance.model.SupplierSupplyDataDto;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface SupplierQueryService {

    /**
     * Checks whether supplier with given supplierId exists.
     */
    boolean supplierWithIdExists(long supplierId);

    Set<SupplierSupplyDataDto> getSupplierSuppliesData(long supplierId);

    Map<Long, Long> getNumberOfSupplierPerSupplier();

    /**
     * Number of suppliers of each of given supplier categories, 0 for a category with no suppliers.
     */
    Map<Long, Long> getNumberOfSuppliersByCategoryIds(Collection<Long> categoryIds);
}
