package com.example.backend.model;

/**
 * 光模块状态枚举
 */
public enum ModuleStatus {
    /**
     * 在库（待用）
     */
    IN_STOCK,

    /**
     * 已部署（在用）
     */
    DEPLOYED,

    /**
     * 故障
     */
    FAULTY,

    /**
     * 维修中
     */
    UNDER_REPAIR,

    /**
     * 已报废
     */
    SCRAPPED
}
