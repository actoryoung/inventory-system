package com.inventory.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inventory.dto.ProductDTO;
import com.inventory.entity.Category;
import com.inventory.entity.Inventory;
import com.inventory.entity.Product;
import com.inventory.event.ProductCreatedEvent;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.impl.ProductServiceImpl;
import com.inventory.vo.ProductVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;

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
 * 商品服务单元测试（重写，对齐当前 ProductService API）
 *
 * 覆盖 create/update/delete/toggleStatus/checkSkuExists/canDelete/getById/page/search。
 *
 * @author inventory-system
 * @since 2026-08-06
 */
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryService categoryService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        productService = new ProductServiceImpl(categoryService, inventoryService, eventPublisher);
        // ServiceImpl 的 baseMapper 需要显式注入
        ReflectionTestUtils.setField(productService, "baseMapper", productMapper);
        // removeById 依赖 MyBatis-Plus TableInfo
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Product.class);
    }

    private ProductDTO validDto() {
        ProductDTO dto = new ProductDTO();
        dto.setSku("SKU-NEW");
        dto.setName("测试商品");
        dto.setCategoryId(1L);
        dto.setUnit("台");
        dto.setPrice(new BigDecimal("99.00"));
        dto.setCostPrice(new BigDecimal("50.00"));
        dto.setWarningStock(10);
        dto.setStatus(1);
        return dto;
    }

    private Category enabledCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("电子产品");
        category.setStatus(1);
        return category;
    }

    private Product productWithId(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setSku("SKU001");
        product.setName("iPhone 15 Pro");
        product.setCategoryId(1L);
        product.setPrice(new BigDecimal("7999.00"));
        product.setWarningStock(10);
        product.setStatus(1);
        return product;
    }

    @Test
    @DisplayName("create 成功返回商品ID并发布创建事件")
    void createSucceeds() {
        when(productMapper.selectCount(any())).thenReturn(0L);
        when(categoryService.getById(any(Serializable.class))).thenReturn(enabledCategory());
        doAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return 1;
        }).when(productMapper).insert(any(Product.class));

        Long id = productService.create(validDto());

        assertEquals(1L, id);
        verify(eventPublisher).publishEvent(any(ProductCreatedEvent.class));
    }

    @Test
    @DisplayName("create SKU 重复抛异常")
    void createThrowsWhenSkuExists() {
        when(productMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.create(validDto()));

        assertTrue(ex.getMessage().contains("商品编码已存在"));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("create 分类不存在抛异常")
    void createThrowsWhenCategoryMissing() {
        when(productMapper.selectCount(any())).thenReturn(0L);
        when(categoryService.getById(any(Serializable.class))).thenReturn(null);

        assertThrows(BusinessException.class, () -> productService.create(validDto()));
    }

    @Test
    @DisplayName("create 分类被禁用抛异常")
    void createThrowsWhenCategoryDisabled() {
        when(productMapper.selectCount(any())).thenReturn(0L);
        Category disabled = enabledCategory();
        disabled.setStatus(0);
        when(categoryService.getById(any(Serializable.class))).thenReturn(disabled);

        assertThrows(BusinessException.class, () -> productService.create(validDto()));
    }

    @Test
    @DisplayName("create 销售价格为负抛异常")
    void createThrowsWhenPriceNegative() {
        when(productMapper.selectCount(any())).thenReturn(0L);
        when(categoryService.getById(any(Serializable.class))).thenReturn(enabledCategory());
        ProductDTO dto = validDto();
        dto.setPrice(new BigDecimal("-1"));

        assertThrows(BusinessException.class, () -> productService.create(dto));
    }

    @Test
    @DisplayName("update 缺少 ID 抛异常")
    void updateThrowsWhenIdMissing() {
        ProductDTO dto = validDto();
        dto.setId(null);

        assertThrows(BusinessException.class, () -> productService.update(dto));
    }

    @Test
    @DisplayName("update 商品不存在抛异常")
    void updateThrowsWhenProductMissing() {
        when(productMapper.selectById(1L)).thenReturn(null);
        ProductDTO dto = validDto();
        dto.setId(1L);

        assertThrows(BusinessException.class, () -> productService.update(dto));
    }

    @Test
    @DisplayName("update SKU 重复抛异常")
    void updateThrowsWhenSkuExists() {
        when(productMapper.selectById(1L)).thenReturn(productWithId(1L));
        when(productMapper.selectCount(any())).thenReturn(1L);
        ProductDTO dto = validDto();
        dto.setId(1L);

        assertThrows(BusinessException.class, () -> productService.update(dto));
    }

    @Test
    @DisplayName("update 修改分类到不存在的分类抛异常")
    void updateThrowsWhenNewCategoryMissing() {
        Product exist = productWithId(1L);
        exist.setCategoryId(2L);
        when(productMapper.selectById(1L)).thenReturn(exist);
        when(productMapper.selectCount(any())).thenReturn(0L);
        when(categoryService.getById(any(Serializable.class))).thenReturn(null);
        ProductDTO dto = validDto();
        dto.setId(1L);
        dto.setCategoryId(1L);

        assertThrows(BusinessException.class, () -> productService.update(dto));
    }

    @Test
    @DisplayName("update 成功返回 true")
    void updateSucceeds() {
        when(productMapper.selectById(1L)).thenReturn(productWithId(1L));
        when(productMapper.selectCount(any())).thenReturn(0L);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        ProductDTO dto = validDto();
        dto.setId(1L);
        dto.setCategoryId(1L);

        assertTrue(productService.update(dto));
    }

    @Test
    @DisplayName("delete 缺少 ID 抛异常")
    void deleteThrowsWhenIdMissing() {
        assertThrows(BusinessException.class, () -> productService.delete(null));
    }

    @Test
    @DisplayName("delete 商品不存在抛异常")
    void deleteThrowsWhenProductMissing() {
        when(productMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> productService.delete(1L));
    }

    @Test
    @DisplayName("delete 存在关联库存记录时抛异常")
    void deleteThrowsWhenHasRecords() {
        when(productMapper.selectById(1L)).thenReturn(productWithId(1L));
        when(productMapper.countInventoryRecords(1L)).thenReturn(1);

        assertThrows(BusinessException.class, () -> productService.delete(1L));
        verify(productMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("delete 成功物理删除")
    void deleteSucceeds() {
        when(productMapper.selectById(1L)).thenReturn(productWithId(1L));
        when(productMapper.countInventoryRecords(1L)).thenReturn(0);
        when(productMapper.countInboundRecords(1L)).thenReturn(0);
        when(productMapper.countOutboundRecords(1L)).thenReturn(0);
        when(productMapper.deleteById(1L)).thenReturn(1);

        assertTrue(productService.delete(1L));
        verify(productMapper).deleteById(1L);
    }

    @Test
    @DisplayName("toggleStatus 无效状态抛异常")
    void toggleStatusInvalidStatusThrows() {
        assertThrows(BusinessException.class, () -> productService.toggleStatus(1L, 5));
    }

    @Test
    @DisplayName("toggleStatus 商品不存在抛异常")
    void toggleStatusProductMissingThrows() {
        when(productMapper.selectById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> productService.toggleStatus(1L, 0));
    }

    @Test
    @DisplayName("toggleStatus 成功返回 true")
    void toggleStatusSucceeds() {
        when(productMapper.selectById(1L)).thenReturn(productWithId(1L));
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        assertTrue(productService.toggleStatus(1L, 0));
    }

    @Test
    @DisplayName("checkSkuExists 空 SKU 返回 false")
    void checkSkuExistsBlankReturnsFalse() {
        assertFalse(productService.checkSkuExists("", null));
        assertFalse(productService.checkSkuExists(null, null));
    }

    @Test
    @DisplayName("checkSkuExists 命中返回 true")
    void checkSkuExistsHitReturnsTrue() {
        when(productMapper.selectCount(any())).thenReturn(1L);

        assertTrue(productService.checkSkuExists("SKU001", null));
    }

    @Test
    @DisplayName("canDelete 有库存记录返回 false")
    void canDeleteFalseWhenHasInventory() {
        when(productMapper.countInventoryRecords(1L)).thenReturn(1);

        assertFalse(productService.canDelete(1L));
    }

    @Test
    @DisplayName("canDelete 无关联记录返回 true")
    void canDeleteTrueWhenNoRecords() {
        when(productMapper.countInventoryRecords(1L)).thenReturn(0);
        when(productMapper.countInboundRecords(1L)).thenReturn(0);
        when(productMapper.countOutboundRecords(1L)).thenReturn(0);

        assertTrue(productService.canDelete(1L));
    }

    @Test
    @DisplayName("getById 返回带分类名与库存的 VO")
    void getByIdReturnsEnrichedVO() {
        when(productMapper.selectById(1L)).thenReturn(productWithId(1L));
        when(categoryService.getById(any(Serializable.class))).thenReturn(enabledCategory());
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setQuantity(5);
        when(inventoryService.getByProductId(1L)).thenReturn(inventory);

        ProductVO vo = productService.getById(1L);

        assertEquals("电子产品", vo.getCategoryName());
        assertEquals(5, vo.getStockQuantity());
    }

    @Test
    @DisplayName("getById 不存在返回 null")
    void getByIdMissingReturnsNull() {
        when(productMapper.selectById(99L)).thenReturn(null);

        assertNull(productService.getById(99L));
    }

    @Test
    @DisplayName("page 分页返回并填充分类名与库存")
    void pageReturnsEnrichedRecords() {
        Product product = productWithId(1L);
        Page<Product> productPage = new Page<>(1, 10, 1);
        productPage.setRecords(Collections.singletonList(product));
        when(productMapper.selectPage(any(), any())).thenReturn(productPage);
        when(categoryService.listByIds(any())).thenReturn(Collections.singletonList(enabledCategory()));
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setQuantity(7);
        when(inventoryService.listByProductIds(any())).thenReturn(Collections.singletonList(inventory));

        IPage<ProductVO> result = productService.page(null, null, null, null, 1, 10);

        assertEquals(1, result.getRecords().size());
        assertEquals("电子产品", result.getRecords().get(0).getCategoryName());
        assertEquals(7, result.getRecords().get(0).getStockQuantity());
    }

    @Test
    @DisplayName("search 空关键词返回空列表")
    void searchBlankKeywordReturnsEmpty() {
        assertTrue(productService.search("").isEmpty());
        assertTrue(productService.search(null).isEmpty());
    }

    @Test
    @DisplayName("search 关键词命中 SKU/名称")
    void searchKeywordHits() {
        Product product = productWithId(1L);
        when(productMapper.selectList(any())).thenReturn(Collections.singletonList(product));
        when(categoryService.listByIds(any())).thenReturn(Collections.singletonList(enabledCategory()));
        Inventory inventory = new Inventory();
        inventory.setProductId(1L);
        inventory.setQuantity(3);
        when(inventoryService.listByProductIds(any())).thenReturn(Collections.singletonList(inventory));

        assertEquals(1, productService.search("iPhone").size());
    }
}
