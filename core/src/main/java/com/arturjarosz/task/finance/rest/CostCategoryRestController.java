package com.arturjarosz.task.finance.rest;

import com.arturjarosz.task.dto.CostCategoryDto;
import com.arturjarosz.task.finance.application.CostCategoryApplicationService;
import com.arturjarosz.task.rest.CostCategoryApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CostCategoryRestController implements CostCategoryApi {

    @NonNull
    private final CostCategoryApplicationService costCategoryApplicationService;

    @Override
    public ResponseEntity<CostCategoryDto> createCostCategory(CostCategoryDto costCategoryDto) {
        var costCategory = this.costCategoryApplicationService.createCostCategory(costCategoryDto);
        var headers = new HttpHeadersBuilder().withLocation("/cost-categories/{costCategoryId}", costCategory.getId())
                .build();
        return new ResponseEntity<>(costCategory, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CostCategoryDto> updateCostCategory(CostCategoryDto costCategoryDto,
            Long costCategoryId) {
        return new ResponseEntity<>(
                this.costCategoryApplicationService.updateCostCategory(costCategoryId, costCategoryDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteCostCategory(Long costCategoryId) {
        this.costCategoryApplicationService.deleteCostCategory(costCategoryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CostCategoryDto> getCostCategory(Long costCategoryId) {
        return new ResponseEntity<>(this.costCategoryApplicationService.getCostCategory(costCategoryId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CostCategoryDto>> getCostCategories() {
        return new ResponseEntity<>(this.costCategoryApplicationService.getCostCategories(), HttpStatus.OK);
    }
}
