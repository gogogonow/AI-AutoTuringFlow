package com.example.backend.repository;

import com.example.backend.model.History;
import com.example.backend.model.OperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作历史数据访问层
 */
@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    /**
     * 根据模块ID查找历史记录（分页）
     */
    Page<History> findByModuleId(Long moduleId, Pageable pageable);

    /**
     * 根据模块ID查找历史记录（列表，按时间倒序）
     */
    List<History> findByModuleIdOrderByOperationTimeDesc(Long moduleId);

    /**
     * 根据操作类型查找历史记录
     */
    Page<History> findByOperationType(OperationType operationType, Pageable pageable);

    /**
     * 根据操作人查找历史记录
     */
    Page<History> findByOperator(String operator, Pageable pageable);

    /**
     * 根据时间范围查找历史记录
     */
    Page<History> findByOperationTimeBetween(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Pageable pageable
    );

    /**
     * 多条件组合查询
     */
    @Query("SELECT h FROM History h WHERE " +
           "(:moduleId IS NULL OR h.moduleId = :moduleId) AND " +
           "(:operationType IS NULL OR h.operationType = :operationType) AND " +
           "(:operator IS NULL OR h.operator LIKE %:operator%) AND " +
           "(:startTime IS NULL OR h.operationTime >= :startTime) AND " +
           "(:endTime IS NULL OR h.operationTime <= :endTime)")
    Page<History> findByMultipleConditions(
        @Param("moduleId") Long moduleId,
        @Param("operationType") OperationType operationType,
        @Param("operator") String operator,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );

    /**
     * 统计各操作类型的数量
     */
    @Query("SELECT h.operationType, COUNT(h) FROM History h GROUP BY h.operationType")
    List<Object[]> countByOperationType();

    /**
     * 删除指定模块的所有历史记录
     */
    void deleteByModuleId(Long moduleId);
}
