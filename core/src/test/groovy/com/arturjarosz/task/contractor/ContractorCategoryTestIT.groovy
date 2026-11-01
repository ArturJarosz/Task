package com.arturjarosz.task.contractor

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.dto.ContractorCategoryDto
import com.arturjarosz.task.dto.ContractorDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

class ContractorCategoryTestIT extends BaseTestIT {
    static final String CATEGORIES_URI = "/contractor-categories"
    static final String CONTRACTORS_URI = "/contractors"
    static final String CONTENT_TYPE = "Content-Type"
    static final String APPLICATION_JSON = "application/json"
    static final long NOT_EXISTING_CATEGORY_ID = 10000L
    // created by BaseTestIT for the contractor fixtures
    static final String SEEDED_CATEGORY_NAME = "General"
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating contractor category should return code 201, created category and its location"() {
        when:
            def response = this.postCategory(new ContractorCategoryDto(name: "  Roofer  "))
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdCategory = MAPPER.readValue(response.contentAsString, ContractorCategoryDto)
            createdCategory.id != null
            createdCategory.name == "Roofer"
            createdCategory.numberOfContractors == 0
            response.getHeader("Location") == "$CATEGORIES_URI/${createdCategory.id}"
    }

    @Transactional
    def "Creating contractor category with empty name should return code 400 and error message"() {
        when:
            def response = this.postCategory(new ContractorCategoryDto(name: " "))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Contractor category name cannot be empty."
    }

    @Transactional
    def "Creating contractor category with name of existing category should return code 400 and error message"() {
        when:
            def response = this.postCategory(new ContractorCategoryDto(name: SEEDED_CATEGORY_NAME.toUpperCase()))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Contractor category with name GENERAL already exists."
    }

    @Transactional
    def "Renaming contractor category should return code 200 and renamed category"() {
        given:
            def category = this.createCategory("Roofer")
        when:
            def response = this.putCategory(category.id, new ContractorCategoryDto(name: "Bricklayer"))
        then:
            response.status == HttpStatus.OK.value()
        and:
            MAPPER.readValue(response.contentAsString, ContractorCategoryDto).name == "Bricklayer"
        and:
            def loaded = MAPPER.readValue(this.mockMvc.perform(
                    MockMvcRequestBuilders.get("$CATEGORIES_URI/${category.id}")).andReturn().response
                    .contentAsString, ContractorCategoryDto)
            loaded.name == "Bricklayer"
    }

    @Transactional
    def "Renaming contractor category to its own name with different letter case should be allowed"() {
        given:
            def category = this.createCategory("Roofer")
        when:
            def response = this.putCategory(category.id, new ContractorCategoryDto(name: "Roofer".toUpperCase()))
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Renaming contractor category to name of another category should return code 400"() {
        given:
            def category = this.createCategory("Roofer")
        when:
            def response = this.putCategory(category.id, new ContractorCategoryDto(name: SEEDED_CATEGORY_NAME))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Contractor category with name General already exists."
    }

    @Transactional
    def "Updating not existing contractor category should return code 404 and error message"() {
        when:
            def response = this.putCategory(NOT_EXISTING_CATEGORY_ID, new ContractorCategoryDto(name: "Any"))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Contractor category with id 10,000 does not exist."
    }

    @Transactional
    def "Deleting contractor category without contractors should return code 200 and remove it"() {
        given:
            def category = this.createCategory("Roofer")
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
    def "Deleting contractor category used by a contractor should return code 400 and keep it"() {
        given:
            def category = this.createCategory("Roofer")
            this.createContractor(category.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$CATEGORIES_URI/${category.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Contractor category used by contractors cannot be removed."
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$CATEGORIES_URI/${category.id}"))
                    .andReturn().response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Getting contractor categories should return them sorted by name with the number of their contractors"() {
        given:
            def used = this.createCategory("Roofer")
            def unused = this.createCategory("Glazier")
            this.createContractor(used.id)
            this.createContractor(used.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(CATEGORIES_URI)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            List<ContractorCategoryDto> categories = MAPPER.readValue(response.contentAsString, ContractorCategoryDto[]).toList()
            categories*.name == ["General", "Glazier", "Roofer"]
            categories.find { it.id == used.id }.numberOfContractors == 2
            categories.find { it.id == unused.id }.numberOfContractors == 0
    }

    @Transactional
    def "Creating contractor with not existing category should return code 404 and error message"() {
        when:
            def response = this.postContractor(NOT_EXISTING_CATEGORY_ID)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Contractor category with id 10,000 does not exist."
    }

    @Transactional
    def "Application configuration should list contractor categories with their id and name"() {
        given:
            def category = this.createCategory("Roofer")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("/configuration")).andReturn().response
        then:
            def configuration = MAPPER.readValue(response.contentAsString, ApplicationConfigurationDto)
            configuration.contractorTypes.find { it.id == String.valueOf(category.id) }.label == "Roofer"
    }

    private def postCategory(ContractorCategoryDto categoryDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(CATEGORIES_URI)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(categoryDto))).andReturn().response
    }

    private def putCategory(Long categoryId, ContractorCategoryDto categoryDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put("$CATEGORIES_URI/$categoryId")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(categoryDto))).andReturn().response
    }

    private ContractorCategoryDto createCategory(String name) {
        return MAPPER.readValue(this.postCategory(new ContractorCategoryDto(name: name)).contentAsString, ContractorCategoryDto)
    }

    private def postContractor(Long categoryId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(CONTRACTORS_URI)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(new ContractorDto(name: "Kowalski Elektryka", categoryId: categoryId))))
                .andReturn().response
    }

    private void createContractor(Long categoryId) {
        assert this.postContractor(categoryId).status == HttpStatus.CREATED.value()
    }
}
