package com.example.backend.service;

import com.example.backend.model.Module;
import com.example.backend.model.History;
import com.example.backend.repository.ModuleRepository;
import com.example.backend.repository.HistoryRepository;
import com.example.backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * ModuleService 单元测试
 * 测试核心业务逻辑，包括 CRUD 操作、异常处理、历史记录追踪
 */
@SpringBootTest
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private HistoryRepository historyRepository;

    @InjectMocks
    private ModuleService moduleService;

    private Module testModule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testModule = new Module();
        testModule.setId(1L);
        testModule.setSerialNumber("SN-12345");
        testModule.setManufacturer("华为");
        testModule.setModelNumber("eSFP-GE-SX-MM850");
        testModule.setWavelength(850.0);
        testModule.setTransmitPower(-5.0);
        testModule.setReceiveSensitivity(-18.0);
        testModule.setTransmissionDistance(550.0);
        testModule.setCreatedAt(LocalDateTime.now());
        testModule.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试获取所有模块 - 成功场景")
    void testGetAllModules_Success() {
        // Arrange
        Module module2 = new Module();
        module2.setId(2L);
        module2.setSerialNumber("SN-67890");
        List<Module> expectedModules = Arrays.asList(testModule, module2);
        
        when(moduleRepository.findAll()).thenReturn(expectedModules);

        // Act
        List<Module> actualModules = moduleService.getAllModules();

        // Assert
        assertNotNull(actualModules);
        assertEquals(2, actualModules.size());
        assertEquals("SN-12345", actualModules.get(0).getSerialNumber());
        verify(moduleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("测试获取所有模块 - 空列表")
    void testGetAllModules_EmptyList() {
        // Arrange
        when(moduleRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Module> actualModules = moduleService.getAllModules();

        // Assert
        assertNotNull(actualModules);
        assertTrue(actualModules.isEmpty());
        verify(moduleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("测试根据ID获取模块 - 成功场景")
    void testGetModuleById_Success() {
        // Arrange
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(testModule));

        // Act
        Module actualModule = moduleService.getModuleById(1L);

        // Assert
        assertNotNull(actualModule);
        assertEquals("SN-12345", actualModule.getSerialNumber());
        assertEquals("华为", actualModule.getManufacturer());
        verify(moduleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("测试根据ID获取模块 - 模块不存在")
    void testGetModuleById_NotFound() {
        // Arrange
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            moduleService.getModuleById(999L);
        });
        
        verify(moduleRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("测试创建模块 - 成功场景")
    void testCreateModule_Success() {
        // Arrange
        Module newModule = new Module();
        newModule.setSerialNumber("SN-NEW-001");
        newModule.setManufacturer("中兴");
        
        when(moduleRepository.save(any(Module.class))).thenReturn(testModule);
        when(historyRepository.save(any(History.class))).thenReturn(new History());

        // Act
        Module createdModule = moduleService.createModule(newModule);

        // Assert
        assertNotNull(createdModule);
        assertNotNull(createdModule.getCreatedAt());
        assertNotNull(createdModule.getUpdatedAt());
        verify(moduleRepository, times(1)).save(any(Module.class));
        verify(historyRepository, times(1)).save(any(History.class));
    }

    @Test
    @DisplayName("测试创建模块 - 空对象")
    void testCreateModule_NullModule() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            moduleService.createModule(null);
        });
        
        verify(moduleRepository, never()).save(any(Module.class));
    }

    @Test
    @DisplayName("测试更新模块 - 成功场景")
    void testUpdateModule_Success() {
        // Arrange
        Module updatedData = new Module();
        updatedData.setSerialNumber("SN-UPDATED");
        updatedData.setManufacturer("诺基亚");
        updatedData.setTransmitPower(-4.0);
        
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(testModule));
        when(moduleRepository.save(any(Module.class))).thenReturn(testModule);
        when(historyRepository.save(any(History.class))).thenReturn(new History());

        // Act
        Module updatedModule = moduleService.updateModule(1L, updatedData);

        // Assert
        assertNotNull(updatedModule);
        verify(moduleRepository, times(1)).findById(1L);
        verify(moduleRepository, times(1)).save(any(Module.class));
        verify(historyRepository, atLeastOnce()).save(any(History.class));
    }

    @Test
    @DisplayName("测试更新模块 - 模块不存在")
    void testUpdateModule_NotFound() {
        // Arrange
        Module updatedData = new Module();
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            moduleService.updateModule(999L, updatedData);
        });
        
        verify(moduleRepository, times(1)).findById(999L);
        verify(moduleRepository, never()).save(any(Module.class));
    }

    @Test
    @DisplayName("测试删除模块 - 成功场景")
    void testDeleteModule_Success() {
        // Arrange
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(testModule));
        when(historyRepository.save(any(History.class))).thenReturn(new History());
        doNothing().when(moduleRepository).deleteById(1L);

        // Act
        moduleService.deleteModule(1L);

        // Assert
        verify(moduleRepository, times(1)).findById(1L);
        verify(moduleRepository, times(1)).deleteById(1L);
        verify(historyRepository, times(1)).save(any(History.class));
    }

    @Test
    @DisplayName("测试删除模块 - 模块不存在")
    void testDeleteModule_NotFound() {
        // Arrange
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            moduleService.deleteModule(999L);
        });
        
        verify(moduleRepository, times(1)).findById(999L);
        verify(moduleRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("测试获取模块历史记录 - 成功场景")
    void testGetModuleHistory_Success() {
        // Arrange
        History history1 = new History();
        history1.setId(1L);
        history1.setModuleId(1L);
        history1.setOperation("CREATE");
        
        History history2 = new History();
        history2.setId(2L);
        history2.setModuleId(1L);
        history2.setOperation("UPDATE");
        
        List<History> expectedHistory = Arrays.asList(history1, history2);
        
        when(moduleRepository.existsById(1L)).thenReturn(true);
        when(historyRepository.findByModuleIdOrderByCreatedAtDesc(1L)).thenReturn(expectedHistory);

        // Act
        List<History> actualHistory = moduleService.getModuleHistory(1L);

        // Assert
        assertNotNull(actualHistory);
        assertEquals(2, actualHistory.size());
        assertEquals("CREATE", actualHistory.get(0).getOperation());
        verify(historyRepository, times(1)).findByModuleIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("测试获取模块历史记录 - 模块不存在")
    void testGetModuleHistory_ModuleNotFound() {
        // Arrange
        when(moduleRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            moduleService.getModuleHistory(999L);
        });
        
        verify(moduleRepository, times(1)).existsById(999L);
        verify(historyRepository, never()).findByModuleIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    @DisplayName("测试边界值 - 波长范围")
    void testWavelengthBoundary() {
        // 测试极端波长值
        Module extremeModule = new Module();
        extremeModule.setWavelength(0.1); // 极小值
        
        when(moduleRepository.save(any(Module.class))).thenReturn(extremeModule);
        when(historyRepository.save(any(History.class))).thenReturn(new History());
        
        Module saved = moduleService.createModule(extremeModule);
        assertNotNull(saved);
        assertEquals(0.1, saved.getWavelength());
    }

    @Test
    @DisplayName("测试边界值 - 传输距离为零")
    void testZeroTransmissionDistance() {
        Module zeroDistModule = new Module();
        zeroDistModule.setTransmissionDistance(0.0);
        
        when(moduleRepository.save(any(Module.class))).thenReturn(zeroDistModule);
        when(historyRepository.save(any(History.class))).thenReturn(new History());
        
        Module saved = moduleService.createModule(zeroDistModule);
        assertNotNull(saved);
        assertEquals(0.0, saved.getTransmissionDistance());
    }

    @Test
    @DisplayName("测试并发更新 - 时间戳验证")
    void testConcurrentUpdate_TimestampValidation() throws InterruptedException {
        // Arrange
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(testModule));
        when(moduleRepository.save(any(Module.class))).thenReturn(testModule);
        when(historyRepository.save(any(History.class))).thenReturn(new History());
        
        LocalDateTime beforeUpdate = LocalDateTime.now();
        Thread.sleep(10); // 确保时间差异
        
        // Act
        Module updatedModule = moduleService.updateModule(1L, new Module());
        
        // Assert
        assertNotNull(updatedModule.getUpdatedAt());
        assertTrue(updatedModule.getUpdatedAt().isAfter(beforeUpdate));
    }
}