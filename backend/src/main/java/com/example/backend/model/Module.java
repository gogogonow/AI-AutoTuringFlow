package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 光模块实体类
 * 代表一个物理光收发器设备
 */
@Entity
@Table(name = "module", indexes = {
    @Index(name = "idx_serial_number", columnList = "serial_number"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_model", columnList = "model"),
    @Index(name = "idx_vendor", columnList = "vendor")
})
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false, unique = true, length = 50)
    private String serialNumber;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "vendor", nullable = false, length = 100)
    private String vendor;

    @Column(name = "speed", length = 20)
    private String speed;

    @Column(name = "wavelength", length = 20)
    private String wavelength;

    @Column(name = "transmission_distance")
    private Integer transmissionDistance;

    @Column(name = "connector_type", length = 20)
    private String connectorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ModuleStatus status = ModuleStatus.IN_STOCK;

    // New comprehensive fields for optical module specifications

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", length = 20)
    private LifecycleStatus lifecycleStatus;

    @Column(name = "package_form", length = 50)
    private String packageForm;

    @Column(name = "fiber_count")
    private Integer fiberCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "light_type", length = 20)
    private LightType lightType;

    @Column(name = "speed_set", columnDefinition = "TEXT")
    private String speedSet;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiber_type", length = 20)
    private FiberType fiberType;

    @Column(name = "max_power_consumption")
    private Double maxPowerConsumption;

    @Column(name = "min_case_temp")
    private Integer minCaseTemp;

    @Column(name = "max_case_temp")
    private Integer maxCaseTemp;

    @Column(name = "last_shipment_time")
    private LocalDateTime lastShipmentTime;

    @Column(name = "total_shipment_volume")
    private Long totalShipmentVolume;

    @Column(name = "recent_5year_shipment_volume")
    private Long recent5yearShipmentVolume;

    @Column(name = "shipment_regions", columnDefinition = "TEXT")
    private String shipmentRegions;

    @Column(name = "is_mainstream_shipment")
    private Boolean isMainstreamShipment;

    @Column(name = "spec_template_version", length = 50)
    private String specTemplateVersion;

    @Column(name = "current_shipping_vendors", columnDefinition = "TEXT")
    private String currentShippingVendors;

    @Column(name = "inbound_time", nullable = false)
    private LocalDateTime inboundTime;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.inboundTime == null) {
            this.inboundTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Module() {}

    public Module(String serialNumber, String model, String vendor) {
        this.serialNumber = serialNumber;
        this.model = model;
        this.vendor = vendor;
    }

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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public LifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(LifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getPackageForm() {
        return packageForm;
    }

    public void setPackageForm(String packageForm) {
        this.packageForm = packageForm;
    }

    public Integer getFiberCount() {
        return fiberCount;
    }

    public void setFiberCount(Integer fiberCount) {
        this.fiberCount = fiberCount;
    }

    public LightType getLightType() {
        return lightType;
    }

    public void setLightType(LightType lightType) {
        this.lightType = lightType;
    }

    public String getSpeedSet() {
        return speedSet;
    }

    public void setSpeedSet(String speedSet) {
        this.speedSet = speedSet;
    }

    public FiberType getFiberType() {
        return fiberType;
    }

    public void setFiberType(FiberType fiberType) {
        this.fiberType = fiberType;
    }

    public Double getMaxPowerConsumption() {
        return maxPowerConsumption;
    }

    public void setMaxPowerConsumption(Double maxPowerConsumption) {
        this.maxPowerConsumption = maxPowerConsumption;
    }

    public Integer getMinCaseTemp() {
        return minCaseTemp;
    }

    public void setMinCaseTemp(Integer minCaseTemp) {
        this.minCaseTemp = minCaseTemp;
    }

    public Integer getMaxCaseTemp() {
        return maxCaseTemp;
    }

    public void setMaxCaseTemp(Integer maxCaseTemp) {
        this.maxCaseTemp = maxCaseTemp;
    }

    public LocalDateTime getLastShipmentTime() {
        return lastShipmentTime;
    }

    public void setLastShipmentTime(LocalDateTime lastShipmentTime) {
        this.lastShipmentTime = lastShipmentTime;
    }

    public Long getTotalShipmentVolume() {
        return totalShipmentVolume;
    }

    public void setTotalShipmentVolume(Long totalShipmentVolume) {
        this.totalShipmentVolume = totalShipmentVolume;
    }

    public Long getRecent5yearShipmentVolume() {
        return recent5yearShipmentVolume;
    }

    public void setRecent5yearShipmentVolume(Long recent5yearShipmentVolume) {
        this.recent5yearShipmentVolume = recent5yearShipmentVolume;
    }

    public String getShipmentRegions() {
        return shipmentRegions;
    }

    public void setShipmentRegions(String shipmentRegions) {
        this.shipmentRegions = shipmentRegions;
    }

    public Boolean getIsMainstreamShipment() {
        return isMainstreamShipment;
    }

    public void setIsMainstreamShipment(Boolean isMainstreamShipment) {
        this.isMainstreamShipment = isMainstreamShipment;
    }

    public String getSpecTemplateVersion() {
        return specTemplateVersion;
    }

    public void setSpecTemplateVersion(String specTemplateVersion) {
        this.specTemplateVersion = specTemplateVersion;
    }

    public String getCurrentShippingVendors() {
        return currentShippingVendors;
    }

    public void setCurrentShippingVendors(String currentShippingVendors) {
        this.currentShippingVendors = currentShippingVendors;
    }

    @Override
    public String toString() {
        return "Module{" +
                "id=" + id +
                ", serialNumber='" + serialNumber + '\'' +
                ", model='" + model + '\'' +
                ", vendor='" + vendor + '\'' +
                ", status=" + status +
                '}';
    }
}
