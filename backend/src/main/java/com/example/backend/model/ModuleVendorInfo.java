package com.example.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 光模块厂家信息实体类
 * 与光模块为多对一关系（一个光模块可对应多个厂家信息）
 */
@Entity
@Table(name = "module_vendor_info", indexes = {
    @Index(name = "idx_mvi_module_id", columnList = "module_id")
})
public class ModuleVendorInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    /** 厂家名称 */
    @Column(name = "vendor", nullable = false, length = 100)
    private String vendor;

    /** 流程状态 */
    @Column(name = "process_status", length = 50)
    private String processStatus;

    /** 版本/批次标识 (用于区分同一厂家的不同供货时期或版本) */
    @Column(name = "version_batch", length = 100)
    private String versionBatch;

    /** 进入时间 */
    @Column(name = "entry_time")
    private LocalDateTime entryTime;

    /** 退出时间 */
    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    /** LD芯片供应商/型号 */
    @Column(name = "ld", length = 200)
    private String ld;

    /** PD芯片供应商/型号 */
    @Column(name = "pd", length = 200)
    private String pd;

    /** LA+LDO芯片供应商/型号 */
    @Column(name = "la_ldo", length = 200)
    private String laLdo;

    /** TIA芯片供应商/型号 */
    @Column(name = "tia", length = 200)
    private String tia;

    /** MCU芯片供应商/型号 */
    @Column(name = "mcu", length = 200)
    private String mcu;

    /** PCN变更点 */
    @Column(name = "pcn_changes", columnDefinition = "TEXT")
    private String pcnChanges;

    /** 是否建议高速重点测试 */
    @Column(name = "high_speed_test_recommended")
    private Boolean highSpeedTestRecommended;

    /** 获取性 */
    @Column(name = "availability", length = 100)
    private String availability;

    /** 电眼数据 */
    @Column(name = "photodetector_data", columnDefinition = "TEXT")
    private String photodetectorData;

    /** 电眼数据文件路径 */
    @Column(name = "photodetector_data_file", length = 500)
    private String photodetectorDataFile;

    /** 目前已知已覆盖过的单板 */
    @Column(name = "covered_boards", columnDefinition = "TEXT")
    private String coveredBoards;

    /** 导入测试报告链接 */
    @Column(name = "test_report_link", length = 500)
    private String testReportLink;

    /** 备注 */
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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public ModuleVendorInfo() {}

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

    public String getPhotodetectorDataFile() { return photodetectorDataFile; }
    public void setPhotodetectorDataFile(String photodetectorDataFile) { this.photodetectorDataFile = photodetectorDataFile; }

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

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
