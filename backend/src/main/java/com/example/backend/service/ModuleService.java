package com.example.backend.service;

import com.example.backend.model.History;
import com.example.backend.model.Module;
import com.example.backend.repository.HistoryRepository;
import com.example.backend.repository.ModuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取所有模块
     * @return List<Module>
     */
    public List<Module> getAllModules() {
        try {
            return moduleRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch modules: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取模块
     * @param id 模块ID
     * @return Optional<Module>
     */
    public Optional<Module> getModuleById(Integer id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid module ID");
            }
            return moduleRepository.findById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch module by ID: " + e.getMessage(), e);
        }
    }

    /**
     * 创建新模块
     * @param module 模块对象
     * @return Module
     */
    @Transactional
    public Module createModule(Module module) {
        try {
            // 验证必填字段
            if (module.getCode() == null || module.getCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Module code is required");
            }
            if (module.getStatus() == null || module.getStatus().trim().isEmpty()) {
                throw new IllegalArgumentException("Module status is required");
            }

            // 检查代码是否已存在
            if (moduleRepository.existsByCode(module.getCode())) {
                throw new IllegalArgumentException("Module code already exists: " + module.getCode());
            }

            // 保存模块
            Module savedModule = moduleRepository.save(module);

            // 记录创建历史
            String newValue = convertModuleToJson(savedModule);
            History history = new History(
                savedModule.getId(),
                "CREATE",
                null,
                newValue
            );
            historyRepository.save(history);

            return savedModule;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create module: " + e.getMessage(), e);
        }
    }

    /**
     * 更新模块
     * @param id 模块ID
     * @param updatedModule 更新后的模块对象
     * @return Module
     */
    @Transactional
    public Module updateModule(Integer id, Module updatedModule) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid module ID");
            }

            Module existingModule = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + id));

            // 记录旧值
            String oldValue = convertModuleToJson(existingModule);

            // 更新字段
            if (updatedModule.getCode() != null) {
                // 检查新代码是否与其他模块冲突
                if (!existingModule.getCode().equals(updatedModule.getCode()) && 
                    moduleRepository.existsByCode(updatedModule.getCode())) {
                    throw new IllegalArgumentException("Module code already exists: " + updatedModule.getCode());
                }
                existingModule.setCode(updatedModule.getCode());
            }
            if (updatedModule.getStatus() != null) {
                existingModule.setStatus(updatedModule.getStatus());
            }
            if (updatedModule.getVendor() != null) {
                existingModule.setVendor(updatedModule.getVendor());
            }
            if (updatedModule.getProcessStatus() != null) {
                existingModule.setProcessStatus(updatedModule.getProcessStatus());
            }
            if (updatedModule.getEnterTime() != null) {
                existingModule.setEnterTime(updatedModule.getEnterTime());
            }
            if (updatedModule.getExitTime() != null) {
                existingModule.setExitTime(updatedModule.getExitTime());
            }
            if (updatedModule.getLD() != null) {
                existingModule.setLD(updatedModule.getLD());
            }
            if (updatedModule.getPD() != null) {
                existingModule.setPD(updatedModule.getPD());
            }
            if (updatedModule.getRemarks() != null) {
                existingModule.setRemarks(updatedModule.getRemarks());
            }

            // 保存更新
            Module savedModule = moduleRepository.save(existingModule);

            // 记录更新历史
            String newValue = convertModuleToJson(savedModule);
            History history = new History(
                savedModule.getId(),
                "UPDATE",
                oldValue,
                newValue
            );
            historyRepository.save(history);

            return savedModule;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update module: " + e.getMessage(), e);
        }
    }

    /**
     * 删除模块
     * @param id 模块ID
     */
    @Transactional
    public void deleteModule(Integer id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid module ID");
            }

            Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + id));

            // 记录删除历史
            String oldValue = convertModuleToJson(module);
            History history = new History(
                id,
                "DELETE",
                oldValue,
                null
            );
            historyRepository.save(history);

            // 删除模块
            moduleRepository.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete module: " + e.getMessage(), e);
        }
    }

    /**
     * 获取模块的历史记录
     * @param id 模块ID
     * @return List<History>
     */
    public List<History> getModuleHistory(Integer id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid module ID");
            }

            // 验证模块是否存在
            if (!moduleRepository.existsById(id)) {
                throw new IllegalArgumentException("Module not found with ID: " + id);
            }

            return historyRepository.findByModuleIdOrderByTimestampDesc(id);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch module history: " + e.getMessage(), e);
        }
    }

    /**
     * 将模块对象转换为JSON字符串
     * @param module 模块对象
     * @return JSON字符串
     */
    private String convertModuleToJson(Module module) {
        try {
            return objectMapper.writeValueAsString(module);
        } catch (Exception e) {
            return module.toString();
        }
    }
}
