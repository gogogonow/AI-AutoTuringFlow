package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 操作历史实体类
 * 记录每一次光模块状态变更或操作事件
 */
@Entity
@Table(name = "history", indexes = {
    @Index(name = "idx_module_id", columnList = "module_id"),
    @Index(name = "idx_operation_time", columnList = "operation_time"),
    @Index(name = "idx_operation_type", columnList = "operation_type")
})
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, columnDefinition = "VARCHAR(50)")
    private OperationType operationType;

    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    @Column(name = "operator", length = 100)
    private String operator;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "change_details", columnDefinition = "TEXT")
    private String changeDetails;

    @Column(name = "serial_number", length = 50)
    private String serialNumber;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.operationTime == null) {
            this.operationTime = LocalDateTime.now();
        }
    }

    // Constructors
    public History() {}

    public History(Long moduleId, OperationType operationType, String operator) {
        this.moduleId = moduleId;
        this.operationType = operationType;
        this.operator = operator;
    }

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

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", moduleId=" + moduleId +
                ", operationType=" + operationType +
                ", operationTime=" + operationTime +
                ", operator='" + operator + '\'' +
                '}';
    }
}
