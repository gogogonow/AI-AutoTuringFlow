package com.example.backend.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "modules")
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(nullable = false)
    private String status;

    @Column
    private String vendor;

    @Column(name = "process_status")
    private String processStatus;

    @Column(name = "enter_time")
    private LocalDateTime enterTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "ld")
    private String LD;

    @Column(name = "pd")
    private String PD;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    // Constructors
    public Module() {
        this.createTime = LocalDateTime.now();
    }

    public Module(String code, String status) {
        this.code = code;
        this.status = status;
        this.createTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public LocalDateTime getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(LocalDateTime enterTime) {
        this.enterTime = enterTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public String getLD() {
        return LD;
    }

    public void setLD(String LD) {
        this.LD = LD;
    }

    public String getPD() {
        return PD;
    }

    public void setPD(String PD) {
        this.PD = PD;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return Objects.equals(id, module.id) && Objects.equals(code, module.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "Module{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", status='" + status + '\'' +
                ", vendor='" + vendor + '\'' +
                '}';
    }
}
