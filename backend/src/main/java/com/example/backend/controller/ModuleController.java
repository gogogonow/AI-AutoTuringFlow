package com.example.backend.controller;

import com.example.backend.dto.ModuleDto;
import com.example.backend.dto.StatusChangeRequest;
import com.example.backend.model.ModuleStatus;
import com.example.backend.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 光模块控制器
 */
@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "*")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    /**
     * 创建光模块（入库）
     */
    @PostMapping
    public ResponseEntity<ModuleDto> createModule(@Valid @RequestBody ModuleDto moduleDto) {
        ModuleDto created = moduleService.createModule(moduleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 根据ID获取光模块
     */
    @GetMapping("/{id}")
    public ResponseEntity<ModuleDto> getModuleById(@PathVariable Long id) {
        ModuleDto module = moduleService.getModuleById(id);
        return ResponseEntity.ok(module);
    }

    /**
     * 根据序列号获取光模块
     */
    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<ModuleDto> getModuleBySerialNumber(@PathVariable String serialNumber) {
        ModuleDto module = moduleService.getModuleBySerialNumber(serialNumber);
        return ResponseEntity.ok(module);
    }

    /**
     * 更新光模块信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ModuleDto> updateModule(
        @PathVariable Long id,
        @Valid @RequestBody ModuleDto moduleDto
    ) {
        ModuleDto updated = moduleService.updateModule(id, moduleDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除光模块
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 分页查询光模块列表
     */
    @GetMapping
    public ResponseEntity<Page<ModuleDto>> getModules(
        @RequestParam(required = false) String serialNumber,
        @RequestParam(required = false) String model,
        @RequestParam(required = false) String vendor,
        @RequestParam(required = false) ModuleStatus status,
        @RequestParam(required = false) String speed,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ModuleDto> modules;
        if (serialNumber != null || model != null || vendor != null || status != null || speed != null) {
            modules = moduleService.searchModules(serialNumber, model, vendor, status, speed, pageable);
        } else {
            modules = moduleService.getModules(pageable);
        }

        return ResponseEntity.ok(modules);
    }

    /**
     * 根据状态查询光模块
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ModuleDto>> getModulesByStatus(
        @PathVariable ModuleStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "inboundTime"));
        Page<ModuleDto> modules = moduleService.getModulesByStatus(status, pageable);
        return ResponseEntity.ok(modules);
    }

    /**
     * 变更光模块状态
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<ModuleDto> changeStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusChangeRequest request
    ) {
        ModuleDto updated = moduleService.changeStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 批量入库
     */
    @PostMapping("/batch")
    public ResponseEntity<List<ModuleDto>> batchInbound(@Valid @RequestBody List<ModuleDto> moduleDtos) {
        List<ModuleDto> results = moduleService.batchInbound(moduleDtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    /**
     * 统计各状态的光模块数量
     */
    @GetMapping("/statistics/status")
    public ResponseEntity<Map<ModuleStatus, Long>> getStatusStatistics() {
        Map<ModuleStatus, Long> stats = moduleService.getStatusStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * 统计各供应商的光模块数量
     */
    @GetMapping("/statistics/vendor")
    public ResponseEntity<Map<String, Long>> getVendorStatistics() {
        Map<String, Long> stats = moduleService.getVendorStatistics();
        return ResponseEntity.ok(stats);
    }
}
