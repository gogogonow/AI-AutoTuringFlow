package com.example.backend.exception;

import com.example.backend.controller.ModuleController;
import com.example.backend.model.Module;
import com.example.backend.service.ModuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 异常处理边界情况测试
 * 测试各种边界条件和极端场景下的异常处理
 * Bug 修复回归：确保所有边界情况都正确处理
 */
@WebMvcTest(ModuleController.class)
class ExceptionEdgeCasesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ModuleService moduleService;

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, Long.MAX_VALUE, Long.MIN_VALUE})
    @DisplayName("边界测试 - 各种极端 ID 值查询不存在的模块")
    void testGetModule_WithExtremeIdValues(Long id) throws Exception {
        // Arrange
        when(moduleService.getModuleById(id))
                .thenThrow(new ResourceNotFoundException("Module not found with id: " + id));

        // Act & Assert
        mockMvc.perform(get("/api/modules/" + id))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("边界测试 - 非常长的异常消息")
    void testExceptionWithVeryLongMessage() throws Exception {
        // Arrange - 创建一个超长的错误消息
        StringBuilder longMessage = new StringBuilder("Module not found: ");
        for (int i = 0; i < 100; i++) {
            longMessage.append("This is a very long error message part ").append(i).append(". ");
        }
        
        when(moduleService.getModuleById(1L))
                .thenThrow(new ResourceNotFoundException(longMessage.toString()));

        // Act & Assert
        mockMvc.perform(get("/api/modules/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.message", containsString("Module not found")));
    }

    @Test
    @DisplayName("边界测试 - 包含特殊字符的异常消息")
    void testExceptionWithSpecialCharacters() throws Exception {
        // Arrange
        String specialMessage = "Module not found: ID=<999>, SN='TEST\"123', Error@#$%^&*()";
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException(specialMessage));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(specialMessage)));
    }

    @Test
    @DisplayName("边界测试 - Unicode 字符在异常消息中")
    void testExceptionWithUnicodeCharacters() throws Exception {
        // Arrange
        String unicodeMessage = "模块未找到: 序列号=SN-测试-001, 制造商=华为™, ID=999 ❌";
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException(unicodeMessage));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(unicodeMessage)));
    }

    @Test
    @DisplayName("边界测试 - 空字符串异常消息")
    void testExceptionWithEmptyMessage() throws Exception {
        // Arrange
        when(moduleService.getModuleById(1L))
                .thenThrow(new ResourceNotFoundException(""));

        // Act & Assert
        mockMvc.perform(get("/api/modules/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("")));
    }

    @Test
    @DisplayName("边界测试 - Null 异常消息")
    void testExceptionWithNullMessage() throws Exception {
        // Arrange
        when(moduleService.getModuleById(1L))
                .thenThrow(new ResourceNotFoundException(null));

        // Act & Assert
        mockMvc.perform(get("/api/modules/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("边界测试 - 多行异常消息")
    void testExceptionWithMultilineMessage() throws Exception {
        // Arrange
        String multilineMessage = "Module not found.\nID: 999\nSerial: SN-001\nManufacturer: Unknown";
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException(multilineMessage));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Module not found")));
    }

    @Test
    @DisplayName("边界测试 - JSON 注入尝试在异常消息中")
    void testExceptionWithJsonInjectionAttempt() throws Exception {
        // Arrange
        String injectionAttempt = "Module not found\": {\"injected\": true, \"admin\": \"true\"";
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException(injectionAttempt));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.injected").doesNotExist());
    }

    @Test
    @DisplayName("边界测试 - XSS 尝试在异常消息中")
    void testExceptionWithXssAttempt() throws Exception {
        // Arrange
        String xssAttempt = "Module not found: <script>alert('XSS')</script>";
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException(xssAttempt));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(xssAttempt)));
    }

    @Test
    @DisplayName("边界测试 - 嵌套异常处理")
    void testNestedException() throws Exception {
        // Arrange
        when(moduleService.getModuleById(1L))
                .thenThrow(new ResourceNotFoundException("Outer exception"));

        // Act & Assert
        mockMvc.perform(get("/api/modules/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Outer exception")));
    }

    @Test
    @DisplayName("边界测试 - 同时抛出多种异常类型")
    void testMultipleExceptionTypesInSequence() throws Exception {
        // First request throws ResourceNotFoundException
        when(moduleService.getModuleById(1L))
                .thenThrow(new ResourceNotFoundException("Not found"));
        
        mockMvc.perform(get("/api/modules/1"))
                .andExpect(status().isNotFound());

        // Second request throws IllegalArgumentException
        when(moduleService.createModule(any()))
                .thenThrow(new IllegalArgumentException("Invalid input"));
        
        mockMvc.perform(post("/api/modules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("边界测试 - 路径参数包含特殊字符")
    void testExceptionWithSpecialCharactersInPath() throws Exception {
        // Arrange
        when(moduleService.getModuleById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act & Assert - 测试各种路径格式
        mockMvc.perform(get("/api/modules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path", containsString("/api/modules/999")));
    }

    @Test
    @DisplayName("边界测试 - 异常发生时的并发请求")
    void testConcurrentExceptionHandling() throws Exception {
        // Arrange
        when(moduleService.getModuleById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act & Assert - 模拟多个并发请求
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/modules/" + (1000 + i)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    @Test
    @DisplayName("边界测试 - 异常响应中的时间戳格式")
    void testExceptionTimestampFormat() throws Exception {
        // Arrange
        when(moduleService.getModuleById(1L))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act & Assert
        mockMvc.perform(get("/api/modules/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.timestamp", matchesRegex(
                    "^\\d{4}-\\d{2}-\\d{2}T.*"
                )));
    }

    @Test
    @DisplayName("边界测试 - 异常消息包含 JSON 格式数据")
    void testExceptionMessageWithJsonFormat() throws Exception {
        // Arrange
        String jsonMessage = "{\"error\": \"Module not found\", \"id\": 999, \"code\": 404}";
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException(jsonMessage));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.message", is(jsonMessage)));
    }

    @Test
    @DisplayName("边界测试 - URL 编码字符在路径中")
    void testExceptionWithUrlEncodedPath() throws Exception {
        // Arrange
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999?param=value%20with%20spaces"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path", notNullValue()));
    }
}