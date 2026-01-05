package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.entity.Inbound;
import com.inventory.entity.Product;
import com.inventory.service.InboundService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 入库控制器集成测试
 *
 * 测试覆盖：
 * - REST API 端点测试
 * - 请求/响应格式验证
 * - 错误码验证
 * - 状态操作测试
 */
@WebMvcTest(InboundController.class)
@DisplayName("入库控制器测试 (InboundControllerTest)")
class InboundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InboundService inboundService;

    @Autowired
    private ObjectMapper objectMapper;

    private Inbound testInbound;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 准备测试商品数据
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSku("SKU001");
        testProduct.setName("iPhone 15");
        testProduct.setUnit("台");
        testProduct.setPrice(new BigDecimal("5999.00"));

        // 准备测试入库单数据
        testInbound = new Inbound();
        testInbound.setId(1L);
        testInbound.setInboundNo("IN202601040001");
        testInbound.setProductId(1L);
        testInbound.setQuantity(100);
        testInbound.setSupplier("供应商A");
        testInbound.setInboundDate(LocalDateTime.now());
        testInbound.setStatus(0); // 待审核
        testInbound.setRemark("测试入库单");
        testInbound.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/inbound - 创建入库单")
    class CreateInboundApiTests {

        @Test
        @DisplayName("应返回200 - when creating inbound with valid data")
        void shouldReturn200_whenCreatingInboundWithValidData() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", 100);
            requestBody.put("supplier", "供应商A");
            requestBody.put("inboundDate", "2026-01-04T10:00:00");
            requestBody.put("remark", "测试备注");

            when(inboundService.create(any(Inbound.class))).thenReturn(1L);

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("入库单创建成功"))
                    .andExpect(jsonPath("$.data").value(1));

            verify(inboundService, times(1)).create(any(Inbound.class));
        }

        @Test
        @DisplayName("应返回400 - when product does not exist")
        void shouldReturn400_whenProductNotExists() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 999L);
            requestBody.put("quantity", 100);
            requestBody.put("supplier", "供应商");

            when(inboundService.create(any(Inbound.class)))
                .thenThrow(new RuntimeException("商品不存在"));

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("商品")));
        }

        @Test
        @DisplayName("应返回400 - when quantity is zero")
        void shouldReturn400_whenQuantityIsZero() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", 0);
            requestBody.put("supplier", "供应商");

            when(inboundService.create(any(Inbound.class)))
                .thenThrow(new IllegalArgumentException("入库数量必须大于0"));

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("入库数量")));
        }

        @Test
        @DisplayName("应返回400 - when quantity is negative")
        void shouldReturn400_whenQuantityIsNegative() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", -50);
            requestBody.put("supplier", "供应商");

            when(inboundService.create(any(Inbound.class)))
                .thenThrow(new IllegalArgumentException("入库数量必须大于0"));

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("入库数量")));
        }

        @Test
        @DisplayName("应返回400 - when supplier is null")
        void shouldReturn400_whenSupplierIsNull() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", 100);
            requestBody.put("supplier", null);

            when(inboundService.create(any(Inbound.class)))
                .thenThrow(new IllegalArgumentException("供应商不能为空"));

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("供应商")));
        }

        @Test
        @DisplayName("应返回400 - when supplier is empty")
        void shouldReturn400_whenSupplierIsEmpty() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", 100);
            requestBody.put("supplier", "");

            when(inboundService.create(any(Inbound.class)))
                .thenThrow(new IllegalArgumentException("供应商不能为空"));

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("供应商")));
        }

        @Test
        @DisplayName("应返回400 - when required fields are missing")
        void shouldReturn400_whenRequiredFieldsAreMissing() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("remark", "测试");
            // 缺少必填字段：productId, quantity, supplier

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/inbound/{id} - 获取入库单详情")
    class GetInboundApiTests {

        @Test
        @DisplayName("应返回200 - when inbound exists")
        void shouldReturn200_whenInboundExists() throws Exception {
            // Arrange
            testInbound.setProductName("iPhone 15");
            when(inboundService.getByIdWithProduct(1L)).thenReturn(testInbound);

            // Act & Assert
            mockMvc.perform(get("/api/inbound/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.inboundNo").value("IN202601040001"))
                    .andExpect(jsonPath("$.data.productId").value(1))
                    .andExpect(jsonPath("$.data.quantity").value(100))
                    .andExpect(jsonPath("$.data.supplier").value("供应商A"));
        }

        @Test
        @DisplayName("应返回404 - when inbound not found")
        void shouldReturn404_whenInboundNotFound() throws Exception {
            // Arrange
            when(inboundService.getByIdWithProduct(999L)).thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/api/inbound/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/inbound - 获取入库单列表")
    class GetInboundsApiTests {

        @Test
        @DisplayName("应返回200 - when querying inbounds without filters")
        void shouldReturn200_whenQueryingInboundsWithoutFilters() throws Exception {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));
            page.setTotal(1);

            when(inboundService.page(eq(1), eq(10), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inbound")
                    .param("page", "1")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("应返回200 - when filtering by productId")
        void shouldReturn200_whenFilteringByProductId() throws Exception {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));

            when(inboundService.page(eq(1), eq(10), eq(1L), isNull(), isNull(), isNull()))
                .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inbound")
                    .param("page", "1")
                    .param("size", "10")
                    .param("productId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(inboundService, times(1)).page(eq(1), eq(10), eq(1L), isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("应返回200 - when filtering by status")
        void shouldReturn200_whenFilteringByStatus() throws Exception {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));

            when(inboundService.page(eq(1), eq(10), isNull(), eq(0), isNull(), isNull()))
                .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inbound")
                    .param("page", "1")
                    .param("size", "10")
                    .param("status", "0"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(inboundService, times(1)).page(eq(1), eq(10), isNull(), eq(0), isNull(), isNull());
        }

        @Test
        @DisplayName("应返回200 - when filtering by date range")
        void shouldReturn200_whenFilteringByDateRange() throws Exception {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));

            when(inboundService.page(eq(1), eq(10), isNull(), isNull(), any(), any()))
                .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inbound")
                    .param("page", "1")
                    .param("size", "10")
                    .param("startDate", "2026-01-01T00:00:00")
                    .param("endDate", "2026-01-31T23:59:59"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(inboundService, times(1)).page(eq(1), eq(10), isNull(), isNull(), any(), any());
        }

        @Test
        @DisplayName("应使用默认分页参数 - when page and size not provided")
        void shouldUseDefaultPagination_whenPageAndSizeNotProvided() throws Exception {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));

            when(inboundService.page(eq(1), eq(10), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/inbound"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(inboundService, times(1)).page(eq(1), eq(10), isNull(), isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("应返回空列表 - when no inbounds match filter")
        void shouldReturnEmptyList_whenNoInboundsMatch() throws Exception {
            // Arrange
            Page<Inbound> emptyPage = new Page<>(1, 10);
            emptyPage.setRecords(Arrays.asList());
            emptyPage.setTotal(0);

            when(inboundService.page(eq(1), eq(10), eq(999L), isNull(), isNull(), isNull()))
                .thenReturn(emptyPage);

            // Act & Assert
            mockMvc.perform(get("/api/inbound")
                    .param("page", "1")
                    .param("size", "10")
                    .param("productId", "999"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").value(0));
        }
    }

    @Nested
    @DisplayName("PUT /api/inbound/{id} - 更新入库单")
    class UpdateInboundApiTests {

        @Test
        @DisplayName("应返回200 - when updating inbound successfully")
        void shouldReturn200_whenUpdatingInboundSuccessfully() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", 1L);
            requestBody.put("quantity", 150);
            requestBody.put("supplier", "新供应商");
            requestBody.put("remark", "更新备注");

            when(inboundService.update(any(Inbound.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(put("/api/inbound/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("更新成功"));
        }

        @Test
        @DisplayName("应返回400 - when inbound is already approved")
        void shouldReturn400_whenInboundIsApproved() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", 1L);
            requestBody.put("quantity", 200);

            when(inboundService.update(any(Inbound.class)))
                .thenThrow(new RuntimeException("入库单已审核，无法修改"));

            // Act & Assert
            mockMvc.perform(put("/api/inbound/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("已审核")));
        }

        @Test
        @DisplayName("应返回400 - when updating with invalid quantity")
        void shouldReturn400_whenUpdatingWithInvalidQuantity() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", 1L);
            requestBody.put("quantity", 0);

            when(inboundService.update(any(Inbound.class)))
                .thenThrow(new IllegalArgumentException("入库数量必须大于0"));

            // Act & Assert
            mockMvc.perform(put("/api/inbound/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("入库数量")));
        }

        @Test
        @DisplayName("应返回404 - when inbound not found")
        void shouldReturn404_whenInboundNotFound() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("id", 999L);

            when(inboundService.update(any(Inbound.class)))
                .thenThrow(new RuntimeException("入库单不存在"));

            // Act & Assert
            mockMvc.perform(put("/api/inbound/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("不存在")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/inbound/{id} - 删除入库单")
    class DeleteInboundApiTests {

        @Test
        @DisplayName("应返回200 - when deleting inbound successfully")
        void shouldReturn200_whenDeletingInboundSuccessfully() throws Exception {
            // Arrange
            when(inboundService.delete(1L)).thenReturn(true);

            // Act & Assert
            mockMvc.perform(delete("/api/inbound/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("删除成功"));
        }

        @Test
        @DisplayName("应返回400 - when inbound is already approved")
        void shouldReturn400_whenInboundIsApproved() throws Exception {
            // Arrange
            when(inboundService.delete(1L))
                .thenThrow(new RuntimeException("入库单已审核，无法删除"));

            // Act & Assert
            mockMvc.perform(delete("/api/inbound/1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("已审核")));
        }

        @Test
        @DisplayName("应返回404 - when inbound not found")
        void shouldReturn404_whenInboundNotFound() throws Exception {
            // Arrange
            when(inboundService.delete(999L))
                .thenThrow(new RuntimeException("入库单不存在"));

            // Act & Assert
            mockMvc.perform(delete("/api/inbound/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("不存在")));
        }
    }

    @Nested
    @DisplayName("PATCH /api/inbound/{id}/approve - 审核入库单")
    class ApproveInboundApiTests {

        @Test
        @DisplayName("应返回200 - when approving inbound successfully")
        void shouldReturn200_whenApprovingInboundSuccessfully() throws Exception {
            // Arrange
            when(inboundService.approve(1L, "admin")).thenReturn(true);

            // Act & Assert
            mockMvc.perform(patch("/api/inbound/1/approve")
                    .param("approvedBy", "admin"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("审核成功"));
        }

        @Test
        @DisplayName("应返回400 - when inbound is already approved")
        void shouldReturn400_whenInboundIsAlreadyApproved() throws Exception {
            // Arrange
            when(inboundService.approve(1L, "admin"))
                .thenThrow(new RuntimeException("入库单已审核，无法重复审核"));

            // Act & Assert
            mockMvc.perform(patch("/api/inbound/1/approve")
                    .param("approvedBy", "admin"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("已审核")));
        }

        @Test
        @DisplayName("应返回400 - when inbound is voided")
        void shouldReturn400_whenInboundIsVoided() throws Exception {
            // Arrange
            when(inboundService.approve(1L, "admin"))
                .thenThrow(new RuntimeException("入库单已作废，无法审核"));

            // Act & Assert
            mockMvc.perform(patch("/api/inbound/1/approve")
                    .param("approvedBy", "admin"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("已作废")));
        }

        @Test
        @DisplayName("应返回404 - when inbound not found")
        void shouldReturn404_whenInboundNotFound() throws Exception {
            // Arrange
            when(inboundService.approve(999L, "admin"))
                .thenThrow(new RuntimeException("入库单不存在"));

            // Act & Assert
            mockMvc.perform(patch("/api/inbound/999/approve")
                    .param("approvedBy", "admin"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("不存在")));
        }
    }

    @Nested
    @DisplayName("PATCH /api/inbound/{id}/void - 作废入库单")
    class VoidInboundApiTests {

        @Test
        @DisplayName("应返回200 - when voiding inbound successfully")
        void shouldReturn200_whenVoidingInboundSuccessfully() throws Exception {
            // Arrange
            when(inboundService.void(1L)).thenReturn(true);

            // Act & Assert
            mockMvc.perform(patch("/api/inbound/1/void"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("作废成功"));
        }

        @Test
        @DisplayName("应返回400 - when inbound is already approved")
        void shouldReturn400_whenInboundIsApproved() throws Exception {
            // Arrange
            when(inboundService.void(1L))
                .thenThrow(new RuntimeException("入库单已审核，无法作废"));

            // Act & Assert
            mockMvc.perform(patch("/api/inbound/1/void"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("已审核")));
        }

        @Test
        @DisplayName("应返回400 - when inbound is already voided")
        void shouldReturn400_whenInboundIsAlreadyVoided() throws Exception {
            // Arrange
            when(inboundService.void(1L))
                .thenThrow(new RuntimeException("入库单已作废，无法重复作废"));

            // Act & Assert
            mockMvc.perform(patch("/api/inbound/1/void"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("已作废")));
        }

        @Test
        @DisplayName("应返回404 - when inbound not found")
        void shouldReturn404_whenInboundNotFound() throws Exception {
            // Arrange
            when(inboundService.void(999L))
                .thenThrow(new RuntimeException("入库单不存在"));

            // Act & Assert
            mockMvc.perform(patch("/api/inbound/999/void"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("不存在")));
        }
    }

    @Nested
    @DisplayName("边界条件测试 (Boundary Tests)")
    class BoundaryTests {

        @Test
        @DisplayName("应成功创建 - when quantity is at minimum (1)")
        void shouldCreateSuccessfully_whenQuantityIsAtMinimum() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", 1);
            requestBody.put("supplier", "供应商");

            when(inboundService.create(any(Inbound.class))).thenReturn(1L);

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("应成功创建 - when quantity is at maximum (999999)")
        void shouldCreateSuccessfully_whenQuantityIsAtMaximum() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", 999999);
            requestBody.put("supplier", "供应商");

            when(inboundService.create(any(Inbound.class))).thenReturn(1L);

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("应返回400 - when quantity exceeds maximum")
        void shouldReturn400_whenQuantityExceedsMaximum() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", 1000000);
            requestBody.put("supplier", "供应商");

            when(inboundService.create(any(Inbound.class)))
                .thenThrow(new IllegalArgumentException("入库数量超过最大限制"));

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("应返回400 - when supplier name is at maximum length (100)")
        void shouldReturn400_whenSupplierNameIsAtMaximum() throws Exception {
            // Arrange
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("productId", 1L);
            requestBody.put("quantity", 100);
            requestBody.put("supplier", "A".repeat(101)); // 超过100字符

            when(inboundService.create(any(Inbound.class)))
                .thenThrow(new IllegalArgumentException("供应商名称长度不能超过100个字符"));

            // Act & Assert
            mockMvc.perform(post("/api/inbound")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBody)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
