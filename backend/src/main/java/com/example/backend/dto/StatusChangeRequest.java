package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 状态变更请求DTO
 */
public class StatusChangeRequest {

    @NotNull(message = "操作类型不能为空")
    private String action;

    @NotBlank(message = "操作人不能为空")
    private String operator;

    private String remark;

    // For deployment action
    private String targetDevice;

    // Constructors
    public StatusChangeRequest() {}

    // Getters and Setters
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getTargetDevice() {
        return targetDevice;
    }

    public void setTargetDevice(String targetDevice) {
        this.targetDevice = targetDevice;
    }
}
