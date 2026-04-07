package com.example.backend.service;

import com.example.backend.dto.HistoryDto;
import com.example.backend.model.History;
import com.example.backend.model.ModuleStatus;
import com.example.backend.model.OperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作历史服务接口
 */
public interface HistoryService {

    /**
     * 创建历史记录
     */
    HistoryDto createHistory(
        Long moduleId,
        OperationType operationType,
        String operator,
        ModuleStatus previousStatus,
        ModuleStatus nextStatus,
        String remark,
        String changeDetails
    );

    /**
     * 创建历史记录（无变更详情）
     */
    default HistoryDto createHistory(
        Long moduleId,
        OperationType operationType,
        String operator,
        ModuleStatus previousStatus,
        ModuleStatus nextStatus,
        String remark
    ) {
        return createHistory(moduleId, operationType, operator, previousStatus, nextStatus, remark, null);
    }

    /**
     * 根据ID获取历史记录
     */
    HistoryDto getHistoryById(Long id);

    /**
     * 分页查询所有历史记录
     */
    Page<HistoryDto> getAllHistories(Pageable pageable);

    /**
     * 根据模块ID获取历史记录
     */
    List<HistoryDto> getHistoriesByModuleId(Long moduleId);

    /**
     * 根据模块ID分页获取历史记录
     */
    Page<HistoryDto> getHistoriesByModuleId(Long moduleId, Pageable pageable);

    /**
     * 多条件筛选查询
     */
    Page<HistoryDto> searchHistories(
        Long moduleId,
        OperationType operationType,
        String operator,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Pageable pageable
    );

    /**
     * 统计各操作类型的数量
     */
    Map<OperationType, Long> getOperationTypeStatistics();

    /**
     * Entity转DTO
     */
    HistoryDto toDto(History history);
}
