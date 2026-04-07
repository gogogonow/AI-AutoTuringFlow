package com.example.backend.service;

import com.example.backend.dto.ModuleDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Module;
import com.example.backend.model.OperationType;
import com.example.backend.repository.ModuleRepository;
import com.example.backend.repository.ModuleVendorInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private ModuleVendorInfoRepository vendorInfoRepository;

    @Mock
    private HistoryService historyService;

    @InjectMocks
    private ModuleServiceImpl moduleService;

    private Module testModule;
    private ModuleDto testModuleDto;

    @BeforeEach
    void setUp() {
        testModule = new Module();
        testModule.setId(1L);
        testModule.setSerialNumber("TEST001");
        testModule.setModel("SFP-10G-SR");
        testModule.setSpeed("10G");
        testModule.setInboundTime(LocalDateTime.now());

        testModuleDto = new ModuleDto();
        testModuleDto.setSerialNumber("TEST001");
        testModuleDto.setModel("SFP-10G-SR");
        testModuleDto.setSpeed("10G");
    }

    @Test
    void testCreateModule_Success() {
        when(moduleRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(moduleRepository.save(any(Module.class))).thenReturn(testModule);

        ModuleDto result = moduleService.createModule(testModuleDto);

        assertNotNull(result);
        assertEquals("TEST001", result.getSerialNumber());
        verify(moduleRepository).save(any(Module.class));
        verify(historyService).createHistory(any(), eq(OperationType.INBOUND), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void testCreateModule_DuplicateSerialNumber() {
        when(moduleRepository.existsBySerialNumber("TEST001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            moduleService.createModule(testModuleDto);
        });

        verify(moduleRepository, never()).save(any(Module.class));
    }

    @Test
    void testGetModuleById_Found() {
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(testModule));

        ModuleDto result = moduleService.getModuleById(1L);

        assertNotNull(result);
        assertEquals("TEST001", result.getSerialNumber());
    }

    @Test
    void testGetModuleById_NotFound() {
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            moduleService.getModuleById(999L);
        });
    }

    @Test
    void testUpdateModule_Success() {
        ModuleDto updateDto = new ModuleDto();
        updateDto.setSerialNumber("TEST001");
        updateDto.setModel("SFP-10G-LR");

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(testModule));
        when(moduleRepository.save(any(Module.class))).thenReturn(testModule);

        ModuleDto result = moduleService.updateModule(1L, updateDto);

        assertNotNull(result);
        verify(moduleRepository).save(any(Module.class));
        verify(historyService).createHistory(any(), eq(OperationType.UPDATE_INFO), anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void testDeleteModule_Success() {
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(testModule));

        moduleService.deleteModule(1L);

        verify(vendorInfoRepository).deleteByModuleId(1L);
        verify(moduleRepository).deleteById(1L);
        verify(historyService).createHistory(eq(1L), eq(OperationType.OUTBOUND), anyString(),
            isNull(), isNull(), anyString(), anyString());
    }

    @Test
    void testDeleteModule_NotFound() {
        when(moduleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            moduleService.deleteModule(999L);
        });
    }

    @Test
    void testGetModules_Paginated() {
        List<Module> modules = Arrays.asList(testModule);
        Page<Module> modulePage = new PageImpl<>(modules);
        Pageable pageable = PageRequest.of(0, 20);

        when(moduleRepository.findAll(pageable)).thenReturn(modulePage);

        Page<ModuleDto> result = moduleService.getModules(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testBatchInbound() {
        ModuleDto dto1 = new ModuleDto();
        dto1.setSerialNumber("BATCH001");
        dto1.setModel("Model1");

        ModuleDto dto2 = new ModuleDto();
        dto2.setSerialNumber("BATCH002");
        dto2.setModel("Model2");

        when(moduleRepository.existsBySerialNumber(anyString())).thenReturn(false);
        when(moduleRepository.save(any(Module.class))).thenReturn(testModule);

        List<ModuleDto> results = moduleService.batchInbound(Arrays.asList(dto1, dto2));

        assertEquals(2, results.size());
        verify(moduleRepository, times(2)).save(any(Module.class));
    }
}
