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
        historyService.createHistory(moduleId, OperationType.VENDOR_ADD, "system",
                null, null, "新增厂家信息: " + (dto.getVendor() != null ? dto.getVendor() : ""));
        return saved;
    }

    @Override
    public ModuleVendorInfoDto updateVendorInfo(Long id, ModuleVendorInfoDto dto) {
        ModuleVendorInfo existing = vendorInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("厂家信息不存在: ID=" + id));
        Long moduleId = existing.getModuleId();
        updateEntity(existing, dto);
        ModuleVendorInfoDto saved = toDto(vendorInfoRepository.save(existing));
        historyService.createHistory(moduleId, OperationType.VENDOR_UPDATE, "system",
                null, null, "更新厂家信息: " + (dto.getVendor() != null ? dto.getVendor() : ""));
        return saved;
    }

    @Override
    public void deleteVendorInfo(Long id) {
        ModuleVendorInfo existing = vendorInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("厂家信息不存在: ID=" + id));
        Long moduleId = existing.getModuleId();
        String vendor = existing.getVendor();
        vendorInfoRepository.deleteById(id);
        historyService.createHistory(moduleId, OperationType.VENDOR_DELETE, "system",
                null, null, "删除厂家信息: " + (vendor != null ? vendor : ""));
    }

    private ModuleVendorInfoDto toDto(ModuleVendorInfo info) {
        if (info == null) return null;
        ModuleVendorInfoDto dto = new ModuleVendorInfoDto();
        dto.setId(info.getId());
        dto.setModuleId(info.getModuleId());
        dto.setVendor(info.getVendor());
        dto.setProcessStatus(info.getProcessStatus());
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
}
