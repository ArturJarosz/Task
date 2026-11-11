package com.arturjarosz.task.role

import com.arturjarosz.task.configuration.BaseTestIT
import com.arturjarosz.task.dto.PermissionActionDto
import com.arturjarosz.task.dto.PermissionDto
import com.arturjarosz.task.dto.PermissionResourceDto
import com.arturjarosz.task.dto.ResourcePermissionsDto
import com.arturjarosz.task.dto.RoleDto
import com.arturjarosz.task.role.infrastructure.repository.RoleRepository
import com.arturjarosz.task.security.permission.Permission
import com.arturjarosz.task.sharedkernel.exceptions.ErrorMessage
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.transaction.annotation.Transactional
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

@Transactional
class RoleTestIT extends BaseTestIT {
    static final String ROLES_URI = "/roles"
    static final long NOT_EXISTING_ROLE_ID = 10000L
    // created by the migration adding roles, the only predefined one
    static final String ADMINISTRATOR = "Administrator"
    static final ObjectMapper MAPPER = new ObjectMapper()

    @Autowired
    MockMvc mockMvc

    @Autowired
    RoleRepository roleRepository

    @Autowired
    UserRepository userRepository

    def "Getting roles should return them sorted by name, the predefined administrator role with every permission"() {
        given:
            this.createRole("Viewer", [p("PROJECT", "READ")])
            this.createRole("Accountant", [p("COMPANY_COST", "READ")])
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get(ROLES_URI)).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            List<RoleDto> roles = MAPPER.readValue(response.contentAsString, new TypeReference<List<RoleDto>>() {})
            // other tests may have left roles of their own
            roles*.name.findAll { it in ["Accountant", ADMINISTRATOR, "Viewer"] } == ["Accountant", ADMINISTRATOR, "Viewer"]
            roles*.name == roles*.name.toSorted()
            def administrator = roles.find { it.name == ADMINISTRATOR }
            administrator.administrator
            administrator.permissions == Permission.all().collect { p(it.resource().name(), it.action().name()) }
            roles.findAll { it.name != ADMINISTRATOR }.every { !it.administrator }
    }

    def "Creating role should return code 201, created role and its location"() {
        when:
            def response = this.postRole(new RoleDto(name: "  Accountant  ",
                    permissions: [p("PROJECT_FINANCE", "EDIT"), p("PROJECT_FINANCE", "READ"), p("PROJECT", "READ")]))
        then:
            response.status == HttpStatus.CREATED.value()
        and:
            def createdRole = MAPPER.readValue(response.contentAsString, RoleDto)
            createdRole.id != null
            createdRole.name == "Accountant"
            !createdRole.administrator
            createdRole.permissions == [p("PROJECT", "READ"), p("PROJECT_FINANCE", "READ"), p("PROJECT_FINANCE", "EDIT")]
            createdRole.numberOfUsers == 0
            response.getHeader("Location") == "$ROLES_URI/${createdRole.id}"
    }

    def "Creating role marked as administrator should create an ordinary role"() {
        when:
            def response = this.postRole(new RoleDto(name: "Accountant", administrator: true,
                    permissions: [p("COMPANY_COST", "READ")]))
        then:
            response.status == HttpStatus.CREATED.value()
            def createdRole = MAPPER.readValue(response.contentAsString, RoleDto)
            !createdRole.administrator
            createdRole.permissions == [p("COMPANY_COST", "READ")]
    }

    def "Creating role with name of existing role should return code 400 and error message"() {
        when:
            def response = this.postRole(new RoleDto(name: ADMINISTRATOR.toUpperCase(), permissions: []))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Role with name ADMINISTRATOR already exists."
    }

    def "Creating role with wrong data should return code 400 and error message"() {
        when:
            def response = this.postRole(roleDto)
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == message
        where:
            roleDto                                                       || message
            new RoleDto(name: " ", permissions: [])                       || "Role name cannot be empty."
            new RoleDto(permissions: [])                                  || "Role name has to be specified."
            new RoleDto(name: "x" * 101, permissions: [])                 || "Role name cannot be longer than 100 characters."
            new RoleDto(name: "Accountant", permissions: null)            || "Role permissions have to be provided."
    }

    def "Creating role with unknown permission should return code 400"() {
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.post(ROLES_URI)
                    .header("Content-Type", "application/json")
                    .content('{"name": "Accountant", "permissions": [{"resource": "EVERYTHING", "action": "READ"}]}'))
                    .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.roleRepository.findAll().every { it.name != "Accountant" }
    }

    def "Creating role with an action not available for its resource should return code 400 and error message"() {
        when:
            def response = this.postRole(new RoleDto(name: "Accountant", permissions: [p(resource, action)]))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "Action $action is not available for $resource."
        where:
            resource           | action
            "CONTRACT"         | "DELETE"
            "FINANCIAL_REPORT" | "EDIT"
    }

    def "Creating role without a permission another one requires should return code 400 and error message"() {
        when:
            def response = this.postRole(new RoleDto(name: "Accountant", permissions: permissions))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == message
        where:
            permissions                                             || message
            [p("CLIENT", "EDIT")]                                   || "Action EDIT on CLIENT needs action READ on CLIENT."
            [p("PROJECT_FINANCE", "READ")]                          || "Action READ on PROJECT_FINANCE needs action READ on PROJECT."
            [p("PROJECT", "EDIT"), p("PROJECT", "READ"),
             p("PROJECT_FINANCE", "EDIT")]                          || "Action EDIT on PROJECT_FINANCE needs action READ on PROJECT_FINANCE."
    }

    def "Creating role with a permission missing its action should return code 400 and error message"() {
        when:
            def response = this.postRole(new RoleDto(name: "Accountant",
                    permissions: [new PermissionDto(resource: PermissionResourceDto.CLIENT)]))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "Role permissions have to be provided."
    }

    def "Getting available permissions should return every resource with its actions"() {
        when:
            def response = this.mockMvc.perform(MockMvcRequestBuilders.get("/permissions")).andReturn().response
        then:
            response.status == HttpStatus.OK.value()
            List<ResourcePermissionsDto> resources = MAPPER.readValue(response.contentAsString,
                    new TypeReference<List<ResourcePermissionsDto>>() {})
            resources*.resource == PermissionResourceDto.values() as List
            resources.find { it.resource == PermissionResourceDto.CLIENT }.actions == PermissionActionDto.values() as List
            resources.find { it.resource == PermissionResourceDto.CONTRACT }.actions ==
                    [PermissionActionDto.READ, PermissionActionDto.EDIT]
            resources.find { it.resource == PermissionResourceDto.FINANCIAL_REPORT }.actions ==
                    [PermissionActionDto.READ]
            resources.find { it.resource == PermissionResourceDto.PROJECT_FINANCE }.requiredResource ==
                    PermissionResourceDto.PROJECT
            resources.findAll { it.requiredResource != null }*.resource == [PermissionResourceDto.PROJECT_FINANCE]
    }

    def "Updating role should return code 200 and role with new name and permissions"() {
        given:
            def role = this.createRole("Accountant", [p("COMPANY_COST", "READ")])
        when:
            def response = this.putRole(role.id, new RoleDto(name: "Bookkeeper",
                    permissions: [p("COMPANY_COST", "READ"), p("COMPANY_COST", "EDIT")]))
        then:
            response.status == HttpStatus.OK.value()
            MAPPER.readValue(response.contentAsString, RoleDto).name == "Bookkeeper"
        and:
            def loaded = this.getRole(role.id)
            loaded.name == "Bookkeeper"
            loaded.permissions == [p("COMPANY_COST", "READ"), p("COMPANY_COST", "EDIT")]
    }

    def "Updating role to its own name with different letter case should be allowed"() {
        given:
            def role = this.createRole("Accountant", [])
        when:
            def response = this.putRole(role.id, new RoleDto(name: "ACCOUNTANT", permissions: []))
        then:
            response.status == HttpStatus.OK.value()
    }

    def "Updating administrator role should return code 400 and keep it unchanged"() {
        given:
            def administrator = this.findRole(ADMINISTRATOR)
        when:
            def response = this.putRole(administrator.id, new RoleDto(name: "Boss", permissions: []))
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message ==
                    "The administrator role always has all permissions and cannot be changed or removed."
            this.getRole(administrator.id).name == ADMINISTRATOR
    }

    def "Updating not existing role should return code 404 and error message"() {
        when:
            def response = this.putRole(NOT_EXISTING_ROLE_ID, new RoleDto(name: "Any", permissions: []))
        then:
            response.status == HttpStatus.NOT_FOUND.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "Role with id 10,000 does not exist."
    }

    def "Deleting role nobody has should return code 200 and remove it"() {
        given:
            def role = this.createRole("Accountant", [p("COMPANY_COST", "READ")])
        when:
            def response = this.deleteRole(role.id)
        then:
            response.status == HttpStatus.OK.value()
            this.roleRepository.findById(role.id).isEmpty()
    }

    def "Deleting role given to a user should return code 400 and keep it"() {
        given:
            def role = this.createRole("Accountant", [p("COMPANY_COST", "READ")])
            def user = new User("Role user", "role@example.com", "auth0|role-test")
            user.assignRoles([role.id])
            this.userRepository.saveAndFlush(user)
        when:
            def response = this.deleteRole(role.id)
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            MAPPER.readValue(response.contentAsString, ErrorMessage).message == "Role given to users cannot be removed."
            this.getRole(role.id).numberOfUsers == 1
    }

    def "Deleting administrator role should return code 400 and keep it"() {
        given:
            def administrator = this.findRole(ADMINISTRATOR)
        when:
            def response = this.deleteRole(administrator.id)
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
            this.roleRepository.findById(administrator.id).isPresent()
    }

    def "Getting not existing role should return code 404"() {
        expect:
            this.mockMvc.perform(MockMvcRequestBuilders.get("$ROLES_URI/$NOT_EXISTING_ROLE_ID")).andReturn()
                    .response.status == HttpStatus.NOT_FOUND.value()
    }

    private static PermissionDto p(String resource, String action) {
        return new PermissionDto(resource: PermissionResourceDto.valueOf(resource),
                action: PermissionActionDto.valueOf(action))
    }

    private def postRole(RoleDto roleDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(ROLES_URI)
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(roleDto))).andReturn().response
    }

    private def putRole(Long roleId, RoleDto roleDto) {
        return this.mockMvc.perform(MockMvcRequestBuilders.put("$ROLES_URI/$roleId")
                .header("Content-Type", "application/json")
                .content(MAPPER.writeValueAsString(roleDto))).andReturn().response
    }

    private def deleteRole(Long roleId) {
        return this.mockMvc.perform(MockMvcRequestBuilders.delete("$ROLES_URI/$roleId")).andReturn().response
    }

    private RoleDto getRole(Long roleId) {
        return MAPPER.readValue(this.mockMvc.perform(MockMvcRequestBuilders.get("$ROLES_URI/$roleId")).andReturn()
                .response.contentAsString, RoleDto)
    }

    private RoleDto createRole(String name, List<PermissionDto> permissions) {
        return MAPPER.readValue(this.postRole(new RoleDto(name: name, permissions: permissions)).contentAsString,
                RoleDto)
    }

    private RoleDto findRole(String name) {
        def role = this.roleRepository.findAll().find { it.name == name }
        return new RoleDto(id: role.id, name: role.name)
    }
}
