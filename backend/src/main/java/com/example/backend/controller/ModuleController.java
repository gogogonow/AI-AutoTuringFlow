package com.example.backend.controller;

import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.History;
import com.example.backend.model.Module;
import com.example.backend.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "*")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    /**
     * 获取所有模块列表
     * GET /api/modules
     */
    @GetMapping
    public ResponseEntity<List<Module>> getAllModules() {
        List<Module> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }

    /**
     * 根据ID获取单个模块详情
     * GET /api/modules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Module> getModuleById(@PathVariable Long id) {
        Module module = moduleService.getModuleById(id);
        return ResponseEntity.ok(module);
    }

    /**
     * 创建新模块
     * POST /api/modules
     */
    @PostMapping
    public ResponseEntity<Module> createModule(@RequestBody Module module) {
        Module createdModule = moduleService.createModule(module);
        return ResponseEntity.ok(createdModule);
    }

    /**
     * 更新模块信息
     * PUT /api/modules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Module> updateModule(@PathVariable Long id, @RequestBody Module module) {
        Module updatedModule = moduleService.updateModule(id, module);
        return ResponseEntity.ok(updatedModule);
    }

    /**
     * 删除模块
     * DELETE /api/modules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取模块的修改历史记录
     * GET /api/modules/{id}/history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<History>> getModuleHistory(@PathVariable Long id) {
        List<History> history = moduleService.getModuleHistory(id);
        return ResponseEntity.ok(history);
    }
}
