package com.example.backend.service;

import com.example.backend.dto.ModuleDto;
import com.example.backend.dto.StatusChangeRequest;
import com.example.backend.model.*;
import com.example.backend.repository.ModuleRepository;
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

        // 记录入库历史
        historyService.createHistory(
            savedModule.getId(),
            OperationType.INBOUND,
            "system",
            null,
            savedModule.getStatus(),
            "首次入库"
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

        // 记录更新历史
        historyService.createHistory(
            updatedModule.getId(),
            OperationType.UPDATE_INFO,
            "system",
            null,
            null,
            "更新光模块信息"
        );

        return toDto(updatedModule);
    }

    @Override
    public void deleteModule(Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new IllegalArgumentException("光模块不存在: ID=" + id);
        }
        // 级联删除会自动删除相关历史记录
        moduleRepository.deleteById(id);
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

        // 记录状态变更历史
        historyService.createHistory(
            updatedModule.getId(),
            operationType,
            request.getOperator(),
            previousStatus,
            nextStatus,
            request.getRemark()
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
}
