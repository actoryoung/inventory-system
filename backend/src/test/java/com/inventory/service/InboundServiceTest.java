package com.inventory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inventory.entity.Inbound;
import com.inventory.entity.Product;
import com.inventory.entity.Inventory;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.InboundMapper;
import com.inventory.mapper.ProductMapper;
import com.inventory.mapper.InventoryMapper;
import com.inventory.service.impl.InboundServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 入库服务层单元测试
 *
 * 测试覆盖：
 * - 正常场景：创建、审核、修改、删除、作废、查询入库单
 * - 异常场景：商品不存在、数量不合法、状态不允许操作
 * - 边界条件：数量边界、单号序号边界
 */
@DisplayName("入库服务层测试 (InboundServiceTest)")
class InboundServiceTest {

    @Mock
    private InboundMapper inboundMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InboundServiceImpl inboundService;

    private Inbound testInbound;
    private Product testProduct;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 准备测试商品数据
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSku("SKU001");
        testProduct.setName("iPhone 15");
        testProduct.setCategoryId(1L);
        testProduct.setUnit("台");
        testProduct.setPrice(new BigDecimal("5999.00"));
        testProduct.setCostPrice(new BigDecimal("4500.00"));
        testProduct.setWarningStock(10);
        testProduct.setStatus(1);
        testProduct.setCreatedAt(LocalDateTime.now());

        // 准备测试库存数据
        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProductId(1L);
        testInventory.setWarehouseId(1L);
        testInventory.setQuantity(100);
        testInventory.setWarningStock(10);
        testInventory.setCreatedAt(LocalDateTime.now());

        // 准备测试入库单数据
        testInbound = new Inbound();
        testInbound.setId(1L);
        testInbound.setInboundNo("IN202601040001");
        testInbound.setProductId(1L);
        testInbound.setQuantity(50);
        testInbound.setSupplier("供应商A");
        testInbound.setInboundDate(LocalDateTime.now());
        testInbound.setStatus(0); // 待审核
        testInbound.setRemark("测试入库单");
        testInbound.setCreatedBy("admin");
        testInbound.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("创建入库单测试 (Create Inbound)")
    class CreateInboundTests {

        @Test
        @DisplayName("应成功创建入库单并生成单号 - given valid inbound data")
        void shouldCreateInboundSuccessfully_whenValidDataProvided() {
            // Arrange
            Inbound newInbound = new Inbound();
            newInbound.setProductId(1L);
            newInbound.setQuantity(100);
            newInbound.setSupplier("供应商B");
            newInbound.setInboundDate(LocalDateTime.now());
            newInbound.setRemark("新入库单");

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(inboundMapper.selectCount(any())).thenReturn(0L);
            when(inboundMapper.insert(any(Inbound.class))).thenAnswer(invocation -> {
                Inbound inbound = invocation.getArgument(0);
                inbound.setId(2L);
                return 1;
            });

            // Act
            Long id = inboundService.create(newInbound);

            // Assert
            assertNotNull(id, "入库单ID不应为空");
            assertEquals(2L, id);
            assertNotNull(newInbound.getInboundNo(), "入库单号应自动生成");
            assertTrue(newInbound.getInboundNo().startsWith("IN"), "单号应以IN开头");
            assertTrue(newInbound.getInboundNo().length() == 16, "单号长度应为16位");
            assertEquals(0, newInbound.getStatus(), "新建入库单状态应为待审核");
            verify(inboundMapper, times(1)).insert(any(Inbound.class));
        }

        @Test
        @DisplayName("应生成格式正确的单号 - when generating inbound number")
        void shouldGenerateCorrectFormatNumber_whenGeneratingInboundNumber() {
            // Arrange
            Inbound newInbound = new Inbound();
            newInbound.setProductId(1L);
            newInbound.setQuantity(50);
            newInbound.setSupplier("供应商");
            newInbound.setInboundDate(LocalDateTime.now());

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(inboundMapper.selectCount(any())).thenReturn(0L);
            when(inboundMapper.insert(any(Inbound.class))).thenReturn(1);

            // Act
            inboundService.create(newInbound);

            // Assert
            String inboundNo = newInbound.getInboundNo();
            assertTrue(inboundNo.matches("IN\\d{12}"), "单号格式应为IN + 12位数字");

            // 验证日期部分 (yyyyMMdd)
            String datePart = inboundNo.substring(2, 10);
            String todayDate = LocalDate.now().toString().replace("-", "");
            assertEquals(todayDate, datePart, "日期部分应为当天日期");

            // 验证序号部分 (4位)
            String sequencePart = inboundNo.substring(10, 14);
            assertTrue(sequencePart.matches("\\d{4}"), "序号应为4位数字");
        }

