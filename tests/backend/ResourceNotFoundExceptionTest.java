package com.example.backend.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResourceNotFoundException 回归测试
 * 确保自定义异常类行为正确
 * Bug 修复：验证异常消息正确传递且异常类型正确识别
 */
class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("Bug 回归测试 - 异常创建时消息正确存储")
    void testExceptionCreation_MessageStoredCorrectly() {
        // Arrange
        String expectedMessage = "Module not found with id: 999";
        
        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(expectedMessage);
        
        // Assert
        assertNotNull(exception);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Bug 回归测试 - 异常是 RuntimeException 的子类")
    void testException_IsRuntimeException() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        
        // Assert
        assertTrue(exception instanceof RuntimeException,
            "ResourceNotFoundException should extend RuntimeException");
    }

    @Test
    @DisplayName("Bug 回归测试 - 可以正常抛出和捕获")
    void testException_CanBeThrown() {
        // Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("Test exception");
        });
    }

    @Test
    @DisplayName("Bug 回归测试 - 空消息处理")
    void testException_WithEmptyMessage() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException("");
        
        // Assert
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }

    @Test
    @DisplayName("Bug 回归测试 - Null 消息处理")
    void testException_WithNullMessage() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException(null);
        
        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Bug 回归测试 - 长消息处理")
    void testException_WithLongMessage() {
        // Arrange
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("Module not found. ");
        }
        String expectedMessage = longMessage.toString();
        
        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(expectedMessage);
        
        // Assert
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Bug 回归测试 - 特殊字符消息处理")
    void testException_WithSpecialCharacters() {
        // Arrange
        String specialMessage = "模块未找到: ID=999, 序列号='SN-中文-001', 错误码<404>";
        
        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(specialMessage);
        
        // Assert
        assertEquals(specialMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Bug 回归测试 - 异常堆栈跟踪正常")
    void testException_StackTraceAvailable() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        
        // Assert
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0,
            "Stack trace should not be empty");
    }

    @Test
    @DisplayName("Bug 回归测试 - 多个异常实例独立")
    void testMultipleExceptionInstances_AreIndependent() {
        // Arrange & Act
        ResourceNotFoundException exception1 = new ResourceNotFoundException("Message 1");
        ResourceNotFoundException exception2 = new ResourceNotFoundException("Message 2");
        
        // Assert
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals("Message 1", exception1.getMessage());
        assertEquals("Message 2", exception2.getMessage());
    }

    @Test
    @DisplayName("Bug 回归测试 - 异常可以作为 cause 传递")
    void testException_CanBeUsedAsCause() {
        // Arrange
        ResourceNotFoundException cause = new ResourceNotFoundException("Root cause");
        
        // Act
        RuntimeException wrapper = new RuntimeException("Wrapper exception", cause);
        
        // Assert
        assertNotNull(wrapper.getCause());
        assertTrue(wrapper.getCause() instanceof ResourceNotFoundException);
        assertEquals("Root cause", wrapper.getCause().getMessage());
    }
}