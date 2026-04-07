package com.example.backend.service;

import com.example.backend.dto.ModuleDto;
import com.example.backend.dto.StatusChangeRequest;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.History;
import com.example.backend.model.Module;
import com.example.backend.model.ModuleStatus;
import com.example.backend.model.OperationType;
import com.example.backend.repository.ModuleRepository;
import com.example.backend.repository.ModuleVendorInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 光模块服务实现类
 */
@Service
@Transactional
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ModuleVendorInfoRepository vendorInfoRepository;

    @Autowired
    private HistoryService historyService;

    @Override
    public ModuleDto createModule(ModuleDto moduleDto) {
        // 检查序列号是否已存在
        if (moduleRepository.existsBySerialNumber(moduleDto.getSerialNumber())) {
            throw new IllegalArgumentException("序列号已存在: " + moduleDto.getSerialNumber());
        }

        Module module = toEntity(moduleDto);
        if (module.getStatus() == null) {
            module.setStatus(ModuleStatus.IN_STOCK);
        }
        if (module.getInboundTime() == null) {
            module.setInboundTime(LocalDateTime.now());
        }

        Module savedModule = moduleRepository.save(module);

        // 记录入库历史（含新增字段详情）
        String changeDetails = buildCreateDetails(savedModule);
        historyService.createHistory(
            savedModule.getId(),
            OperationType.INBOUND,
            "system",
            null,
            savedModule.getStatus(),
            "首次入库",
            changeDetails
        );

        return toDto(savedModule);
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleDto getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("光模块不存在: ID=" + id));
        return toDto(module);
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleDto getModuleBySerialNumber(String serialNumber) {
        Module module = moduleRepository.findBySerialNumber(serialNumber)
            .orElseThrow(() -> new IllegalArgumentException("光模块不存在: SN=" + serialNumber));
        return toDto(module);
    }

    @Override
    public ModuleDto updateModule(Long id, ModuleDto moduleDto) {
        Module existingModule = moduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("光模块不存在: ID=" + id));

        // 检查序列号是否被其他模块使用
        if (!existingModule.getSerialNumber().equals(moduleDto.getSerialNumber())) {
            if (moduleRepository.existsBySerialNumber(moduleDto.getSerialNumber())) {
                throw new IllegalArgumentException("序列号已被占用: " + moduleDto.getSerialNumber());
            }
        }

        // 记录更新前的值用于变更详情
        String changeDetails = buildUpdateDetails(existingModule, moduleDto);

        // 更新字段
        existingModule.setSerialNumber(moduleDto.getSerialNumber());
        existingModule.setModel(moduleDto.getModel());
        existingModule.setVendor(moduleDto.getVendor());
        existingModule.setSpeed(moduleDto.getSpeed());
        existingModule.setWavelength(moduleDto.getWavelength());
        existingModule.setTransmissionDistance(moduleDto.getTransmissionDistance());
        existingModule.setConnectorType(moduleDto.getConnectorType());
        existingModule.setRemark(moduleDto.getRemark());

        Module updatedModule = moduleRepository.save(existingModule);

        // 记录更新历史（含变更详情）
        historyService.createHistory(
            updatedModule.getId(),
            OperationType.UPDATE_INFO,
            "system",
            null,
            null,
            "更新光模块信息",
            changeDetails
        );

        return toDto(updatedModule);
    }

    @Override
    public void deleteModule(Long id) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("光模块不存在: ID=" + id));

        // 记录删除历史（含删除前各字段详情）
        String changeDetails = buildDeleteDetails(module);
        historyService.createHistory(
            module.getId(),
            OperationType.OUTBOUND,
            "system",
            module.getStatus(),
            null,
            "删除光模块",
            changeDetails
        );

        // 软删除：设置删除标记和删除时间
        module.setDeleted(true);
        module.setDeletedAt(LocalDateTime.now());
        moduleRepository.save(module);

        // 软删除关联的厂家信息
        vendorInfoRepository.deleteByModuleId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleDto> getModules(Pageable pageable) {
        return moduleRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleDto> searchModules(
        String serialNumber,
        String model,
        String vendor,
        ModuleStatus status,
        String speed,
        Pageable pageable
    ) {
        return moduleRepository.findByMultipleConditions(
            serialNumber,
            model,
            vendor,
            status,
            speed,
            pageable
        ).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ModuleDto> getModulesByStatus(ModuleStatus status, Pageable pageable) {
        return moduleRepository.findByStatus(status, pageable).map(this::toDto);
    }

    @Override
    public ModuleDto changeStatus(Long id, StatusChangeRequest request) {
        Module module = moduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("光模块不存在: ID=" + id));

        ModuleStatus previousStatus = module.getStatus();
        OperationType operationType;
        ModuleStatus nextStatus;

        switch (request.getAction().toUpperCase()) {
            case "DEPLOY":
                operationType = OperationType.DEPLOY;
                nextStatus = ModuleStatus.DEPLOYED;
                break;
            case "RETRIEVE":
                operationType = OperationType.RETRIEVE;
                nextStatus = ModuleStatus.IN_STOCK;
                break;
            case "MARK_FAULTY":
                operationType = OperationType.MARK_FAULTY;
                nextStatus = ModuleStatus.FAULTY;
                break;
            case "SEND_REPAIR":
                operationType = OperationType.SEND_REPAIR;
                nextStatus = ModuleStatus.UNDER_REPAIR;
                break;
            case "RETURN_REPAIR":
                operationType = OperationType.RETURN_REPAIR;
                nextStatus = ModuleStatus.IN_STOCK;
                break;
            case "SCRAP":
                operationType = OperationType.SCRAP;
                nextStatus = ModuleStatus.SCRAPPED;
                break;
            default:
                throw new IllegalArgumentException("无效的操作类型: " + request.getAction());
        }

        module.setStatus(nextStatus);
        Module updatedModule = moduleRepository.save(module);

        // 记录状态变更历史（含变更详情）
        String changeDetails = buildStatusChangeDetails(previousStatus, nextStatus, request);
        historyService.createHistory(
            updatedModule.getId(),
            operationType,
            request.getOperator(),
            previousStatus,
            nextStatus,
            request.getRemark(),
            changeDetails
        );

        return toDto(updatedModule);
    }

    @Override
    public List<ModuleDto> batchInbound(List<ModuleDto> moduleDtos) {
        List<ModuleDto> results = new ArrayList<>();
        for (ModuleDto dto : moduleDtos) {
            try {
                results.add(createModule(dto));
            } catch (Exception e) {
                // 记录错误但继续处理其他模块
                System.err.println("批量入库失败: SN=" + dto.getSerialNumber() + ", Error=" + e.getMessage());
            }
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ModuleStatus, Long> getStatusStatistics() {
        List<Object[]> results = moduleRepository.countByStatus();
        return results.stream()
            .collect(Collectors.toMap(
                row -> (ModuleStatus) row[0],
                row -> (Long) row[1]
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getVendorStatistics() {
        List<Object[]> results = moduleRepository.countByVendor();
        return results.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1],
                (v1, v2) -> v1,
                LinkedHashMap::new
            ));
    }

    @Override
    public ModuleDto toDto(Module module) {
        if (module == null) return null;

        ModuleDto dto = new ModuleDto();
        dto.setId(module.getId());
        dto.setSerialNumber(module.getSerialNumber());
        dto.setModel(module.getModel());
        dto.setVendor(module.getVendor());
        dto.setSpeed(module.getSpeed());
        dto.setWavelength(module.getWavelength());
        dto.setTransmissionDistance(module.getTransmissionDistance());
        dto.setConnectorType(module.getConnectorType());
        dto.setStatus(module.getStatus());
        dto.setInboundTime(module.getInboundTime());
        dto.setRemark(module.getRemark());
        dto.setCreatedAt(module.getCreatedAt());
        dto.setUpdatedAt(module.getUpdatedAt());
        return dto;
    }

    @Override
    public Module toEntity(ModuleDto dto) {
        if (dto == null) return null;

        Module module = new Module();
        module.setId(dto.getId());
        module.setSerialNumber(dto.getSerialNumber());
        module.setModel(dto.getModel());
        module.setVendor(dto.getVendor());
        module.setSpeed(dto.getSpeed());
        module.setWavelength(dto.getWavelength());
        module.setTransmissionDistance(dto.getTransmissionDistance());
        module.setConnectorType(dto.getConnectorType());
        module.setStatus(dto.getStatus());
        module.setInboundTime(dto.getInboundTime());
        module.setRemark(dto.getRemark());
        return module;
    }

    /**
     * 构建新增操作的变更详情
     */
    private String buildCreateDetails(Module module) {
        StringBuilder sb = new StringBuilder();
        sb.append("新增字段：");
        appendField(sb, "序列号", null, module.getSerialNumber());
        appendField(sb, "型号", null, module.getModel());
        appendField(sb, "供应商", null, module.getVendor());
        appendField(sb, "速率", null, module.getSpeed());
        appendField(sb, "波长", null, module.getWavelength());
        appendField(sb, "传输距离", null, module.getTransmissionDistance());
        appendField(sb, "接口类型", null, module.getConnectorType());
        appendField(sb, "状态", null, module.getStatus());
        appendField(sb, "备注", null, module.getRemark());
        return sb.toString();
    }

    /**
     * 构建更新操作的变更详情（仅记录有变化的字段）
     */
    private String buildUpdateDetails(Module existing, ModuleDto updated) {
        StringBuilder sb = new StringBuilder();
        sb.append("更新字段：");
        boolean hasChange = false;
        hasChange |= appendField(sb, "序列号", existing.getSerialNumber(), updated.getSerialNumber());
        hasChange |= appendField(sb, "型号", existing.getModel(), updated.getModel());
        hasChange |= appendField(sb, "供应商", existing.getVendor(), updated.getVendor());
        hasChange |= appendField(sb, "速率", existing.getSpeed(), updated.getSpeed());
        hasChange |= appendField(sb, "波长", existing.getWavelength(), updated.getWavelength());
        hasChange |= appendField(sb, "传输距离", existing.getTransmissionDistance(), updated.getTransmissionDistance());
        hasChange |= appendField(sb, "接口类型", existing.getConnectorType(), updated.getConnectorType());
        hasChange |= appendField(sb, "备注", existing.getRemark(), updated.getRemark());
        if (!hasChange) {
            sb.append("无变化");
        }
        return sb.toString();
    }

    /**
     * 构建删除操作的变更详情
     */
    private String buildDeleteDetails(Module module) {
        StringBuilder sb = new StringBuilder();
        sb.append("删除前字段：");
        sb.append("序列号=").append(nullSafe(module.getSerialNumber()));
        sb.append(", 型号=").append(nullSafe(module.getModel()));
        sb.append(", 供应商=").append(nullSafe(module.getVendor()));
        sb.append(", 速率=").append(nullSafe(module.getSpeed()));
        sb.append(", 波长=").append(nullSafe(module.getWavelength()));
        sb.append(", 传输距离=").append(nullSafe(module.getTransmissionDistance()));
        sb.append(", 接口类型=").append(nullSafe(module.getConnectorType()));
        sb.append(", 状态=").append(nullSafe(module.getStatus()));
        sb.append(", 备注=").append(nullSafe(module.getRemark()));
        return sb.toString();
    }

    /**
     * 构建状态变更的变更详情
     */
    private String buildStatusChangeDetails(ModuleStatus from, ModuleStatus to, StatusChangeRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("状态变更：").append(nullSafe(from)).append(" → ").append(nullSafe(to));
        if (request.getTargetDevice() != null && !request.getTargetDevice().isEmpty()) {
            sb.append(", 目标设备=").append(request.getTargetDevice());
        }
        return sb.toString();
    }

    /**
     * 追加字段变更（用于更新）。仅当值不同时追加，返回是否有变化。
     */
    private boolean appendField(StringBuilder sb, String label, Object oldVal, Object newVal) {
        if (Objects.equals(oldVal, newVal)) {
            return false;
        }
        if (oldVal == null) {
            // 新增场景
            sb.append(label).append("=").append(nullSafe(newVal)).append("; ");
        } else {
            sb.append(label).append(": ").append(nullSafe(oldVal)).append(" → ").append(nullSafe(newVal)).append("; ");
        }
        return true;
    }

    private String nullSafe(Object val) {
        return val == null ? "(空)" : val.toString();
    }
}
