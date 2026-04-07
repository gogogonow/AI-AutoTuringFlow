package com.example.backend.controller;

import com.example.backend.dto.HistoryDto;
import com.example.backend.model.OperationType;
import com.example.backend.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 操作历史控制器
 */
@RestController
@RequestMapping("/api/histories")
@CrossOrigin(origins = "*")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    /**
     * 根据ID获取历史记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<HistoryDto> getHistoryById(@PathVariable Long id) {
        HistoryDto history = historyService.getHistoryById(id);
        return ResponseEntity.ok(history);
    }

    /**
     * 分页查询所有历史记录
     */
    @GetMapping
    public ResponseEntity<Page<HistoryDto>> getAllHistories(
        @RequestParam(required = false) Long moduleId,
        @RequestParam(required = false) OperationType operationType,
        @RequestParam(required = false) String operator,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "operationTime") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<HistoryDto> histories;
        if (moduleId != null || operationType != null || operator != null || startTime != null || endTime != null) {
            histories = historyService.searchHistories(moduleId, operationType, operator, startTime, endTime, pageable);
        } else {
            histories = historyService.getAllHistories(pageable);
        }

        return ResponseEntity.ok(histories);
    }

    /**
     * 根据模块ID获取历史记录（列表）
     */
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<HistoryDto>> getHistoriesByModuleId(@PathVariable Long moduleId) {
        List<HistoryDto> histories = historyService.getHistoriesByModuleId(moduleId);
        return ResponseEntity.ok(histories);
    }

    /**
     * 根据模块ID分页获取历史记录
     */
    @GetMapping("/module/{moduleId}/page")
    public ResponseEntity<Page<HistoryDto>> getHistoriesByModuleIdPaged(
        @PathVariable Long moduleId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<HistoryDto> histories = historyService.getHistoriesByModuleId(moduleId, pageable);
        return ResponseEntity.ok(histories);
    }

    /**
     * 统计各操作类型的数量
     */
    @GetMapping("/statistics/operation-type")
    public ResponseEntity<Map<OperationType, Long>> getOperationTypeStatistics() {
        Map<OperationType, Long> stats = historyService.getOperationTypeStatistics();
        return ResponseEntity.ok(stats);
    }
}
