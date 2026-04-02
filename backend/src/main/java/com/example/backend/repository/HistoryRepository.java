package com.example.backend.repository;

import com.example.backend.model.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {
    
    /**
     * 根据模块ID查找所有历史记录
     * @param moduleId 模块ID
     * @return List<History>
     */
    List<History> findByModuleIdOrderByTimestampDesc(Integer moduleId);
}
