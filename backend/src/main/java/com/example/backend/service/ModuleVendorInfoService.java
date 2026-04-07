package com.example.backend.service;

import com.example.backend.dto.ModuleVendorInfoDto;

import java.util.List;

/**
 * 光模块厂家信息服务接口
 */
public interface ModuleVendorInfoService {

    /**
     * 获取指定模块的所有厂家信息
     */
    List<ModuleVendorInfoDto> getVendorInfosByModuleId(Long moduleId);

    /**
     * 获取单条厂家信息
     */
    ModuleVendorInfoDto getVendorInfoById(Long id);

    /**
     * 新增厂家信息
     */
    ModuleVendorInfoDto createVendorInfo(Long moduleId, ModuleVendorInfoDto dto);

    /**
     * 更新厂家信息
     */
    ModuleVendorInfoDto updateVendorInfo(Long id, ModuleVendorInfoDto dto);

    /**
     * 删除厂家信息
     */
    void deleteVendorInfo(Long id);
}
