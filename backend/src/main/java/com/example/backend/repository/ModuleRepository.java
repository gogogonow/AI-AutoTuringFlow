package com.example.backend.repository;

import com.example.backend.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    
    /**
     * 根据序列号查找模块
     * @param serialNumber 序列号
     * @return Optional<Module>
     */
    Optional<Module> findBySerialNumber(String serialNumber);
    
    /**
     * 检查序列号是否已存在
     * @param serialNumber 序列号
     * @return boolean
     */
    boolean existsBySerialNumber(String serialNumber);
}
