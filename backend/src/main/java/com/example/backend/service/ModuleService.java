package com.example.backend.service;

import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.History;
import com.example.backend.model.Module;
import com.example.backend.repository.HistoryRepository;
import com.example.backend.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private HistoryRepository historyRepository;

    /**
     * 获取所有模块
     * @return List<Module>
     */
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }

    /**
     * 根据ID获取模块
     * @param id 模块ID
     * @return Module
     * @throws ResourceNotFoundException 如果模块不存在
     */
    public Module getModuleById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Module ID cannot be null");
        }
        return moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));
    }

    /**
     * 创建新模块
     * @param module 模块对象
     * @return Module
     * @throws IllegalArgumentException 如果模块为null或必填字段为空
     */
    @Transactional
    public Module createModule(Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null");
        }

        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        module.setCreatedAt(now);
        module.setUpdatedAt(now);

        // 保存模块
        Module savedModule = moduleRepository.save(module);

        // 记录创建历史 - 为每个非空字段创建历史记录
        recordFieldHistory(savedModule.getId(), "CREATE", "serialNumber", null, savedModule.getSerialNumber());
        if (savedModule.getManufacturer() != null) {
            recordFieldHistory(savedModule.getId(), "CREATE", "manufacturer", null, savedModule.getManufacturer());
        }
        if (savedModule.getModelNumber() != null) {
            recordFieldHistory(savedModule.getId(), "CREATE", "modelNumber", null, savedModule.getModelNumber());
        }
        if (savedModule.getWavelength() != null) {
            recordFieldHistory(savedModule.getId(), "CREATE", "wavelength", null, savedModule.getWavelength().toString());
        }
        if (savedModule.getTransmitPower() != null) {
            recordFieldHistory(savedModule.getId(), "CREATE", "transmitPower", null, savedModule.getTransmitPower().toString());
        }

        return savedModule;
    }

    /**
     * 更新模块
     * @param id 模块ID
     * @param moduleDetails 更新后的模块对象
     * @return Module
     * @throws ResourceNotFoundException 如果模块不存在
     */
    @Transactional
    public Module updateModule(Long id, Module moduleDetails) {
        if (id == null) {
            throw new IllegalArgumentException("Module ID cannot be null");
        }
        if (moduleDetails == null) {
            throw new IllegalArgumentException("Module details cannot be null");
        }

        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));

        // 更新字段并记录变更历史
        if (moduleDetails.getSerialNumber() != null && !moduleDetails.getSerialNumber().equals(module.getSerialNumber())) {
            String oldValue = module.getSerialNumber();
            module.setSerialNumber(moduleDetails.getSerialNumber());
            recordFieldHistory(id, "UPDATE", "serialNumber", oldValue, moduleDetails.getSerialNumber());
        }

        if (moduleDetails.getManufacturer() != null && !moduleDetails.getManufacturer().equals(module.getManufacturer())) {
            String oldValue = module.getManufacturer();
            module.setManufacturer(moduleDetails.getManufacturer());
            recordFieldHistory(id, "UPDATE", "manufacturer", oldValue, moduleDetails.getManufacturer());
        }

        if (moduleDetails.getModelNumber() != null && !moduleDetails.getModelNumber().equals(module.getModelNumber())) {
            String oldValue = module.getModelNumber();
            module.setModelNumber(moduleDetails.getModelNumber());
            recordFieldHistory(id, "UPDATE", "modelNumber", oldValue, moduleDetails.getModelNumber());
        }

        if (moduleDetails.getWavelength() != null && !moduleDetails.getWavelength().equals(module.getWavelength())) {
            String oldValue = module.getWavelength() != null ? module.getWavelength().toString() : null;
            module.setWavelength(moduleDetails.getWavelength());
            recordFieldHistory(id, "UPDATE", "wavelength", oldValue, moduleDetails.getWavelength().toString());
        }

        if (moduleDetails.getTransmitPower() != null && !moduleDetails.getTransmitPower().equals(module.getTransmitPower())) {
            String oldValue = module.getTransmitPower() != null ? module.getTransmitPower().toString() : null;
            module.setTransmitPower(moduleDetails.getTransmitPower());
            recordFieldHistory(id, "UPDATE", "transmitPower", oldValue, moduleDetails.getTransmitPower().toString());
        }

        if (moduleDetails.getReceiveSensitivity() != null && !moduleDetails.getReceiveSensitivity().equals(module.getReceiveSensitivity())) {
            String oldValue = module.getReceiveSensitivity() != null ? module.getReceiveSensitivity().toString() : null;
            module.setReceiveSensitivity(moduleDetails.getReceiveSensitivity());
            recordFieldHistory(id, "UPDATE", "receiveSensitivity", oldValue, moduleDetails.getReceiveSensitivity().toString());
        }

        if (moduleDetails.getTransmissionDistance() != null && !moduleDetails.getTransmissionDistance().equals(module.getTransmissionDistance())) {
            String oldValue = module.getTransmissionDistance() != null ? module.getTransmissionDistance().toString() : null;
            module.setTransmissionDistance(moduleDetails.getTransmissionDistance());
            recordFieldHistory(id, "UPDATE", "transmissionDistance", oldValue, moduleDetails.getTransmissionDistance().toString());
        }

        if (moduleDetails.getFiberType() != null && !moduleDetails.getFiberType().equals(module.getFiberType())) {
            String oldValue = module.getFiberType();
            module.setFiberType(moduleDetails.getFiberType());
            recordFieldHistory(id, "UPDATE", "fiberType", oldValue, moduleDetails.getFiberType());
        }

        if (moduleDetails.getConnectorType() != null && !moduleDetails.getConnectorType().equals(module.getConnectorType())) {
            String oldValue = module.getConnectorType();
            module.setConnectorType(moduleDetails.getConnectorType());
            recordFieldHistory(id, "UPDATE", "connectorType", oldValue, moduleDetails.getConnectorType());
        }

        if (moduleDetails.getTemperatureRange() != null && !moduleDetails.getTemperatureRange().equals(module.getTemperatureRange())) {
            String oldValue = module.getTemperatureRange();
            module.setTemperatureRange(moduleDetails.getTemperatureRange());
            recordFieldHistory(id, "UPDATE", "temperatureRange", oldValue, moduleDetails.getTemperatureRange());
        }

        if (moduleDetails.getVoltage() != null && !moduleDetails.getVoltage().equals(module.getVoltage())) {
            String oldValue = module.getVoltage() != null ? module.getVoltage().toString() : null;
            module.setVoltage(moduleDetails.getVoltage());
            recordFieldHistory(id, "UPDATE", "voltage", oldValue, moduleDetails.getVoltage().toString());
        }

        if (moduleDetails.getPowerConsumption() != null && !moduleDetails.getPowerConsumption().equals(module.getPowerConsumption())) {
            String oldValue = module.getPowerConsumption() != null ? module.getPowerConsumption().toString() : null;
            module.setPowerConsumption(moduleDetails.getPowerConsumption());
            recordFieldHistory(id, "UPDATE", "powerConsumption", oldValue, moduleDetails.getPowerConsumption().toString());
        }

        // 更新时间戳
        module.setUpdatedAt(LocalDateTime.now());

        return moduleRepository.save(module);
    }

    /**
     * 删除模块
     * @param id 模块ID
     * @throws ResourceNotFoundException 如果模块不存在
     */
    @Transactional
    public void deleteModule(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Module ID cannot be null");
        }

        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + id));

        // 记录删除历史
        recordFieldHistory(id, "DELETE", "serialNumber", module.getSerialNumber(), null);

        moduleRepository.deleteById(id);
    }

    /**
     * 获取模块的历史记录
     * @param id 模块ID
     * @return List<History>
     * @throws ResourceNotFoundException 如果模块不存在
     */
    public List<History> getModuleHistory(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Module ID cannot be null");
        }

        // 验证模块是否存在
        if (!moduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Module not found with id: " + id);
        }

        return historyRepository.findByModuleIdOrderByCreatedAtDesc(id);
    }

    /**
     * 记录字段变更历史
     * @param moduleId 模块ID
     * @param operation 操作类型
     * @param fieldName 字段名
     * @param oldValue 旧值
     * @param newValue 新值
     */
    private void recordFieldHistory(Long moduleId, String operation, String fieldName, String oldValue, String newValue) {
        History history = new History(moduleId, operation, fieldName, oldValue, newValue);
        historyRepository.save(history);
    }
}
