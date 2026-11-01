package com.arturjarosz.task.contractor.rest;

import com.arturjarosz.task.dto.ContractorCategoryDto;
import com.arturjarosz.task.contractor.application.ContractorCategoryApplicationService;
import com.arturjarosz.task.rest.ContractorCategoryApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ContractorCategoryRestController implements ContractorCategoryApi {

    @NonNull
    private final ContractorCategoryApplicationService contractorCategoryApplicationService;

    @Override
    public ResponseEntity<ContractorCategoryDto> createContractorCategory(ContractorCategoryDto contractorCategoryDto) {
        var contractorCategory = this.contractorCategoryApplicationService.createContractorCategory(contractorCategoryDto);
        var headers = new HttpHeadersBuilder().withLocation("/contractor-categories/{contractorCategoryId}", contractorCategory.getId())
                .build();
        return new ResponseEntity<>(contractorCategory, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ContractorCategoryDto> updateContractorCategory(ContractorCategoryDto contractorCategoryDto,
            Long contractorCategoryId) {
        return new ResponseEntity<>(
                this.contractorCategoryApplicationService.updateContractorCategory(contractorCategoryId, contractorCategoryDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteContractorCategory(Long contractorCategoryId) {
        this.contractorCategoryApplicationService.deleteContractorCategory(contractorCategoryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ContractorCategoryDto> getContractorCategory(Long contractorCategoryId) {
        return new ResponseEntity<>(this.contractorCategoryApplicationService.getContractorCategory(contractorCategoryId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ContractorCategoryDto>> getContractorCategories() {
        return new ResponseEntity<>(this.contractorCategoryApplicationService.getContractorCategories(), HttpStatus.OK);
    }
}
