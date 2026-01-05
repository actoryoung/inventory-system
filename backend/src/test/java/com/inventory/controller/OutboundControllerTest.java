package com.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.OutboundDTO;
import com.inventory.entity.Outbound;
import com.inventory.exception.BusinessException;
import com.inventory.service.OutboundService;
import com.inventory.vo.OutboundVO;
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
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 出库单控制器测试
 * Outbound Controller Tests
 *
 * 测试覆盖：
 * - POST /api/outbound - 创建出库单
 * - GET /api/outbound/{id} - 获取详情
 * - GET /api/outbound - 获取列表（分页）
 * - PUT /api/outbound/{id} - 更新出库单
 * - DELETE /api/outbound/{id} - 删除出库单
 * - PATCH /api/outbound/{id}/approve - 审核出库单
 * - PATCH /api/outbound/{id}/void - 作废出库单
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@WebMvcTest(OutboundController.class)
@DisplayName("出库单控制器测试 (OutboundController Tests)")
class OutboundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OutboundService outboundService;

    private OutboundDTO testOutboundDTO;
    private OutboundVO testOutboundVO;

    @BeforeEach
    void setUp() {
        // 创建测试DTO
        testOutboundDTO = new OutboundDTO();
        testOutboundDTO.setProductId(1L);
        testOutboundDTO.setQuantity(50);
        testOutboundDTO.setReceiver("客户A");
        testOutboundDTO.setReceiverPhone("13800138000");
        testOutboundDTO.setOutboundDate(LocalDateTime.now());
        testOutboundDTO.setRemark("测试出库单");

        // 创建测试VO
        testOutboundVO = new OutboundVO();
        testOutboundVO.setId(1L);
        testOutboundVO.setOutboundNo("OUT202601040001");
        testOutboundVO.setProductId(1L);
        testOutboundVO.setProductName("iPhone 15");
        testOutboundVO.setProductSku("SKU001");
        testOutboundVO.setQuantity(50);
        testOutboundVO.setReceiver("客户A");
        testOutboundVO.setReceiverPhone("13800138000");
        testOutboundVO.setOutboundDate(LocalDateTime.now());
        testOutboundVO.setStatus(Outbound.STATUS_PENDING);
        testOutboundVO.setStatusText("待审核");
        testOutboundVO.setRemark("测试出库单");
        testOutboundVO.setCreatedBy("system");
        testOutboundVO.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/outbound - 创建出库单")
    class CreateOutboundTests {

        @Test
        @DisplayName("应成功创建出库单并返回200")
        void should_Create_Success() throws Exception {
            // Arrange
            when(outboundService.create(any(OutboundDTO.class))).thenReturn(1L);

            // Act & Assert
            mockMvc.perform(post("/api/outbound")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("出库单创建成功"))
                    .andExpect(jsonPath("$.data.id").value(1));

            verify(outboundService, times(1)).create(any(OutboundDTO.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 商品不存在（返回400）")
        void should_Return400_When_ProductNotFound() throws Exception {
            // Arrange
            when(outboundService.create(any(OutboundDTO.class)))
                    .thenThrow(new BusinessException("商品不存在或已禁用"));

            // Act & Assert
            mockMvc.perform(post("/api/outbound")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("商品不存在")));

            verify(outboundService, times(1)).create(any(OutboundDTO.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 验证错误（数量为0）")
        void should_Return400_When_Quantity_Zero() throws Exception {
            // Arrange
            testOutboundDTO.setQuantity(0);
            when(outboundService.create(any(OutboundDTO.class)))
                    .thenThrow(new BusinessException("出库数量必须大于0"));

            // Act & Assert
            mockMvc.perform(post("/api/outbound")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isBadRequest());

            verify(outboundService, times(1)).create(any(OutboundDTO.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 验证错误（收货人为空）")
        void should_Return400_When_Receiver_Null() throws Exception {
            // Arrange
            testOutboundDTO.setReceiver(null);

            // Act & Assert
            mockMvc.perform(post("/api/outbound")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isBadRequest());

            verify(outboundService, never()).create(any(OutboundDTO.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 验证错误（收货人为空字符串）")
        void should_Return400_When_Receiver_Empty() throws Exception {
            // Arrange
            testOutboundDTO.setReceiver("");

            // Act & Assert
            mockMvc.perform(post("/api/outbound")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isBadRequest());

            verify(outboundService, never()).create(any(OutboundDTO.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 请求数据格式错误")
        void should_Return400_When_InvalidJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/outbound")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isBadRequest());

            verify(outboundService, never()).create(any(OutboundDTO.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 数量为负数")
        void should_Return400_When_Quantity_Negative() throws Exception {
            // Arrange
            testOutboundDTO.setQuantity(-10);

            // Act & Assert
            mockMvc.perform(post("/api/outbound")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isBadRequest());

            verify(outboundService, never()).create(any(OutboundDTO.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 数量超过最大值")
        void should_Return400_When_Quantity_Exceeds_Max() throws Exception {
            // Arrange
            testOutboundDTO.setQuantity(1000000);

            // Act & Assert
            mockMvc.perform(post("/api/outbound")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isBadRequest());

            verify(outboundService, never()).create(any(OutboundDTO.class));
        }
    }

    @Nested
    @DisplayName("GET /api/outbound/{id} - 获取出库单详情")
    class GetOutboundDetailTests {

        @Test
        @DisplayName("应成功获取出库单详情")
        void should_GetDetail_Success() throws Exception {
            // Arrange
            when(outboundService.getDetail(1L)).thenReturn(testOutboundVO);

            // Act & Assert
            mockMvc.perform(get("/api/outbound/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.outboundNo").value("OUT202601040001"))
                    .andExpect(jsonPath("$.data.productName").value("iPhone 15"))
                    .andExpect(jsonPath("$.data.receiver").value("客户A"))
                    .andExpect(jsonPath("$.data.status").value(Outbound.STATUS_PENDING))
                    .andExpect(jsonPath("$.data.statusText").value("待审核"));

            verify(outboundService, times(1)).getDetail(1L);
        }

        @Test
        @DisplayName("获取出库单详情失败 - 出库单不存在（返回404）")
        void should_Return404_When_OutboundNotFound() throws Exception {
            // Arrange
            when(outboundService.getDetail(999L))
                    .thenThrow(new BusinessException("出库单不存在"));

            // Act & Assert
            mockMvc.perform(get("/api/outbound/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(containsString("出库单不存在")));

            verify(outboundService, times(1)).getDetail(999L);
        }

        @Test
        @DisplayName("获取出库单详情失败 - ID格式错误")
        void should_Return400_When_InvalidId() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/outbound/invalid"))
                    .andExpect(status().isBadRequest());

            verify(outboundService, never()).getDetail(anyLong());
        }
    }

    @Nested
    @DisplayName("GET /api/outbound - 获取出库单列表（分页）")
    class GetOutboundListTests {

        @Test
        @DisplayName("应成功获取出库单列表")
        void should_GetList_Success() throws Exception {
            // Arrange
            IPage<OutboundVO> pageResult = mock(IPage.class);
            when(pageResult.getRecords()).thenReturn(Arrays.asList(testOutboundVO));
            when(pageResult.getTotal()).thenReturn(1L);
            when(pageResult.getCurrent()).thenReturn(1L);
            when(pageResult.getSize()).thenReturn(10);

            when(outboundService.page(
                    eq(null), eq(null), eq(null), eq(null), eq(1), eq(10)
            )).thenReturn(pageResult);

            // Act & Assert
            mockMvc.perform(get("/api/outbound")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records").isNotEmpty())
                    .andExpect(jsonPath("$.data.records[0].id").value(1))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));

            verify(outboundService, times(1)).page(
                    eq(null), eq(null), eq(null), eq(null), eq(1), eq(10)
            );
        }

        @Test
        @DisplayName("应支持按商品ID筛选")
        void should_Filter_By_ProductId() throws Exception {
            // Arrange
            IPage<OutboundVO> pageResult = mock(IPage.class);
            when(pageResult.getRecords()).thenReturn(Arrays.asList(testOutboundVO));
            when(pageResult.getTotal()).thenReturn(1L);
            when(pageResult.getCurrent()).thenReturn(1L);
            when(pageResult.getSize()).thenReturn(10);

            when(outboundService.page(
                    eq(1L), eq(null), eq(null), eq(null), eq(1), eq(10)
            )).thenReturn(pageResult);

            // Act & Assert
            mockMvc.perform(get("/api/outbound")
                            .param("page", "1")
                            .param("size", "10")
                            .param("productId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records").isArray());

            verify(outboundService, times(1)).page(
                    eq(1L), eq(null), eq(null), eq(null), eq(1), eq(10)
            );
        }

        @Test
        @DisplayName("应支持按状态筛选")
        void should_Filter_By_Status() throws Exception {
            // Arrange
            IPage<OutboundVO> pageResult = mock(IPage.class);
            when(pageResult.getRecords()).thenReturn(Arrays.asList(testOutboundVO));
            when(pageResult.getTotal()).thenReturn(1L);

            when(outboundService.page(
                    eq(null), eq(Outbound.STATUS_PENDING), eq(null), eq(null), eq(1), eq(10)
            )).thenReturn(pageResult);

            // Act & Assert
            mockMvc.perform(get("/api/outbound")
                            .param("page", "1")
                            .param("size", "10")
                            .param("status", String.valueOf(Outbound.STATUS_PENDING)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records").isArray());

            verify(outboundService, times(1)).page(
                    eq(null), eq(Outbound.STATUS_PENDING), eq(null), eq(null), eq(1), eq(10)
            );
        }

        @Test
        @DisplayName("应支持按日期范围筛选")
        void should_Filter_By_DateRange() throws Exception {
            // Arrange
            IPage<OutboundVO> pageResult = mock(IPage.class);
            when(pageResult.getRecords()).thenReturn(Arrays.asList(testOutboundVO));
            when(pageResult.getTotal()).thenReturn(1L);

            when(outboundService.page(
                    eq(null), eq(null),
                    eq("2026-01-01 00:00:00"),
                    eq("2026-01-31 23:59:59"),
                    eq(1), eq(10)
            )).thenReturn(pageResult);

            // Act & Assert
            mockMvc.perform(get("/api/outbound")
                            .param("page", "1")
                            .param("size", "10")
                            .param("startDate", "2026-01-01 00:00:00")
                            .param("endDate", "2026-01-31 23:59:59"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records").isArray());

            verify(outboundService, times(1)).page(
                    eq(null), eq(null),
                    eq("2026-01-01 00:00:00"),
                    eq("2026-01-31 23:59:59"),
                    eq(1), eq(10)
            );
        }

        @Test
        @DisplayName("应返回空列表当没有数据")
        void should_Return_EmptyList() throws Exception {
            // Arrange
            IPage<OutboundVO> pageResult = mock(IPage.class);
            when(pageResult.getRecords()).thenReturn(Arrays.asList());
            when(pageResult.getTotal()).thenReturn(0L);

            when(outboundService.page(
                    any(), any(), any(), any(), anyInt(), anyInt()
            )).thenReturn(pageResult);

            // Act & Assert
            mockMvc.perform(get("/api/outbound")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records").isEmpty())
                    .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("应使用默认分页参数")
        void should_Use_DefaultPagination() throws Exception {
            // Arrange
            IPage<OutboundVO> pageResult = mock(IPage.class);
            when(pageResult.getRecords()).thenReturn(Arrays.asList());
            when(pageResult.getTotal()).thenReturn(0L);

            when(outboundService.page(
                    eq(null), eq(null), eq(null), eq(null), eq(1), eq(10)
            )).thenReturn(pageResult);

            // Act & Assert
            mockMvc.perform(get("/api/outbound"))
                    .andExpect(status().isOk());

            verify(outboundService, times(1)).page(
                    eq(null), eq(null), eq(null), eq(null), eq(1), eq(10)
            );
        }
    }

    @Nested
    @DisplayName("PUT /api/outbound/{id} - 更新出库单")
    class UpdateOutboundTests {

        @Test
        @DisplayName("应成功更新出库单")
        void should_Update_Success() throws Exception {
            // Arrange
            when(outboundService.update(anyLong(), any(OutboundDTO.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(put("/api/outbound/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("出库单更新成功"))
                    .andExpect(jsonPath("$.data.success").value(true));

            verify(outboundService, times(1)).update(eq(1L), any(OutboundDTO.class));
        }

        @Test
        @DisplayName("更新出库单失败 - 出库单不存在（返回404）")
        void should_Return404_When_OutboundNotFound() throws Exception {
            // Arrange
            when(outboundService.update(anyLong(), any(OutboundDTO.class)))
                    .thenThrow(new BusinessException("出库单不存在"));

            // Act & Assert
            mockMvc.perform(put("/api/outbound/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(containsString("出库单不存在")));

            verify(outboundService, times(1)).update(eq(999L), any(OutboundDTO.class));
        }

        @Test
        @DisplayName("更新出库单失败 - 已审核状态（返回400）")
        void should_Return400_When_AlreadyApproved() throws Exception {
            // Arrange
            when(outboundService.update(anyLong(), any(OutboundDTO.class)))
                    .thenThrow(new BusinessException("出库单已审核，无法修改"));

            // Act & Assert
            mockMvc.perform(put("/api/outbound/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("已审核")));

            verify(outboundService, times(1)).update(eq(1L), any(OutboundDTO.class));
        }

        @Test
        @DisplayName("更新出库单失败 - 验证错误")
        void should_Return400_When_ValidationError() throws Exception {
            // Arrange
            testOutboundDTO.setReceiver(null);

            // Act & Assert
            mockMvc.perform(put("/api/outbound/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isBadRequest());

            verify(outboundService, never()).update(anyLong(), any(OutboundDTO.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/outbound/{id} - 删除出库单")
    class DeleteOutboundTests {

        @Test
        @DisplayName("应成功删除出库单")
        void should_Delete_Success() throws Exception {
            // Arrange
            doNothing().when(outboundService).voidOutbound(1L);
            when(outboundService.removeById(1L)).thenReturn(true);

            // Act & Assert
            mockMvc.perform(delete("/api/outbound/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("出库单删除成功"))
                    .andExpect(jsonPath("$.data.success").value(true));

            verify(outboundService, times(1)).voidOutbound(1L);
            verify(outboundService, times(1)).removeById(1L);
        }

        @Test
        @DisplayName("删除出库单失败 - 出库单不存在（返回404）")
        void should_Return404_When_OutboundNotFound() throws Exception {
            // Arrange
            doThrow(new BusinessException("出库单不存在"))
                    .when(outboundService).voidOutbound(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/outbound/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(containsString("出库单不存在")));

            verify(outboundService, times(1)).voidOutbound(999L);
            verify(outboundService, never()).removeById(anyLong());
        }

        @Test
        @DisplayName("删除出库单失败 - 已审核状态（返回400）")
        void should_Return400_When_AlreadyApproved() throws Exception {
            // Arrange
            doThrow(new BusinessException("出库单已审核，无法删除"))
                    .when(outboundService).voidOutbound(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/outbound/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("已审核")));

            verify(outboundService, times(1)).voidOutbound(1L);
            verify(outboundService, never()).removeById(anyLong());
        }
    }

    @Nested
    @DisplayName("PATCH /api/outbound/{id}/approve - 审核出库单")
    class ApproveOutboundTests {

        @Test
        @DisplayName("应成功审核出库单")
        void should_Approve_Success() throws Exception {
            // Arrange
            when(outboundService.approve(1L, "admin")).thenReturn(true);

            // Act & Assert
            mockMvc.perform(patch("/api/outbound/1/approve")
                            .param("approvedBy", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("出库单审核成功"))
                    .andExpect(jsonPath("$.data.success").value(true));

            verify(outboundService, times(1)).approve(1L, "admin");
        }

        @Test
        @DisplayName("应使用默认审核人")
        void should_Use_DefaultApprovedBy() throws Exception {
            // Arrange
            when(outboundService.approve(1L, "system")).thenReturn(true);

            // Act & Assert
            mockMvc.perform(patch("/api/outbound/1/approve"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(outboundService, times(1)).approve(1L, "system");
        }

        @Test
        @DisplayName("审核出库单失败 - 出库单不存在（返回404）")
        void should_Return404_When_OutboundNotFound() throws Exception {
            // Arrange
            when(outboundService.approve(999L, "admin"))
                    .thenThrow(new BusinessException("出库单不存在"));

            // Act & Assert
            mockMvc.perform(patch("/api/outbound/999/approve")
                            .param("approvedBy", "admin"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(containsString("出库单不存在")));

            verify(outboundService, times(1)).approve(999L, "admin");
        }

        @Test
        @DisplayName("审核出库单失败 - 已审核状态（返回400）")
        void should_Return400_When_AlreadyApproved() throws Exception {
            // Arrange
            when(outboundService.approve(1L, "admin"))
                    .thenThrow(new BusinessException("出库单已审核，无法重复审核"));

            // Act & Assert
            mockMvc.perform(patch("/api/outbound/1/approve")
                            .param("approvedBy", "admin"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("已审核")));

            verify(outboundService, times(1)).approve(1L, "admin");
        }

        @Test
        @DisplayName("审核出库单失败 - 库存不足（返回400）")
        void should_Return400_When_InsufficientStock() throws Exception {
            // Arrange
            when(outboundService.approve(1L, "admin"))
                    .thenThrow(new BusinessException("库存不足，当前库存为30"));

            // Act & Assert
            mockMvc.perform(patch("/api/outbound/1/approve")
                            .param("approvedBy", "admin"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("库存不足")));

            verify(outboundService, times(1)).approve(1L, "admin");
        }
    }

    @Nested
    @DisplayName("PATCH /api/outbound/{id}/void - 作废出库单")
    class VoidOutboundTests {

        @Test
        @DisplayName("应成功作废出库单")
        void should_Void_Success() throws Exception {
            // Arrange
            doNothing().when(outboundService).voidOutbound(1L);

            // Act & Assert
            mockMvc.perform(patch("/api/outbound/1/void"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("出库单作废成功"));

            verify(outboundService, times(1)).voidOutbound(1L);
        }

        @Test
        @DisplayName("作废出库单失败 - 出库单不存在（返回404）")
        void should_Return404_When_OutboundNotFound() throws Exception {
            // Arrange
            doThrow(new BusinessException("出库单不存在"))
                    .when(outboundService).voidOutbound(999L);

            // Act & Assert
            mockMvc.perform(patch("/api/outbound/999/void"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(containsString("出库单不存在")));

            verify(outboundService, times(1)).voidOutbound(999L);
        }

        @Test
        @DisplayName("作废出库单失败 - 已审核状态（返回400）")
        void should_Return400_When_AlreadyApproved() throws Exception {
            // Arrange
            doThrow(new BusinessException("出库单已审核，无法作废"))
                    .when(outboundService).voidOutbound(1L);

            // Act & Assert
            mockMvc.perform(patch("/api/outbound/1/void"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("已审核")));

            verify(outboundService, times(1)).voidOutbound(1L);
        }

        @Test
        @DisplayName("作废出库单失败 - 已作废状态（返回400）")
        void should_Return400_When_AlreadyVoided() throws Exception {
            // Arrange
            doThrow(new BusinessException("出库单已作废，无法重复作废"))
                    .when(outboundService).voidOutbound(1L);

            // Act & Assert
            mockMvc.perform(patch("/api/outbound/1/void"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("已作废")));

            verify(outboundService, times(1)).voidOutbound(1L);
        }
    }

    @Nested
    @DisplayName("综合测试 (Integration Tests)")
    class IntegrationTests {

        @Test
        @DisplayName("完整流程测试：创建 -> 查询 -> 审核")
        void should_Complete_Flow_Create_Query_Approve() throws Exception {
            // Arrange - 创建
            when(outboundService.create(any(OutboundDTO.class))).thenReturn(1L);

            // Act & Assert - 创建
            mockMvc.perform(post("/api/outbound")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testOutboundDTO)))
                    .andExpect(status().isOk());

            // Arrange - 查询
            when(outboundService.getDetail(1L)).thenReturn(testOutboundVO);

            // Act & Assert - 查询
            mockMvc.perform(get("/api/outbound/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value(Outbound.STATUS_PENDING));

            // Arrange - 审核
            when(outboundService.approve(1L, "admin")).thenReturn(true);

            // Act & Assert - 审核
            mockMvc.perform(patch("/api/outbound/1/approve")
                            .param("approvedBy", "admin"))
                    .andExpect(status().isOk());

            verify(outboundService, times(1)).create(any(OutboundDTO.class));
            verify(outboundService, times(1)).getDetail(1L);
            verify(outboundService, times(1)).approve(1L, "admin");
        }
    }

    @Nested
    @DisplayName("边界条件测试 (Boundary Tests)")
    class BoundaryTests {

        @Test
        @DisplayName("应处理最小ID值")
        void should_Handle_MinId() throws Exception {
            // Arrange
            when(outboundService.getDetail(1L)).thenReturn(testOutboundVO);

            // Act & Assert
            mockMvc.perform(get("/api/outbound/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("应处理大ID值")
        void should_Handle_LargeId() throws Exception {
            // Arrange
            Long largeId = Long.MAX_VALUE;
            when(outboundService.getDetail(largeId))
                    .thenThrow(new BusinessException("出库单不存在"));

            // Act & Assert
            mockMvc.perform(get("/api/outbound/" + largeId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("应处理空字符串参数")
        void should_Handle_EmptyStringParams() throws Exception {
            // Arrange
            IPage<OutboundVO> pageResult = mock(IPage.class);
            when(pageResult.getRecords()).thenReturn(Arrays.asList());
            when(pageResult.getTotal()).thenReturn(0L);

            when(outboundService.page(
                    eq(null), eq(null), eq(null), eq(null), eq(1), eq(10)
            )).thenReturn(pageResult);

            // Act & Assert
            mockMvc.perform(get("/api/outbound")
                            .param("page", "")
                            .param("size", ""))
                    .andExpect(status().isBadRequest());
        }
    }
}
