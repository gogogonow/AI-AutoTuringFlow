package com.example.backend.service;

import com.example.backend.dto.ModuleDto;
import com.example.backend.model.Module;
import com.example.backend.model.FiberType;
import com.example.backend.model.LifecycleStatus;
import com.example.backend.model.LightType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
     * 根据编码获取光模块
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
     * 分页查询光模块列表（含厂家信息摘要）
     */
    Page<ModuleDto> getModules(Pageable pageable);

    /**
     * 多条件筛选查询（支持自定义字段，含厂家信息摘要）
     */
    Page<ModuleDto> searchModules(
        String serialNumber,
        String speed,
        String wavelength,
        Integer transmissionDistance,
        String connectorType,
        LifecycleStatus lifecycleStatus,
        String packageForm,
        FiberType fiberType,
        LightType lightType,
        Pageable pageable
    );

    /**
     * 批量入库
     */
    List<ModuleDto> batchInbound(List<ModuleDto> moduleDtos);

    /**
     * Entity转DTO
     */
    ModuleDto toDto(Module module);

    /**
     * DTO转Entity
     */
    Module toEntity(ModuleDto moduleDto);
}
