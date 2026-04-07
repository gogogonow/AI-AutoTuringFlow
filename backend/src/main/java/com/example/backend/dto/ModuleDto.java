package com.example.backend.dto;

import com.example.backend.model.ModuleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 光模块数据传输对象
 */
public class ModuleDto {

    private Long id;

    @NotBlank(message = "序列号不能为空")
    @Size(min = 6, max = 50, message = "序列号长度必须在6-50之间")
    @Pattern(regexp = "^\\S+$", message = "序列号不能包含空格")
    private String serialNumber;

    @NotBlank(message = "型号不能为空")
    @Size(max = 100, message = "型号长度不能超过100")
    private String model;

    @NotBlank(message = "供应商不能为空")
    @Size(max = 100, message = "供应商长度不能超过100")
    private String vendor;

    private String speed;

    private String wavelength;

    private Integer transmissionDistance;

    private String connectorType;

    private ModuleStatus status;

    private LocalDateTime inboundTime;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public ModuleDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getWavelength() {
        return wavelength;
    }

    public void setWavelength(String wavelength) {
        this.wavelength = wavelength;
    }

    public Integer getTransmissionDistance() {
        return transmissionDistance;
    }

    public void setTransmissionDistance(Integer transmissionDistance) {
        this.transmissionDistance = transmissionDistance;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public ModuleStatus getStatus() {
        return status;
    }

    public void setStatus(ModuleStatus status) {
        this.status = status;
    }

    public LocalDateTime getInboundTime() {
        return inboundTime;
    }

    public void setInboundTime(LocalDateTime inboundTime) {
        this.inboundTime = inboundTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
