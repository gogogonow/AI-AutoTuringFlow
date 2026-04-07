package com.example.backend.service;

import com.example.backend.dto.ModuleDto;
import com.example.backend.dto.StatusChangeRequest;
import com.example.backend.model.Module;
import com.example.backend.model.ModuleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 光模块服务接口
 */
public interface ModuleService {

    /**
     * 创建光模块（入库）
     */
    ModuleDto createModule(ModuleDto moduleDto);

    /**
     * 根据ID获取光模块
     */
    ModuleDto getModuleById(Long id);

    /**
     * 根据序列号获取光模块
     */
    ModuleDto getModuleBySerialNumber(String serialNumber);

    /**
     * 更新光模块信息
     */
    ModuleDto updateModule(Long id, ModuleDto moduleDto);

    /**
     * 删除光模块
     */
    void deleteModule(Long id);

    /**
     * 分页查询光模块列表
     */
    Page<ModuleDto> getModules(Pageable pageable);

    /**
     * 多条件筛选查询
     */
    Page<ModuleDto> searchModules(
        String serialNumber,
        String model,
        String vendor,
        ModuleStatus status,
        String speed,
        Pageable pageable
    );

    /**
     * 根据状态查询光模块
     */
    Page<ModuleDto> getModulesByStatus(ModuleStatus status, Pageable pageable);

    /**
     * 变更光模块状态
     */
    ModuleDto changeStatus(Long id, StatusChangeRequest request);

    /**
     * 批量入库
     */
    List<ModuleDto> batchInbound(List<ModuleDto> moduleDtos);

    /**
     * 统计各状态的光模块数量
     */
    Map<ModuleStatus, Long> getStatusStatistics();

    /**
     * 统计各供应商的光模块数量
     */
    Map<String, Long> getVendorStatistics();

    /**
     * Entity转DTO
     */
    ModuleDto toDto(Module module);

    /**
     * DTO转Entity
     */
    Module toEntity(ModuleDto moduleDto);
}
