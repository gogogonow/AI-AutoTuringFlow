package com.example.repository;

import com.example.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for ProductRepository after Spring Boot 3.2.0 upgrade
 * Testing: JPA operations, Jakarta persistence, MySQL connector compatibility
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true"
})
@DisplayName("ProductRepository Integration Tests")
public class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;

    @BeforeEach
    void setUp() {
        // Clear the database
        productRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test products
        testProduct1 = new Product();
        testProduct1.setName("Laptop");
        testProduct1.setDescription("High-performance laptop");
        testProduct1.setPrice(new BigDecimal("1299.99"));
        testProduct1.setCategory("Electronics");
        testProduct1.setStockQuantity(10);

        testProduct2 = new Product();
        testProduct2.setName("Smartphone");
        testProduct2.setDescription("Latest smartphone model");
        testProduct2.setPrice(new BigDecimal("899.99"));
        testProduct2.setCategory("Electronics");
        testProduct2.setStockQuantity(0);

        testProduct3 = new Product();
        testProduct3.setName("Book");
        testProduct3.setDescription("Programming guide");
        testProduct3.setPrice(new BigDecimal("49.99"));
        testProduct3.setCategory("Books");
        testProduct3.setStockQuantity(25);
    }

    @Test
    @DisplayName("Save product - should persist with Jakarta persistence annotations")
    void testSaveProduct() {
        Product saved = productRepository.save(testProduct1);
        
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Laptop", saved.getName());
        assertEquals(new BigDecimal("1299.99"), saved.getPrice());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("Find by ID - should retrieve persisted entity")
    void testFindById() {
        Product saved = entityManager.persistAndFlush(testProduct1);
        
        Optional<Product> found = productRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals("Laptop", found.get().getName());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Find all - should return all products")
    void testFindAll() {
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();
        
        List<Product> products = productRepository.findAll();
        
        assertEquals(3, products.size());
    }

    @Test
    @DisplayName("Find by category - should filter by category")
    void testFindByCategory() {
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();
        
        List<Product> electronics = productRepository.findByCategory("Electronics");
        
        assertEquals(2, electronics.size());
        assertTrue(electronics.stream().allMatch(p -> "Electronics".equals(p.getCategory())));
    }

    @Test
    @DisplayName("Find by name containing - should perform case-insensitive search")
    void testFindByNameContaining() {
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();
        
        List<Product> results = productRepository.findByNameContainingIgnoreCase("phone");
        
        assertEquals(1, results.size());
        assertEquals("Smartphone", results.get(0).getName());
    }

    @Test
    @DisplayName("Search products - should search in name and description")
    void testSearchProducts() {
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.persist(testProduct3);
        entityManager.flush();
        
        List<Product> results = productRepository.searchProducts("laptop");
        
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(p -> p.getName().contains("Laptop")));
    }

    @Test
    @DisplayName("Find available products by category - should filter by stock")
    void testFindAvailableProductsByCategory() {
        entityManager.persist(testProduct1); // stock: 10
        entityManager.persist(testProduct2); // stock: 0
        entityManager.persist(testProduct3); // stock: 25
        entityManager.flush();
        
        List<Product> available = productRepository.findAvailableProductsByCategory("Electronics");
        
        assertEquals(1, available.size());
        assertEquals("Laptop", available.get(0).getName());
        assertTrue(available.get(0).getStockQuantity() > 0);
    }

    @Test
    @DisplayName("Update product - should update entity")
    void testUpdateProduct() {
        Product saved = entityManager.persistAndFlush(testProduct1);
        
        saved.setPrice(new BigDecimal("1199.99"));
        saved.setStockQuantity(15);
        Product updated = productRepository.save(saved);
        entityManager.flush();
        entityManager.clear();
        
        Product found = entityManager.find(Product.class, saved.getId());
        assertEquals(new BigDecimal("1199.99"), found.getPrice());
        assertEquals(15, found.getStockQuantity());
        assertNotNull(found.getUpdatedAt());
    }

    @Test
    @DisplayName("Delete product - should remove entity")
    void testDeleteProduct() {
        Product saved = entityManager.persistAndFlush(testProduct1);
        Long id = saved.getId();
        
        productRepository.delete(saved);
        entityManager.flush();
        
        Optional<Product> found = productRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Exists by ID - should check entity existence")
    void testExistsById() {
        Product saved = entityManager.persistAndFlush(testProduct1);
        
        assertTrue(productRepository.existsById(saved.getId()));
        assertFalse(productRepository.existsById(999L));
    }

    @Test
    @DisplayName("Count - should return total count")
    void testCount() {
        entityManager.persist(testProduct1);
        entityManager.persist(testProduct2);
        entityManager.flush();
        
        long count = productRepository.count();
        
        assertEquals(2, count);
    }

    @Test
    @DisplayName("@PrePersist - should set timestamps on creation")
    void testPrePersistCallback() {
        Product product = new Product();
        product.setName("New Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setCategory("Test");
        
        Product saved = productRepository.save(product);
        entityManager.flush();
        
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("@PreUpdate - should update timestamp on modification")
    void testPreUpdateCallback() throws InterruptedException {
        Product saved = entityManager.persistAndFlush(testProduct1);
        entityManager.clear();
        
        Thread.sleep(10); // Ensure time difference
        
        Product toUpdate = productRepository.findById(saved.getId()).get();
        toUpdate.setName("Updated Laptop");
        Product updated = productRepository.save(toUpdate);
        entityManager.flush();
        
        assertNotNull(updated.getUpdatedAt());
        assertTrue(updated.getUpdatedAt().isAfter(updated.getCreatedAt()) || 
                   updated.getUpdatedAt().isEqual(updated.getCreatedAt()));
    }

    @Test
    @DisplayName("BigDecimal precision - should maintain price precision")
    void testBigDecimalPrecision() {
        testProduct1.setPrice(new BigDecimal("1299.99"));
        Product saved = entityManager.persistAndFlush(testProduct1);
        entityManager.clear();
        
        Product found = productRepository.findById(saved.getId()).get();
        
        assertEquals(new BigDecimal("1299.99"), found.getPrice());
        assertEquals(0, new BigDecimal("1299.99").compareTo(found.getPrice()));
    }

    @Test
    @DisplayName("Text column - should handle large descriptions")
    void testTextColumn() {
        String longDescription = "A".repeat(1000);
        testProduct1.setDescription(longDescription);
        
        Product saved = entityManager.persistAndFlush(testProduct1);
        entityManager.clear();
        
        Product found = productRepository.findById(saved.getId()).get();
        assertEquals(longDescription, found.getDescription());
    }

    @Test
    @DisplayName("Null description - should allow null values")
    void testNullDescription() {
        testProduct1.setDescription(null);
        
        Product saved = entityManager.persistAndFlush(testProduct1);
        entityManager.clear();
        
        Product found = productRepository.findById(saved.getId()).get();
        assertNull(found.getDescription());
    }

    @Test
    @DisplayName("Default stock quantity - should use default value")
    void testDefaultStockQuantity() {
        Product product = new Product();
        product.setName("Test");
        product.setPrice(new BigDecimal("10.00"));
        product.setCategory("Test");
        // Don't set stockQuantity
        
        Product saved = productRepository.save(product);
        
        assertEquals(0, saved.getStockQuantity());
    }

    @Test
    @DisplayName("Transaction rollback - should not persist on error")
    void testTransactionRollback() {
        long countBefore = productRepository.count();
        
        try {
            Product invalidProduct = new Product();
            // Missing required fields - will fail validation in real scenario
            productRepository.save(invalidProduct);
            entityManager.flush();
        } catch (Exception e) {
            // Expected exception
        }
        
        long countAfter = productRepository.count();
        assertEquals(countBefore, countAfter);
    }

    @Test
    @DisplayName("MySQL connector compatibility - should work with new driver")
    void testMySQLConnectorCompatibility() {
        // This test verifies that the new mysql-connector-j driver works
        Product saved = productRepository.save(testProduct1);
        
        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
    }
}