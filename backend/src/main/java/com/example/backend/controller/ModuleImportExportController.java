package com.example.backend.controller;

import com.example.backend.dto.ModuleDto;
import com.example.backend.service.ModuleService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 光模块导入导出控制器（使用 Apache POI 处理 Excel 文件）
 */
@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "*")
public class ModuleImportExportController {

    private static final DateTimeFormatter DT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 导出列头顺序（与导入列顺序一致） */
    private static final String[] HEADERS = {
        "序列号", "型号", "端口速率", "波长",
        "传输距离(m)", "接口类型", "入库时间", "备注"
    };

    @Autowired
    private ModuleService moduleService;

    /**
     * 导出光模块列表为 Excel (.xlsx)
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportModules(
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String speed) throws IOException {

        // 取全部数据（最多 10000 条，防止内存溢出）
        Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "id"));
        Page<ModuleDto> page;
        if (serialNumber != null || model != null || speed != null) {
            page = moduleService.searchModules(serialNumber, model, speed, pageable);
        } else {
            page = moduleService.getModules(pageable);
        }
        List<ModuleDto> modules = page.getContent();

        byte[] bytes = buildExcel(modules);

        String filename = "modules_" + System.currentTimeMillis() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    /**
     * 从 Excel (.xlsx) 导入光模块
     */
    @PostMapping("/import")
    public ResponseEntity<ImportResult> importModules(@RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ImportResult(0, 0, List.of("上传文件不能为空")));
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || (!originalName.endsWith(".xlsx") && !originalName.endsWith(".xls"))) {
            return ResponseEntity.badRequest()
                    .body(new ImportResult(0, 0, List.of("仅支持 .xlsx / .xls 格式")));
        }

        List<ModuleDto> toImport = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 跳过表头行
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;
                // 跳过空行
                if (isRowEmpty(row)) continue;

                try {
                    ModuleDto dto = parseRow(row);
                    toImport.add(dto);
                } catch (Exception e) {
                    errors.add("第 " + rowNum + " 行: " + e.getMessage());
                }
            }
        }

        if (toImport.isEmpty() && !errors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ImportResult(0, errors.size(), errors));
        }

        List<ModuleDto> imported = moduleService.batchInbound(toImport);
        int successCount = imported.size();
        int failCount = toImport.size() - successCount + errors.size();

        return ResponseEntity.ok(new ImportResult(successCount, failCount, errors));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private byte[] buildExcel(List<ModuleDto> modules) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("光模块列表");

            // 表头
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            int rowIdx = 1;
            for (ModuleDto m : modules) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nullSafe(m.getSerialNumber()));
                row.createCell(1).setCellValue(nullSafe(m.getModel()));
                row.createCell(2).setCellValue(nullSafe(m.getSpeed()));
                row.createCell(3).setCellValue(nullSafe(m.getWavelength()));
                row.createCell(4).setCellValue(
                        m.getTransmissionDistance() != null ? m.getTransmissionDistance() : 0);
                row.createCell(5).setCellValue(nullSafe(m.getConnectorType()));
                row.createCell(6).setCellValue(
                        m.getInboundTime() != null ? m.getInboundTime().format(DT_FORMATTER) : "");
                row.createCell(7).setCellValue(nullSafe(m.getRemark()));
            }

            // 自动列宽
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private ModuleDto parseRow(Row row) {
        ModuleDto dto = new ModuleDto();

        String serialNumber = getCellString(row, 0);
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("序列号不能为空");
        }
        dto.setSerialNumber(serialNumber.trim());

        String model = getCellString(row, 1);
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("型号不能为空");
        }
        dto.setModel(model.trim());

        dto.setSpeed(getCellString(row, 2));
        dto.setWavelength(getCellString(row, 3));

        Cell distCell = row.getCell(4);
        if (distCell != null) {
            try {
                dto.setTransmissionDistance((int) distCell.getNumericCellValue());
            } catch (Exception ignored) {
                // 非数字则忽略
            }
        }

        dto.setConnectorType(getCellString(row, 5));

        String inboundTimeStr = getCellString(row, 6);
        if (inboundTimeStr != null && !inboundTimeStr.isBlank()) {
            try {
                dto.setInboundTime(LocalDateTime.parse(inboundTimeStr.trim(), DT_FORMATTER));
            } catch (DateTimeParseException e) {
                // 解析失败则忽略，使用 @PrePersist 默认值
            }
        }

        dto.setRemark(getCellString(row, 7));

        return dto;
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().format(DT_FORMATTER);
                }
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String v = getCellString(row, i);
                if (v != null && !v.isBlank()) return false;
            }
        }
        return true;
    }

    private String nullSafe(String s) {
        return s != null ? s : "";
    }

    /**
     * 导入结果 DTO（内部类）
     */
    public static class ImportResult {
        private int successCount;
        private int failCount;
        private List<String> errors;

        public ImportResult(int successCount, int failCount, List<String> errors) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.errors = errors;
        }

        public int getSuccessCount() { return successCount; }
        public int getFailCount() { return failCount; }
        public List<String> getErrors() { return errors; }
    }
}
