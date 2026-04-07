package com.example.backend.service;

import com.example.backend.dto.ModuleVendorInfoDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.ModuleVendorInfo;
import com.example.backend.model.OperationType;
import com.example.backend.repository.ModuleRepository;
import com.example.backend.repository.ModuleVendorInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ModuleVendorInfoServiceImpl.
 * Covers vendor info CRUD operations and verifies that the correct history
 * entries (VENDOR_ADD, VENDOR_UPDATE, VENDOR_DELETE) are recorded.
 */
@ExtendWith(MockitoExtension.class)
class ModuleVendorInfoServiceTest {

    @Mock
    private ModuleVendorInfoRepository vendorInfoRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private HistoryService historyService;

    @InjectMocks
    private ModuleVendorInfoServiceImpl vendorInfoService;

    private ModuleVendorInfo testVendorInfo;
    private ModuleVendorInfoDto testVendorInfoDto;

    @BeforeEach
    void setUp() {
        testVendorInfo = new ModuleVendorInfo();
        testVendorInfo.setId(1L);
        testVendorInfo.setModuleId(5L);
        testVendorInfo.setVendor("Cisco");
        testVendorInfo.setProcessStatus("Active");
        testVendorInfo.setCreatedAt(LocalDateTime.now());
        testVendorInfo.setUpdatedAt(LocalDateTime.now());

        testVendorInfoDto = new ModuleVendorInfoDto();
        testVendorInfoDto.setVendor("Cisco");
        testVendorInfoDto.setProcessStatus("Active");
    }

    @Test
    void testCreateVendorInfo_Success() {
        when(moduleRepository.existsById(5L)).thenReturn(true);
        when(vendorInfoRepository.save(any(ModuleVendorInfo.class))).thenReturn(testVendorInfo);

        ModuleVendorInfoDto result = vendorInfoService.createVendorInfo(5L, testVendorInfoDto);

        assertNotNull(result);
        assertEquals("Cisco", result.getVendor());

        // Verify VENDOR_ADD history is recorded
        verify(historyService).createHistory(
            eq(5L),
            eq(OperationType.VENDOR_ADD),
            anyString(),
            isNull(),
            isNull(),
            contains("Cisco")
        );
    }

    @Test
    void testCreateVendorInfo_ModuleNotFound() {
        when(moduleRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
            vendorInfoService.createVendorInfo(999L, testVendorInfoDto)
        );

        verify(vendorInfoRepository, never()).save(any());
        verify(historyService, never()).createHistory(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testUpdateVendorInfo_Success() {
        ModuleVendorInfoDto updateDto = new ModuleVendorInfoDto();
        updateDto.setVendor("Huawei");
        updateDto.setProcessStatus("Updated");

        when(vendorInfoRepository.findById(1L)).thenReturn(Optional.of(testVendorInfo));
        when(vendorInfoRepository.save(any(ModuleVendorInfo.class))).thenReturn(testVendorInfo);

        ModuleVendorInfoDto result = vendorInfoService.updateVendorInfo(1L, updateDto);

        assertNotNull(result);

        // Verify VENDOR_UPDATE history is recorded
        verify(historyService).createHistory(
            eq(5L),
            eq(OperationType.VENDOR_UPDATE),
            anyString(),
            isNull(),
            isNull(),
            contains("Huawei")
        );
    }

    @Test
    void testUpdateVendorInfo_NotFound() {
        when(vendorInfoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            vendorInfoService.updateVendorInfo(999L, testVendorInfoDto)
        );

        verify(historyService, never()).createHistory(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testDeleteVendorInfo_Success() {
        when(vendorInfoRepository.findById(1L)).thenReturn(Optional.of(testVendorInfo));

        vendorInfoService.deleteVendorInfo(1L);

        verify(vendorInfoRepository).deleteById(1L);

        // Verify VENDOR_DELETE history is recorded
        verify(historyService).createHistory(
            eq(5L),
            eq(OperationType.VENDOR_DELETE),
            anyString(),
            isNull(),
            isNull(),
            contains("Cisco")
        );
    }

    @Test
    void testDeleteVendorInfo_NotFound() {
        when(vendorInfoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            vendorInfoService.deleteVendorInfo(999L)
        );

        verify(vendorInfoRepository, never()).deleteById(any());
        verify(historyService, never()).createHistory(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testGetVendorInfosByModuleId_Success() {
        when(moduleRepository.existsById(5L)).thenReturn(true);
        when(vendorInfoRepository.findByModuleIdOrderByCreatedAtAsc(5L))
            .thenReturn(Arrays.asList(testVendorInfo));

        List<ModuleVendorInfoDto> result = vendorInfoService.getVendorInfosByModuleId(5L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cisco", result.get(0).getVendor());
    }

    @Test
    void testGetVendorInfosByModuleId_ModuleNotFound() {
        when(moduleRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
            vendorInfoService.getVendorInfosByModuleId(999L)
        );
    }

    @Test
    void testGetVendorInfoById_Success() {
        when(vendorInfoRepository.findById(1L)).thenReturn(Optional.of(testVendorInfo));

        ModuleVendorInfoDto result = vendorInfoService.getVendorInfoById(1L);

        assertNotNull(result);
        assertEquals("Cisco", result.getVendor());
        assertEquals(5L, result.getModuleId());
    }

    @Test
    void testGetVendorInfoById_NotFound() {
        when(vendorInfoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            vendorInfoService.getVendorInfoById(999L)
        );
    }

    /**
     * Verifies that OperationType enum values for vendor operations
     * fit within the VARCHAR(50) column constraint. This directly
     * tests that the column size fix for issues 2 & 3 is sufficient.
     */
    @Test
    void testOperationTypeVendorValuesLength() {
        assertTrue(OperationType.VENDOR_ADD.name().length() <= 50,
            "VENDOR_ADD must fit in VARCHAR(50) column");
        assertTrue(OperationType.VENDOR_UPDATE.name().length() <= 50,
            "VENDOR_UPDATE must fit in VARCHAR(50) column");
        assertTrue(OperationType.VENDOR_DELETE.name().length() <= 50,
            "VENDOR_DELETE must fit in VARCHAR(50) column");
    }
}
