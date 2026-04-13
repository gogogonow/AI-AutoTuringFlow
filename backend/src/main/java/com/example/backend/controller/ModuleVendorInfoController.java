package com.example.backend.controller;

import com.example.backend.dto.ModuleVendorInfoDto;
import com.example.backend.service.ModuleVendorInfoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 光模块厂家信息控制器
 */
@RestController
@RequestMapping("/api/modules/{moduleId}/vendor-infos")
@CrossOrigin(origins = "*")
public class ModuleVendorInfoController {

    @Autowired
    private ModuleVendorInfoService vendorInfoService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

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

    /**
     * 上传电眼数据文件
     */
    @PostMapping("/{id}/photodetector-file")
    public ResponseEntity<Map<String, String>> uploadPhotodetectorFile(
            @PathVariable Long moduleId,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件不能为空"));
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, "photodetector");
            Files.createDirectories(uploadPath);

            // Sanitize filename: extract only the extension and validate it
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                String rawExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
                // Only allow alphanumeric extensions to prevent path traversal
                if (rawExt.matches("[a-zA-Z0-9]+")) {
                    extension = "." + rawExt;
                }
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Resolve and validate the target path is within upload directory
            Path filePath = uploadPath.resolve(uniqueFilename).normalize();
            if (!filePath.startsWith(uploadPath.normalize())) {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的文件名"));
            }

            // Save file
            Files.copy(file.getInputStream(), filePath);

            // Update vendor info with file path
            ModuleVendorInfoDto vendorInfo = vendorInfoService.getVendorInfoById(id);
            vendorInfo.setPhotodetectorDataFile(uniqueFilename);
            vendorInfoService.updateVendorInfo(id, vendorInfo);

            return ResponseEntity.ok(Map.of(
                "filename", uniqueFilename,
                "originalFilename", originalFilename != null ? originalFilename : uniqueFilename
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }
}
