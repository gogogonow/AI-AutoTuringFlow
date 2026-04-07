package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 光模块厂家信息数据传输对象
 */
public class ModuleVendorInfoDto {

    private Long id;

    private Long moduleId;

    @NotBlank(message = "厂家名称不能为空")
    @Size(max = 100, message = "厂家名称长度不能超过100")
    private String vendor;

    private String processStatus;

    private String versionBatch;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private String ld;

    private String pd;

    private String laLdo;

    private String tia;

    private String mcu;

    private String pcnChanges;

    private Boolean highSpeedTestRecommended;

    private String availability;

    private String photodetectorData;

    private String coveredBoards;

    private String testReportLink;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public ModuleVendorInfoDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }

    public String getVersionBatch() { return versionBatch; }
    public void setVersionBatch(String versionBatch) { this.versionBatch = versionBatch; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public String getLd() { return ld; }
    public void setLd(String ld) { this.ld = ld; }

    public String getPd() { return pd; }
    public void setPd(String pd) { this.pd = pd; }

    public String getLaLdo() { return laLdo; }
    public void setLaLdo(String laLdo) { this.laLdo = laLdo; }

    public String getTia() { return tia; }
    public void setTia(String tia) { this.tia = tia; }

    public String getMcu() { return mcu; }
    public void setMcu(String mcu) { this.mcu = mcu; }

    public String getPcnChanges() { return pcnChanges; }
    public void setPcnChanges(String pcnChanges) { this.pcnChanges = pcnChanges; }

    public Boolean getHighSpeedTestRecommended() { return highSpeedTestRecommended; }
    public void setHighSpeedTestRecommended(Boolean highSpeedTestRecommended) {
        this.highSpeedTestRecommended = highSpeedTestRecommended;
    }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getPhotodetectorData() { return photodetectorData; }
    public void setPhotodetectorData(String photodetectorData) { this.photodetectorData = photodetectorData; }

    public String getCoveredBoards() { return coveredBoards; }
    public void setCoveredBoards(String coveredBoards) { this.coveredBoards = coveredBoards; }

    public String getTestReportLink() { return testReportLink; }
    public void setTestReportLink(String testReportLink) { this.testReportLink = testReportLink; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
