package com.arturjarosz.task.utils

import com.arturjarosz.task.dto.ArchitectDto
import com.arturjarosz.task.dto.ClientDto
import com.arturjarosz.task.dto.ProjectCreateDto
import com.arturjarosz.task.dto.ProjectDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class TestsHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper()

    static ArchitectDto createArchitect(ArchitectDto architectDto, String architectUri, MockMvc mockMvc) {
        def architectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(architectDto)
        def architectResponse = mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(architectUri))
                        .header("Content-Type", "application/json")
                        .content(architectRequestBody)
        ).andReturn().response.contentAsString
        return MAPPER.readValue(architectResponse, ArchitectDto)
    }

    static ClientDto createClient(ClientDto clientDto, String clientUri, MockMvc mockMvc) {
        def clientRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(clientDto)
        def clientResponse = mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(clientUri))
                        .header("Content-Type", "application/json")
                        .content(clientRequestBody)
        ).andReturn().response.contentAsString
        return MAPPER.readValue(clientResponse, ClientDto)
    }

    static ProjectDto createProject(ProjectCreateDto projectDto, String projectUri, MockMvc mockMvc) {
        def projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(projectDto)
        def projectResponse = mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(projectUri))
                        .header("Content-Type", "application/json")
                        .content(projectRequestBody)
        ).andReturn().response
        return MAPPER.readValue(projectResponse.contentAsString, ProjectDto)
    }

}
