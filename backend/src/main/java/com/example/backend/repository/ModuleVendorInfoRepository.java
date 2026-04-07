package com.example.backend.repository;

import com.example.backend.model.ModuleVendorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 光模块厂家信息数据访问层
 */
@Repository
public interface ModuleVendorInfoRepository extends JpaRepository<ModuleVendorInfo, Long> {

    /**
     * 根据模块ID查询所有厂家信息
     */
    List<ModuleVendorInfo> findByModuleIdOrderByCreatedAtAsc(Long moduleId);

    /**
     * 删除指定模块的所有厂家信息
     */
    void deleteByModuleId(Long moduleId);
}
