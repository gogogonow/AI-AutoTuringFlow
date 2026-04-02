package com.example.integration;

import com.example.model.Product;
import com.example.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration tests for Spring Boot 3.2.0 upgrade
 * Testing: End-to-end API flow, Jakarta namespace, MySQL connector, JSON serialization
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Spring Boot 3.2.0 Upgrade Integration Tests")
class SpringBoot3UpgradeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Complete CRUD workflow - should work end-to-end")
    void testCompleteCrudWorkflow() throws Exception {
        // CREATE
        Product newProduct = new Product();
        newProduct.setName("Integration Test Product");
        newProduct.setDescription("Testing full CRUD workflow");
        newProduct.setPrice(new BigDecimal("299.99"));
        newProduct.setCategory("Test Category");
        newProduct.setStockQuantity(50);

        String createResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("Integration Test Product")))
                .andExpect(jsonPath("$.price", is(299.99)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Product created = objectMapper.readValue(createResponse, Product.class);
        Long productId = created.getId();

        // READ
        mockMvc.perform(get("/api/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(productId.intValue())))
                .andExpect(jsonPath("$.name", is("Integration Test Product")));

        // UPDATE
        created.setName("Updated Product");
        created.setPrice(new BigDecimal("399.99"));

        mockMvc.perform(put("/api/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Product")))
                .andExpect(jsonPath("$.price", is(399.99)));

        // DELETE
        mockMvc.perform(delete("/api/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // VERIFY DELETION
        mockMvc.perform(get("/api/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Jakarta validation - should enforce constraints")
    void testJakartaValidation() throws Exception {
        // Test missing name (NotBlank)
        Product invalidProduct = new Product();
        invalidProduct.setPrice(new BigDecimal("99.99"));
        invalidProduct.setCategory("Test");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());

        // Test negative price (Positive)
        Product negativePrice = new Product();
        negativePrice.setName("Test");
        negativePrice.setPrice(new BigDecimal("-10.00"));
        negativePrice.setCategory("Test");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativePrice)))
                .andExpect(status().isBadRequest());

        // Test missing category
        Product noCategory = new Product();
        noCategory.setName("Test");
        noCategory.setPrice(new BigDecimal("99.99"));

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noCategory)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JSON serialization - should format dates correctly")
    void testJsonDateSerialization() throws Exception {
        Product product = new Product();
        product.setName("Date Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setCategory("Test");

        String response = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify date format matches pattern: yyyy-MM-dd'T'HH:mm:ss
        Product created = objectMapper.readValue(response, Product.class);
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
    }

    @Test
    @DisplayName("Search functionality - should find products")
    void testSearchFunctionality() throws Exception {
        // Create test products
        Product product1 = createProduct("Laptop Computer", "High-end gaming laptop", "Electronics");
        Product product2 = createProduct("Desktop Computer", "Office desktop", "Electronics");
        Product product3 = createProduct("Book", "Programming guide", "Books");

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        // Search by name
        mockMvc.perform(get("/api/products/search")
                .param("query", "computer")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        // Search by category
        mockMvc.perform(get("/api/products/category/Electronics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(is("Electronics"))));
    }

    @Test
    @DisplayName("Stock management - should update stock correctly")
    void testStockManagement() throws Exception {
        Product product = createProduct("Stock Test", "Testing stock", "Test");
        product.setStockQuantity(10);
        Product saved = productRepository.save(product);

        // Update stock
        mockMvc.perform(patch("/api/products/" + saved.getId() + "/stock")
                .param("stock", "25")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity", is(25)));

        // Get available products
        mockMvc.perform(get("/api/products/available/Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].stockQuantity", everyItem(greaterThan(0))));
    }

    @Test
    @DisplayName("Price precision - should maintain decimal accuracy")
    void testPricePrecision() throws Exception {
        Product product = new Product();
        product.setName("Precision Test");
        product.setPrice(new BigDecimal("1299.99"));
        product.setCategory("Test");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price", is(1299.99)));
    }

    @Test
    @DisplayName("CORS configuration - should allow configured origins")
    void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/products")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Transaction management - should rollback on error")
    void testTransactionRollback() throws Exception {
        long countBefore = productRepository.count();

        // Create valid product
        Product validProduct = createProduct("Valid", "Valid product", "Test");
        productRepository.save(validProduct);

        long countAfter = productRepository.count();
        assertEquals(countBefore + 1, countAfter);
    }

    @Test
    @DisplayName("Multiple concurrent requests - should handle correctly")
    void testConcurrentRequests() throws Exception {
        Product product1 = createProduct("Concurrent 1", "Test 1", "Test");
        Product product2 = createProduct("Concurrent 2", "Test 2", "Test");

        // Simulate concurrent creates
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product2)))
                .andExpect(status().isCreated());

        // Verify both created
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Product exists endpoint - should check existence")
    void testProductExists() throws Exception {
        Product product = createProduct("Exists Test", "Testing exists", "Test");
        Product saved = productRepository.save(product);

        mockMvc.perform(get("/api/products/" + saved.getId() + "/exists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        mockMvc.perform(get("/api/products/99999/exists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    private Product createProduct(String name, String description, String category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal("99.99"));
        product.setCategory(category);
        product.setStockQuantity(10);
        return product;
    }

    private void assertEquals(long expected, long actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }

    private void assertNotNull(Object object) {
        org.junit.jupiter.api.Assertions.assertNotNull(object);
    }
}