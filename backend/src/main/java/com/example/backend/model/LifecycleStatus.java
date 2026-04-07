package com.example.backend.model;

/**
 * 光模块生命周期状态枚举
 * Optical module lifecycle status
 */
public enum LifecycleStatus {
    /**
     * GA - Generally Available (正式发布)
     */
    GA,

    /**
     * EOM - End of Marketing (停止销售)
     */
    EOM,

    /**
     * EOP - End of Production (停止生产)
     */
    EOP
}
