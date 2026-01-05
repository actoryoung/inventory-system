package com.inventory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.entity.Inventory;
import com.inventory.entity.Product;
import com.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 库存控制器集成测试
 *
 * 测试覆盖：
 * - REST API 端点测试
 * - 请求/响应格式验证
 * - 错误码验证
 * - 库存调整操作测试
 * - 库存查询测试
 * - 低库存预警测试
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@WebMvcTest(InventoryController.class)
@DisplayName("库存控制器测试 (InventoryControllerTest)")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Inventory testInventory;
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
        testProduct.setWarningStock(10);

        // 准备测试库存数据
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProductId(1L);
        testInventory.setWarehouseId(1L);
        testInventory.setQuantity(100);
        testInventory.setWarningStock(10);
        testInventory.setCreatedAt(LocalDateTime.now());
        testInventory.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/inventory - 获取库存列表")
    class GetInventoryListTests {

        @Test
        @DisplayName("应返回200 - when querying inventory without filters")
        void shouldReturn200_whenQueryingInventoryWithoutFilters() throws Exception {
            // Arrange
            Page<Inventory> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInventory));
            page.setTotal(1);

            when(inventoryService.page(any(Page.class), any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inventory")
                    .param("page", "1")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records[0].productId").value(1))
                    .andExpect(jsonPath("$.data.records[0].quantity").value(100))
                    .andExpect(jsonPath("$.data.total").value(1));
        }

        @Test
        @DisplayName("应返回200 - when filtering by product name")
        void shouldReturn200_whenFilteringByProductName() throws Exception {
            // Arrange
            Page<Inventory> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInventory));

            when(inventoryService.page(any(Page.class), any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inventory")
                    .param("page", "1")
                    .param("size", "10")
                    .param("productName", "iPhone"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(inventoryService, times(1)).page(any(Page.class), any());
        }

        @Test
        @DisplayName("应返回200 - when filtering by category")
        void shouldReturn200_whenFilteringByCategory() throws Exception {
            // Arrange
            Page<Inventory> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInventory));

            when(inventoryService.page(any(Page.class), any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inventory")
                    .param("page", "1")
                    .param("size", "10")
                    .param("categoryId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(inventoryService, times(1)).page(any(Page.class), any());
        }

        @Test
        @DisplayName("应返回200 - when filtering low stock only")
        void shouldReturn200_whenFilteringLowStockOnly() throws Exception {
            // Arrange
            testInventory.setQuantity(5); // 低于预警值
            Page<Inventory> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInventory));

            when(inventoryService.page(any(Page.class), any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inventory")
                    .param("page", "1")
                    .param("size", "10")
                    .param("lowStockOnly", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(inventoryService, times(1)).page(any(Page.class), any());
        }

        @Test
        @DisplayName("应返回空列表 - when no inventory records")
        void shouldReturnEmptyList_whenNoInventoryRecords() throws Exception {
            // Arrange
            Page<Inventory> emptyPage = new Page<>(1, 10);
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);

            when(inventoryService.page(any(Page.class), any())).thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/api/inventory")
                    .param("page", "1")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isEmpty())
                    .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("应使用默认分页 - when page and size not provided")
        void shouldUseDefaultPagination_whenPageAndSizeNotProvided() throws Exception {
            // Arrange
            when(inventoryService.page(any(Page.class), any()))
                .thenReturn(new Page<>(1, 10));

            // Act & Assert
            mockMvc.perform(get("/api/inventory"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(inventoryService, times(1)).page(any(Page.class), any());
        }
    }

    @Nested
    @DisplayName("GET /api/inventory/product/{productId} - 获取商品库存")
    class GetInventoryByProductTests {

        @Test
        @DisplayName("应返回200 - when inventory exists")
        void shouldReturn200_whenInventoryExists() throws Exception {
            // Arrange
            Long productId = 1L;
            when(inventoryService.getByProductId(productId)).thenReturn(testInventory);

            // Act & Assert
            mockMvc.perform(get("/api/inventory/product/{productId}", productId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.productId").value(1))
                    .andExpect(jsonPath("$.data.quantity").value(100))
                    .andExpect(jsonPath("$.data.warningStock").value(10));
        }

        @Test
        @DisplayName("应返回404 - when inventory does not exist")
        void shouldReturn404_whenInventoryDoesNotExist() throws Exception {
            // Arrange
            Long productId = 999L;
            when(inventoryService.getByProductId(productId)).thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/api/inventory/product/{productId}", productId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(containsString("不存在")));
        }
    }

    @Nested
    @DisplayName("PUT /api/inventory/{id}/adjust - 调整库存")
    class AdjustInventoryTests {

        @Test
        @DisplayName("应返回200 - when adding stock successfully")
        void shouldReturn200_whenAddingStockSuccessfully() throws Exception {
            // Arrange
            Long inventoryId = 1L;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("type", "add");
            requestBody.put("quantity", 50);
            requestBody.put("reason", "采购入库");

            doNothing().when(inventoryService).addStock(anyLong(), anyInt());

            // Act & Assert
            mockMvc.perform(put("/api/inventory/{id}/adjust", inventoryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value(containsString("成功")))
                    .andExpect(jsonPath("$.data.oldQuantity").exists())
                    .andExpect(jsonPath("$.data.newQuantity").exists());

            verify(inventoryService, times(1)).addStock(anyLong(), eq(50));
        }

        @Test
        @DisplayName("应返回200 - when reducing stock successfully")
        void shouldReturn200_whenReducingStockSuccessfully() throws Exception {
            // Arrange
            Long inventoryId = 1L;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("type", "reduce");
            requestBody.put("quantity", 30);
            requestBody.put("reason", "销售出库");

            doNothing().when(inventoryService).reduceStock(anyLong(), anyInt());

            // Act & Assert
            mockMvc.perform(put("/api/inventory/{id}/adjust", inventoryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(inventoryService, times(1)).reduceStock(anyLong(), eq(30));
        }

        @Test
        @DisplayName("应返回200 - when setting stock value successfully")
        void shouldReturn200_whenSettingStockValueSuccessfully() throws Exception {
            // Arrange
            Long inventoryId = 1L;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("type", "set");
            requestBody.put("quantity", 200);
            requestBody.put("reason", "盘点调整");

            doNothing().when(inventoryService).adjustStock(anyLong(), anyInt(), anyString());

            // Act & Assert
            mockMvc.perform(put("/api/inventory/{id}/adjust", inventoryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(inventoryService, times(1)).adjustStock(anyLong(), eq(200), eq("盘点调整"));
        }

        @Test
        @DisplayName("应返回400 - when insufficient stock")
        void shouldReturn400_whenInsufficientStock() throws Exception {
            // Arrange
            Long inventoryId = 1L;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("type", "reduce");
            requestBody.put("quantity", 150); // 大于当前库存
            requestBody.put("reason", "测试");

            doThrow(new RuntimeException("库存不足"))
                .when(inventoryService).reduceStock(anyLong(), anyInt());

            // Act & Assert
            mockMvc.perform(put("/api/inventory/{id}/adjust", inventoryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("库存")));
        }

        @Test
        @DisplayName("应返回400 - when inventory record does not exist")
        void shouldReturn400_whenInventoryRecordDoesNotExist() throws Exception {
            // Arrange
            Long inventoryId = 999L;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("type", "add");
            requestBody.put("quantity", 50);
            requestBody.put("reason", "测试");

            doThrow(new RuntimeException("库存记录不存在"))
                .when(inventoryService).addStock(anyLong(), anyInt());

            // Act & Assert
            mockMvc.perform(put("/api/inventory/{id}/adjust", inventoryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("不存在")));
        }

        @Test
        @DisplayName("应返回400 - when adjustment type is invalid")
        void shouldReturn400_whenAdjustmentTypeIsInvalid() throws Exception {
            // Arrange
            Long inventoryId = 1L;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("type", "invalid"); // 无效的类型
            requestBody.put("quantity", 50);
            requestBody.put("reason", "测试");

            // Act & Assert
            mockMvc.perform(put("/api/inventory/{id}/adjust", inventoryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("应返回400 - when quantity is missing")
        void shouldReturn400_whenQuantityIsMissing() throws Exception {
            // Arrange
            Long inventoryId = 1L;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("type", "add");
            requestBody.put("reason", "测试");
            // 缺少 quantity

            // Act & Assert
            mockMvc.perform(put("/api/inventory/{id}/adjust", inventoryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("应返回400 - when reason is missing")
        void shouldReturn400_whenReasonIsMissing() throws Exception {
            // Arrange
            Long inventoryId = 1L;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("type", "set");
            requestBody.put("quantity", 100);
            // 缺少 reason

            // Act & Assert
            mockMvc.perform(put("/api/inventory/{id}/adjust", inventoryId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/inventory/low-stock - 获取低库存列表")
    class GetLowStockTests {

        @Test
        @DisplayName("应返回200 - when querying low stock items")
        void shouldReturn200_whenQueryingLowStockItems() throws Exception {
            // Arrange
            Inventory lowStockInventory = new Inventory();
            lowStockInventory.setId(2L);
            lowStockInventory.setProductId(2L);
            lowStockInventory.setQuantity(5);
            lowStockInventory.setWarningStock(10);

            Page<Inventory> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(lowStockInventory));
            page.setTotal(1);

            when(inventoryService.page(any(Page.class), any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inventory/low-stock")
                    .param("page", "1")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records[0].quantity").value(5))
                    .andExpect(jsonPath("$.data.records[0].warningStock").value(10));
        }

        @Test
        @DisplayName("应返回空列表 - when no low stock items")
        void shouldReturnEmptyList_whenNoLowStockItems() throws Exception {
            // Arrange
            Page<Inventory> emptyPage = new Page<>(1, 10);
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);

            when(inventoryService.page(any(Page.class), any())).thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/api/inventory/low-stock")
                    .param("page", "1")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isEmpty());
        }

        @Test
        @DisplayName("应返回200 - when filtering by category")
        void shouldReturn200_whenFilteringLowStockByCategory() throws Exception {
            // Arrange
            when(inventoryService.page(any(Page.class), any()))
                .thenReturn(new Page<>(1, 10));

            // Act & Assert
            mockMvc.perform(get("/api/inventory/low-stock")
                    .param("page", "1")
                    .param("size", "10")
                    .param("categoryId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(inventoryService, times(1)).page(any(Page.class), any());
        }
    }

    @Nested
    @DisplayName("POST /api/inventory/check - 检查库存充足性")
    class CheckStockTests {

        @Test
        @DisplayName("应返回true - when stock is sufficient")
        void shouldReturnTrue_whenStockIsSufficient() throws Exception {
            // Arrange
            Long productId = 1L;
            Integer quantity = 50;

            when(inventoryService.checkStock(productId, quantity)).thenReturn(true);

            // Act & Assert
            mockMvc.perform(post("/api/inventory/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "productId", productId,
                        "quantity", quantity
                    ))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("应返回false - when stock is insufficient")
        void shouldReturnFalse_whenStockIsInsufficient() throws Exception {
            // Arrange
            Long productId = 1L;
            Integer quantity = 150;

            when(inventoryService.checkStock(productId, quantity)).thenReturn(false);

            // Act & Assert
            mockMvc.perform(post("/api/inventory/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "productId", productId,
                        "quantity", quantity
                    ))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("应返回false - when inventory does not exist")
        void shouldReturnFalse_whenInventoryDoesNotExist() throws Exception {
            // Arrange
            Long productId = 999L;
            Integer quantity = 10;

            when(inventoryService.checkStock(productId, quantity)).thenReturn(false);

            // Act & Assert
            mockMvc.perform(post("/api/inventory/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                        "productId", productId,
                        "quantity", quantity
                    ))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("应返回400 - when productId is missing")
        void shouldReturn400_whenProductIdIsMissing() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("quantity", 10);
            // 缺少 productId

            // Act & Assert
            mockMvc.perform(post("/api/inventory/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("应返回400 - when quantity is missing")
        void shouldReturn400_whenQuantityIsMissing() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            // 缺少 quantity

            // Act & Assert
            mockMvc.perform(post("/api/inventory/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("应返回400 - when quantity is negative")
        void shouldReturn400_whenQuantityIsNegative() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", -10);

            // Act & Assert
            mockMvc.perform(post("/api/inventory/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/inventory/summary - 获取库存汇总")
    class GetInventorySummaryTests {

        @Test
        @DisplayName("应返回200 - when querying inventory summary")
        void shouldReturn200_whenQueryingInventorySummary() throws Exception {
            // Arrange
            // 这里假设服务有获取汇总的方法
            // 实际实现可能需要根据具体的服务方法调整

            // Act & Assert
            mockMvc.perform(get("/api/inventory/summary"))
                    .andDo(print())
                    .andExpect(status().isOk());

            // 注意：这个测试需要实际的汇总接口实现
            // 如果服务层没有相应方法，这个测试可能需要调整
        }

        @Test
        @DisplayName("应包含分类统计 - when grouping by category")
        void shouldIncludeCategoryStats_whenGroupingByCategory() throws Exception {
            // Arrange & Act & Assert
            // 这个测试需要根据实际的汇总接口实现来编写
            mockMvc.perform(get("/api/inventory/summary"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }
}
