package com.example.backend.repository;

import com.example.backend.model.Module;
import com.example.backend.model.ModuleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 光模块数据访问层
 */
@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    /**
     * 根据序列号查找光模块
     */
    Optional<Module> findBySerialNumber(String serialNumber);

    /**
     * 检查序列号是否存在
     */
    boolean existsBySerialNumber(String serialNumber);

    /**
     * 根据状态查找光模块列表
     */
    Page<Module> findByStatus(ModuleStatus status, Pageable pageable);

    /**
     * 根据型号查找光模块列表
     */
    Page<Module> findByModelContaining(String model, Pageable pageable);

    /**
     * 根据供应商查找光模块列表
     */
    Page<Module> findByVendorContaining(String vendor, Pageable pageable);

    /**
     * 多条件组合查询
     */
    @Query("SELECT m FROM Module m WHERE " +
           "(:serialNumber IS NULL OR m.serialNumber LIKE %:serialNumber%) AND " +
           "(:model IS NULL OR m.model LIKE %:model%) AND " +
           "(:vendor IS NULL OR m.vendor LIKE %:vendor%) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:speed IS NULL OR m.speed = :speed)")
    Page<Module> findByMultipleConditions(
        @Param("serialNumber") String serialNumber,
        @Param("model") String model,
        @Param("vendor") String vendor,
        @Param("status") ModuleStatus status,
        @Param("speed") String speed,
        Pageable pageable
    );

    /**
     * 统计各状态的光模块数量
     */
    @Query("SELECT m.status, COUNT(m) FROM Module m GROUP BY m.status")
    List<Object[]> countByStatus();

    /**
     * 统计供应商的光模块数量
     */
    @Query("SELECT m.vendor, COUNT(m) FROM Module m GROUP BY m.vendor ORDER BY COUNT(m) DESC")
    List<Object[]> countByVendor();
}
