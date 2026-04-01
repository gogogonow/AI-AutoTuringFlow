package com.example.opticalmodule.controller;

import com.example.opticalmodule.model.OpticalModule;
import com.example.opticalmodule.service.OpticalModuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpticalModuleControllerTest {

    @InjectMocks
    private OpticalModuleController opticalModuleController;

    @Mock
    private OpticalModuleService opticalModuleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllModules() {
        List<OpticalModule> mockModules = Arrays.asList(new OpticalModule(), new OpticalModule());
        when(opticalModuleService.getAllModules(0, 10, null)).thenReturn(mockModules);

        ResponseEntity<List<OpticalModule>> response = opticalModuleController.getAllModules(0, 10, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetModuleById() {
        OpticalModule mockModule = new OpticalModule();
        when(opticalModuleService.getModuleById(1L)).thenReturn(mockModule);

        ResponseEntity<OpticalModule> response = opticalModuleController.getModuleById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockModule, response.getBody());
    }

    @Test
    void testCreateModule() {
        OpticalModule mockModule = new OpticalModule();
        when(opticalModuleService.createModule(mockModule)).thenReturn(mockModule);

        ResponseEntity<OpticalModule> response = opticalModuleController.createModule(mockModule);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockModule, response.getBody());
    }

    @Test
    void testUpdateModule() {
        OpticalModule mockModule = new OpticalModule();
        when(opticalModuleService.updateModule(1L, mockModule)).thenReturn(mockModule);

        ResponseEntity<OpticalModule> response = opticalModuleController.updateModule(1L, mockModule);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockModule, response.getBody());
    }

    @Test
    void testDeleteModule() {
        doNothing().when(opticalModuleService).deleteModule(1L);

        ResponseEntity<Void> response = opticalModuleController.deleteModule(1L);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(opticalModuleService, times(1)).deleteModule(1L);
    }
}