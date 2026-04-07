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
     * 根据ID查找光模块（排除已删除）
     */
    @Query("SELECT m FROM Module m WHERE m.id = :id AND m.deleted = false")
    Optional<Module> findById(@Param("id") Long id);

    /**
     * 查找所有光模块（排除已删除）
     */
    @Query("SELECT m FROM Module m WHERE m.deleted = false")
    Page<Module> findAll(Pageable pageable);

    /**
     * 根据序列号查找光模块
     */
    @Query("SELECT m FROM Module m WHERE m.serialNumber = :serialNumber AND m.deleted = false")
    Optional<Module> findBySerialNumber(@Param("serialNumber") String serialNumber);

    /**
     * 检查序列号是否存在
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Module m WHERE m.serialNumber = :serialNumber AND m.deleted = false")
    boolean existsBySerialNumber(@Param("serialNumber") String serialNumber);

    /**
     * 根据状态查找光模块列表
     */
    @Query("SELECT m FROM Module m WHERE m.status = :status AND m.deleted = false")
    Page<Module> findByStatus(@Param("status") ModuleStatus status, Pageable pageable);

    /**
     * 根据型号查找光模块列表
     */
    @Query("SELECT m FROM Module m WHERE m.model LIKE %:model% AND m.deleted = false")
    Page<Module> findByModelContaining(@Param("model") String model, Pageable pageable);

    /**
     * 根据供应商查找光模块列表
     */
    @Query("SELECT m FROM Module m WHERE m.vendor LIKE %:vendor% AND m.deleted = false")
    Page<Module> findByVendorContaining(@Param("vendor") String vendor, Pageable pageable);

    /**
     * 多条件组合查询
     */
    @Query("SELECT m FROM Module m WHERE " +
           "(:serialNumber IS NULL OR m.serialNumber LIKE %:serialNumber%) AND " +
           "(:model IS NULL OR m.model LIKE %:model%) AND " +
           "(:vendor IS NULL OR m.vendor LIKE %:vendor%) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:speed IS NULL OR m.speed = :speed) AND " +
           "m.deleted = false")
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
    @Query("SELECT m.status, COUNT(m) FROM Module m WHERE m.deleted = false GROUP BY m.status")
    List<Object[]> countByStatus();

    /**
     * 统计供应商的光模块数量
     */
    @Query("SELECT m.vendor, COUNT(m) FROM Module m WHERE m.deleted = false GROUP BY m.vendor ORDER BY COUNT(m) DESC")
    List<Object[]> countByVendor();
}
