package com.arturjarosz.task.utils

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto
import com.arturjarosz.task.architect.application.dto.ArchitectDto
import com.arturjarosz.task.client.application.dto.ClientDto
import com.arturjarosz.task.project.application.dto.ProjectCreateDto
import com.arturjarosz.task.project.application.dto.ProjectDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class TestsHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper()

    static ArchitectDto createArchitect(ArchitectBasicDto architectBasicDto, String architectUri, MockMvc mockMvc) {
        String architectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(architectBasicDto)
        def architectResponse = mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(architectUri))
                        .header("Content-Type", "application/json")
                        .content(architectRequestBody)
        ).andReturn().response.contentAsString
        return MAPPER.readValue(architectResponse, ArchitectDto.class)
    }

    static ClientDto createClient(ClientDto clientDto, String clientUri, MockMvc mockMvc) {
        String clientRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(clientDto)
        def clientResponse = mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(clientUri))
                        .header("Content-Type", "application/json")
                        .content(clientRequestBody)
        ).andReturn().response.contentAsString
        return MAPPER.readValue(clientResponse, ClientDto.class)
    }

    static ProjectDto createProject(ProjectCreateDto projectDto, String projectUri, MockMvc mockMvc) {
        String projectRequestBody = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(projectDto)
        def projectResponse = mockMvc.perform(
                MockMvcRequestBuilders.post(URI.create(projectUri))
                        .header("Content-Type", "application/json")
                        .content(projectRequestBody)
        ).andReturn().response
        return MAPPER.readValue(projectResponse.contentAsString, ProjectDto.class)
    }
}
