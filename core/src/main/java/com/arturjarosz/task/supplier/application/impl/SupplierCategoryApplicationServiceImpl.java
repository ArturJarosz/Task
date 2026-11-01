package com.arturjarosz.task.supplier.application.impl;

import com.arturjarosz.task.dto.SupplierCategoryDto;
import com.arturjarosz.task.supplier.application.SupplierCategoryApplicationService;
import com.arturjarosz.task.supplier.application.SupplierCategoryValidator;
import com.arturjarosz.task.supplier.application.mapper.SupplierCategoryMapper;
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository;
import com.arturjarosz.task.supplier.model.SupplierCategory;
import com.arturjarosz.task.supplier.query.SupplierQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class SupplierCategoryApplicationServiceImpl implements SupplierCategoryApplicationService {

    private final SupplierCategoryRepository supplierCategoryRepository;
    private final SupplierQueryService supplierQueryService;
    private final SupplierCategoryValidator supplierCategoryValidator;
    private final SupplierCategoryMapper supplierCategoryMapper;

    @Transactional
    @Override
    public SupplierCategoryDto createSupplierCategory(SupplierCategoryDto supplierCategoryDto) {
        LOG.info("Creating Supplier category.");

        this.supplierCategoryValidator.validateCreateSupplierCategoryDto(supplierCategoryDto);
        var supplierCategory = this.saveWithUniqueName(new SupplierCategory(supplierCategoryDto.getName().strip()));

        LOG.info("Supplier category with id {} created.", supplierCategory.getId());
        return this.supplierCategoryMapper.mapToDto(supplierCategory, 0L);
    }

    @Transactional
    @Override
    public SupplierCategoryDto updateSupplierCategory(Long supplierCategoryId, SupplierCategoryDto supplierCategoryDto) {
        LOG.info("Updating Supplier category with id {}.", supplierCategoryId);

        var maybeSupplierCategory = this.supplierCategoryRepository.findById(supplierCategoryId);
        this.supplierCategoryValidator.validateSupplierCategoryExistence(maybeSupplierCategory, supplierCategoryId);
        this.supplierCategoryValidator.validateUpdateSupplierCategoryDto(supplierCategoryDto, supplierCategoryId);
        var supplierCategory = maybeSupplierCategory.orElseThrow(ResourceNotFoundException::new);
        supplierCategory.rename(supplierCategoryDto.getName().strip());
        supplierCategory = this.saveWithUniqueName(supplierCategory);

        LOG.info("Supplier category with id {} updated.", supplierCategoryId);
        return this.supplierCategoryMapper.mapToDto(supplierCategory, this.getNumberOfSuppliers(supplierCategoryId));
    }

    @Transactional
    @Override
    public void deleteSupplierCategory(Long supplierCategoryId) {
        LOG.info("Deleting Supplier category with id {}.", supplierCategoryId);

        this.supplierCategoryValidator.validateSupplierCategoryExistence(supplierCategoryId);
        this.supplierCategoryValidator.validateSupplierCategoryHasNoSuppliers(supplierCategoryId);
        try {
            this.supplierCategoryRepository.deleteById(supplierCategoryId);
            // flushed here, so a supplier given this category after the check above fails the delete with the same
            // message
            this.supplierCategoryRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(SupplierCategoryValidator.SUPPLIER_CATEGORY_WITH_SUPPLIERS_MESSAGE_CODE,
                    exception);
        }

        LOG.info("Supplier category with id {} deleted.", supplierCategoryId);
    }

    @Override
    public SupplierCategoryDto getSupplierCategory(Long supplierCategoryId) {
        LOG.info("Loading Supplier category with id {}.", supplierCategoryId);

        var maybeSupplierCategory = this.supplierCategoryRepository.findById(supplierCategoryId);
        this.supplierCategoryValidator.validateSupplierCategoryExistence(maybeSupplierCategory, supplierCategoryId);
        var supplierCategory = maybeSupplierCategory.orElseThrow(ResourceNotFoundException::new);
        return this.supplierCategoryMapper.mapToDto(supplierCategory, this.getNumberOfSuppliers(supplierCategoryId));
    }

    @Override
    public List<SupplierCategoryDto> getSupplierCategories() {
        LOG.info("Loading Supplier categories list.");

        var supplierCategories = this.supplierCategoryRepository.findAll(Sort.by("name"));
        var suppliersCountByCategoryId = this.supplierQueryService.getNumberOfSuppliersByCategoryIds(
                supplierCategories.stream().map(SupplierCategory::getId).toList());
        return supplierCategories.stream()
                .map(supplierCategory -> this.supplierCategoryMapper.mapToDto(supplierCategory,
                        suppliersCountByCategoryId.get(supplierCategory.getId())))
                .toList();
    }

    /**
     * Saves and flushes, so a category given the same name in the meantime (after the validator checked it) fails
     * with the same message as the validator.
     */
    private SupplierCategory saveWithUniqueName(SupplierCategory supplierCategory) {
        try {
            return this.supplierCategoryRepository.saveAndFlush(supplierCategory);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(SupplierCategoryValidator.SUPPLIER_CATEGORY_NAME_UNIQUE_MESSAGE_CODE,
                    exception, supplierCategory.getName());
        }
    }

    private Long getNumberOfSuppliers(Long supplierCategoryId) {
        return this.supplierQueryService.getNumberOfSuppliersByCategoryIds(List.of(supplierCategoryId)).get(supplierCategoryId);
    }
}
