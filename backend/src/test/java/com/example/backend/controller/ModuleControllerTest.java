package com.example.backend.controller;

import com.example.backend.dto.ModuleDto;
import com.example.backend.dto.StatusChangeRequest;
import com.example.backend.model.ModuleStatus;
import com.example.backend.service.ModuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModuleController.class)
class ModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModuleService moduleService;

    private ObjectMapper objectMapper;
    private ModuleDto testModuleDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testModuleDto = new ModuleDto();
        testModuleDto.setId(1L);
        testModuleDto.setSerialNumber("TEST001");
        testModuleDto.setModel("SFP-10G-SR");
        testModuleDto.setVendor("Cisco");
        testModuleDto.setSpeed("10G");
        testModuleDto.setStatus(ModuleStatus.IN_STOCK);
        testModuleDto.setInboundTime(LocalDateTime.now());
    }

    @Test
    void testCreateModule_Success() throws Exception {
        when(moduleService.createModule(any(ModuleDto.class))).thenReturn(testModuleDto);

        mockMvc.perform(post("/api/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testModuleDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.serialNumber").value("TEST001"))
            .andExpect(jsonPath("$.model").value("SFP-10G-SR"));
    }

    @Test
    void testCreateModule_ValidationFail() throws Exception {
        ModuleDto invalidDto = new ModuleDto();
        // Missing required fields

        mockMvc.perform(post("/api/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testGetModuleById_Found() throws Exception {
        when(moduleService.getModuleById(1L)).thenReturn(testModuleDto);

        mockMvc.perform(get("/api/modules/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.serialNumber").value("TEST001"));
    }

    @Test
    void testGetModuleById_NotFound() throws Exception {
        when(moduleService.getModuleById(999L))
            .thenThrow(new IllegalArgumentException("光模块不存在: ID=999"));

        mockMvc.perform(get("/api/modules/999"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateModule_Success() throws Exception {
        when(moduleService.updateModule(anyLong(), any(ModuleDto.class))).thenReturn(testModuleDto);

        mockMvc.perform(put("/api/modules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testModuleDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.serialNumber").value("TEST001"));
    }

    @Test
    void testDeleteModule_Success() throws Exception {
        mockMvc.perform(delete("/api/modules/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void testGetModules_Paginated() throws Exception {
        List<ModuleDto> modules = Arrays.asList(testModuleDto);
        Page<ModuleDto> modulePage = new PageImpl<>(modules);

        when(moduleService.getModules(any())).thenReturn(modulePage);

        mockMvc.perform(get("/api/modules")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].serialNumber").value("TEST001"));
    }

    @Test
    void testGetModules_WithFilters() throws Exception {
        List<ModuleDto> modules = Arrays.asList(testModuleDto);
        Page<ModuleDto> modulePage = new PageImpl<>(modules);

        when(moduleService.searchModules(any(), any(), any(), any(), any(), any())).thenReturn(modulePage);

        mockMvc.perform(get("/api/modules")
                .param("serialNumber", "TEST")
                .param("status", "IN_STOCK")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testChangeStatus_Success() throws Exception {
        StatusChangeRequest request = new StatusChangeRequest();
        request.setAction("DEPLOY");
        request.setOperator("admin");
        request.setRemark("Deploy to device");

        testModuleDto.setStatus(ModuleStatus.DEPLOYED);
        when(moduleService.changeStatus(anyLong(), any(StatusChangeRequest.class))).thenReturn(testModuleDto);

        mockMvc.perform(post("/api/modules/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DEPLOYED"));
    }

    @Test
    void testGetStatusStatistics() throws Exception {
        Map<ModuleStatus, Long> stats = new HashMap<>();
        stats.put(ModuleStatus.IN_STOCK, 10L);
        stats.put(ModuleStatus.DEPLOYED, 5L);

        when(moduleService.getStatusStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/modules/statistics/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.IN_STOCK").value(10))
            .andExpect(jsonPath("$.DEPLOYED").value(5));
    }

    @Test
    void testGetVendorStatistics() throws Exception {
        Map<String, Long> stats = new HashMap<>();
        stats.put("Cisco", 15L);
        stats.put("Huawei", 10L);

        when(moduleService.getVendorStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/modules/statistics/vendor"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.Cisco").value(15))
            .andExpect(jsonPath("$.Huawei").value(10));
    }
}
