package com.example.backend.repository;

import com.example.backend.model.ModuleVendorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 光模块厂家信息数据访问层
 */
@Repository
public interface ModuleVendorInfoRepository extends JpaRepository<ModuleVendorInfo, Long> {

    /**
     * 根据ID查找厂家信息（排除已删除）
     */
    @Query("SELECT v FROM ModuleVendorInfo v WHERE v.id = :id AND v.deleted = false")
    Optional<ModuleVendorInfo> findById(@Param("id") Long id);

    /**
     * 根据模块ID查询所有厂家信息（排除已删除）
     */
    @Query("SELECT v FROM ModuleVendorInfo v WHERE v.moduleId = :moduleId AND v.deleted = false ORDER BY v.createdAt ASC")
    List<ModuleVendorInfo> findByModuleIdOrderByCreatedAtAsc(@Param("moduleId") Long moduleId);

    /**
     * 删除指定模块的所有厂家信息（用于模块删除的级联操作，实际已改为软删除）
     */
    @Modifying
    @Query("UPDATE ModuleVendorInfo v SET v.deleted = true, v.deletedAt = CURRENT_TIMESTAMP WHERE v.moduleId = :moduleId AND v.deleted = false")
    void deleteByModuleId(@Param("moduleId") Long moduleId);
}
