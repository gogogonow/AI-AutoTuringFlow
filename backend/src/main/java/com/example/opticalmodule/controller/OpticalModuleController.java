package com.example.opticalmodule.controller;

import com.example.opticalmodule.model.OpticalModule;
import com.example.opticalmodule.service.OpticalModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class OpticalModuleController {

    @Autowired
    private OpticalModuleService opticalModuleService;

    @GetMapping
    public ResponseEntity<List<OpticalModule>> getAllModules(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "filter", required = false) String filter) {
        return ResponseEntity.ok(opticalModuleService.getAllModules(page, size, filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpticalModule> getModuleById(@PathVariable Long id) {
        return ResponseEntity.ok(opticalModuleService.getModuleById(id));
    }

    @PostMapping
    public ResponseEntity<OpticalModule> createModule(@RequestBody OpticalModule opticalModule) {
        return ResponseEntity.ok(opticalModuleService.createModule(opticalModule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpticalModule> updateModule(@PathVariable Long id, @RequestBody OpticalModule opticalModule) {
        return ResponseEntity.ok(opticalModuleService.updateModule(id, opticalModule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        opticalModuleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
}