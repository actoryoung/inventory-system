package com.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inventory.dto.InventoryAdjustDTO;
import com.inventory.entity.Category;
import com.inventory.entity.Inventory;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.CategoryMapper;
import com.inventory.mapper.InventoryMapper;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.impl.InventoryServiceImpl;
import com.inventory.vo.InventoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存服务单元测试（重写，对齐当前 InventoryService API）
 *
 * 覆盖 initInventory/addStock/reduceStock/adjustStock/getByProductId/checkStock/
 * page/getLowStockList/getSummary/listByProductIds。
 *
 * @author inventory-system
 * @since 2026-08-06
 */
@DisplayName("InventoryService")
class InventoryServiceTest {

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryMapper categoryMapper;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inventoryService = new InventoryServiceImpl(productMapper, categoryMapper);
        // ServiceImpl 的 baseMapper 需要显式注入
        ReflectionTestUtils.setField(inventoryService, "baseMapper", inventoryMapper);
    }

    private Inventory inventoryWithQuantity(int quantity) {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setProductId(1L);
        inv.setQuantity(quantity);
        inv.setWarningStock(10);
        return inv;
    }

    private Product productWithWarningStock(int warningStock) {
        Product product = new Product();
        product.setId(1L);
        product.setSku("SKU001");
        product.setName("iPhone 15 Pro");
        product.setCategoryId(1L);
        product.setPrice(new BigDecimal("100.00"));
        product.setWarningStock(warningStock);
        return product;
    }

    @Test
    @DisplayName("initInventory 成功初始化并冗余预警值")
    void initInventorySucceeds() {
        when(inventoryMapper.countByProductAndWarehouse(1L, 1L)).thenReturn(0);
        when(productMapper.selectById(1L)).thenReturn(productWithWarningStock(10));
        doAnswer(inv -> {
            Inventory inventory = inv.getArgument(0);
            inventory.setId(1L);
            return 1;
        }).when(inventoryMapper).insert(any(Inventory.class));

        inventoryService.initInventory(1L, 5);

        verify(inventoryMapper).insert(any(Inventory.class));
        verify(productMapper).selectById(1L);
    }

    @Test
    @DisplayName("initInventory 库存已存在抛异常")
    void initInventoryThrowsWhenExists() {
        when(inventoryMapper.countByProductAndWarehouse(1L, 1L)).thenReturn(1);

        assertThrows(BusinessException.class, () -> inventoryService.initInventory(1L, 5));
        verify(inventoryMapper, never()).insert(any());
    }

    @Test
    @DisplayName("addStock 数量为负抛异常")
    void addStockNegativeThrows() {
        assertThrows(BusinessException.class, () -> inventoryService.addStock(1L, -1));
    }

    @Test
    @DisplayName("addStock 库存记录不存在抛异常")
    void addStockMissingInventoryThrows() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> inventoryService.addStock(1L, 10));
    }

    @Test
    @DisplayName("addStock 成功原子增加")
    void addStockSucceeds() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(100));
        when(inventoryMapper.incrementStock(1L, 50)).thenReturn(1);

        inventoryService.addStock(1L, 50);

        verify(inventoryMapper).incrementStock(1L, 50);
    }

    @Test
    @DisplayName("reduceStock 库存不足抛异常")
    void reduceStockInsufficientThrows() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(50));
        when(inventoryMapper.decrementStock(1L, 100)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> inventoryService.reduceStock(1L, 100));

        assertTrue(ex.getMessage().contains("库存不足"));
    }

    @Test
    @DisplayName("reduceStock 成功原子扣减")
    void reduceStockSucceeds() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(100));
        when(inventoryMapper.decrementStock(1L, 30)).thenReturn(1);

        inventoryService.reduceStock(1L, 30);

        verify(inventoryMapper).decrementStock(1L, 30);
    }

    @Test
    @DisplayName("adjustStock 数量为负抛异常")
    void adjustStockNegativeThrows() {
        assertThrows(BusinessException.class, () -> inventoryService.adjustStock(1L, -1, "test"));
    }

    @Test
    @DisplayName("adjustStock 成功设置目标值")
    void adjustStockSucceeds() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(100));
        when(inventoryMapper.setStock(1L, 200)).thenReturn(1);

        inventoryService.adjustStock(1L, 200, "盘点");

        verify(inventoryMapper).setStock(1L, 200);
    }

    @Test
    @DisplayName("adjustInventory 无效类型抛异常")
    void adjustInventoryInvalidTypeThrows() {
        when(inventoryMapper.selectById(1L)).thenReturn(inventoryWithQuantity(100));
        InventoryAdjustDTO dto = new InventoryAdjustDTO();
        dto.setType("unknown");
        dto.setQuantity(10);

        assertThrows(BusinessException.class, () -> inventoryService.adjustInventory(1L, dto));
    }

    @Test
    @DisplayName("getByProductId 返回对应库存")
    void getByProductIdReturnsInventory() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(100));

        Inventory result = inventoryService.getByProductId(1L);

        assertEquals(100, result.getQuantity());
    }

    @Test
    @DisplayName("getByProductId 无记录返回 null")
    void getByProductIdMissingReturnsNull() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(null);

        assertNull(inventoryService.getByProductId(1L));
    }

    @Test
    @DisplayName("checkStock 库存充足返回 true")
    void checkStockSufficientReturnsTrue() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(100));

        assertTrue(inventoryService.checkStock(1L, 50));
    }

    @Test
    @DisplayName("checkStock 库存不足返回 false")
    void checkStockInsufficientReturnsFalse() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(10));

        assertFalse(inventoryService.checkStock(1L, 50));
    }

    @Test
    @DisplayName("checkStock 无库存记录返回 false")
    void checkStockMissingReturnsFalse() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(null);

        assertFalse(inventoryService.checkStock(1L, 1));
    }

    @Test
    @DisplayName("page 返回填充商品/分类/金额的 VO")
    void pageReturnsEnrichedRecords() {
        Inventory inventory = inventoryWithQuantity(5);
        Page<Inventory> inventoryPage = new Page<>(1, 10, 1);
        inventoryPage.setRecords(Collections.singletonList(inventory));
        when(inventoryMapper.selectInventoryPage(any(), any(), any(), any())).thenReturn(inventoryPage);
        when(productMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(productWithWarningStock(10)));
        Category category = new Category();
        category.setId(1L);
        category.setName("电子产品");
        when(categoryMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(category));

        IPage<InventoryVO> result = inventoryService.page(null, null, null, 1, 10);

        assertEquals(1, result.getRecords().size());
        InventoryVO vo = result.getRecords().get(0);
        assertEquals("SKU001", vo.getProductSku());
        assertEquals("iPhone 15 Pro", vo.getProductName());
        assertEquals("电子产品", vo.getCategoryName());
        assertEquals(new BigDecimal("500.00"), vo.getAmount());
        assertEquals(10, vo.getWarningStock());
    }

    @Test
    @DisplayName("getLowStockList 标记低库存并冗余预警值")
    void getLowStockListMarksLowStock() {
        when(inventoryMapper.selectLowStockInventories())
                .thenReturn(Collections.singletonList(inventoryWithQuantity(2)));
        when(productMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(productWithWarningStock(10)));
        Category category = new Category();
        category.setId(1L);
        category.setName("电子产品");
        when(categoryMapper.selectBatchIds(any())).thenReturn(Collections.singletonList(category));

        List<InventoryVO> result = inventoryService.getLowStockList();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsLowStock());
        assertEquals(10, result.get(0).getWarningStock());
    }

    @Test
    @DisplayName("getSummary 使用聚合 SQL 返回值")
    void getSummaryReturnsAggregates() {
        when(productMapper.selectCount(any())).thenReturn(5L);
        when(inventoryMapper.sumQuantity()).thenReturn(100L);
        when(inventoryMapper.countLowStock()).thenReturn(3L);
        when(inventoryMapper.sumAmount()).thenReturn(new BigDecimal("500.00"));

        Map<String, Object> summary = inventoryService.getSummary();

        assertEquals(5L, summary.get("totalProducts"));
        assertEquals(100L, summary.get("totalQuantity"));
        assertEquals(3L, summary.get("lowStockCount"));
        assertEquals(new BigDecimal("500.00"), summary.get("totalAmount"));
    }

    @Test
    @DisplayName("listByProductIds 空集合返回空列表")
    void listByProductIdsEmptyReturnsEmpty() {
        assertTrue(inventoryService.listByProductIds(Collections.emptyList()).isEmpty());
        assertTrue(inventoryService.listByProductIds(null).isEmpty());
    }

    @Test
    @DisplayName("listByProductIds 批量查询返回库存")
    void listByProductIdsReturnsInventories() {
        when(inventoryMapper.selectByProductIds(any())).thenReturn(Collections.singletonList(inventoryWithQuantity(5)));

        List<Inventory> result = inventoryService.listByProductIds(Collections.singletonList(1L));

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getQuantity());
    }
}
