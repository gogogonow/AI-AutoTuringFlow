package com.example.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for Product entity after Spring Boot 3.2.0 upgrade
 * Testing: Jakarta namespace migration, JPA annotations, validation constraints
 */
@DisplayName("Product Entity Regression Tests")
class ProductEntityTest {

    private Validator validator;
    private Product product;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("99.99"));
        product.setCategory("Electronics");
        product.setStockQuantity(10);
    }

    @Test
    @DisplayName("Valid product should pass Jakarta validation")
    void testValidProduct() {
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertTrue(violations.isEmpty(), "Valid product should have no violations");
    }

    @Test
    @DisplayName("Product name is required - Jakarta @NotBlank validation")
    void testProductNameRequired() {
        product.setName("");
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Empty name should cause validation error");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Product name is required")),
            "Should have 'Product name is required' message");
    }

    @Test
    @DisplayName("Product name null should fail - Jakarta @NotBlank validation")
    void testProductNameNull() {
        product.setName(null);
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Null name should cause validation error");
    }

    @Test
    @DisplayName("Product price is required - Jakarta @NotNull validation")
    void testProductPriceRequired() {
        product.setPrice(null);
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Null price should cause validation error");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Price is required")),
            "Should have 'Price is required' message");
    }

    @Test
    @DisplayName("Product price must be positive - Jakarta @Positive validation")
    void testProductPriceMustBePositive() {
        product.setPrice(new BigDecimal("-10.00"));
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Negative price should cause validation error");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Price must be positive")),
            "Should have 'Price must be positive' message");
    }

    @Test
    @DisplayName("Product price zero should fail - Jakarta @Positive validation")
    void testProductPriceZero() {
        product.setPrice(BigDecimal.ZERO);
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Zero price should cause validation error");
    }

    @Test
    @DisplayName("Product category is required - Jakarta @NotBlank validation")
    void testProductCategoryRequired() {
        product.setCategory("");
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertFalse(violations.isEmpty(), "Empty category should cause validation error");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Category is required")),
            "Should have 'Category is required' message");
    }

    @Test
    @DisplayName("Product description can be null")
    void testProductDescriptionOptional() {
        product.setDescription(null);
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertTrue(violations.isEmpty(), "Null description should be allowed");
    }

    @Test
    @DisplayName("Product stock quantity defaults to zero")
    void testDefaultStockQuantity() {
        Product newProduct = new Product();
        assertEquals(0, newProduct.getStockQuantity(), 
            "Default stock quantity should be 0");
    }

    @Test
    @DisplayName("Product constructor sets all required fields")
    void testProductConstructor() {
        Product newProduct = new Product(
            "Laptop",
            "High-end gaming laptop",
            new BigDecimal("1299.99"),
            "Computers",
            5
        );
        
        assertEquals("Laptop", newProduct.getName());
        assertEquals("High-end gaming laptop", newProduct.getDescription());
        assertEquals(new BigDecimal("1299.99"), newProduct.getPrice());
        assertEquals("Computers", newProduct.getCategory());
        assertEquals(5, newProduct.getStockQuantity());
    }

    @Test
    @DisplayName("Product getters and setters work correctly")
    void testGettersAndSetters() {
        product.setId(1L);
        product.setName("Updated Name");
        product.setDescription("Updated Description");
        product.setPrice(new BigDecimal("199.99"));
        product.setCategory("Updated Category");
        product.setStockQuantity(20);
        
        assertEquals(1L, product.getId());
        assertEquals("Updated Name", product.getName());
        assertEquals("Updated Description", product.getDescription());
        assertEquals(new BigDecimal("199.99"), product.getPrice());
        assertEquals("Updated Category", product.getCategory());
        assertEquals(20, product.getStockQuantity());
    }

    @Test
    @DisplayName("Product timestamps can be set and retrieved")
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        
        assertEquals(now, product.getCreatedAt());
        assertEquals(now, product.getUpdatedAt());
    }

    @Test
    @DisplayName("Product price supports decimal precision")
    void testPriceDecimalPrecision() {
        product.setPrice(new BigDecimal("99.99"));
        assertEquals(new BigDecimal("99.99"), product.getPrice(),
            "Price should maintain decimal precision");
    }

    @Test
    @DisplayName("Product can have large price values")
    void testLargePriceValues() {
        product.setPrice(new BigDecimal("99999999.99"));
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertTrue(violations.isEmpty(), "Large price values should be accepted");
    }

    @Test
    @DisplayName("Multiple validation errors are collected")
    void testMultipleValidationErrors() {
        product.setName("");
        product.setPrice(null);
        product.setCategory("");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertTrue(violations.size() >= 3, 
            "Should have at least 3 validation errors (name, price, category)");
    }
}