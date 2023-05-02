package com.arturjarosz.task.contractor

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.contractor.application.dto.ContractorDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

import javax.transaction.Transactional

class ContractorTestIT extends BaseTestIT {

    static final CONTRACTORS_URI = "/contractors"
    static final NOT_EXISTING_CONTRACTOR_ID = Integer.MAX_VALUE
    static final MAPPER = new ObjectMapper()
    static final CONTENT_TYPE = "Content-Type"
    static final APPLICATION_JSON = "application/json"
    static final String LOCATION = "Location"

    @Autowired
    private MockMvc mockMvc;

    final createContractorDto = createObjectFromJson("json/contractor/createContractorDto.json", ContractorDto)
    final updateContractorDto = createObjectFromJson("json/contractor/updateContractorDto.json", ContractorDto)

    @Transactional
    def "Creating new Contractor should return created ContractorDto, code 201 and entity location"() {
        given:
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createContractorDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$CONTRACTORS_URI")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdContractor = MAPPER.readValue(response.contentAsString, ContractorDto)
            createdContractor.name == createContractorDto.name
            createdContractor.category == createContractorDto.category
        and:
            response.getHeader(LOCATION) == "$CONTRACTORS_URI/${createdContractor.id}"
    }

    @Transactional
    def "Creating new Contractor with not correct dto should return code 400 and error message"() {
        given:
            createContractorDto.category = null
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createContractorDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$CONTRACTORS_URI")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Contractor category has to be specified."
    }

    @Transactional
    def "Updating Contractor should return code 200 and updated ContractorDto body"() {
        given:
            def createdContractor = createContractor()
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateContractorDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$CONTRACTORS_URI/${createdContractor.id}")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def updatedContractor = MAPPER.readValue(response.contentAsString, ContractorDto)
            updatedContractor.name == updateContractorDto.name
            updatedContractor.category == updateContractorDto.category
            updatedContractor.telephone == updateContractorDto.telephone
            updatedContractor.email == updateContractorDto.email
            updatedContractor.note == updateContractorDto.note
    }

    @Transactional
    def "Updating not existing Contractor should return code 400 and error message"() {
        given:
            createContractor()
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateContractorDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$CONTRACTORS_URI/${NOT_EXISTING_CONTRACTOR_ID}")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Contractor with id ${String.format("%,d", NOT_EXISTING_CONTRACTOR_ID)} does not exist."
    }

    @Transactional
    def "Updating Contractor with incorrect data should return code 400 and error message"() {
        given:
            def createdContractor = createContractor()
            updateContractorDto.name = null
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateContractorDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$CONTRACTORS_URI/${createdContractor.id}")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Name of the Contractor has to be specified."
    }

    @Transactional
    def "Removing not existing Contractor should return code 400 and error message"() {
        given:
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$CONTRACTORS_URI/$NOT_EXISTING_CONTRACTOR_ID"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Contractor with id ${String.format("%,d", NOT_EXISTING_CONTRACTOR_ID)} does not exist."
    }

    @Transactional
    def "Removing Contractor should return code 200 and remove that entity"() {
        given:
            def createdContractor = createContractor()
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$CONTRACTORS_URI/${createdContractor.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def fetchContractorResponse = this.mockMvc.perform(MockMvcRequestBuilders.get("$CONTRACTORS_URI/${createdContractor.id}"))
                    .andReturn().response
            fetchContractorResponse.status == HttpStatus.BAD_REQUEST.value()
            ErrorMessage errorMessage = MAPPER.readValue(fetchContractorResponse.contentAsString, ErrorMessage.class)
            errorMessage.message == "Contractor with id ${String.format("%,d", createdContractor.id)} does not exist."
    }

    @Transactional
    def "Fetching not existing Contractor should return code 400 and error message"() {
        given:
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$CONTRACTORS_URI/$NOT_EXISTING_CONTRACTOR_ID"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            ErrorMessage errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage.class)
            errorMessage.message == "Contractor with id ${String.format("%,d", NOT_EXISTING_CONTRACTOR_ID)} does not exist."
    }

    @Transactional
    def "Fetching Contractor should return code 200, entity with given id and given type"() {
        given:
            def createdContractor = createContractor()
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$CONTRACTORS_URI/${createdContractor.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def fetchedContractor = MAPPER.readValue(response.contentAsString, ContractorDto)
            fetchedContractor.id == createdContractor.id
    }

    @Transactional
    def "Fetching list of BasicContractorDto should return list of all Contractors"() {
        given:
            createContractor()
            createContractor()
            createContractor()
            createContractor()
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$CONTRACTORS_URI"))
                    .andReturn().response
        then:
            def contractors = MAPPER.readValue(response.contentAsString, List<ContractorDto>)
            contractors.size() == 4
    }


    private ContractorDto createContractor() {
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createContractorDto)
        def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$CONTRACTORS_URI")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(requestBody)).andReturn().response
        return MAPPER.readValue(response.contentAsString, ContractorDto)
    }

    private <T> T createObjectFromJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
