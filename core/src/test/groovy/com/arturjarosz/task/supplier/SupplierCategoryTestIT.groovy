package com.arturjarosz.task.supplier

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.ApplicationConfigurationDto
import com.arturjarosz.task.dto.SupplierCategoryDto
import com.arturjarosz.task.dto.SupplierDto
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

class SupplierCategoryTestIT extends BaseTestIT {
    static final String CATEGORIES_URI = "/supplier-categories"
    static final String SUPPLIERS_URI = "/suppliers"
    static final String CONTENT_TYPE = "Content-Type"
    static final String APPLICATION_JSON = "application/json"
    static final long NOT_EXISTING_CATEGORY_ID = 10000L
    // inserted by the test changelog and used by the supplier fixtures
    static final String SEEDED_CATEGORY_NAME = "Flooring shop"
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    private MockMvc mockMvc

    @Transactional
    def "Creating supplier category should return code 201, created category and its location"() {
        when:
            def response = this.postCategory(new SupplierCategoryDto(name: "  Tile shop  "))
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdCategory = MAPPER.readValue(response.contentAsString, SupplierCategoryDto)
            createdCategory.id != null
            createdCategory.name == "Tile shop"
            createdCategory.numberOfSuppliers == 0
            response.getHeader("Location") == "$CATEGORIES_URI/${createdCategory.id}"
    }

    @Transactional
    def "Creating supplier category with empty name should return code 400 and error message"() {
        when:
            def response = this.postCategory(new SupplierCategoryDto(name: " "))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Supplier category name cannot be empty."
    }

    @Transactional
    def "Creating supplier category with name of existing category should return code 400 and error message"() {
        when:
            def response = this.postCategory(new SupplierCategoryDto(name: SEEDED_CATEGORY_NAME.toUpperCase()))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Supplier category with name FLOORING SHOP already exists."
    }

    @Transactional
    def "Renaming supplier category should return code 200 and renamed category"() {
        given:
            def category = this.createCategory("Tile shop")
        when:
            def response = this.putCategory(category.id, new SupplierCategoryDto(name: "Window shop"))
        then:
            response.status == HttpStatus.OK.value()
        and:
            MAPPER.readValue(response.contentAsString, SupplierCategoryDto).name == "Window shop"
        and:
            def loaded = MAPPER.readValue(this.mockMvc.perform(
                    MockMvcRequestBuilders.get("$CATEGORIES_URI/${category.id}")).andReturn().response
                    .contentAsString, SupplierCategoryDto)
            loaded.name == "Window shop"
    }

    @Transactional
    def "Renaming supplier category to its own name with different letter case should be allowed"() {
        given:
            def category = this.createCategory("Tile shop")
        when:
            def response = this.putCategory(category.id, new SupplierCategoryDto(name: "Tile shop".toUpperCase()))
        then:
            response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Renaming supplier category to name of another category should return code 400"() {
        given:
            def category = this.createCategory("Tile shop")
        when:
            def response = this.putCategory(category.id, new SupplierCategoryDto(name: SEEDED_CATEGORY_NAME))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Supplier category with name Flooring shop already exists."
    }

    @Transactional
    def "Updating not existing supplier category should return code 404 and error message"() {
        when:
            def response = this.putCategory(NOT_EXISTING_CATEGORY_ID, new SupplierCategoryDto(name: "Any"))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Supplier category with id 10,000 does not exist."
    }

    @Transactional
    def "Deleting supplier category without suppliers should return code 200 and remove it"() {
        given:
            def category = this.createCategory("Tile shop")
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
    def "Deleting supplier category used by a supplier should return code 400 and keep it"() {
        given:
            def category = this.createCategory("Tile shop")
            this.createSupplier(category.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.delete("$CATEGORIES_URI/${category.id}"))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Supplier category used by suppliers cannot be removed."
        and:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$CATEGORIES_URI/${category.id}"))
                    .andReturn().response.status == HttpStatus.OK.value()
    }

    @Transactional
    def "Getting supplier categories should return them sorted by name with the number of their suppliers"() {
        given:
            def used = this.createCategory("Tile shop")
            def unused = this.createCategory("Door shop")
            this.createSupplier(used.id)
            this.createSupplier(used.id)
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(CATEGORIES_URI)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            List<SupplierCategoryDto> categories = MAPPER.readValue(response.contentAsString, SupplierCategoryDto[]).toList()
            categories*.name == ["Door shop", "Flooring shop", "Tile shop"]
            categories.find { it.id == used.id }.numberOfSuppliers == 2
            categories.find { it.id == unused.id }.numberOfSuppliers == 0
    }

    @Transactional
    def "Creating supplier with not existing category should return code 404 and error message"() {
        when:
            def response = this.postSupplier(NOT_EXISTING_CATEGORY_ID)
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Supplier category with id 10,000 does not exist."
    }

    @Transactional
    def "Application configuration should list supplier categories with their id and name"() {
        given:
            def category = this.createCategory("Tile shop")
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("/configuration")).andReturn().response
        then:
            def configuration = MAPPER.readValue(response.contentAsString, ApplicationConfigurationDto)
            configuration.supplierTypes.find { it.id == String.valueOf(category.id) }.label == "Tile shop"
    }

    private def postCategory(SupplierCategoryDto categoryDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(CATEGORIES_URI)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(categoryDto))).andReturn().response
    }

    private def putCategory(Long categoryId, SupplierCategoryDto categoryDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put("$CATEGORIES_URI/$categoryId")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(categoryDto))).andReturn().response
    }

    private SupplierCategoryDto createCategory(String name) {
        return MAPPER.readValue(this.postCategory(new SupplierCategoryDto(name: name)).contentAsString, SupplierCategoryDto)
    }

    private def postSupplier(Long categoryId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(SUPPLIERS_URI)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .content(MAPPER.writeValueAsString(new SupplierDto(name: "Hurtownia Lublin", categoryId: categoryId))))
                .andReturn().response
    }

    private void createSupplier(Long categoryId) {
        assert this.postSupplier(categoryId).status == HttpStatus.CREATED.value()
    }
}
