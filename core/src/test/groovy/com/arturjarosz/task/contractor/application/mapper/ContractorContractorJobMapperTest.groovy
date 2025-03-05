package com.arturjarosz.task.contractor.application.mapper

import com.arturjarosz.task.configuration.UserProperties
import com.arturjarosz.task.dto.ContractorJobDto
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto
import com.arturjarosz.task.finance.model.ContractorContractorJobDto
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils
import spock.lang.Specification

class ContractorContractorJobMapperTest extends Specification {

    def subject = new ContractorContractorJobMapperImpl()

    def setup() {
        def userProperties = new UserProperties(incomeTax: 0.1D, vatTax: 0.23D)
        TestUtils.setFieldForObject(subject, "userProperties", userProperties)
    }

    def "should return correctly mapped dto"() {
        given:
            var jobs = prepareContractorJobs(numberOfJobs)

        when:
            def result = subject.mapToContractorContractorJobsDataDto(jobs)

        then:
            result.contractorJobs.size() == numberOfJobs
            result.financialData.count == numberOfJobs
            result.averageFinancialData.netValue == averageNet
            result.averageFinancialData.grossValue == averageGross
            result.averageFinancialData.incomeTax == averageIncome
            result.averageFinancialData.vatTax == averageVat

        where:
            numberOfJobs || averageNet | averageGross | averageIncome | averageVat
            0            || 0.0D       | 0.0D         | 0.0D          | 0.0D
            1            || 200.0D     | 246.0D       | 20.0D         | 46.0D
            2            || 250.0D     | 307.5D       | 25.0D         | 57.5D
    }

    Set<ContractorContractorJobDto> prepareContractorJobs(int number) {
        var contractorJobDtos = new HashSet<ContractorContractorJobDto>()
        for (int i = 0; i < number; i++) {
            var contractorJobDto = new ContractorJobDto()
            var financialData = new FinancialDataDto(value: (200.0D + 100.0D * i), hasInvoice: true)
            var supplyData = new ContractorContractorJobDto(contractorJobDto, financialData)
            contractorJobDtos.add(supplyData)
        }
        return contractorJobDtos
    }
}
