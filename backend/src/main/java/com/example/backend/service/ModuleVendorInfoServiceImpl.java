package com.example.backend.service;

import com.example.backend.dto.ModuleVendorInfoDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.ModuleVendorInfo;
import com.example.backend.model.OperationType;
import com.example.backend.repository.ModuleRepository;
import com.example.backend.repository.ModuleVendorInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 光模块厂家信息服务实现类
 */
@Service
@Transactional
public class ModuleVendorInfoServiceImpl implements ModuleVendorInfoService {

    @Autowired
    private ModuleVendorInfoRepository vendorInfoRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private HistoryService historyService;

    @Override
    @Transactional(readOnly = true)
    public List<ModuleVendorInfoDto> getVendorInfosByModuleId(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("光模块不存在: ID=" + moduleId);
        }
        return vendorInfoRepository.findByModuleIdOrderByCreatedAtAsc(moduleId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ModuleVendorInfoDto getVendorInfoById(Long id) {
        ModuleVendorInfo info = vendorInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("厂家信息不存在: ID=" + id));
        return toDto(info);
    }

    @Override
    public ModuleVendorInfoDto createVendorInfo(Long moduleId, ModuleVendorInfoDto dto) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("光模块不存在: ID=" + moduleId);
        }
        ModuleVendorInfo info = toEntity(dto);
        info.setModuleId(moduleId);
        ModuleVendorInfoDto saved = toDto(vendorInfoRepository.save(info));

        // Get module info for history
        String serialNumber = null;
        String model = null;
        var moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isPresent()) {
            serialNumber = moduleOpt.get().getSerialNumber();
            model = moduleOpt.get().getModel();
        }

        String changeDetails = buildVendorCreateDetails(dto);
        historyService.createHistory(moduleId, OperationType.VENDOR_ADD, "system",
                null, null, "新增厂家信息: " + (dto.getVendor() != null ? dto.getVendor() : ""),
                changeDetails, serialNumber, model);
        return saved;
    }

    @Override
    public ModuleVendorInfoDto updateVendorInfo(Long id, ModuleVendorInfoDto dto) {
        ModuleVendorInfo existing = vendorInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("厂家信息不存在: ID=" + id));
        Long moduleId = existing.getModuleId();

        // Get module info for history
        String serialNumber = null;
        String model = null;
        var moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isPresent()) {
            serialNumber = moduleOpt.get().getSerialNumber();
            model = moduleOpt.get().getModel();
        }

        String changeDetails = buildVendorUpdateDetails(existing, dto);
        updateEntity(existing, dto);
        ModuleVendorInfoDto saved = toDto(vendorInfoRepository.save(existing));
        historyService.createHistory(moduleId, OperationType.VENDOR_UPDATE, "system",
                null, null, "更新厂家信息: " + (dto.getVendor() != null ? dto.getVendor() : ""),
                changeDetails, serialNumber, model);
        return saved;
    }

    @Override
    public void deleteVendorInfo(Long id) {
        ModuleVendorInfo existing = vendorInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("厂家信息不存在: ID=" + id));
        Long moduleId = existing.getModuleId();
        String vendor = existing.getVendor();
        String changeDetails = buildVendorDeleteDetails(existing);

        // Get module info for history
        String serialNumber = null;
        String model = null;
        var moduleOpt = moduleRepository.findById(moduleId);
        if (moduleOpt.isPresent()) {
            serialNumber = moduleOpt.get().getSerialNumber();
            model = moduleOpt.get().getModel();
        }

        // 软删除：设置删除标记和删除时间
        existing.setDeleted(true);
        existing.setDeletedAt(LocalDateTime.now());
        vendorInfoRepository.save(existing);

        historyService.createHistory(moduleId, OperationType.VENDOR_DELETE, "system",
                null, null, "删除厂家信息: " + (vendor != null ? vendor : ""),
                changeDetails, serialNumber, model);
    }

    private ModuleVendorInfoDto toDto(ModuleVendorInfo info) {
        if (info == null) return null;
        ModuleVendorInfoDto dto = new ModuleVendorInfoDto();
        dto.setId(info.getId());
        dto.setModuleId(info.getModuleId());
        dto.setVendor(info.getVendor());
        dto.setProcessStatus(info.getProcessStatus());
        dto.setVersionBatch(info.getVersionBatch());
        dto.setEntryTime(info.getEntryTime());
        dto.setExitTime(info.getExitTime());
        dto.setLd(info.getLd());
        dto.setPd(info.getPd());
        dto.setLaLdo(info.getLaLdo());
        dto.setTia(info.getTia());
        dto.setMcu(info.getMcu());
        dto.setPcnChanges(info.getPcnChanges());
        dto.setHighSpeedTestRecommended(info.getHighSpeedTestRecommended());
        dto.setAvailability(info.getAvailability());
        dto.setPhotodetectorData(info.getPhotodetectorData());
        dto.setCoveredBoards(info.getCoveredBoards());
        dto.setTestReportLink(info.getTestReportLink());
        dto.setRemark(info.getRemark());
        dto.setCreatedAt(info.getCreatedAt());
        dto.setUpdatedAt(info.getUpdatedAt());
        return dto;
    }

    private ModuleVendorInfo toEntity(ModuleVendorInfoDto dto) {
        ModuleVendorInfo info = new ModuleVendorInfo();
        updateEntity(info, dto);
        return info;
    }

    private void updateEntity(ModuleVendorInfo info, ModuleVendorInfoDto dto) {
        info.setVendor(dto.getVendor());
        info.setProcessStatus(dto.getProcessStatus());
        info.setVersionBatch(dto.getVersionBatch());
        info.setEntryTime(dto.getEntryTime());
        info.setExitTime(dto.getExitTime());
        info.setLd(dto.getLd());
        info.setPd(dto.getPd());
        info.setLaLdo(dto.getLaLdo());
        info.setTia(dto.getTia());
        info.setMcu(dto.getMcu());
        info.setPcnChanges(dto.getPcnChanges());
        info.setHighSpeedTestRecommended(dto.getHighSpeedTestRecommended());
        info.setAvailability(dto.getAvailability());
        info.setPhotodetectorData(dto.getPhotodetectorData());
        info.setCoveredBoards(dto.getCoveredBoards());
        info.setTestReportLink(dto.getTestReportLink());
        info.setRemark(dto.getRemark());
    }

    private String buildVendorCreateDetails(ModuleVendorInfoDto dto) {
        StringBuilder sb = new StringBuilder("新增厂家字段：");
        sb.append("厂家=").append(nullSafe(dto.getVendor()));
        appendIfNotNull(sb, "流程状态", dto.getProcessStatus());
        appendIfNotNull(sb, "版本/批次", dto.getVersionBatch());
        appendIfNotNull(sb, "LD", dto.getLd());
        appendIfNotNull(sb, "PD", dto.getPd());
        appendIfNotNull(sb, "LA+LDO", dto.getLaLdo());
        appendIfNotNull(sb, "TIA", dto.getTia());
        appendIfNotNull(sb, "MCU", dto.getMcu());
        appendIfNotNull(sb, "PCN变更点", dto.getPcnChanges());
        appendIfNotNull(sb, "获取性", dto.getAvailability());
        appendIfNotNull(sb, "备注", dto.getRemark());
        return sb.toString();
    }

    private String buildVendorUpdateDetails(ModuleVendorInfo existing, ModuleVendorInfoDto dto) {
        StringBuilder sb = new StringBuilder("更新厂家字段：");
        boolean hasChange = false;
        hasChange |= appendChange(sb, "厂家", existing.getVendor(), dto.getVendor());
        hasChange |= appendChange(sb, "流程状态", existing.getProcessStatus(), dto.getProcessStatus());
        hasChange |= appendChange(sb, "版本/批次", existing.getVersionBatch(), dto.getVersionBatch());
        hasChange |= appendChange(sb, "LD", existing.getLd(), dto.getLd());
        hasChange |= appendChange(sb, "PD", existing.getPd(), dto.getPd());
        hasChange |= appendChange(sb, "LA+LDO", existing.getLaLdo(), dto.getLaLdo());
        hasChange |= appendChange(sb, "TIA", existing.getTia(), dto.getTia());
        hasChange |= appendChange(sb, "MCU", existing.getMcu(), dto.getMcu());
        hasChange |= appendChange(sb, "PCN变更点", existing.getPcnChanges(), dto.getPcnChanges());
        hasChange |= appendChange(sb, "高速重点测试", existing.getHighSpeedTestRecommended(), dto.getHighSpeedTestRecommended());
        hasChange |= appendChange(sb, "获取性", existing.getAvailability(), dto.getAvailability());
        hasChange |= appendChange(sb, "备注", existing.getRemark(), dto.getRemark());
        if (!hasChange) {
            sb.append("无变化");
        }
        return sb.toString();
    }

    private String buildVendorDeleteDetails(ModuleVendorInfo info) {
        StringBuilder sb = new StringBuilder("删除前厂家字段：");
        sb.append("厂家=").append(nullSafe(info.getVendor()));
        appendIfNotNull(sb, "流程状态", info.getProcessStatus());
        appendIfNotNull(sb, "版本/批次", info.getVersionBatch());
        appendIfNotNull(sb, "LD", info.getLd());
        appendIfNotNull(sb, "PD", info.getPd());
        appendIfNotNull(sb, "LA+LDO", info.getLaLdo());
        appendIfNotNull(sb, "TIA", info.getTia());
        appendIfNotNull(sb, "MCU", info.getMcu());
        appendIfNotNull(sb, "PCN变更点", info.getPcnChanges());
        appendIfNotNull(sb, "获取性", info.getAvailability());
        appendIfNotNull(sb, "备注", info.getRemark());
        return sb.toString();
    }

    private boolean appendChange(StringBuilder sb, String label, Object oldVal, Object newVal) {
        if (java.util.Objects.equals(oldVal, newVal)) {
            return false;
        }
        sb.append(label).append(": ").append(nullSafe(oldVal)).append(" → ").append(nullSafe(newVal)).append("; ");
        return true;
    }

    private void appendIfNotNull(StringBuilder sb, String label, Object val) {
        if (val != null) {
            sb.append(", ").append(label).append("=").append(val);
        }
    }

    private String nullSafe(Object val) {
        return val == null ? "(空)" : val.toString();
    }
}
