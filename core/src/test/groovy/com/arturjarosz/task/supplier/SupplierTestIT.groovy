package com.arturjarosz.task.supplier

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.supplier.application.dto.SupplierDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import javax.transaction.Transactional

class SupplierTestIT extends BaseTestIT {

    static final SUPPLIERS_URI = "/suppliers"
    static final NOT_EXISTING_SUPPLIER_ID = Integer.MAX_VALUE
    static final MAPPER = new ObjectMapper()
    static final CONTENT_TYPE = "Content-Type"
    static final APPLICATION_JSON = "application/json"
    static final String LOCATION = "Location"

    final createSupplierDto = createObjectFromJson("json/supplier/createSupplierDto.json", SupplierDto)
    final incorrectCreateSupplierDto =
            createObjectFromJson("json/supplier/incorrectCreateSupplierDto.json", SupplierDto)
    final updateSupplierDto = createObjectFromJson("json/supplier/updateSupplierDto.json", SupplierDto)
    final incorrectUpdateSupplierDto =
            createObjectFromJson("json/supplier/incorrectUpdateSupplierDto.json", SupplierDto)

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating new supplier should return created SupplierDto, code 201 and entity location"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createSupplierDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$SUPPLIERS_URI")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdSupplier = MAPPER.readValue(response.contentAsString, SupplierDto)
            createdSupplier != null
            createdSupplier.category == createSupplierDto.category
            createdSupplier.name == createSupplierDto.name
        and:
            response.getHeader(LOCATION) == "$SUPPLIERS_URI/${createdSupplier.id}"
    }

    @Transactional
    def "Creating new supplier with not correct dto should return code 400 and error message"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(incorrectCreateSupplierDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$SUPPLIERS_URI")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Name of Supplier cannot be empty."
    }

    @Transactional
    def "Updating supplier should return code 200 and updated SupplierDto body"() {
        given:
            def createdSupplier = createSupplier()
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateSupplierDto)
        when:
            def response =
                    this.mockMvc.perform(MockMvcRequestBuilders.put("$SUPPLIERS_URI/${createdSupplier.id}")
                            .header(CONTENT_TYPE, APPLICATION_JSON)
                            .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def updatedSupplier = MAPPER.readValue(response.contentAsString, SupplierDto)
            updatedSupplier.id == createdSupplier.id
    }

    @Transactional
    def "Updating not existing supplier should return code 200 and updated SupplierDto body"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateSupplierDto)
        when:
            def response =
                    this.mockMvc.perform(MockMvcRequestBuilders.put("$SUPPLIERS_URI/$NOT_EXISTING_SUPPLIER_ID")
                            .header(CONTENT_TYPE, APPLICATION_JSON)
                            .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Supplier with id ${String.format("%,d", NOT_EXISTING_SUPPLIER_ID)} does not exist."
    }

    @Transactional
    def "Updating supplier with incorrect data should return code 400 and error message"() {
        given:
            def createdSupplier = createSupplier()
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(incorrectUpdateSupplierDto)
        when:
            def response =
                    this.mockMvc.perform(MockMvcRequestBuilders.put("$SUPPLIERS_URI/${createdSupplier.id}")
                            .header(CONTENT_TYPE, APPLICATION_JSON)
                            .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Name of Supplier cannot be empty."
    }

    @Transactional
    def "Deleting not existing supplier should return code 400 and error message"() {
        given:
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$SUPPLIERS_URI/$NOT_EXISTING_SUPPLIER_ID"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Supplier with id ${String.format("%,d", NOT_EXISTING_SUPPLIER_ID)} does not exist."
    }

    @Transactional
    def "Deleting existing supplier should return code 200"() {
        given:
            def createdSupplier = createSupplier()
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$SUPPLIERS_URI/$createdSupplier.id"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Getting not existing supplier should return code 400 and error message"() {
        given:
        when:
            def response =
                    this.mockMvc.perform(MockMvcRequestBuilders.get("$SUPPLIERS_URI/$NOT_EXISTING_SUPPLIER_ID"))
                            .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Supplier with id ${String.format("%,d", NOT_EXISTING_SUPPLIER_ID)} does not exist."
    }

    @Transactional
    def "Getting supplier should return code 200 and object"() {
        given:
            def createdSupplier = createSupplier()
        when:
            def response =
                    this.mockMvc.perform(MockMvcRequestBuilders.get("$SUPPLIERS_URI/${createdSupplier.id}"))
                            .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def supplier = MAPPER.readValue(response.contentAsString, SupplierDto)
            supplier.id == createdSupplier.id
    }

    @Transactional
    def "Getting list of suppliers should return list of all suppliers"() {
        given:
            createSupplier()
            createSupplier()
        when:
            def response =
                    this.mockMvc.perform(MockMvcRequestBuilders.get("$SUPPLIERS_URI")).andReturn().response
        then:
            def suppliers = MAPPER.readValue(response.contentAsString, List<SupplierDto>)
            suppliers.size() == 2
    }

    private SupplierDto createSupplier() {
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createSupplierDto)
        def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$SUPPLIERS_URI")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(requestBody)).andReturn().response
        return MAPPER.readValue(response.contentAsString, SupplierDto)
    }


    private <T> T createObjectFromJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