        @Test
        @DisplayName("应递增序号 - when creating multiple inbound orders in same day")
        void shouldIncrementSequence_whenCreatingMultipleInboundsInSameDay() {
            // Arrange
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(inboundMapper.selectCount(any())).thenReturn(5L); // 已有5单
            when(inboundMapper.insert(any(Inbound.class))).thenReturn(1);

            Inbound inbound1 = new Inbound();
            inbound1.setProductId(1L);
            inbound1.setQuantity(50);
            inbound1.setSupplier("供应商1");

            Inbound inbound2 = new Inbound();
            inbound2.setProductId(1L);
            inbound2.setQuantity(100);
            inbound2.setSupplier("供应商2");

            // Act
            inboundService.create(inbound1);
            inboundService.create(inbound2);

            // Assert
            String sequence1 = inbound1.getInboundNo().substring(10, 14);
            String sequence2 = inbound2.getInboundNo().substring(10, 14);
            assertTrue(Integer.parseInt(sequence2) > Integer.parseInt(sequence1),
                "后续单号序号应大于前面的单号");
        }

        @Test
        @DisplayName("应抛出异常 - when product does not exist")
        void shouldThrowException_whenProductNotExists() {
            // Arrange
            Inbound invalidInbound = new Inbound();
            invalidInbound.setProductId(999L);
            invalidInbound.setQuantity(50);
            invalidInbound.setSupplier("供应商");

            when(productMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.create(invalidInbound));

            assertTrue(exception.getMessage().contains("商品不存在") ||
                        exception.getMessage().contains("已禁用"));
            verify(inboundMapper, never()).insert(any());
        }

        @Test
        @DisplayName("应抛出异常 - when product is disabled")
        void shouldThrowException_whenProductIsDisabled() {
            // Arrange
            Product disabledProduct = new Product();
            disabledProduct.setId(2L);
            disabledProduct.setName("已禁用商品");
            disabledProduct.setStatus(0);

            Inbound invalidInbound = new Inbound();
            invalidInbound.setProductId(2L);
            invalidInbound.setQuantity(50);
            invalidInbound.setSupplier("供应商");

            when(productMapper.selectById(2L)).thenReturn(disabledProduct);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.create(invalidInbound));

            assertTrue(exception.getMessage().contains("已禁用"));
            verify(inboundMapper, never()).insert(any());
        }

        @Test
        @DisplayName("应抛出异常 - when quantity is zero")
        void shouldThrowException_whenQuantityIsZero() {
            // Arrange
            Inbound invalidInbound = new Inbound();
            invalidInbound.setProductId(1L);
            invalidInbound.setQuantity(0);
            invalidInbound.setSupplier("供应商");

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.create(invalidInbound));

