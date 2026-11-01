package com.arturjarosz.task.finance

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.CostCategoryDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.utils.TestsHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

class CostCategoryTestIT extends BaseTestIT {
    static final String CATEGORIES_URI = "/cost-categories"
    static final String ARCHITECTS_URI = "/architects"
    static final String CLIENTS_URI = "/clients"
    static final String PROJECTS_URI = "/projects"
    static final String CONTENT_TYPE = "Content-Type"
    static final String APPLICATION_JSON = "application/json"
    static final long NOT_EXISTING_CATEGORY_ID = 10000L
    // created by the migration from the former cost category enum, like "Mistake"
    static final String SEEDED_CATEGORY_NAME = "Fuel"
    static final ObjectMapper MAPPER = new ObjectMapper()

    final def architect = readJson('json/architect/architect.json', ArchitectDto)
    final def privateClientDto = readJson('json/client/privateClient.json', ClientDto)
    final def properProjectDto = readJson('json/project/properProject.json', ProjectCreateDto)

    @Autowired
    private MockMvc mockMvc


    def setup() {
        // ids of the data created by BaseTestIT
        properProjectDto.typeId = projectTypeId
    }

    @Transactional
    def "Creating cost category should return code 201, created category and its location"() {
        when:
            def response = this.postCategory(new CostCategoryDto(name: "  Parking  "))
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdCategory = MAPPER.readValue(response.contentAsString, CostCategoryDto)
            createdCategory.id != null
            createdCategory.name == "Parking"
            createdCategory.numberOfCosts == 0
            response.getHeader("Location") == "$CATEGORIES_URI/${createdCategory.id}"
    }

    @Transactional
    def "Creating cost category with empty name should return code 400 and error message"() {
        when:
            def response = this.postCategory(new CostCategoryDto(name: " "))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "Cost category name cannot be empty."
    }

    @Transactional
    def "Creating cost category with name of existing category should return code 400 and error message"() {
        when:
            def response = this.postCategory(new CostCategoryDto(name: SEEDED_CATEGORY_NAME.toUpperCase()))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Cost category with name FUEL already exists."
    }

    @Transactional
    def "Renaming cost category should return code 200 and renamed category"() {
        given:
            def category = this.createCategory("Parking")
        when:
            def response = this.putCategory(category.id, new CostCategoryDto(name: "Tolls"))
        then:
            response.status == HttpStatus.OK.value()
        and:
            MAPPER.readValue(response.contentAsString, CostCategoryDto).name == "Tolls"
        and:
            def loaded = MAPPER.readValue(this.mockMvc.perform(
                    MockMvcRequestBuilders.get("$CATEGORIES_URI/${category.id}")).andReturn().response
                    .contentAsString, CostCategoryDto)
            loaded.name == "Tolls"
    }

    @Transactional
    def "Renaming cost category to its own name with different letter case should be allowed"() {
        given:
            def category = this.createCategory("Parking")
        when:
            def response = this.putCategory(category.id, new CostCategoryDto(name: "Parking".toUpperCase()))
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Renaming cost category to name of another category should return code 400"() {
        given:
            def category = this.createCategory("Parking")
        when:
            def response = this.putCategory(category.id, new CostCategoryDto(name: SEEDED_CATEGORY_NAME))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Cost category with name Fuel already exists."
    }

    @Transactional
    def "Updating not existing cost category should return code 404 and error message"() {
        when:
            def response = this.putCategory(NOT_EXISTING_CATEGORY_ID, new CostCategoryDto(name: "Any"))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Cost category with id 10,000 does not exist."
    }

    @Transactional
    def "Deleting cost category without costs should return code 200 and remove it"() {
        given:
            def category = this.createCategory("Parking")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$CATEGORIES_URI/${category.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$CATEGORIES_URI/${category.id}"))
                    .andReturn().response.status == HttpStatus.NOT_FOUND.value()
    }

    @Transactional
    def "Deleting cost category used by a cost should return code 400 and keep it"() {
        given:
            def category = this.createCategory("Parking")
            this.createCost(this.createProject(), category.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$CATEGORIES_URI/${category.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Cost category used by costs cannot be removed."
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$CATEGORIES_URI/${category.id}"))
                    .andReturn().response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Getting cost categories should return them sorted by name with the number of their costs"() {
        given:
            def used = this.createCategory("Parking")
            def unused = this.createCategory("Accommodation")
            def projectId = this.createProject()
            this.createCost(projectId, used.id)
            this.createCost(projectId, used.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(CATEGORIES_URI)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            List<CostCategoryDto> categories = MAPPER.readValue(response.contentAsString, CostCategoryDto[]).toList()
            categories*.name == ["Accommodation", "Fuel", "Mistake", "Parking"]
            categories.find { it.id == used.id }.numberOfCosts == 2
            categories.find { it.id == unused.id }.numberOfCosts == 0
    }

    @Transactional
    def "Creating cost with not existing category should return code 404 and error message"() {
        when:
            def response = this.postCost(this.createProject(), NOT_EXISTING_CATEGORY_ID)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Cost category with id 10,000 does not exist."
    }

    @Transactional
    def "Application configuration should list cost categories with their id and name"() {
        given:
            def category = this.createCategory("Parking")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("/configuration")).andReturn().response
        then:
            def configuration = MAPPER.readValue(response.contentAsString, ApplicationConfigurationDto)
            configuration.costCategories.find { it.id == String.valueOf(category.id) }.label == "Parking"
    }

    private def postCategory(CostCategoryDto categoryDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(CATEGORIES_URI)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(categoryDto))).andReturn().response
    }

    private def putCategory(Long categoryId, CostCategoryDto categoryDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put("$CATEGORIES_URI/$categoryId")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(categoryDto))).andReturn().response
    }

    private CostCategoryDto createCategory(String name) {
        return MAPPER.readValue(this.postCategory(new CostCategoryDto(name: name)).contentAsString, CostCategoryDto)
    }

    private Long createProject() {
        properProjectDto.architectId = TestsHelper.createArchitect(architect, "$HOST:$port$ARCHITECTS_URI",
                this.mockMvc).id
        properProjectDto.clientId = TestsHelper.createClient(privateClientDto, "$HOST:$port$CLIENTS_URI",
                this.mockMvc).id
        return TestsHelper.createProject(properProjectDto, "$HOST:$port$PROJECTS_URI", this.mockMvc).id
    }

    private def postCost(Long projectId, Long categoryId) {
        def requestBody = """{"name": "Fuel for site visit", "value": 200.0, "date": "2026-01-15", "payable": true,
                "paid": false, "categoryId": $categoryId}"""
        return this.mockMvc.perform(MockMvcRequestBuilders.post("$PROJECTS_URI/$projectId/costs")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(requestBody)).andReturn().response
    }

    private void createCost(Long projectId, Long categoryId) {
        assert this.postCost(projectId, categoryId).status == HttpStatus.CREATED.value()
    }

    private <T> T readJson(String path, Class<T> clazz) {
        return MAPPER.readValue(new File(getClass().classLoader.getResource(path).file), clazz)
    }
}
