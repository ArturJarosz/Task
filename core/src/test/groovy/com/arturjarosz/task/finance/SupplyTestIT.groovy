package com.arturjarosz.task.finance

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.*
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class SupplyTestIT extends BaseTestIT {
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String SUPPLIERS_URI = "/suppliers"
    static final String SUPPLIES_URI = "/supplies"

    static final NOT_EXISTING_SUPPLIER_ID = Integer.MAX_VALUE
    static final NOT_EXISTING_SUPPLY_ID = Integer.MAX_VALUE
    static final NOT_EXISTING_PROJECT_ID = Integer.MAX_VALUE

    static final ObjectMapper MAPPER = new ObjectMapper()
    static final CONTENT_TYPE = "Content-Type"
    static final APPLICATION_JSON = "application/json"
    static final String LOCATION = "Location"

    def architectDto = createObjectFromJson('json/architect/architect.json', ArchitectDto)
    def privateClientDto = createObjectFromJson('json/client/privateClient.json', ClientDto)
    def projectDto = createObjectFromJson('json/project/properProject.json', ProjectCreateDto)
    def createSupplierDto = createObjectFromJson("json/supplier/createSupplierDto.json", SupplierDto)
    def createSupplyDto = createObjectFromJson("json/finance/supply/createSupply.json", SupplyDto)
    def updateSupplyDto = createObjectFromJson("json/finance/supply/updateSupply.json", SupplyDto)

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "creating supply with wrong input data should return code 400 and error message"() {
        given:
            def project = createProject()
            createSupplyDto.supplierId = NOT_EXISTING_SUPPLIER_ID
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createSupplyDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/${project.id}$SUPPLIES_URI")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Supplier with id ${String.format("%,d", NOT_EXISTING_SUPPLIER_ID)} does not exist."
    }

    @Transactional
    def "creating supplier with correct input should return code 201, created object and its location"() {
        given:
            def project = createProject()
            def supplier = createSupplier()
            createSupplyDto.supplierId = supplier.id
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createSupplyDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/${project.id}$SUPPLIES_URI")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdSupply = MAPPER.readValue(response.contentAsString, SupplyDto)
        and:
            response.getHeader(LOCATION) == "$PROJECTS_URI/${project.id}$SUPPLIES_URI/${createdSupply.id}"
    }

    @Transactional
    def "updating not existing supply should return code 400 and error message"() {
        given:
            def project = createProject()
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateSupplyDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$PROJECTS_URI/${project.id}$SUPPLIES_URI/${NOT_EXISTING_SUPPLY_ID}")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Supply with id ${String.format("%,d", NOT_EXISTING_SUPPLIER_ID)} does not exist in Project with id ${project.id}."
    }

    @Transactional
    def "updating supply with incorrect data should return code 400 and error message"() {
        given:
            def project = createProject()
            def supplier = createSupplier()
            def supply = createSupply(project.id, supplier.id)
            updateSupplyDto.name = null
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateSupplyDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$PROJECTS_URI/${project.id}$SUPPLIES_URI/${supply.id}")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Supply name was not provided."
    }

    @Transactional
    def "updating supply with correct data should return code 200 and updated object"() {
        given:
            def project = createProject()
            def supplier = createSupplier()
            def supply = createSupply(project.id, supplier.id)
            def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(updateSupplyDto)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.put("$PROJECTS_URI/${project.id}$SUPPLIES_URI/${supply.id}")
                    .header(CONTENT_TYPE, APPLICATION_JSON)
                    .content(requestBody)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def updatedSupply = MAPPER.readValue(response.contentAsString, SupplyDto)
            updatedSupply.name == updateSupplyDto.name
            updatedSupply.note == updateSupplyDto.note
            updatedSupply.value == updateSupplyDto.value
    }

    @Transactional
    def "fetching not existing supply should return code 400 and error message"() {
        given:
            def project = createProject()
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/${project.id}$SUPPLIES_URI/${NOT_EXISTING_SUPPLY_ID}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Supply with id ${String.format("%,d", NOT_EXISTING_SUPPLIER_ID)} does not exist in Project with id ${project.id}."
    }

    @Transactional
    def "fetching existing supply should return code 200 and supply"() {
        given:
            def project = createProject()
            def supplier = createSupplier()
            def supply = createSupply(project.id, supplier.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/${project.id}$SUPPLIES_URI/${supply.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def fetchedSupply = MAPPER.readValue(response.contentAsString, SupplyDto)
            fetchedSupply.id == supply.id
    }

    @Transactional
    def "deleting not existing supply should return code 400 and error message"() {
        given:
            def project = createProject()
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$PROJECTS_URI/${project.id}$SUPPLIES_URI/${NOT_EXISTING_SUPPLY_ID}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Supply with id ${String.format("%,d", NOT_EXISTING_SUPPLIER_ID)} does not exist in Project with id ${project.id}."
    }

    @Transactional
    def "deleting existing supply should return code 200"() {
        given:
            def project = createProject()
            def supplier = createSupplier()
            def supply = createSupply(project.id, supplier.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$PROJECTS_URI/${project.id}$SUPPLIES_URI/${supply.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "fetching supplies for not existing project should return code 400 and error message"() {
        given:
            def project = createProject()
            def supplier = createSupplier()
            createSupply(project.id, supplier.id)
            createSupply(project.id, supplier.id)
            createSupply(project.id, supplier.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/${NOT_EXISTING_PROJECT_ID}$SUPPLIES_URI"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            def errorMessage = MAPPER.readValue(response.contentAsString, ErrorMessage)
            errorMessage.message == "Project with id ${String.format("%,d", NOT_EXISTING_PROJECT_ID)} does not exist."
    }

    @Transactional
    def "fetching supplies for project should return code 200 and list of supplies"() {
        given:
            def project = createProject()
            def supplier = createSupplier()
            createSupply(project.id, supplier.id)
            createSupply(project.id, supplier.id)
            createSupply(project.id, supplier.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("$PROJECTS_URI/${project.id}$SUPPLIES_URI"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            def supplies = MAPPER.readValue(response.contentAsString, List<SupplyDto>)
            supplies.size() == 3
    }

    private ProjectDto createProject() {
        def architectDto = TestsHelper.createArchitect(this.architectDto, "$ARCHITECTS_URI", this.mockMvc)
        this.projectDto.architectId = architectDto.id
        def clientDto = TestsHelper.createClient(this.privateClientDto, "$CLIENTS_URI", this.mockMvc)
        this.projectDto.clientId = clientDto.id
        return TestsHelper.createProject(this.projectDto, "$PROJECTS_URI", this.mockMvc)
    }

    private SupplierDto createSupplier() {
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createSupplierDto)
        def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$SUPPLIERS_URI")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(requestBody)).andReturn().response
        return MAPPER.readValue(response.contentAsString, SupplierDto)
    }

    private SupplyDto createSupply(long projectId, long supplierId) {
        createSupplyDto.supplierId = supplierId
        def requestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(createSupplyDto)
        def response = this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/$projectId$SUPPLIES_URI")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(requestBody)).andReturn().response
        return MAPPER.readValue(response.contentAsString, SupplyDto)
    }

    private <T> T createObjectFromJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
