package com.example.backend.model;

/**
 * 操作类型枚举
 */
public enum OperationType {
    /**
     * 入库
     */
    INBOUND,

    /**
     * 出库
     */
    OUTBOUND,

    /**
     * 部署
     */
    DEPLOY,

    /**
     * 收回（从设备卸下回库）
     */
    RETRIEVE,

    /**
     * 标记故障
     */
    MARK_FAULTY,

    /**
     * 送修
     */
    SEND_REPAIR,

    /**
     * 维修归还
     */
    RETURN_REPAIR,

    /**
     * 报废
     */
    SCRAP,

    /**
     * 更新信息
     */
    UPDATE_INFO,

    /**
     * 新增厂家信息
     */
    VENDOR_ADD,

    /**
     * 更新厂家信息
     */
    VENDOR_UPDATE,

    /**
     * 删除厂家信息
     */
    VENDOR_DELETE,

    /**
     * 删除光模块
     */
    DELETE_MODULE
}
