package com.arturjarosz.task.systemparameter

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.SystemParameterDto
import com.arturjarosz.task.systemparameter.application.TaxRatesProvider
import com.arturjarosz.task.systemparameter.application.impl.SystemParameterInitializer
import com.arturjarosz.task.systemparameter.domain.SystemParameters
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

class SystemParameterTestIT extends BaseTestIT {
    static final String SYSTEM_PARAMETERS_URI = "/system-parameters"
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    MockMvc mockMvc

    @Autowired
    SystemParameterInitializer systemParameterInitializer

    @Autowired
    SystemParameterRepository systemParameterRepository

    @Autowired
    TaxRatesProvider taxRatesProvider

    def "Default system parameters should be stored in database on application start"() {
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .get(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI))).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            List<SystemParameterDto> parameters = MAPPER.readValue(response.contentAsString,
                    new TypeReference<List<SystemParameterDto>>() {})
            def valuesByName = parameters.collectEntries { [(it.name): it.value] }
            valuesByName[SystemParameters.VAT_TAX] == "0.23"
            valuesByName[SystemParameters.INCOME_TAX] == "0.18"
            valuesByName[SystemParameters.THEME_COLOR] == "#465fff"
    }

    def "Initializing system parameters should not override values already stored in database"() {
        given:
            def vatTax = this.getParameterByName(SystemParameters.VAT_TAX)
            def updateDto = new SystemParameterDto(name: SystemParameters.VAT_TAX, value: "0.08")
            this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI + "/" + vatTax.id))
                    .header("Content-Type", "application/json")
                    .content(MAPPER.writeValueAsString(updateDto))).andReturn()
        when:
            this.systemParameterInitializer.run(null)
        then:
            this.getParameterByName(SystemParameters.VAT_TAX).value == "0.08"
            this.systemParameterRepository.findAll().count { it.name == SystemParameters.VAT_TAX } == 1
        cleanup:
            updateDto.value = "0.23"
            this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI + "/" + vatTax.id))
                    .header("Content-Type", "application/json")
                    .content(MAPPER.writeValueAsString(updateDto))).andReturn()
    }

    def "Tax rates used in financial calculations should reflect values updated in database"() {
        given:
            def incomeTax = this.getParameterByName(SystemParameters.INCOME_TAX)
            def updateDto = new SystemParameterDto(name: SystemParameters.INCOME_TAX, value: "0.12")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI + "/" + incomeTax.id))
                    .header("Content-Type", "application/json")
                    .content(MAPPER.writeValueAsString(updateDto))).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
            this.taxRatesProvider.getTaxRates().incomeTax() == new BigDecimal("0.12")
            this.taxRatesProvider.getTaxRates().vatTax() == new BigDecimal("0.23")
        cleanup:
            updateDto.value = "0.18"
            this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI + "/" + incomeTax.id))
                    .header("Content-Type", "application/json")
                    .content(MAPPER.writeValueAsString(updateDto))).andReturn()
    }

    def "Updating theme color with value that is not hex color should return 400 and keep previous value"() {
        given:
            def themeColor = this.getParameterByName(SystemParameters.THEME_COLOR)
            def updateDto = new SystemParameterDto(name: SystemParameters.THEME_COLOR, value: "blue")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders
                    .put(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI + "/" + themeColor.id))
                    .header("Content-Type", "application/json")
                    .content(MAPPER.writeValueAsString(updateDto))).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.getParameterByName(SystemParameters.THEME_COLOR).value == "#465fff"
    }

    private SystemParameterDto getParameterByName(String name) {
        def response = this.mockMvc.perform(MockMvcRequestBuilders
                .get(URI.create(HOST + ":" + port + SYSTEM_PARAMETERS_URI))).andReturn().response
        List<SystemParameterDto> parameters = MAPPER.readValue(response.contentAsString,
                new TypeReference<List<SystemParameterDto>>() {})
        return parameters.find { it.name == name }
    }
}
