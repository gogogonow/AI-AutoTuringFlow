package com.example.backend.service;

import com.example.backend.dto.HistoryDto;
import com.example.backend.model.History;
import com.example.backend.model.ModuleStatus;
import com.example.backend.model.OperationType;
import com.example.backend.repository.HistoryRepository;
import com.example.backend.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HistoryServiceImpl.
 * Covers history creation for all operation types, including vendor operations
 * introduced to fix issues 2 & 3 (VENDOR_ADD, VENDOR_UPDATE, VENDOR_DELETE).
 */
@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private HistoryServiceImpl historyService;

    private History savedHistory;

    @BeforeEach
    void setUp() {
        savedHistory = new History();
        savedHistory.setId(1L);
        savedHistory.setModuleId(5L);
        savedHistory.setOperationTime(LocalDateTime.now());
        savedHistory.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateHistory_UpdateInfo() {
        savedHistory.setOperationType(OperationType.UPDATE_INFO);
        when(historyRepository.save(any(History.class))).thenReturn(savedHistory);
        when(moduleRepository.findById(5L)).thenReturn(Optional.empty());

        HistoryDto result = historyService.createHistory(
            5L, OperationType.UPDATE_INFO, "system", null, null, "更新光模块信息"
        );

        assertNotNull(result);
        assertEquals(OperationType.UPDATE_INFO, result.getOperationType());
        verify(historyRepository).save(any(History.class));
    }

    @Test
    void testCreateHistory_VendorAdd_RecordsCorrectOperationType() {
        savedHistory.setOperationType(OperationType.VENDOR_ADD);
        when(historyRepository.save(any(History.class))).thenReturn(savedHistory);
        when(moduleRepository.findById(5L)).thenReturn(Optional.empty());

        HistoryDto result = historyService.createHistory(
            5L, OperationType.VENDOR_ADD, "system", null, null, "新增厂家信息: Cisco"
        );

        assertNotNull(result);
        assertEquals(OperationType.VENDOR_ADD, result.getOperationType());

        // Verify the history entity saved to DB has VENDOR_ADD as operation type
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(captor.capture());
        assertEquals(OperationType.VENDOR_ADD, captor.getValue().getOperationType());
        assertEquals(5L, captor.getValue().getModuleId());
        assertNotNull(captor.getValue().getOperationTime(),
            "operationTime must not be null — fixes Issue 1 (timestamp issue)");
    }

    @Test
    void testCreateHistory_VendorUpdate_RecordsCorrectOperationType() {
        savedHistory.setOperationType(OperationType.VENDOR_UPDATE);
        when(historyRepository.save(any(History.class))).thenReturn(savedHistory);
        when(moduleRepository.findById(5L)).thenReturn(Optional.empty());

        HistoryDto result = historyService.createHistory(
            5L, OperationType.VENDOR_UPDATE, "system", null, null, "更新厂家信息: Huawei"
        );

        assertNotNull(result);

        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(captor.capture());
        assertEquals(OperationType.VENDOR_UPDATE, captor.getValue().getOperationType());
        assertNotNull(captor.getValue().getOperationTime());
    }

    @Test
    void testCreateHistory_VendorDelete_RecordsCorrectOperationType() {
        savedHistory.setOperationType(OperationType.VENDOR_DELETE);
        when(historyRepository.save(any(History.class))).thenReturn(savedHistory);
        when(moduleRepository.findById(5L)).thenReturn(Optional.empty());

        HistoryDto result = historyService.createHistory(
            5L, OperationType.VENDOR_DELETE, "system", null, null, "删除厂家信息: Cisco"
        );

        assertNotNull(result);

        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(captor.capture());
        assertEquals(OperationType.VENDOR_DELETE, captor.getValue().getOperationType());
        assertNotNull(captor.getValue().getOperationTime());
    }

    @Test
    void testCreateHistory_OperationTimeIsAlwaysSet() {
        // Issue 1: operationTime must never be null to avoid DB errors.
        savedHistory.setOperationType(OperationType.UPDATE_INFO);
        when(historyRepository.save(any(History.class))).thenReturn(savedHistory);
        when(moduleRepository.findById(5L)).thenReturn(Optional.empty());

        historyService.createHistory(5L, OperationType.UPDATE_INFO, "admin", null, null, null);

        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(captor.capture());
        assertNotNull(captor.getValue().getOperationTime(),
            "operationTime must be set by service before saving");
    }

    @Test
    void testCreateHistory_WithStatusChange() {
        savedHistory.setOperationType(OperationType.DEPLOY);
        savedHistory.setPreviousStatus(ModuleStatus.IN_STOCK);
        savedHistory.setNextStatus(ModuleStatus.DEPLOYED);
        when(historyRepository.save(any(History.class))).thenReturn(savedHistory);
        when(moduleRepository.findById(5L)).thenReturn(Optional.empty());

        HistoryDto result = historyService.createHistory(
            5L, OperationType.DEPLOY, "admin",
            ModuleStatus.IN_STOCK, ModuleStatus.DEPLOYED, "部署到设备"
        );

        assertNotNull(result);
        assertEquals(ModuleStatus.IN_STOCK, result.getPreviousStatus());
        assertEquals(ModuleStatus.DEPLOYED, result.getNextStatus());
    }

    /**
     * Verifies that all OperationType enum values (including new VENDOR_* types)
     * fit within the VARCHAR(50) column size used in the history table.
     * This is a regression test for Issue 2 & 3.
     */
    @Test
    void testAllOperationTypesFitInDatabaseColumn() {
        for (OperationType type : OperationType.values()) {
            assertTrue(type.name().length() <= 50,
                "OperationType." + type.name() + " (" + type.name().length()
                + " chars) must fit in VARCHAR(50) database column");
        }
    }
}
