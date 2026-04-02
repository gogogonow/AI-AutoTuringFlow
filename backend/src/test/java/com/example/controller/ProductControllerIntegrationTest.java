package com.example.controller;

import com.example.model.Product;
import com.example.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests for ProductController after Spring Boot 3.2.0 upgrade
 * Testing: REST endpoints, JSON serialization, Jakarta validation, CORS
 */
@WebMvcTest(ProductController.class)
@DisplayName("ProductController Integration Tests")
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private Product testProduct;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setCategory("Electronics");
        testProduct.setStockQuantity(10);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Another Product");
        product2.setDescription("Another Description");
        product2.setPrice(new BigDecimal("149.99"));
        product2.setCategory("Electronics");
        product2.setStockQuantity(5);

        testProducts = Arrays.asList(testProduct, product2);
    }

    @Test
    @DisplayName("GET /api/products - should return all products")
    void testGetAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(testProducts);

        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Product")))
                .andExpect(jsonPath("$[0].price", is(99.99)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Another Product")));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("GET /api/products/{id} - should return product by id")
    void testGetProductById() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        mockMvc.perform(get("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.price", is(99.99)))
                .andExpect(jsonPath("$.category", is("Electronics")))
                .andExpect(jsonPath("$.stockQuantity", is(10)));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/products/{id} - should return 404 for non-existent product")
    void testGetProductByIdNotFound() throws Exception {
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - should return products by category")
    void testGetProductsByCategory() throws Exception {
        when(productService.getProductsByCategory("Electronics")).thenReturn(testProducts);

        mockMvc.perform(get("/api/products/category/Electronics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category", is("Electronics")))
                .andExpect(jsonPath("$[1].category", is("Electronics")));

        verify(productService, times(1)).getProductsByCategory("Electronics");
    }

    @Test
    @DisplayName("GET /api/products/search?query= - should search products")
    void testSearchProducts() throws Exception {
        when(productService.searchProducts("Test")).thenReturn(Arrays.asList(testProduct));

        mockMvc.perform(get("/api/products/search")
                .param("query", "Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Product")));

        verify(productService, times(1)).searchProducts("Test");
    }

    @Test
    @DisplayName("GET /api/products/available/{category} - should return available products")
    void testGetAvailableProducts() throws Exception {
        when(productService.getAvailableProductsByCategory("Electronics"))
            .thenReturn(testProducts);

        mockMvc.perform(get("/api/products/available/Electronics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(productService, times(1)).getAvailableProductsByCategory("Electronics");
    }

    @Test
    @DisplayName("POST /api/products - should create new product with valid data")
    void testCreateProduct() throws Exception {
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setDescription("New Description");
        newProduct.setPrice(new BigDecimal("199.99"));
        newProduct.setCategory("Electronics");
        newProduct.setStockQuantity(15);

        Product savedProduct = new Product();
        savedProduct.setId(3L);
        savedProduct.setName(newProduct.getName());
        savedProduct.setDescription(newProduct.getDescription());
        savedProduct.setPrice(newProduct.getPrice());
        savedProduct.setCategory(newProduct.getCategory());
        savedProduct.setStockQuantity(newProduct.getStockQuantity());

        when(productService.createProduct(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New Product")));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("POST /api/products - should fail with invalid data (Jakarta validation)")
    void testCreateProductInvalidData() throws Exception {
        Product invalidProduct = new Product();
        // Missing required fields: name, price, category

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("POST /api/products - should fail with blank name (Jakarta @NotBlank)")
    void testCreateProductBlankName() throws Exception {
        Product invalidProduct = new Product();
        invalidProduct.setName("");
        invalidProduct.setPrice(new BigDecimal("99.99"));
        invalidProduct.setCategory("Electronics");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("POST /api/products - should fail with negative price (Jakarta @Positive)")
    void testCreateProductNegativePrice() throws Exception {
        Product invalidProduct = new Product();
        invalidProduct.setName("Test");
        invalidProduct.setPrice(new BigDecimal("-10.00"));
        invalidProduct.setCategory("Electronics");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - should update existing product")
    void testUpdateProduct() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setPrice(new BigDecimal("149.99"));
        updatedProduct.setCategory("Electronics");
        updatedProduct.setStockQuantity(20);

        when(productService.updateProduct(eq(1L), any(Product.class)))
            .thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Product")));

        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - should return 404 for non-existent product")
    void testUpdateProductNotFound() throws Exception {
        when(productService.updateProduct(eq(999L), any(Product.class)))
            .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(put("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).updateProduct(eq(999L), any(Product.class));
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/stock - should update stock quantity")
    void testUpdateStock() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName(testProduct.getName());
        updatedProduct.setStockQuantity(25);

        when(productService.updateStock(1L, 25)).thenReturn(updatedProduct);

        mockMvc.perform(patch("/api/products/1/stock")
                .param("stock", "25")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.stockQuantity", is(25)));

        verify(productService, times(1)).updateStock(1L, 25);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - should delete product")
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - should return 404 for non-existent product")
    void testDeleteProductNotFound() throws Exception {
        doThrow(new RuntimeException("Product not found"))
            .when(productService).deleteProduct(999L);

        mockMvc.perform(delete("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).deleteProduct(999L);
    }

    @Test
    @DisplayName("GET /api/products/{id}/exists - should check if product exists")
    void testProductExists() throws Exception {
        when(productService.productExists(1L)).thenReturn(true);

        mockMvc.perform(get("/api/products/1/exists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(productService, times(1)).productExists(1L);
    }

    @Test
    @DisplayName("GET /api/products/{id}/exists - should return false for non-existent product")
    void testProductNotExists() throws Exception {
        when(productService.productExists(999L)).thenReturn(false);

        mockMvc.perform(get("/api/products/999/exists")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));

        verify(productService, times(1)).productExists(999L);
    }

    @Test
    @DisplayName("JSON serialization should format dates correctly")
    void testJsonDateFormatting() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        mockMvc.perform(get("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }
}