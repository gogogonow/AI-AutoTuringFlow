package com.example.backend.controller;

import com.example.backend.dto.ModuleVendorInfoDto;
import com.example.backend.service.ModuleVendorInfoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 光模块厂家信息控制器
 */
@RestController
@RequestMapping("/api/modules/{moduleId}/vendor-infos")
@CrossOrigin(origins = "*")
public class ModuleVendorInfoController {

    @Autowired
    private ModuleVendorInfoService vendorInfoService;

    /**
     * 获取指定模块的所有厂家信息
     */
    @GetMapping
    public ResponseEntity<List<ModuleVendorInfoDto>> getVendorInfos(@PathVariable Long moduleId) {
        return ResponseEntity.ok(vendorInfoService.getVendorInfosByModuleId(moduleId));
    }

    /**
     * 获取单条厂家信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ModuleVendorInfoDto> getVendorInfo(
            @PathVariable Long moduleId,
            @PathVariable Long id) {
        return ResponseEntity.ok(vendorInfoService.getVendorInfoById(id));
    }

    /**
     * 新增厂家信息
     */
    @PostMapping
    public ResponseEntity<ModuleVendorInfoDto> createVendorInfo(
            @PathVariable Long moduleId,
            @Valid @RequestBody ModuleVendorInfoDto dto) {
        ModuleVendorInfoDto created = vendorInfoService.createVendorInfo(moduleId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新厂家信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ModuleVendorInfoDto> updateVendorInfo(
            @PathVariable Long moduleId,
            @PathVariable Long id,
            @Valid @RequestBody ModuleVendorInfoDto dto) {
        ModuleVendorInfoDto updated = vendorInfoService.updateVendorInfo(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除厂家信息
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendorInfo(
            @PathVariable Long moduleId,
            @PathVariable Long id) {
        vendorInfoService.deleteVendorInfo(id);
        return ResponseEntity.noContent().build();
    }
}
