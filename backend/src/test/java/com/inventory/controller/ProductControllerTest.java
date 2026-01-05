package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.entity.Product;
import com.inventory.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 商品控制器集成测试
 *
 * 测试覆盖：
 * - REST API 端点测试
 * - 请求/响应格式验证
 * - 错误码验证
 * - 批量操作测试
 */
@WebMvcTest(ProductController.class)
@DisplayName("商品控制器测试 (ProductControllerTest)")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 准备测试商品数据
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSku("SKU001");
        testProduct.setName("iPhone 15");
        testProduct.setCategoryId(1L);
        testProduct.setCategoryName("电子产品");
        testProduct.setUnit("台");
        testProduct.setPrice(new BigDecimal("5999.00"));
        testProduct.setCostPrice(new BigDecimal("4500.00"));
        testProduct.setWarningStock(10);
        testProduct.setStatus(1);
    }

    @Nested
    @DisplayName("POST /api/products - 创建商品")
    class CreateProductApiTests {

        @Test
        @DisplayName("应返回200 - when creating product with valid data")
        void shouldReturn200_whenCreatingProductWithValidData() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sku", "SKU002");
            requestBody.put("name", "MacBook Pro");
            requestBody.put("categoryId", 1L);
            requestBody.put("unit", "台");
            requestBody.put("price", "12999.00");
            requestBody.put("warningStock", 5);
            requestBody.put("status", 1);

            when(productService.createProduct(any(Product.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("创建成功"))
                    .andExpect(jsonPath("$.data").value(1));

            verify(productService, times(1)).createProduct(any(Product.class));
        }

        @Test
        @DisplayName("应返回400 - when SKU is duplicate")
        void shouldReturn400_whenSkuIsDuplicate() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sku", "SKU001"); // 重复的SKU
            requestBody.put("name", "测试商品");
            requestBody.put("categoryId", 1L);
            requestBody.put("price", "100.00");

            when(productService.createProduct(any(Product.class)))
                .thenThrow(new RuntimeException("SKU已存在"));

            // Act & Assert
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("SKU")));
        }

        @Test
        @DisplayName("应返回400 - when price is negative")
        void shouldReturn400_whenPriceIsNegative() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sku", "SKU999");
            requestBody.put("name", "测试商品");
            requestBody.put("categoryId", 1L);
            requestBody.put("price", "-100.00");

            when(productService.createProduct(any(Product.class)))
                .thenThrow(new IllegalArgumentException("价格不能为负数"));

            // Act & Assert
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("价格")));
        }

        @Test
        @DisplayName("应返回400 - when required fields are missing")
        void shouldReturn400_whenRequiredFieldsAreMissing() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", "测试商品");
            // 缺少必填字段：sku, categoryId, price

            // Act & Assert
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("应返回400 - when category does not exist")
        void shouldReturn400_whenCategoryNotExists() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sku", "SKU999");
            requestBody.put("name", "测试商品");
            requestBody.put("categoryId", 999L);
            requestBody.put("price", "100.00");

            when(productService.createProduct(any(Product.class)))
                .thenThrow(new RuntimeException("分类不存在"));

            // Act & Assert
            mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("分类")));
        }
    }

    @Nested
    @DisplayName("PUT /api/products/{id} - 更新商品")
    class UpdateProductApiTests {

        @Test
        @DisplayName("应返回200 - when updating product successfully")
        void shouldReturn200_whenUpdatingProductSuccessfully() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", 1L);
            requestBody.put("sku", "SKU001");
            requestBody.put("name", "iPhone 15 Pro");
            requestBody.put("price", "6999.00");

            when(productService.updateProduct(any(Product.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(put("/api/products/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("更新成功"));
        }

        @Test
        @DisplayName("应返回400 - when updating with duplicate SKU")
        void shouldReturn400_whenUpdatingWithDuplicateSku() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", 1L);
            requestBody.put("sku", "SKU002"); // 另一个商品的SKU

            when(productService.updateProduct(any(Product.class)))
                .thenThrow(new RuntimeException("SKU已存在"));

            // Act & Assert
            mockMvc.perform(put("/api/products/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("SKU")));
        }

        @Test
        @DisplayName("应返回404 - when product not found")
        void shouldReturn404_whenProductNotFound() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", 999L);

            when(productService.updateProduct(any(Product.class)))
                .thenThrow(new RuntimeException("商品不存在"));

            // Act & Assert
            mockMvc.perform(put("/api/products/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("不存在")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/{id} - 删除商品")
    class DeleteProductApiTests {

        @Test
        @DisplayName("应返回200 - when deleting product successfully")
        void shouldReturn200_whenDeletingProductSuccessfully() throws Exception {
            // Arrange
            when(productService.deleteProduct(1L)).thenReturn(true);

            // Act & Assert
            mockMvc.perform(delete("/api/products/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("删除成功"));
        }

        @Test
        @DisplayName("应返回400 - when product has inventory records")
        void shouldReturn400_whenProductHasInventoryRecords() throws Exception {
            // Arrange
            when(productService.deleteProduct(1L))
                .thenThrow(new RuntimeException("该商品有库存记录，无法删除"));

            // Act & Assert
            mockMvc.perform(delete("/api/products/1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("库存")));
        }

        @Test
        @DisplayName("应返回400 - when product has inbound/outbound records")
        void shouldReturn400_whenProductHasInboundOutboundRecords() throws Exception {
            // Arrange
            when(productService.deleteProduct(1L))
                .thenThrow(new RuntimeException("该商品有出入库记录，无法删除"));

            // Act & Assert
            mockMvc.perform(delete("/api/products/1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("出入库")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/products/batch - 批量删除")
    class BatchDeleteApiTests {

        @Test
        @DisplayName("应返回200 - when batch deleting successfully")
        void shouldReturn200_whenBatchDeletingSuccessfully() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", Arrays.asList(1L, 2L, 3L));

            when(productService.batchDelete(anyList())).thenReturn(3);

            // Act & Assert
            mockMvc.perform(delete("/api/products/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("成功删除3个商品"));
        }

        @Test
        @DisplayName("应返回400 - when any product has associations")
        void shouldReturn400_whenAnyProductHasAssociations() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("ids", Arrays.asList(1L, 2L));

            when(productService.batchDelete(anyList()))
                .thenThrow(new RuntimeException("有商品无法删除"));

            // Act & Assert
            mockMvc.perform(delete("/api/products/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id} - 获取商品详情")
    class GetProductApiTests {

        @Test
        @DisplayName("应返回200 - when product exists")
        void shouldReturn200_whenProductExists() throws Exception {
            // Arrange
            when(productService.getProductById(1L)).thenReturn(testProduct);

            // Act & Assert
            mockMvc.perform(get("/api/products/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.sku").value("SKU001"))
                    .andExpect(jsonPath("$.data.name").value("iPhone 15"));
        }

        @Test
        @DisplayName("应返回404 - when product not found")
        void shouldReturn404_whenProductNotFound() throws Exception {
            // Arrange
            when(productService.getProductById(999L)).thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/api/products/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/products - 获取商品列表")
    class GetProductsApiTests {

        @Test
        @DisplayName("应返回200 - when querying products without filters")
        void shouldReturn200_whenQueryingProductsWithoutFilters() throws Exception {
            // Arrange
            when(productService.getProducts(eq(1), eq(10), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

            // Act & Assert
            mockMvc.perform(get("/api/products")
                    .param("page", "1")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("应返回200 - when filtering by name")
        void shouldReturn200_whenFilteringByName() throws Exception {
            // Arrange
            when(productService.getProducts(eq(1), eq(10), eq("iPhone"), isNull(), isNull(), isNull()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

            // Act & Assert
            mockMvc.perform(get("/api/products")
                    .param("page", "1")
                    .param("size", "10")
                    .param("name", "iPhone"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(productService, times(1)).getProducts(eq(1), eq(10), eq("iPhone"), isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("应返回200 - when filtering by SKU")
        void shouldReturn200_whenFilteringBySku() throws Exception {
            // Arrange
            when(productService.getProducts(eq(1), eq(10), isNull(), eq("SKU001"), isNull(), isNull()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

            // Act & Assert
            mockMvc.perform(get("/api/products")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sku", "SKU001"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(productService, times(1)).getProducts(eq(1), eq(10), isNull(), eq("SKU001"), isNull(), isNull());
        }

        @Test
        @DisplayName("应返回200 - when filtering by category")
        void shouldReturn200_whenFilteringByCategory() throws Exception {
            // Arrange
            when(productService.getProducts(eq(1), eq(10), isNull(), isNull(), eq(1L), isNull()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

            // Act & Assert
            mockMvc.perform(get("/api/products")
                    .param("page", "1")
                    .param("size", "10")
                    .param("categoryId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(productService, times(1)).getProducts(eq(1), eq(10), isNull(), isNull(), eq(1L), isNull());
        }

        @Test
        @DisplayName("应返回200 - when filtering by status")
        void shouldReturn200_whenFilteringByStatus() throws Exception {
            // Arrange
            when(productService.getProducts(eq(1), eq(10), isNull(), isNull(), isNull(), eq(1)))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

            // Act & Assert
            mockMvc.perform(get("/api/products")
                    .param("page", "1")
                    .param("size", "10")
                    .param("status", "1"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(productService, times(1)).getProducts(eq(1), eq(10), isNull(), isNull(), isNull(), eq(1));
        }

        @Test
        @DisplayName("应使用默认分页参数 - when page and size not provided")
        void shouldUseDefaultPagination_whenPageAndSizeNotProvided() throws Exception {
            // Arrange
            when(productService.getProducts(eq(1), eq(10), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

            // Act & Assert
            mockMvc.perform(get("/api/products"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(productService, times(1)).getProducts(eq(1), eq(10), isNull(), isNull(), isNull(), isNull());
        }
    }

    @Nested
    @DisplayName("PATCH /api/products/{id}/status - 切换状态")
    class ToggleStatusApiTests {

        @Test
        @DisplayName("应返回200 - when toggling status successfully")
        void shouldReturn200_whenTogglingStatusSuccessfully() throws Exception {
            // Arrange
            when(productService.toggleStatus(1L)).thenReturn(true);

            // Act & Assert
            mockMvc.perform(patch("/api/products/1/status"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("状态切换成功"));
        }

        @Test
        @DisplayName("应返回404 - when product not found")
        void shouldReturn404_whenProductNotFound() throws Exception {
            // Arrange
            when(productService.toggleStatus(999L))
                .thenThrow(new RuntimeException("商品不存在"));

            // Act & Assert
            mockMvc.perform(patch("/api/products/999/status"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/products/check-sku - 检查SKU")
    class CheckSkuApiTests {

        @Test
        @DisplayName("应返回true - when SKU exists")
        void shouldReturnTrue_whenSkuExists() throws Exception {
            // Arrange
            when(productService.checkSkuExists("SKU001")).thenReturn(true);

            // Act & Assert
            mockMvc.perform(get("/api/products/check-sku")
                    .param("sku", "SKU001"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("应返回false - when SKU does not exist")
        void shouldReturnFalse_whenSkuNotExists() throws Exception {
            // Arrange
            when(productService.checkSkuExists("SKU999")).thenReturn(false);

            // Act & Assert
            mockMvc.perform(get("/api/products/check-sku")
                    .param("sku", "SKU999"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("应排除当前商品 - when checking SKU for existing product")
        void shouldExcludeCurrentProduct_whenCheckingSkuForExistingProduct() throws Exception {
            // Arrange
            when(productService.checkSkuExists("SKU001", 1L)).thenReturn(false);

            // Act & Assert
            mockMvc.perform(get("/api/products/check-sku")
                    .param("sku", "SKU001")
                    .param("excludeId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("应返回400 - when SKU parameter is missing")
        void shouldReturn400_whenSkuParameterIsMissing() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/products/check-sku"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/products/search - 搜索商品")
    class SearchProductsApiTests {

        @Test
        @DisplayName("应返回200 - when searching by keyword")
        void shouldReturn200_whenSearchingByKeyword() throws Exception {
            // Arrange
            when(productService.searchProducts(anyString()))
                .thenReturn(Arrays.asList(testProduct));

            // Act & Assert
            mockMvc.perform(get("/api/products/search")
                    .param("keyword", "iPhone"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("应返回空列表 - when no results found")
        void shouldReturnEmptyList_whenNoResultsFound() throws Exception {
            // Arrange
            when(productService.searchProducts(anyString()))
                .thenReturn(Arrays.asList());

            // Act & Assert
            mockMvc.perform(get("/api/products/search")
                    .param("keyword", "不存在"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
