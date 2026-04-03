package com.example.backend.exception;

import com.example.backend.controller.ModuleController;
import com.example.backend.service.ModuleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GlobalExceptionHandler 回归测试
 * 测试全局异常处理器是否正确捕获并格式化各种异常
 * Bug 修复：确保 ResourceNotFoundException 返回 404 而不是 500
 * Bug 修复：确保异常响应格式统一，包含 timestamp, status, error, message, path
 */
@WebMvcTest(ModuleController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModuleService moduleService;

    @Test
    @DisplayName("Bug 回归测试 - ResourceNotFoundException 返回 404 而不是 500")
    void testResourceNotFoundException_Returns404NotFound() throws Exception {
        // Arrange - 模拟服务层抛出 ResourceNotFoundException
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException("Module not found with id: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999"))
                .andDo(print())
                .andExpect(status().isNotFound()) // 必须是 404，不是 500
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Module not found")))
                .andExpect(jsonPath("$.path", containsString("/api/modules/999")));
    }

    @Test
    @DisplayName("Bug 回归测试 - 删除不存在资源时返回 404")
    void testDeleteNonExistentResource_Returns404() throws Exception {
        // Arrange
        when(moduleService.deleteModule(888L))
                .thenThrow(new ResourceNotFoundException("Module not found with id: 888"));

        // Act & Assert
        mockMvc.perform(delete("/api/modules/888"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("Bug 回归测试 - 更新不存在资源时返回 404")
    void testUpdateNonExistentResource_Returns404() throws Exception {
        // Arrange
        when(moduleService.updateModule(anyLong(), any()))
                .thenThrow(new ResourceNotFoundException("Module not found with id: 777"));

        // Act & Assert
        mockMvc.perform(put("/api/modules/777")
                .contentType("application/json")
                .content("{\"serialNumber\":\"SN-TEST\"}"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("Bug 回归测试 - IllegalArgumentException 返回 400")
    void testIllegalArgumentException_Returns400BadRequest() throws Exception {
        // Arrange
        when(moduleService.createModule(null))
                .thenThrow(new IllegalArgumentException("Module cannot be null"));

        // Act & Assert
        mockMvc.perform(post("/api/modules")
                .contentType("application/json")
                .content("null"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.path", notNullValue()));
    }

    @Test
    @DisplayName("Bug 回归测试 - 异常响应格式一致性检查")
    void testExceptionResponseFormat_Consistency() throws Exception {
        // Arrange
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException("Test exception"));

        // Act & Assert - 验证响应包含所有必需字段
        mockMvc.perform(get("/api/modules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.path", notNullValue()))
                // 验证字段类型
                .andExpect(jsonPath("$.status", isA(Integer.class)))
                .andExpect(jsonPath("$.error", isA(String.class)))
                .andExpect(jsonPath("$.message", isA(String.class)));
    }

    @Test
    @DisplayName("Bug 回归测试 - 通用异常返回 500")
    void testGenericException_Returns500() throws Exception {
        // Arrange - 模拟未预期的运行时异常
        when(moduleService.getAllModules())
                .thenThrow(new RuntimeException("Unexpected database error"));

        // Act & Assert
        mockMvc.perform(get("/api/modules"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred")))
                .andExpect(jsonPath("$.details", notNullValue()));
    }

    @Test
    @DisplayName("Bug 回归测试 - ResourceNotFoundException 消息正确传递")
    void testResourceNotFoundExceptionMessage_CorrectlyPassed() throws Exception {
        // Arrange - 测试自定义错误消息是否正确传递
        String customMessage = "Custom error: Module with serial SN-12345 not found";
        when(moduleService.getModuleById(100L))
                .thenThrow(new ResourceNotFoundException(customMessage));

        // Act & Assert
        mockMvc.perform(get("/api/modules/100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(customMessage)));
    }

    @Test
    @DisplayName("Bug 回归测试 - 多个连续的 404 错误")
    void testMultipleConsecutive404Errors() throws Exception {
        // Arrange - 测试多次请求不存在的资源
        when(moduleService.getModuleById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Module not found"));

        // Act & Assert - 连续多次请求都应返回 404
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(get("/api/modules/" + (1000 + i)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)));
        }
    }

    @Test
    @DisplayName("Bug 回归测试 - 获取历史记录时模块不存在")
    void testGetHistoryForNonExistentModule_Returns404() throws Exception {
        // Arrange
        when(moduleService.getModuleHistory(555L))
                .thenThrow(new ResourceNotFoundException("Module not found with id: 555"));

        // Act & Assert
        mockMvc.perform(get("/api/modules/555/history"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Module not found")));
    }

    @Test
    @DisplayName("Bug 回归测试 - Path 字段正确反映请求路径")
    void testExceptionResponsePath_ReflectsRequestPath() throws Exception {
        // Arrange
        when(moduleService.getModuleById(123L))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act & Assert
        mockMvc.perform(get("/api/modules/123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path", containsString("/api/modules/123")));
    }

    @Test
    @DisplayName("Bug 回归测试 - 时间戳格式正确")
    void testExceptionResponseTimestamp_IsValid() throws Exception {
        // Arrange
        when(moduleService.getModuleById(999L))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // Act & Assert
        mockMvc.perform(get("/api/modules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", matchesRegex(
                    "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+$"
                )));
    }
}