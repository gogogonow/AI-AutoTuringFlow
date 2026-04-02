package com.example.backend.repository;

import com.example.backend.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {
    
    /**
     * 根据模块代码查找模块
     * @param code 模块代码
     * @return Optional<Module>
     */
    Optional<Module> findByCode(String code);
    
    /**
     * 检查代码是否已存在
     * @param code 模块代码
     * @return boolean
     */
    boolean existsByCode(String code);
}
