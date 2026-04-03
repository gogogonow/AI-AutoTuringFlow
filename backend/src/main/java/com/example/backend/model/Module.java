package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "modules", indexes = {
    @Index(name = "idx_serial_number", columnList = "serial_number"),
    @Index(name = "idx_manufacturer", columnList = "manufacturer")
})
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Column(nullable = false, length = 100)
    private String manufacturer;

    @Column(name = "model_number", length = 100)
    private String modelNumber;

    @Column
    private Double wavelength;

    @Column(name = "transmit_power")
    private Double transmitPower;

    @Column(name = "receive_sensitivity")
    private Double receiveSensitivity;

    @Column(name = "transmission_distance")
    private Double transmissionDistance;

    @Column(name = "fiber_type", length = 50)
    private String fiberType;

    @Column(name = "connector_type", length = 50)
    private String connectorType;

    @Column(name = "temperature_range", length = 50)
    private String temperatureRange;

    @Column
    private Double voltage;

    @Column(name = "power_consumption")
    private Double powerConsumption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Module() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Module(String serialNumber, String manufacturer) {
        this.serialNumber = serialNumber;
        this.manufacturer = manufacturer;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public Double getWavelength() {
        return wavelength;
    }

    public void setWavelength(Double wavelength) {
        this.wavelength = wavelength;
    }

    public Double getTransmitPower() {
        return transmitPower;
    }

    public void setTransmitPower(Double transmitPower) {
        this.transmitPower = transmitPower;
    }

    public Double getReceiveSensitivity() {
        return receiveSensitivity;
    }

    public void setReceiveSensitivity(Double receiveSensitivity) {
        this.receiveSensitivity = receiveSensitivity;
    }

    public Double getTransmissionDistance() {
        return transmissionDistance;
    }

    public void setTransmissionDistance(Double transmissionDistance) {
        this.transmissionDistance = transmissionDistance;
    }

    public String getFiberType() {
        return fiberType;
    }

    public void setFiberType(String fiberType) {
        this.fiberType = fiberType;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getTemperatureRange() {
        return temperatureRange;
    }

    public void setTemperatureRange(String temperatureRange) {
        this.temperatureRange = temperatureRange;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Double getPowerConsumption() {
        return powerConsumption;
    }

    public void setPowerConsumption(Double powerConsumption) {
        this.powerConsumption = powerConsumption;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return Objects.equals(id, module.id) && Objects.equals(serialNumber, module.serialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serialNumber);
    }

    @Override
    public String toString() {
        return "Module{" +
                "id=" + id +
                ", serialNumber='" + serialNumber + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", modelNumber='" + modelNumber + '\'' +
                '}';
    }
}
