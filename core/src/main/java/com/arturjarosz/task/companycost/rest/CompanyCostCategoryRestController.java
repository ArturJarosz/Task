package com.arturjarosz.task.companycost.rest;

import com.arturjarosz.task.companycost.application.CompanyCostCategoryApplicationService;
import com.arturjarosz.task.dto.CompanyCostCategoryDto;
import com.arturjarosz.task.rest.CompanyCostCategoryApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CompanyCostCategoryRestController implements CompanyCostCategoryApi {

    @NonNull
    private final CompanyCostCategoryApplicationService companyCostCategoryApplicationService;

    @Override
    public ResponseEntity<CompanyCostCategoryDto> createCompanyCostCategory(
            CompanyCostCategoryDto companyCostCategoryDto) {
        var category = this.companyCostCategoryApplicationService.create(companyCostCategoryDto);
        var headers = new HttpHeadersBuilder()
                .withLocation("/company-cost-categories/{companyCostCategoryId}", category.getId())
                .build();
        return new ResponseEntity<>(category, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CompanyCostCategoryDto> updateCompanyCostCategory(
            CompanyCostCategoryDto companyCostCategoryDto, Long companyCostCategoryId) {
        return new ResponseEntity<>(
                this.companyCostCategoryApplicationService.update(companyCostCategoryId, companyCostCategoryDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteCompanyCostCategory(Long companyCostCategoryId) {
        this.companyCostCategoryApplicationService.delete(companyCostCategoryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CompanyCostCategoryDto> getCompanyCostCategory(Long companyCostCategoryId) {
        return new ResponseEntity<>(this.companyCostCategoryApplicationService.get(companyCostCategoryId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CompanyCostCategoryDto>> getCompanyCostCategories() {
        return new ResponseEntity<>(this.companyCostCategoryApplicationService.getAll(), HttpStatus.OK);
    }
}
