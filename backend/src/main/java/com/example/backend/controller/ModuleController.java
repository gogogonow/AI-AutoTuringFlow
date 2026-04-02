package com.example.backend.controller;

import com.example.backend.model.History;
import com.example.backend.model.Module;
import com.example.backend.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> getAllModules() {
        try {
            List<Module> modules = moduleService.getAllModules();
            return ResponseEntity.ok(modules);
        } catch (Exception e) {
            return handleException(e, "Failed to retrieve modules");
        }
    }

    /**
     * 根据ID获取单个模块详情
     * GET /api/modules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getModuleById(@PathVariable Integer id) {
        try {
            return moduleService.getModuleById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Module not found with ID: " + id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return handleException(e, "Failed to retrieve module");
        }
    }

    /**
     * 创建新模块
     * POST /api/modules
     */
    @PostMapping
    public ResponseEntity<?> createModule(@RequestBody Module module) {
        try {
            Module createdModule = moduleService.createModule(module);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdModule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return handleException(e, "Failed to create module");
        }
    }

    /**
     * 更新模块信息
     * PUT /api/modules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateModule(@PathVariable Integer id, @RequestBody Module module) {
        try {
            Module updatedModule = moduleService.updateModule(id, module);
            return ResponseEntity.ok(updatedModule);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return handleException(e, "Failed to update module");
        }
    }

    /**
     * 删除模块
     * DELETE /api/modules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Integer id) {
        try {
            moduleService.deleteModule(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Module deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return handleException(e, "Failed to delete module");
        }
    }

    /**
     * 获取模块的修改历史记录
     * GET /api/modules/{id}/history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<?> getModuleHistory(@PathVariable Integer id) {
        try {
            List<History> history = moduleService.getModuleHistory(id);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return handleException(e, "Failed to retrieve module history");
        }
    }

    /**
     * 统一异常处理
     */
    private ResponseEntity<?> handleException(Exception e, String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("details", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 创建错误响应
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
}
