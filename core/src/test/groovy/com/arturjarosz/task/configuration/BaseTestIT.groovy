package com.arturjarosz.task.configuration

import com.arturjarosz.task.DatabaseMain
import com.arturjarosz.task.contractor.infrastructure.ContractorCategoryRepository
import com.arturjarosz.task.contractor.model.ContractorCategory
import com.arturjarosz.task.project.infrastructure.repositor.ProjectTypeRepository
import com.arturjarosz.task.project.model.ProjectType
import com.arturjarosz.task.supplier.infrastructure.SupplierCategoryRepository
import com.arturjarosz.task.supplier.model.SupplierCategory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

/**
 * PostgreSQL is started by Testcontainers from the jdbc:tc: datasource url in the test application.yaml. The database
 * has only the production migrations; data that the json fixtures need (a project type, a contractor and a supplier
 * category) is created here, and tests put its ids into the fixtures.
 */
@SpringBootTest(classes = DatabaseMain.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
class BaseTestIT extends Specification {
    protected static final String HOST = "http://localhost"
    protected static final String FIXTURE_PROJECT_TYPE_NAME = "Interior house"
    protected static final String FIXTURE_CONTRACTOR_CATEGORY_NAME = "General"
    protected static final String FIXTURE_SUPPLIER_CATEGORY_NAME = "Flooring shop"
    protected static String baseUrl;

    @Value('${server.port}')
    protected String port

    @Autowired
    private ProjectTypeRepository projectTypeRepository
    @Autowired
    private ContractorCategoryRepository contractorCategoryRepository
    @Autowired
    private SupplierCategoryRepository supplierCategoryRepository

    protected Long projectTypeId
    protected Long contractorCategoryId
    protected Long supplierCategoryId

    def setup() {
        baseUrl = "$HOST:$port"
        // found when an earlier test already created them
        projectTypeId = (projectTypeRepository.findAll().find { it.name == FIXTURE_PROJECT_TYPE_NAME }
                ?: projectTypeRepository.save(new ProjectType(FIXTURE_PROJECT_TYPE_NAME))).id
        contractorCategoryId = (contractorCategoryRepository.findAll().find { it.name == FIXTURE_CONTRACTOR_CATEGORY_NAME }
                ?: contractorCategoryRepository.save(new ContractorCategory(FIXTURE_CONTRACTOR_CATEGORY_NAME))).id
        supplierCategoryId = (supplierCategoryRepository.findAll().find { it.name == FIXTURE_SUPPLIER_CATEGORY_NAME }
                ?: supplierCategoryRepository.save(new SupplierCategory(FIXTURE_SUPPLIER_CATEGORY_NAME))).id
    }
}
