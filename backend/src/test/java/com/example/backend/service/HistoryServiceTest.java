package com.example.backend.service;

import com.example.backend.dto.HistoryDto;
import com.example.backend.model.History;
import com.example.backend.model.Module;
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
    private Module mockModule;

    @BeforeEach
    void setUp() {
        savedHistory = new History();
        savedHistory.setId(1L);
        savedHistory.setModuleId(5L);
        savedHistory.setOperationTime(LocalDateTime.now());
        savedHistory.setCreatedAt(LocalDateTime.now());

        mockModule = new Module();
        mockModule.setId(5L);
        mockModule.setSerialNumber("SN12345");
        mockModule.setModel("SFP-10G-SR");
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

    @Test
    void testCreateHistory_DeleteModule_RecordsCorrectOperationType() {
        savedHistory.setOperationType(OperationType.DELETE_MODULE);
        savedHistory.setSerialNumber("SN12345");
        savedHistory.setModel("SFP-10G-SR");
        when(historyRepository.save(any(History.class))).thenReturn(savedHistory);
        when(moduleRepository.findById(5L)).thenReturn(Optional.of(mockModule));

        HistoryDto result = historyService.createHistory(
            5L, OperationType.DELETE_MODULE, "system", null, null,
            "删除光模块", "删除前字段：序列号=SN12345, 型号=SFP-10G-SR",
            "SN12345", "SFP-10G-SR"
        );

        assertNotNull(result);
        assertEquals(OperationType.DELETE_MODULE, result.getOperationType());

        // Verify the history entity saved to DB has DELETE_MODULE and stores serial number/model
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(captor.capture());
        assertEquals(OperationType.DELETE_MODULE, captor.getValue().getOperationType());
        assertEquals("SN12345", captor.getValue().getSerialNumber());
        assertEquals("SFP-10G-SR", captor.getValue().getModel());
        assertNotNull(captor.getValue().getOperationTime());
    }

    @Test
    void testCreateHistory_WithSerialNumberAndModel() {
        savedHistory.setOperationType(OperationType.INBOUND);
        savedHistory.setSerialNumber("SN98765");
        savedHistory.setModel("QSFP-100G-LR4");
        when(historyRepository.save(any(History.class))).thenReturn(savedHistory);
        when(moduleRepository.findById(5L)).thenReturn(Optional.empty());

        HistoryDto result = historyService.createHistory(
            5L, OperationType.INBOUND, "system", null, null,
            "首次入库", "新增字段：序列号=SN98765, 型号=QSFP-100G-LR4",
            "SN98765", "QSFP-100G-LR4"
        );

        assertNotNull(result);
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(captor.capture());
        assertEquals("SN98765", captor.getValue().getSerialNumber());
        assertEquals("QSFP-100G-LR4", captor.getValue().getModel());
    }

    @Test
    void testToDto_UsesStoredSerialNumberAndModel() {
        History history = new History();
        history.setId(1L);
        history.setModuleId(5L);
        history.setOperationType(OperationType.DELETE_MODULE);
        history.setOperationTime(LocalDateTime.now());
        history.setCreatedAt(LocalDateTime.now());
        history.setSerialNumber("SN-STORED");
        history.setModel("MODEL-STORED");

        HistoryDto dto = historyService.toDto(history);

        assertNotNull(dto);
        assertEquals("SN-STORED", dto.getSerialNumber());
        assertEquals("MODEL-STORED", dto.getModel());
        // Should NOT query moduleRepository when serial number and model are stored
        verify(moduleRepository, never()).findById(any());
    }

    @Test
    void testToDto_FallsBackToModuleJoinWhenFieldsMissing() {
        History history = new History();
        history.setId(1L);
        history.setModuleId(5L);
        history.setOperationType(OperationType.UPDATE_INFO);
        history.setOperationTime(LocalDateTime.now());
        history.setCreatedAt(LocalDateTime.now());
        // serialNumber and model are null

        when(moduleRepository.findById(5L)).thenReturn(Optional.of(mockModule));

        HistoryDto dto = historyService.toDto(history);

        assertNotNull(dto);
        assertEquals("SN12345", dto.getSerialNumber());
        assertEquals("SFP-10G-SR", dto.getModel());
        // Should query moduleRepository as fallback
        verify(moduleRepository).findById(5L);
    }
}
