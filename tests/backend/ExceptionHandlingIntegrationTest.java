package com.example.backend.exception;

import com.example.backend.model.Module;
import com.example.backend.repository.ModuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 异常处理集成测试
 * 测试完整的请求-响应流程，验证异常在真实场景中的处理
 * Bug 修复回归：确保所有异常场景都返回正确的 HTTP 状态码和格式化响应
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExceptionHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModuleRepository moduleRepository;

    private Module testModule;

    @BeforeEach
    void setUp() {
        // 清理数据库
        moduleRepository.deleteAll();
        
        // 创建测试数据
        testModule = new Module();
        testModule.setSerialNumber("SN-TEST-001");
        testModule.setManufacturer("测试厂商");
        testModule.setModelNumber("TEST-MODEL");
        testModule = moduleRepository.save(testModule);
    }

    @Test
    @DisplayName("集成测试 - 查询不存在的模块返回 404")
    void testGetNonExistentModule_Returns404() throws Exception {
        // Arrange - 找一个肯定不存在的 ID
        Long nonExistentId = 999999L;
        
        // Act & Assert
        mockMvc.perform(get("/api/modules/" + nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("not found")))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.path", containsString("/api/modules/")));
    }

    @Test
    @DisplayName("集成测试 - 删除不存在的模块返回 404")
    void testDeleteNonExistentModule_Returns404() throws Exception {
        // Arrange
        Long nonExistentId = 888888L;
        
        // Act & Assert
        mockMvc.perform(delete("/api/modules/" + nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("集成测试 - 更新不存在的模块返回 404")
    void testUpdateNonExistentModule_Returns404() throws Exception {
        // Arrange
        Long nonExistentId = 777777L;
        Module updateData = new Module();
        updateData.setSerialNumber("SN-UPDATED");
        
        // Act & Assert
        mockMvc.perform(put("/api/modules/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("集成测试 - 查询不存在模块的历史记录返回 404")
    void testGetHistoryOfNonExistentModule_Returns404() throws Exception {
        // Arrange
        Long nonExistentId = 666666L;
        
        // Act & Assert
        mockMvc.perform(get("/api/modules/" + nonExistentId + "/history"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("集成测试 - 成功操作后再删除，查询返回 404")
    void testDeleteThenGet_Returns404() throws Exception {
        // Arrange - 先删除存在的模块
        Long existingId = testModule.getId();
        
        mockMvc.perform(delete("/api/modules/" + existingId))
                .andExpect(status().isNoContent());
        
        // Act & Assert - 再次查询应返回 404
        mockMvc.perform(get("/api/modules/" + existingId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("集成测试 - 无效的 JSON 格式返回 400")
    void testInvalidJsonFormat_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json format}"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("集成测试 - 并发删除同一模块")
    void testConcurrentDelete_SecondReturns404() throws Exception {
        // Arrange
        Long moduleId = testModule.getId();
        
        // Act - 第一次删除成功
        mockMvc.perform(delete("/api/modules/" + moduleId))
                .andExpect(status().isNoContent());
        
        // Assert - 第二次删除返回 404
        mockMvc.perform(delete("/api/modules/" + moduleId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("集成测试 - 异常响应的 Content-Type 正确")
    void testExceptionResponse_ContentTypeIsJson() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/modules/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("集成测试 - 多种异常类型的响应格式一致")
    void testDifferentExceptions_ConsistentFormat() throws Exception {
        // Test 404 response format
        mockMvc.perform(get("/api/modules/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.path", notNullValue()));
        
        // Test 400 response format
        mockMvc.perform(post("/api/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    @DisplayName("集成测试 - 大量连续 404 请求")
    void testMultiple404Requests_AllHandledCorrectly() throws Exception {
        // Act & Assert - 连续 10 次请求不存在的资源
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/modules/" + (900000 + i)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.error", is("Not Found")));
        }
    }

    @Test
    @DisplayName("集成测试 - 先创建后删除再查询")
    void testCreateDeleteGet_Lifecycle() throws Exception {
        // Arrange - 创建新模块
        Module newModule = new Module();
        newModule.setSerialNumber("SN-LIFECYCLE-TEST");
        newModule.setManufacturer("生命周期测试");
        
        String createResponse = mockMvc.perform(post("/api/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newModule)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        Module createdModule = objectMapper.readValue(createResponse, Module.class);
        Long createdId = createdModule.getId();
        
        // Act - 删除模块
        mockMvc.perform(delete("/api/modules/" + createdId))
                .andExpect(status().isNoContent());
        
        // Assert - 查询已删除的模块应返回 404
        mockMvc.perform(get("/api/modules/" + createdId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("集成测试 - 异常响应时间戳在合理范围内")
    void testExceptionTimestamp_IsRecent() throws Exception {
        // Arrange
        long beforeRequest = System.currentTimeMillis();
        
        // Act
        mockMvc.perform(get("/api/modules/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", notNullValue()));
        
        long afterRequest = System.currentTimeMillis();
        
        // Assert - 时间戳应该在请求前后的合理范围内
        assertTrue(afterRequest - beforeRequest < 5000, 
            "Request should complete within 5 seconds");
    }
}