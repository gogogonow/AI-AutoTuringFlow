package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.ModuleDto;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.model.Role;
import com.example.backend.repository.ModuleRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Role-Based Authorization Tests
 * Tests that OWNER role can perform all operations and READER role can only read
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    private String ownerToken;
    private String readerToken;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        userRepository.deleteAll();
        roleRepository.deleteAll();
        moduleRepository.deleteAll();

        // Create roles
        Role ownerRole = new Role();
        ownerRole.setName("OWNER");
        ownerRole.setDescription("Owner role with full permissions");
        roleRepository.save(ownerRole);

        Role readerRole = new Role();
        readerRole.setName("READER");
        readerRole.setDescription("Reader role with read-only permissions");
        roleRepository.save(readerRole);

        // Register OWNER user
        RegisterRequest ownerRequest = new RegisterRequest();
        ownerRequest.setUsername("owner");
        ownerRequest.setPassword("password123");
        ownerRequest.setEmail("owner@example.com");
        ownerRequest.setRole("OWNER");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)));

        // Register READER user
        RegisterRequest readerRequest = new RegisterRequest();
        readerRequest.setUsername("reader");
        readerRequest.setPassword("password123");
        readerRequest.setEmail("reader@example.com");
        readerRequest.setRole("READER");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readerRequest)));

        // Login as OWNER
        LoginRequest ownerLogin = new LoginRequest();
        ownerLogin.setUsername("owner");
        ownerLogin.setPassword("password123");

        String ownerResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerLogin)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ownerToken = objectMapper.readTree(ownerResponse).get("token").asText();

        // Login as READER
        LoginRequest readerLogin = new LoginRequest();
        readerLogin.setUsername("reader");
        readerLogin.setPassword("password123");

        String readerResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(readerLogin)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        readerToken = objectMapper.readTree(readerResponse).get("token").asText();
    }

    @Test
    void testOwnerCanCreateModule() throws Exception {
        ModuleDto moduleDto = new ModuleDto();
        moduleDto.setSerialNumber("TEST-001");
        moduleDto.setModel("TEST-MODEL");
        moduleDto.setInboundTime(LocalDateTime.now());

        mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moduleDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testReaderCannotCreateModule() throws Exception {
        ModuleDto moduleDto = new ModuleDto();
        moduleDto.setSerialNumber("TEST-002");
        moduleDto.setModel("TEST-MODEL");
        moduleDto.setInboundTime(LocalDateTime.now());

        mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moduleDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testOwnerCanUpdateModule() throws Exception {
        // Create module as owner
        ModuleDto createDto = new ModuleDto();
        createDto.setSerialNumber("TEST-003");
        createDto.setModel("TEST-MODEL");
        createDto.setInboundTime(LocalDateTime.now());

        String createResponse = mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long moduleId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update module as owner
        ModuleDto updateDto = new ModuleDto();
        updateDto.setSerialNumber("TEST-003");
        updateDto.setModel("TEST-MODEL-UPDATED");
        updateDto.setInboundTime(LocalDateTime.now());

        mockMvc.perform(put("/api/modules/" + moduleId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testReaderCannotUpdateModule() throws Exception {
        // Create module as owner
        ModuleDto createDto = new ModuleDto();
        createDto.setSerialNumber("TEST-004");
        createDto.setModel("TEST-MODEL");
        createDto.setInboundTime(LocalDateTime.now());

        String createResponse = mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long moduleId = objectMapper.readTree(createResponse).get("id").asLong();

        // Try to update module as reader
        ModuleDto updateDto = new ModuleDto();
        updateDto.setSerialNumber("TEST-004");
        updateDto.setModel("TEST-MODEL-UPDATED");
        updateDto.setInboundTime(LocalDateTime.now());

        mockMvc.perform(put("/api/modules/" + moduleId)
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testOwnerCanDeleteModule() throws Exception {
        // Create module as owner
        ModuleDto createDto = new ModuleDto();
        createDto.setSerialNumber("TEST-005");
        createDto.setModel("TEST-MODEL");
        createDto.setInboundTime(LocalDateTime.now());

        String createResponse = mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long moduleId = objectMapper.readTree(createResponse).get("id").asLong();

        // Delete module as owner
        mockMvc.perform(delete("/api/modules/" + moduleId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void testReaderCannotDeleteModule() throws Exception {
        // Create module as owner
        ModuleDto createDto = new ModuleDto();
        createDto.setSerialNumber("TEST-006");
        createDto.setModel("TEST-MODEL");
        createDto.setInboundTime(LocalDateTime.now());

        String createResponse = mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long moduleId = objectMapper.readTree(createResponse).get("id").asLong();

        // Try to delete module as reader
        mockMvc.perform(delete("/api/modules/" + moduleId)
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testBothRolesCanReadModules() throws Exception {
        // Owner can read
        mockMvc.perform(get("/api/modules")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Reader can read
        mockMvc.perform(get("/api/modules")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk());
    }

    @Test
    void testUnauthenticatedCannotAccessProtectedEndpoints() throws Exception {
        // Without token
        mockMvc.perform(get("/api/modules"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testInvalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/modules")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
}
