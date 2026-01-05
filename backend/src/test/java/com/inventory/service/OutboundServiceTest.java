package com.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.dto.OutboundDTO;
import com.inventory.entity.Outbound;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.OutboundMapper;
import com.inventory.mapper.ProductMapper;
import com.inventory.vo.OutboundVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 出库单服务测试
 * Outbound Service Tests
 *
 * 测试覆盖：
 * - 正常场景测试
 * - 异常场景测试（商品不存在、库存不足、数量不合法、状态不允许操作等）
 * - 边界条件测试（数量边界、单号序号边界）
 * - 业务规则测试（单号生成、状态流转、库存处理）
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("出库单服务测试 (OutboundService Tests)")
class OutboundServiceTest {

    @Mock
    private OutboundMapper outboundMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OutboundServiceImpl outboundService;

    private Product testProduct;
    private OutboundDTO testOutboundDTO;
    private Outbound testOutbound;

    @BeforeEach
    void setUp() {
        // 创建测试商品
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSku("SKU001");
        testProduct.setName("iPhone 15");
        testProduct.setPrice(new BigDecimal("7999.00"));
        testProduct.setCostPrice(new BigDecimal("6000.00"));
        testProduct.setUnit("台");
        testProduct.setWarningStock(10);
        testProduct.setStatus(1); // 启用
        testProduct.setCreatedAt(LocalDateTime.now());

        // 创建测试DTO
        testOutboundDTO = new OutboundDTO();
        testOutboundDTO.setProductId(1L);
        testOutboundDTO.setQuantity(50);
        testOutboundDTO.setReceiver("客户A");
        testOutboundDTO.setReceiverPhone("13800138000");
        testOutboundDTO.setOutboundDate(LocalDateTime.now());
        testOutboundDTO.setRemark("测试出库单");

        // 创建测试出库单
        testOutbound = new Outbound();
        testOutbound.setId(1L);
        testOutbound.setOutboundNo("OUT202601040001");
        testOutbound.setProductId(1L);
        testOutbound.setQuantity(50);
        testOutbound.setReceiver("客户A");
        testOutbound.setReceiverPhone("13800138000");
        testOutbound.setOutboundDate(LocalDateTime.now());
        testOutbound.setStatus(Outbound.STATUS_PENDING);
        testOutbound.setRemark("测试出库单");
        testOutbound.setCreatedBy("system");
        testOutbound.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("创建出库单测试 (Create Outbound Tests)")
    class CreateOutboundTests {

        @Test
        @DisplayName("应成功创建出库单并自动生成单号")
        void should_CreateOutbound_Success_When_ValidInput() {
            // Arrange
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(outboundMapper.insert(any(Outbound.class))).thenAnswer(invocation -> {
                Outbound outbound = invocation.getArgument(0);
                outbound.setId(1L);
                return 1;
            });

            // Act
            Long id = outboundService.create(testOutboundDTO);

            // Assert
            assertThat(id).isNotNull();
            verify(outboundMapper, times(1)).insert(any(Outbound.class));
            verify(productMapper, times(1)).selectById(1L);
        }

        @Test
        @DisplayName("应验证单号格式：OUT + yyyyMMdd + 4位序号")
        void should_Generate_Valid_OutboundNo() {
            // Arrange
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(outboundMapper.insert(any(Outbound.class))).thenAnswer(invocation -> {
                Outbound outbound = invocation.getArgument(0);
                outbound.setId(1L);
                // 验证单号格式
                assertThat(outbound.getOutboundNo()).matches("^OUT\\d{12}$");
                return 1;
            });

            // Act
            outboundService.create(testOutboundDTO);

            // Assert
            verify(outboundMapper).insert(argThat(outbound ->
                    outbound.getOutboundNo() != null &&
                            outbound.getOutboundNo().startsWith("OUT") &&
                            outbound.getOutboundNo().length() == 15
            ));
        }

        @Test
        @DisplayName("创建出库单失败 - 商品不存在")
        void should_ThrowException_When_ProductNotFound() {
            // Arrange
            when(productMapper.selectById(999L)).thenReturn(null);
            testOutboundDTO.setProductId(999L);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.create(testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("商品不存在");

            verify(outboundMapper, never()).insert(any(Outbound.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 商品已禁用")
        void should_ThrowException_When_ProductDisabled() {
            // Arrange
            testProduct.setStatus(0); // 禁用
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.create(testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("商品已禁用");

            verify(outboundMapper, never()).insert(any(Outbound.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 数量必须大于0")
        void should_ThrowException_When_Quantity_Zero() {
            // Arrange
            testOutboundDTO.setQuantity(0);
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.create(testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("出库数量必须大于0");

            verify(outboundMapper, never()).insert(any(Outbound.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 数量不能为负数")
        void should_ThrowException_When_Quantity_Negative() {
            // Arrange
            testOutboundDTO.setQuantity(-10);
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.create(testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("出库数量必须大于0");

            verify(outboundMapper, never()).insert(any(Outbound.class));
        }

        @Test
        @DisplayName("创建出库单失败 - 收货人为空")
        void should_ThrowException_When_Receiver_Null() {
            // Arrange
            testOutboundDTO.setReceiver(null);
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.create(testOutboundDTO))
                    .isInstanceOf(BusinessException.class);

            verify(outboundMapper, never()).insert(any(Outbound.class));
        }

        @Test
        @DisplayName("创建出库单成功 - 最小边界数量（1）")
        void should_Create_Success_When_Quantity_MinBoundary() {
            // Arrange
            testOutboundDTO.setQuantity(1);
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(outboundMapper.insert(any(Outbound.class))).thenAnswer(invocation -> {
                Outbound outbound = invocation.getArgument(0);
                outbound.setId(1L);
                return 1;
            });

            // Act
            Long id = outboundService.create(testOutboundDTO);

            // Assert
            assertThat(id).isNotNull();
            verify(outboundMapper).insert(argThat(outbound ->
                    outbound.getQuantity() == 1
            ));
        }

        @Test
        @DisplayName("创建出库单成功 - 最大边界数量（999999）")
        void should_Create_Success_When_Quantity_MaxBoundary() {
            // Arrange
            testOutboundDTO.setQuantity(999999);
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(outboundMapper.insert(any(Outbound.class))).thenAnswer(invocation -> {
                Outbound outbound = invocation.getArgument(0);
                outbound.setId(1L);
                return 1;
            });

            // Act
            Long id = outboundService.create(testOutboundDTO);

            // Assert
            assertThat(id).isNotNull();
        }

        @Test
        @DisplayName("创建出库单失败 - 超过最大数量限制")
        void should_ThrowException_When_Quantity_Exceeds_Max() {
            // Arrange
            testOutboundDTO.setQuantity(1000000);
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.create(testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("出库数量不能超过");

            verify(outboundMapper, never()).insert(any(Outbound.class));
        }
    }

    @Nested
    @DisplayName("更新出库单测试 (Update Outbound Tests)")
    class UpdateOutboundTests {

        @Test
        @DisplayName("应成功更新待审核状态的出库单")
        void should_Update_Success_When_Status_Pending() {
            // Arrange
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(outboundMapper.updateById(any(Outbound.class))).thenReturn(true);

            OutboundDTO updateDTO = new OutboundDTO();
            updateDTO.setProductId(1L);
            updateDTO.setQuantity(60);
            updateDTO.setReceiver("新客户");
            updateDTO.setOutboundDate(LocalDateTime.now());

            // Act
            boolean result = outboundService.update(1L, updateDTO);

            // Assert
            assertThat(result).isTrue();
            verify(outboundMapper, times(1)).updateById(any(Outbound.class));
        }

        @Test
        @DisplayName("更新出库单失败 - 出库单不存在")
        void should_ThrowException_When_Outbound_NotFound() {
            // Arrange
            when(outboundMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.update(999L, testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("出库单不存在");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }

        @Test
        @DisplayName("更新出库单失败 - 已审核状态无法修改")
        void should_ThrowException_When_Status_Approved() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_APPROVED);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.update(1L, testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有待审核状态的出库单可以修改");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }

        @Test
        @DisplayName("更新出库单失败 - 已作废状态无法修改")
        void should_ThrowException_When_Status_Void() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_VOID);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.update(1L, testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有待审核状态的出库单可以修改");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }

        @Test
        @DisplayName("更新出库单失败 - 商品不存在")
        void should_ThrowException_When_Product_NotFound_OnUpdate() {
            // Arrange
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            when(productMapper.selectById(1L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.update(1L, testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("商品不存在");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }
    }

    @Nested
    @DisplayName("审核出库单测试 (Approve Outbound Tests)")
    class ApproveOutboundTests {

        @Test
        @DisplayName("应成功审核出库单并减少库存")
        void should_Approve_Success_When_ValidOutbound() {
            // Arrange
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            when(outboundMapper.updateById(any(Outbound.class))).thenReturn(true);
            doNothing().when(inventoryService).reduceStock(anyLong(), anyInt());

            // Act
            boolean result = outboundService.approve(1L, "admin");

            // Assert
            assertThat(result).isTrue();
            verify(outboundMapper, times(1)).updateById(argThat(outbound ->
                    outbound.getStatus().equals(Outbound.STATUS_APPROVED) &&
                            outbound.getApprovedBy().equals("admin") &&
                            outbound.getApprovedAt() != null
            ));
            verify(inventoryService, times(1)).reduceStock(1L, 50);
        }

        @Test
        @DisplayName("审核出库单失败 - 出库单不存在")
        void should_ThrowException_When_Outbound_NotFound_OnApprove() {
            // Arrange
            when(outboundMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.approve(999L, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("出库单不存在");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
            verify(inventoryService, never()).reduceStock(anyLong(), anyInt());
        }

        @Test
        @DisplayName("审核出库单失败 - 已审核状态无法重复审核")
        void should_ThrowException_When_Already_Approved() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_APPROVED);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.approve(1L, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有待审核状态的出库单可以审核");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
            verify(inventoryService, never()).reduceStock(anyLong(), anyInt());
        }

        @Test
        @DisplayName("审核出库单失败 - 已作废状态无法审核")
        void should_ThrowException_When_Voided() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_VOID);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.approve(1L, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有待审核状态的出库单可以审核");

            verify(inventoryService, never()).reduceStock(anyLong(), anyInt());
        }

        @Test
        @DisplayName("审核出库单失败 - 库存不足")
        void should_ThrowException_When_Insufficient_Stock() {
            // Arrange
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            doThrow(new BusinessException("库存不足，当前库存为30"))
                    .when(inventoryService).reduceStock(anyLong(), anyInt());

            // Act & Assert
            assertThatThrownBy(() -> outboundService.approve(1L, "admin"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("库存不足");

            verify(inventoryService, times(1)).reduceStock(1L, 50);
            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }
    }

    @Nested
    @DisplayName("作废出库单测试 (Void Outbound Tests)")
    class VoidOutboundTests {

        @Test
        @DisplayName("应成功作废待审核状态的出库单")
        void should_Void_Success_When_Status_Pending() {
            // Arrange
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            when(outboundMapper.updateById(any(Outbound.class))).thenReturn(true);

            // Act
            outboundService.voidOutbound(1L);

            // Assert
            verify(outboundMapper, times(1)).updateById(argThat(outbound ->
                    outbound.getStatus().equals(Outbound.STATUS_VOID)
            ));
        }

        @Test
        @DisplayName("作废出库单失败 - 出库单不存在")
        void should_ThrowException_When_Outbound_NotFound_OnVoid() {
            // Arrange
            when(outboundMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.voidOutbound(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("出库单不存在");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }

        @Test
        @DisplayName("作废出库单失败 - 已审核状态无法作废")
        void should_ThrowException_When_Already_Approved_OnVoid() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_APPROVED);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.voidOutbound(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有待审核状态的出库单可以作废");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }

        @Test
        @DisplayName("作废出库单失败 - 已作废状态无法重复作废")
        void should_ThrowException_When_Already_Voided() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_VOID);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.voidOutbound(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有待审核状态的出库单可以作废");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }
    }

    @Nested
    @DisplayName("查询出库单详情测试 (Get Outbound Detail Tests)")
    class GetOutboundDetailTests {

        @Test
        @DisplayName("应成功获取出库单详情")
        void should_GetDetail_Success() {
            // Arrange
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act
            OutboundVO vo = outboundService.getDetail(1L);

            // Assert
            assertThat(vo).isNotNull();
            assertThat(vo.getId()).isEqualTo(1L);
            assertThat(vo.getOutboundNo()).isEqualTo("OUT202601040001");
            assertThat(vo.getProductName()).isEqualTo("iPhone 15");
            assertThat(vo.getProductSku()).isEqualTo("SKU001");
        }

        @Test
        @DisplayName("获取出库单详情失败 - 出库单不存在")
        void should_ThrowException_When_Outbound_NotFound_OnGetDetail() {
            // Arrange
            when(outboundMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.getDetail(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("出库单不存在");
        }

        @Test
        @DisplayName("应处理商品不存在的情况")
        void should_Handle_ProductNotFound_OnGetDetail() {
            // Arrange
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            when(productMapper.selectById(1L)).thenReturn(null);

            // Act
            OutboundVO vo = outboundService.getDetail(1L);

            // Assert
            assertThat(vo).isNotNull();
            assertThat(vo.getProductName()).isNull();
            assertThat(vo.getProductSku()).isNull();
        }
    }

    @Nested
    @DisplayName("分页查询出库单测试 (Page Outbound Tests)")
    class PageOutboundTests {

        @Test
        @DisplayName("应成功分页查询出库单列表")
        void should_Page_Success() {
            // Arrange
            when(outboundMapper.selectPage(any(), any()))
                    .thenAnswer(invocation -> {
                        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Outbound> page =
                                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
                        page.setRecords(Arrays.asList(testOutbound));
                        page.setTotal(1);
                        return page;
                    });
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act
            IPage<OutboundVO> result = outboundService.page(null, null, null, null, 1, 10);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getRecords().get(0).getProductName()).isEqualTo("iPhone 15");
        }

        @Test
        @DisplayName("应支持按商品ID筛选")
        void should_Filter_By_ProductId() {
            // Arrange
            when(outboundMapper.selectPage(any(), any()))
                    .thenAnswer(invocation -> {
                        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Outbound> page =
                                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
                        page.setRecords(Arrays.asList(testOutbound));
                        page.setTotal(1);
                        return page;
                    });
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act
            IPage<OutboundVO> result = outboundService.page(1L, null, null, null, 1, 10);

            // Assert
            assertThat(result.getRecords()).hasSize(1);
            verify(outboundMapper).selectPage(any(), argThat(wrapper ->
                    wrapper != null
            ));
        }

        @Test
        @DisplayName("应支持按状态筛选")
        void should_Filter_By_Status() {
            // Arrange
            when(outboundMapper.selectPage(any(), any()))
                    .thenAnswer(invocation -> {
                        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Outbound> page =
                                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
                        page.setRecords(Arrays.asList(testOutbound));
                        page.setTotal(1);
                        return page;
                    });
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act
            IPage<OutboundVO> result = outboundService.page(null, Outbound.STATUS_PENDING, null, null, 1, 10);

            // Assert
            assertThat(result.getRecords()).hasSize(1);
        }

        @Test
        @DisplayName("应支持按日期范围筛选")
        void should_Filter_By_DateRange() {
            // Arrange
            when(outboundMapper.selectPage(any(), any()))
                    .thenAnswer(invocation -> {
                        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Outbound> page =
                                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
                        page.setRecords(Arrays.asList(testOutbound));
                        page.setTotal(1);
                        return page;
                    });
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act
            IPage<OutboundVO> result = outboundService.page(
                    null, null,
                    "2026-01-01 00:00:00",
                    "2026-01-31 23:59:59",
                    1, 10
            );

            // Assert
            assertThat(result.getRecords()).hasSize(1);
        }

        @Test
        @DisplayName("应处理空列表情况")
        void should_Handle_EmptyList() {
            // Arrange
            when(outboundMapper.selectPage(any(), any()))
                    .thenAnswer(invocation -> {
                        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Outbound> page =
                                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
                        page.setRecords(Arrays.asList());
                        page.setTotal(0);
                        return page;
                    });

            // Act
            IPage<OutboundVO> result = outboundService.page(null, null, null, null, 1, 10);

            // Assert
            assertThat(result.getRecords()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("状态流转测试 (Status Flow Tests)")
    class StatusFlowTests {

        @Test
        @DisplayName("应验证完整的状态流转：待审核 -> 已审核")
        void should_Flow_FromPending_ToApproved() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_PENDING);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            when(outboundMapper.updateById(any(Outbound.class))).thenReturn(true);
            doNothing().when(inventoryService).reduceStock(anyLong(), anyInt());

            // Act
            outboundService.approve(1L, "admin");

            // Assert
            verify(outboundMapper).updateById(argThat(outbound ->
                    outbound.getStatus().equals(Outbound.STATUS_APPROVED)
            ));
        }

        @Test
        @DisplayName("应验证状态流转：待审核 -> 已作废")
        void should_Flow_FromPending_ToVoid() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_PENDING);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            when(outboundMapper.updateById(any(Outbound.class))).thenReturn(true);

            // Act
            outboundService.voidOutbound(1L);

            // Assert
            verify(outboundMapper).updateById(argThat(outbound ->
                    outbound.getStatus().equals(Outbound.STATUS_VOID)
            ));
        }

        @Test
        @DisplayName("应防止从已审核状态回退到待审核")
        void should_Prevent_Revert_FromApproved_ToPending() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_APPROVED);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.update(1L, testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有待审核状态的出库单可以修改");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }

        @Test
        @DisplayName("应防止从已作废状态回退到待审核")
        void should_Prevent_Revert_FromVoid_ToPending() {
            // Arrange
            testOutbound.setStatus(Outbound.STATUS_VOID);
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);

            // Act & Assert
            assertThatThrownBy(() -> outboundService.update(1L, testOutboundDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有待审核状态的出库单可以修改");

            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }
    }

    @Nested
    @DisplayName("库存验证测试 (Inventory Validation Tests)")
    class InventoryValidationTests {

        @Test
        @DisplayName("审核时应验证库存充足性")
        void should_Verify_Stock_OnApproval() {
            // Arrange
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            doNothing().when(inventoryService).reduceStock(1L, 50);
            when(outboundMapper.updateById(any(Outbound.class))).thenReturn(true);

            // Act
            outboundService.approve(1L, "admin");

            // Assert
            verify(inventoryService, times(1)).reduceStock(1L, 50);
        }

        @Test
        @DisplayName("库存不足时应抛出异常且不更新出库单状态")
        void should_NotUpdateStatus_When_StockInsufficient() {
            // Arrange
            when(outboundMapper.selectById(1L)).thenReturn(testOutbound);
            doThrow(new BusinessException("库存不足"))
                    .when(inventoryService).reduceStock(anyLong(), anyInt());

            // Act & Assert
            assertThatThrownBy(() -> outboundService.approve(1L, "admin"))
                    .isInstanceOf(BusinessException.class);

            verify(inventoryService, times(1)).reduceStock(1L, 50);
            verify(outboundMapper, never()).updateById(any(Outbound.class));
        }
    }
}
