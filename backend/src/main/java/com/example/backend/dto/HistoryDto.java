package com.example.backend.dto;

import com.example.backend.model.ModuleStatus;
import com.example.backend.model.OperationType;
import java.time.LocalDateTime;

/**
 * 操作历史数据传输对象
 */
public class HistoryDto {

    private Long id;

    private Long moduleId;

    private OperationType operationType;

    private LocalDateTime operationTime;

    private String operator;

    private ModuleStatus previousStatus;

    private ModuleStatus nextStatus;

    private String remark;

    private String changeDetails;

    private LocalDateTime createdAt;

    // For enriched queries with module information
    private String serialNumber;
    private String model;

    // Constructors
    public HistoryDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public ModuleStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ModuleStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public ModuleStatus getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(ModuleStatus nextStatus) {
        this.nextStatus = nextStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getChangeDetails() {
        return changeDetails;
    }

    public void setChangeDetails(String changeDetails) {
        this.changeDetails = changeDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