            assertTrue(exception.getMessage().contains("入库数量") ||
                        exception.getMessage().contains("大于0"));
            verify(inboundMapper, never()).insert(any());
        }

        @Test
        @DisplayName("应抛出异常 - when quantity is negative")
        void shouldThrowException_whenQuantityIsNegative() {
            // Arrange
            Inbound invalidInbound = new Inbound();
            invalidInbound.setProductId(1L);
            invalidInbound.setQuantity(-10);
            invalidInbound.setSupplier("供应商");

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.create(invalidInbound));

            assertTrue(exception.getMessage().contains("入库数量") ||
                        exception.getMessage().contains("大于0"));
        }

        @Test
        @DisplayName("应抛出异常 - when quantity exceeds maximum")
        void shouldThrowException_whenQuantityExceedsMaximum() {
            // Arrange
            Inbound invalidInbound = new Inbound();
            invalidInbound.setProductId(1L);
            invalidInbound.setQuantity(1000000); // 超过最大值999999
            invalidInbound.setSupplier("供应商");

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.create(invalidInbound));

            assertTrue(exception.getMessage().contains("入库数量") ||
                        exception.getMessage().contains("超过"));
        }

        @Test
        @DisplayName("应抛出异常 - when supplier is null")
        void shouldThrowException_whenSupplierIsNull() {
            // Arrange
            Inbound invalidInbound = new Inbound();
            invalidInbound.setProductId(1L);
            invalidInbound.setQuantity(50);
            invalidInbound.setSupplier(null);

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.create(invalidInbound));

            assertTrue(exception.getMessage().contains("供应商"));
        }

        @Test
        @DisplayName("应抛出异常 - when supplier is empty")
        void shouldThrowException_whenSupplierIsEmpty() {
            // Arrange
            Inbound invalidInbound = new Inbound();
            invalidInbound.setProductId(1L);
            invalidInbound.setQuantity(50);
            invalidInbound.setSupplier("");

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.create(invalidInbound));

            assertTrue(exception.getMessage().contains("供应商"));
        }

        @Test
        @DisplayName("应抛出异常 - when supplier name exceeds maximum length")
        void shouldThrowException_whenSupplierNameExceedsMaxLength() {
            // Arrange
            Inbound invalidInbound = new Inbound();
            invalidInbound.setProductId(1L);
            invalidInbound.setQuantity(50);
            invalidInbound.setSupplier("A".repeat(101)); // 超过100字符

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.create(invalidInbound));

            assertTrue(exception.getMessage().contains("供应商") ||
                        exception.getMessage().contains("长度"));
        }

        @Test
        @DisplayName("应成功创建 - when quantity is at minimum boundary (1)")
        void shouldCreateSuccessfully_whenQuantityIsAtMinimum() {
            // Arrange
            Inbound inbound = new Inbound();
            inbound.setProductId(1L);
            inbound.setQuantity(1); // 最小边界值
            inbound.setSupplier("供应商");
            inbound.setInboundDate(LocalDateTime.now());

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(inboundMapper.selectCount(any())).thenReturn(0L);
            when(inboundMapper.insert(any(Inbound.class))).thenReturn(1);

            // Act
            Long id = inboundService.create(inbound);

            // Assert
            assertNotNull(id);
            verify(inboundMapper, times(1)).insert(any(Inbound.class));
        }

        @Test
        @DisplayName("应成功创建 - when quantity is at maximum boundary (999999)")
        void shouldCreateSuccessfully_whenQuantityIsAtMaximum() {
            // Arrange
            Inbound inbound = new Inbound();
            inbound.setProductId(1L);
            inbound.setQuantity(999999); // 最大边界值
            inbound.setSupplier("供应商");
            inbound.setInboundDate(LocalDateTime.now());

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(inboundMapper.selectCount(any())).thenReturn(0L);
            when(inboundMapper.insert(any(Inbound.class))).thenReturn(1);

            // Act
            Long id = inboundService.create(inbound);

            // Assert
            assertNotNull(id);
            verify(inboundMapper, times(1)).insert(any(Inbound.class));
        }
    }

    @Nested
    @DisplayName("审核入库单测试 (Approve Inbound)")
    class ApproveInboundTests {

        @Test
        @DisplayName("应成功审核并增加库存 - when inbound is in pending status")
        void shouldApproveSuccessfully_andIncreaseInventory() {
            // Arrange
            testInbound.setStatus(0); // 待审核
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);
            when(inboundMapper.updateById(any(Inbound.class))).thenReturn(1);
            when(inventoryMapper.selectOne(any())).thenReturn(testInventory);
            when(inventoryMapper.updateById(any(Inventory.class))).thenReturn(1);

            // Act
            inboundService.approve(1L, "admin");

            // Assert
            assertEquals(1, testInbound.getStatus(), "状态应更新为已审核");
            assertEquals("admin", testInbound.getApprovedBy());
            assertNotNull(testInbound.getApprovedAt());
            verify(inventoryMapper, times(1)).updateById(argThat(inv ->
                inv.getQuantity() == 150 // 100 + 50
            ));
            verify(inboundMapper, times(1)).updateById(any(Inbound.class));
        }

        @Test
        @DisplayName("应初始化库存 - when inventory record does not exist")
        void shouldInitializeInventory_whenInventoryNotExists() {
            // Arrange
            testInbound.setStatus(0);
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);
            when(inboundMapper.updateById(any(Inbound.class))).thenReturn(1);
            when(inventoryMapper.selectOne(any())).thenReturn(null); // 库存不存在
            when(inventoryMapper.insert(any(Inventory.class))).thenReturn(1);

            // Act
            inboundService.approve(1L, "admin");

            // Assert
            assertEquals(1, testInbound.getStatus());
            verify(inventoryMapper, times(1)).insert(argThat(inv ->
                inv.getQuantity() == 50 // 初始库存为入库数量
            ));
        }

        @Test
        @DisplayName("应抛出异常 - when inbound not found")
        void shouldThrowException_whenInboundNotFound() {
            // Arrange
            when(inboundMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.approve(999L, "admin"));

            assertTrue(exception.getMessage().contains("入库单不存在"));
            verify(inboundMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("应抛出异常 - when inbound is already approved")
        void shouldThrowException_whenInboundAlreadyApproved() {
            // Arrange
            testInbound.setStatus(1); // 已审核
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.approve(1L, "admin"));

            assertTrue(exception.getMessage().contains("已审核") ||
                        exception.getMessage().contains("无法重复审核"));
            verify(inboundMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("应抛出异常 - when inbound is already voided")
        void shouldThrowException_whenInboundAlreadyVoided() {
            // Arrange
            testInbound.setStatus(2); // 已作废
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.approve(1L, "admin"));

            assertTrue(exception.getMessage().contains("已作废") ||
                        exception.getMessage().contains("无法审核"));
            verify(inboundMapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("修改入库单测试 (Update Inbound)")
    class UpdateInboundTests {

        @Test
        @DisplayName("应成功修改 - when inbound is in pending status")
        void shouldUpdateSuccessfully_whenInboundIsPending() {
            // Arrange
            Inbound updateData = new Inbound();
            updateData.setId(1L);
            updateData.setQuantity(150); // 修改数量
            updateData.setSupplier("新供应商");
            updateData.setRemark("修改后的备注");

            testInbound.setStatus(0); // 待审核
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(inboundMapper.updateById(any(Inbound.class))).thenReturn(1);

            // Act
            boolean result = inboundService.update(updateData);

            // Assert
            assertTrue(result);
            verify(inboundMapper, times(1)).updateById(any(Inbound.class));
        }

        @Test
        @DisplayName("应抛出异常 - when inbound is already approved")
        void shouldThrowException_whenInboundIsApproved() {
            // Arrange
            testInbound.setStatus(1); // 已审核
            Inbound updateData = new Inbound();
            updateData.setId(1L);
            updateData.setQuantity(200);

            when(inboundMapper.selectById(1L)).thenReturn(testInbound);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.update(updateData));

            assertTrue(exception.getMessage().contains("已审核") ||
                        exception.getMessage().contains("无法修改"));
            verify(inboundMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("应抛出异常 - when inbound is voided")
        void shouldThrowException_whenInboundIsVoided() {
            // Arrange
            testInbound.setStatus(2); // 已作废
            Inbound updateData = new Inbound();
            updateData.setId(1L);

            when(inboundMapper.selectById(1L)).thenReturn(testInbound);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.update(updateData));

            assertTrue(exception.getMessage().contains("已作废") ||
                        exception.getMessage().contains("无法修改"));
        }

        @Test
        @DisplayName("应验证数量合法性 - when updating with invalid quantity")
        void shouldValidateQuantity_whenUpdatingWithInvalidQuantity() {
            // Arrange
            testInbound.setStatus(0);
            Inbound updateData = new Inbound();
            updateData.setId(1L);
            updateData.setQuantity(0); // 不合法的数量

            when(inboundMapper.selectById(1L)).thenReturn(testInbound);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.update(updateData));

            assertTrue(exception.getMessage().contains("入库数量"));
        }
    }

    @Nested
    @DisplayName("删除入库单测试 (Delete Inbound)")
    class DeleteInboundTests {

        @Test
        @DisplayName("应成功删除 - when inbound is in pending status")
        void shouldDeleteSuccessfully_whenInboundIsPending() {
            // Arrange
            testInbound.setStatus(0); // 待审核
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);
            when(inboundMapper.deleteById(1L)).thenReturn(1);

            // Act
            boolean result = inboundService.delete(1L);

            // Assert
            assertTrue(result);
            verify(inboundMapper, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("应抛出异常 - when inbound is already approved")
        void shouldThrowException_whenInboundIsApproved() {
            // Arrange
            testInbound.setStatus(1); // 已审核
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.delete(1L));

            assertTrue(exception.getMessage().contains("已审核") ||
                        exception.getMessage().contains("无法删除"));
            verify(inboundMapper, never()).deleteById(any());
        }

        @Test
        @DisplayName("应抛出异常 - when inbound is voided")
        void shouldThrowException_whenInboundIsVoided() {
            // Arrange
            testInbound.setStatus(2); // 已作废
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.delete(1L));

            assertTrue(exception.getMessage().contains("已作废") ||
                        exception.getMessage().contains("无法删除"));
        }

        @Test
        @DisplayName("应抛出异常 - when inbound not found")
        void shouldThrowException_whenInboundNotFound() {
            // Arrange
            when(inboundMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.delete(999L));

            assertTrue(exception.getMessage().contains("入库单不存在"));
        }
    }

    @Nested
    @DisplayName("作废入库单测试 (Void Inbound)")
    class VoidInboundTests {

        @Test
        @DisplayName("应成功作废 - when inbound is in pending status")
        void shouldVoidSuccessfully_whenInboundIsPending() {
            // Arrange
            testInbound.setStatus(0); // 待审核
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);
            when(inboundMapper.updateById(any(Inbound.class))).thenReturn(1);

            // Act
            boolean result = inboundService.void(1L);

            // Assert
            assertTrue(result);
            assertEquals(2, testInbound.getStatus(), "状态应更新为已作废");
            verify(inboundMapper, times(1)).updateById(any(Inbound.class));
        }

        @Test
        @DisplayName("应抛出异常 - when inbound is already approved")
        void shouldThrowException_whenInboundIsApproved() {
            // Arrange
            testInbound.setStatus(1); // 已审核
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.void(1L));

            assertTrue(exception.getMessage().contains("已审核") ||
                        exception.getMessage().contains("无法作废"));
            verify(inboundMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("应抛出异常 - when inbound is already voided")
        void shouldThrowException_whenInboundIsAlreadyVoided() {
            // Arrange
            testInbound.setStatus(2); // 已作废
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class,
                () -> inboundService.void(1L));

            assertTrue(exception.getMessage().contains("已作废") ||
                        exception.getMessage().contains("无法作废"));
        }
    }

    @Nested
    @DisplayName("查询入库单测试 (Query Inbound)")
    class QueryInboundTests {

        @Test
        @DisplayName("应返回入库单详情 - when inbound exists")
        void shouldReturnInbound_whenInboundExists() {
            // Arrange
            when(inboundMapper.selectById(1L)).thenReturn(testInbound);
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act
            Inbound result = inboundService.getById(1L);

            // Assert
            assertNotNull(result);
            assertEquals("IN202601040001", result.getInboundNo());
            assertEquals(1L, result.getProductId());
            assertEquals(50, result.getQuantity());
        }

        @Test
        @DisplayName("应返回null - when inbound does not exist")
        void shouldReturnNull_whenInboundNotExists() {
            // Arrange
            when(inboundMapper.selectById(999L)).thenReturn(null);

            // Act
            Inbound result = inboundService.getById(999L);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("应返回分页列表 - when querying with pagination")
        void shouldReturnPagedInbounds_whenQueryingWithPagination() {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));
            page.setTotal(1);

            when(inboundMapper.selectPage(any(), any())).thenReturn(page);

            // Act
            Page<Inbound> result = inboundService.page(1, 10, null, null, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("应按商品ID筛选 - when productId filter provided")
        void shouldFilterByProductId_whenProductIdFilterProvided() {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));

            when(inboundMapper.selectPage(any(), any())).thenReturn(page);

            // Act
            Page<Inbound> result = inboundService.page(1, 10, 1L, null, null, null);

            // Assert
            assertNotNull(result);
            verify(inboundMapper, times(1)).selectPage(any(), any());
        }

        @Test
        @DisplayName("应按状态筛选 - when status filter provided")
        void shouldFilterByStatus_whenStatusFilterProvided() {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));

            when(inboundMapper.selectPage(any(), any())).thenReturn(page);

            // Act
            Page<Inbound> result = inboundService.page(1, 10, null, 0, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(0, result.getRecords().get(0).getStatus());
        }

        @Test
        @DisplayName("应按日期范围筛选 - when date range provided")
        void shouldFilterByDateRange_whenDateRangeProvided() {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));

            when(inboundMapper.selectPage(any(), any())).thenReturn(page);

            LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2026, 1, 31, 23, 59);

            // Act
            Page<Inbound> result = inboundService.page(1, 10, null, null, startDate, endDate);

            // Assert
            assertNotNull(result);
            verify(inboundMapper, times(1)).selectPage(any(), any());
        }

        @Test
        @DisplayName("应返回空列表 - when no inbounds match filter")
        void shouldReturnEmptyList_whenNoInboundsMatch() {
            // Arrange
            Page<Inbound> emptyPage = new Page<>(1, 10);
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);

            when(inboundMapper.selectPage(any(), any())).thenReturn(emptyPage);

            // Act
            Page<Inbound> result = inboundService.page(1, 10, 999L, null, null, null);

            // Assert
            assertNotNull(result);
            assertTrue(result.getRecords().isEmpty());
            assertEquals(0, result.getTotal());
        }

        @Test
        @DisplayName("应使用默认分页参数 - when page and size not provided")
        void shouldUseDefaultPagination_whenPageAndSizeNotProvided() {
            // Arrange
            Page<Inbound> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testInbound));

            when(inboundMapper.selectPage(any(), any())).thenReturn(page);

            // Act
            Page<Inbound> result = inboundService.page(0, 0, null, null, null, null);

            // Assert
            assertNotNull(result);
        }
    }
}
