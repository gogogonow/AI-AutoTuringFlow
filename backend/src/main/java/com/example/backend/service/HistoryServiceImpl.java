package com.example.backend.service;

import com.example.backend.dto.HistoryDto;
import com.example.backend.model.History;
import com.example.backend.model.Module;
import com.example.backend.model.OperationType;
import com.example.backend.repository.HistoryRepository;
import com.example.backend.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作历史服务实现类
 */
@Service
@Transactional
public class HistoryServiceImpl implements HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Override
    public HistoryDto createHistory(
        Long moduleId,
        OperationType operationType,
        String operator,
        String remark,
        String changeDetails,
        String serialNumber,
        String model
    ) {
        History history = new History();
        history.setModuleId(moduleId);
        history.setOperationType(operationType);
        history.setOperator(operator);
        history.setRemark(remark);
        history.setChangeDetails(changeDetails);
        history.setSerialNumber(serialNumber);
        history.setModel(model);
        history.setOperationTime(LocalDateTime.now());

        History savedHistory = historyRepository.save(history);
        return toDto(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryDto getHistoryById(Long id) {
        History history = historyRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("历史记录不存在: ID=" + id));
        return toDto(history);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoryDto> getAllHistories(Pageable pageable) {
        return historyRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryDto> getHistoriesByModuleId(Long moduleId) {
        return historyRepository.findByModuleIdOrderByOperationTimeDesc(moduleId)
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoryDto> getHistoriesByModuleId(Long moduleId, Pageable pageable) {
        return historyRepository.findByModuleId(moduleId, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoryDto> searchHistories(
        Long moduleId,
        OperationType operationType,
        String operator,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Pageable pageable
    ) {
        return historyRepository.findByMultipleConditions(
            moduleId,
            operationType,
            operator,
            startTime,
            endTime,
            pageable
        ).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<OperationType, Long> getOperationTypeStatistics() {
        List<Object[]> results = historyRepository.countByOperationType();
        return results.stream()
            .collect(Collectors.toMap(
                row -> (OperationType) row[0],
                row -> (Long) row[1],
                (v1, v2) -> v1,
                LinkedHashMap::new
            ));
    }

    @Override
    public HistoryDto toDto(History history) {
        if (history == null) return null;

        HistoryDto dto = new HistoryDto();
        dto.setId(history.getId());
        dto.setModuleId(history.getModuleId());
        dto.setOperationType(history.getOperationType());
        dto.setOperationTime(history.getOperationTime());
        dto.setOperator(history.getOperator());
        dto.setRemark(history.getRemark());
        dto.setChangeDetails(history.getChangeDetails());
        dto.setCreatedAt(history.getCreatedAt());

        // Use stored serial number and model if available
        if (history.getSerialNumber() != null && history.getModel() != null) {
            dto.setSerialNumber(history.getSerialNumber());
            dto.setModel(history.getModel());
        } else {
            // Fallback: enrich with module information from join
            moduleRepository.findById(history.getModuleId()).ifPresent(module -> {
                dto.setSerialNumber(module.getSerialNumber());
                dto.setModel(module.getModel());
            });
        }

        return dto;
    }
}
