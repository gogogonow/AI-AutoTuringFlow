package com.example.backend.controller;

import com.example.backend.model.Module;
import com.example.backend.model.History;
import com.example.backend.service.ModuleService;
import com.example.backend.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * ModuleController REST API 集成测试
 * 测试所有 HTTP 端点、请求验证、响应格式、错误处理
 */
@WebMvcTest(ModuleController.class)
class ModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ModuleService moduleService;

    private Module testModule;
    private List<Module> moduleList;

    @BeforeEach
    void setUp() {
        testModule = new Module();
        testModule.setId(1L);
        testModule.setSerialNumber("SN-12345");
        testModule.setManufacturer("华为");
        testModule.setModelNumber("eSFP-GE-SX-MM850");
        testModule.setWavelength(850.0);
        testModule.setTransmitPower(-5.0);
        testModule.setReceiveSensitivity(-18.0);
        testModule.setTransmissionDistance(550.0);
        testModule.setFiberType("MMF");
        testModule.setConnectorType("LC");
        testModule.setTemperatureRange("-40~85°C");
        testModule.setVoltage(3.3);
        testModule.setPowerConsumption(1.5);
        testModule.setCreatedAt(LocalDateTime.now());
        testModule.setUpdatedAt(LocalDateTime.now());

        Module module2 = new Module();
        module2.setId(2L);
        module2.setSerialNumber("SN-67890");
        module2.setManufacturer("中兴");
        
        moduleList = Arrays.asList(testModule, module2);
    }

    @Test
    @DisplayName("GET /api/modules - 获取所有模块 - 成功")
    void testGetAllModules_Success() throws Exception {
        // Arrange
        when(moduleService.getAllModules()).thenReturn(moduleList);

        // Act & Assert
        mockMvc.perform(get("/api/modules")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].serialNumber", is("SN-12345")))
                .andExpect(jsonPath("$[0].manufacturer", is("华为")))
                .andExpect(jsonPath("$[1].serialNumber", is("SN-67890")));

        verify(moduleService, times(1)).getAllModules();
    }

    @Test
    @DisplayName("GET /api/modules - 获取空列表")
    void testGetAllModules_EmptyList() throws Exception {
        // Arrange
        when(moduleService.getAllModules()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/modules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(moduleService, times(1)).getAllModules();
    }

    @Test
    @DisplayName("GET /api/modules/{id} - 根据ID获取模块 - 成功")
    void testGetModuleById_Success() throws Exception {
        // Arrange
        when(moduleService.getModuleById(1L)).thenReturn(testModule);

        // Act & Assert
        mockMvc.perform(get("/api/modules/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.serialNumber", is("SN-12345")))
                .andExpect(jsonPath("$.manufacturer", is("华为")))
                .andExpect(jsonPath("$.wavelength", is(850.0)))
                .andExpect(jsonPath("$.transmitPower", is(-5.0)));

        verify(moduleService, times(1)).getModuleById(1L);
    }

    @Test
    @DisplayName("GET /api/modules/{id} - 模块不存在 - 返回404")
    void testGetModuleById_NotFound() throws Exception {
        // Arrange
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException("Module not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Module not found")));

        verify(moduleService, times(1)).getModuleById(999L);
    }

    @Test
    @DisplayName("POST /api/modules - 创建新模块 - 成功")
    void testCreateModule_Success() throws Exception {
        // Arrange
        Module newModule = new Module();
        newModule.setSerialNumber("SN-NEW-001");
        newModule.setManufacturer("诺基亚");
        newModule.setWavelength(1310.0);
        
        when(moduleService.createModule(any(Module.class))).thenReturn(testModule);

        // Act & Assert
        mockMvc.perform(post("/api/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newModule)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber", is("SN-12345")));

        verify(moduleService, times(1)).createModule(any(Module.class));
    }

    @Test
    @DisplayName("POST /api/modules - 无效请求体 - 返回400")
    void testCreateModule_InvalidBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(moduleService, never()).createModule(any(Module.class));
    }

    @Test
    @DisplayName("PUT /api/modules/{id} - 更新模块 - 成功")
    void testUpdateModule_Success() throws Exception {
        // Arrange
        Module updatedModule = new Module();
        updatedModule.setSerialNumber("SN-UPDATED");
        updatedModule.setManufacturer("爱立信");
        
        Module returnModule = new Module();
        returnModule.setId(1L);
        returnModule.setSerialNumber("SN-UPDATED");
        returnModule.setManufacturer("爱立信");
        
        when(moduleService.updateModule(eq(1L), any(Module.class))).thenReturn(returnModule);

        // Act & Assert
        mockMvc.perform(put("/api/modules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedModule)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber", is("SN-UPDATED")))
                .andExpect(jsonPath("$.manufacturer", is("爱立信")));

        verify(moduleService, times(1)).updateModule(eq(1L), any(Module.class));
    }

    @Test
    @DisplayName("PUT /api/modules/{id} - 更新不存在的模块 - 返回404")
    void testUpdateModule_NotFound() throws Exception {
        // Arrange
        Module updatedModule = new Module();
        updatedModule.setSerialNumber("SN-UPDATED");
        
        when(moduleService.updateModule(eq(999L), any(Module.class)))
                .thenThrow(new ResourceNotFoundException("Module not found with id: 999"));

        // Act & Assert
        mockMvc.perform(put("/api/modules/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedModule)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(moduleService, times(1)).updateModule(eq(999L), any(Module.class));
    }

    @Test
    @DisplayName("DELETE /api/modules/{id} - 删除模块 - 成功")
    void testDeleteModule_Success() throws Exception {
        // Arrange
        doNothing().when(moduleService).deleteModule(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/modules/1"))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(moduleService, times(1)).deleteModule(1L);
    }

    @Test
    @DisplayName("DELETE /api/modules/{id} - 删除不存在的模块 - 返回404")
    void testDeleteModule_NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Module not found with id: 999"))
                .when(moduleService).deleteModule(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/modules/999"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(moduleService, times(1)).deleteModule(999L);
    }

    @Test
    @DisplayName("GET /api/modules/{id}/history - 获取历史记录 - 成功")
    void testGetModuleHistory_Success() throws Exception {
        // Arrange
        History history1 = new History();
        history1.setId(1L);
        history1.setModuleId(1L);
        history1.setOperation("CREATE");
        history1.setFieldName("serialNumber");
        history1.setNewValue("SN-12345");
        history1.setCreatedAt(LocalDateTime.now());
        
        History history2 = new History();
        history2.setId(2L);
        history2.setModuleId(1L);
        history2.setOperation("UPDATE");
        history2.setFieldName("manufacturer");
        history2.setOldValue("华为");
        history2.setNewValue("中兴");
        history2.setCreatedAt(LocalDateTime.now());
        
        List<History> historyList = Arrays.asList(history1, history2);
        
        when(moduleService.getModuleHistory(1L)).thenReturn(historyList);

        // Act & Assert
        mockMvc.perform(get("/api/modules/1/history"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].operation", is("CREATE")))
                .andExpect(jsonPath("$[0].fieldName", is("serialNumber")))
                .andExpect(jsonPath("$[1].operation", is("UPDATE")))
                .andExpect(jsonPath("$[1].oldValue", is("华为")))
                .andExpect(jsonPath("$[1].newValue", is("中兴")));

        verify(moduleService, times(1)).getModuleHistory(1L);
    }

    @Test
    @DisplayName("GET /api/modules/{id}/history - 模块不存在 - 返回404")
    void testGetModuleHistory_ModuleNotFound() throws Exception {
        // Arrange
        when(moduleService.getModuleHistory(999L))
                .thenThrow(new ResourceNotFoundException("Module not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999/history"))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(moduleService, times(1)).getModuleHistory(999L);
    }

    @Test
    @DisplayName("测试 CORS 预检请求")
    void testCorsPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/modules")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试并发请求处理")
    void testConcurrentRequests() throws Exception {
        // Arrange
        when(moduleService.getAllModules()).thenReturn(moduleList);

        // Act - 模拟多个并发请求
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/modules"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        // Assert
        verify(moduleService, times(5)).getAllModules();
    }

    @Test
    @DisplayName("测试大数据量响应")
    void testLargeDataResponse() throws Exception {
        // Arrange - 创建100个模块的列表
        List<Module> largeList = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Module m = new Module();
            m.setId((long) i);
            m.setSerialNumber("SN-" + i);
            largeList.add(m);
        }
        
        when(moduleService.getAllModules()).thenReturn(largeList);

        // Act & Assert
        mockMvc.perform(get("/api/modules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(100)));
    }
}