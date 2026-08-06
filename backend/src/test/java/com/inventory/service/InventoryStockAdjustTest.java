package com.inventory.service;

import com.inventory.dto.InventoryAdjustDTO;
import com.inventory.entity.Inventory;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.CategoryMapper;
import com.inventory.mapper.InventoryMapper;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 库存原子增减与负数校验单元测试
 *
 * 覆盖 P1-1（原子 SQL 增减、防超卖）与 P1-3（调整后库存不能为负）。
 *
 * @author inventory-system
 * @since 2026-08-06
 */
@DisplayName("库存原子增减测试 (InventoryStockAdjust)")
class InventoryStockAdjustTest {

    private InventoryMapper inventoryMapper;
    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryMapper = mock(InventoryMapper.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        CategoryMapper categoryMapper = mock(CategoryMapper.class);
        inventoryService = new InventoryServiceImpl(productMapper, categoryMapper);
        // ServiceImpl 的 baseMapper 需要显式注入
        ReflectionTestUtils.setField(inventoryService, "baseMapper", inventoryMapper);
    }

    private Inventory inventoryWithQuantity(int quantity) {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setProductId(1L);
        inv.setQuantity(quantity);
        return inv;
    }

    @Test
    @DisplayName("addStock 使用原子 incrementStock，不再 updateById")
    void addStockUsesAtomicIncrement() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(100));
        when(inventoryMapper.incrementStock(1L, 50)).thenReturn(1);

        inventoryService.addStock(1L, 50);

        verify(inventoryMapper).incrementStock(1L, 50);
        verify(inventoryMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("reduceStock 使用原子 decrementStock 成功扣减")
    void reduceStockUsesAtomicDecrement() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(100));
        when(inventoryMapper.decrementStock(1L, 30)).thenReturn(1);

        inventoryService.reduceStock(1L, 30);

        verify(inventoryMapper).decrementStock(1L, 30);
    }

    @Test
    @DisplayName("reduceStock 库存不足时抛异常（原子扣减返回 0）")
    void reduceStockThrowsWhenInsufficient() {
        when(inventoryMapper.selectByProductId(1L, 1L)).thenReturn(inventoryWithQuantity(50));
        when(inventoryMapper.decrementStock(1L, 100)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> inventoryService.reduceStock(1L, 100));

        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("库存不足"));
    }

    @Test
    @DisplayName("adjustInventory type=add 数量为负导致结果为负时抛异常")
    void adjustInventoryAddNegativeResultThrows() {
        when(inventoryMapper.selectById(1L)).thenReturn(inventoryWithQuantity(100));

        InventoryAdjustDTO dto = new InventoryAdjustDTO();
        dto.setType("add");
        dto.setQuantity(-9999);
        dto.setReason("test");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> inventoryService.adjustInventory(1L, dto));

        org.junit.jupiter.api.Assertions.assertTrue(ex.getMessage().contains("不能为负数"));
        verify(inventoryMapper, never()).incrementStock(any(), any());
    }

    @Test
    @DisplayName("adjustInventory type=reduce 库存不足时抛异常")
    void adjustInventoryReduceInsufficientThrows() {
        when(inventoryMapper.selectById(1L)).thenReturn(inventoryWithQuantity(50));
        when(inventoryMapper.decrementStock(1L, 100)).thenReturn(0);

        InventoryAdjustDTO dto = new InventoryAdjustDTO();
        dto.setType("reduce");
        dto.setQuantity(100);
        dto.setReason("test");

        assertThrows(BusinessException.class,
                () -> inventoryService.adjustInventory(1L, dto));
    }

    @Test
    @DisplayName("adjustInventory type=set 负数时抛异常")
    void adjustInventorySetNegativeThrows() {
        when(inventoryMapper.selectById(1L)).thenReturn(inventoryWithQuantity(50));

        InventoryAdjustDTO dto = new InventoryAdjustDTO();
        dto.setType("set");
        dto.setQuantity(-1);
        dto.setReason("test");

        assertThrows(BusinessException.class,
                () -> inventoryService.adjustInventory(1L, dto));
    }

    @Test
    @DisplayName("adjustInventory type=add 返回正确的 newQuantity")
    void adjustInventoryAddReturnsNewQuantity() {
        when(inventoryMapper.selectById(1L)).thenReturn(inventoryWithQuantity(100));
        when(inventoryMapper.incrementStock(1L, 10)).thenReturn(1);

        InventoryAdjustDTO dto = new InventoryAdjustDTO();
        dto.setType("add");
        dto.setQuantity(10);
        dto.setReason("test");

        java.util.Map<String, Object> result = inventoryService.adjustInventory(1L, dto);

        assertEquals(100, result.get("oldQuantity"));
        assertEquals(110, result.get("newQuantity"));
    }
}
