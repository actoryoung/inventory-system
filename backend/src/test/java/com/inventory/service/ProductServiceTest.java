package com.inventory.service;

import com.inventory.entity.Product;
import com.inventory.entity.Category;
import com.inventory.mapper.ProductMapper;
import com.inventory.mapper.CategoryMapper;
import com.inventory.service.impl.ProductServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 商品服务层单元测试
 *
 * 测试覆盖：
 * - 正常场景：创建、更新、删除、查询商品
 * - 异常场景：SKU重复、价格负数、分类不存在、删除约束
 * - 边界条件：空值、零值、最大值
 */
@DisplayName("商品服务层测试 (ProductServiceTest)")
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 准备测试分类数据
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("电子产品");
        testCategory.setStatus(1);

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
        testProduct.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("创建商品测试 (Create Product)")
    class CreateProductTests {

        @Test
        @DisplayName("应成功创建商品并初始化库存 - given valid product data")
        void shouldCreateProductSuccessfully_whenValidDataProvided() {
            // Arrange - 准备测试数据
            Product newProduct = new Product();
            newProduct.setSku("SKU002");
            newProduct.setName("MacBook Pro");
            newProduct.setCategoryId(1L);
            newProduct.setPrice(new BigDecimal("12999.00"));
            newProduct.setWarningStock(5);
            newProduct.setStatus(1);

            when(categoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productMapper.selectCount(any())).thenReturn(0L);
            when(productMapper.insert(any(Product.class))).thenReturn(1);

            // Act - 执行创建操作
            boolean result = productService.createProduct(newProduct);

            // Assert - 验证结果
            assertTrue(result, "创建商品应该成功");
            verify(inventoryService, times(1)).initializeInventory(any(Product.class));
            verify(productMapper, times(1)).insert(any(Product.class));
        }

        @Test
        @DisplayName("应抛出异常 - when SKU is duplicate")
        void shouldThrowException_whenSkuIsDuplicate() {
            // Arrange
            Product duplicateProduct = new Product();
            duplicateProduct.setSku("SKU001"); // 重复的SKU
            duplicateProduct.setName("测试商品");
            duplicateProduct.setCategoryId(1L);
            duplicateProduct.setPrice(new BigDecimal("100.00"));

            when(productMapper.selectCount(any())).thenReturn(1L);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.createProduct(duplicateProduct));

            assertTrue(exception.getMessage().contains("SKU已存在"));
            verify(productMapper, never()).insert(any());
        }

        @Test
        @DisplayName("应抛出异常 - when SKU is null or empty")
        void shouldThrowException_whenSkuIsNull() {
            // Arrange
            Product invalidProduct = new Product();
            invalidProduct.setSku(null);
            invalidProduct.setName("测试商品");
            invalidProduct.setCategoryId(1L);
            invalidProduct.setPrice(new BigDecimal("100.00"));

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(invalidProduct));

            assertTrue(exception.getMessage().contains("SKU不能为空"));
        }

        @Test
        @DisplayName("应抛出异常 - when price is negative")
        void shouldThrowException_whenPriceIsNegative() {
            // Arrange
            Product invalidProduct = new Product();
            invalidProduct.setSku("SKU999");
            invalidProduct.setName("测试商品");
            invalidProduct.setCategoryId(1L);
            invalidProduct.setPrice(new BigDecimal("-100.00"));

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(invalidProduct));

            assertTrue(exception.getMessage().contains("价格不能为负数"));
        }

        @Test
        @DisplayName("应抛出异常 - when cost price is negative")
        void shouldThrowException_whenCostPriceIsNegative() {
            // Arrange
            Product invalidProduct = new Product();
            invalidProduct.setSku("SKU999");
            invalidProduct.setName("测试商品");
            invalidProduct.setCategoryId(1L);
            invalidProduct.setPrice(new BigDecimal("100.00"));
            invalidProduct.setCostPrice(new BigDecimal("-50.00"));

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(invalidProduct));

            assertTrue(exception.getMessage().contains("成本价不能为负数"));
        }

        @Test
        @DisplayName("应抛出异常 - when category does not exist")
        void shouldThrowException_whenCategoryNotExists() {
            // Arrange
            Product invalidProduct = new Product();
            invalidProduct.setSku("SKU999");
            invalidProduct.setName("测试商品");
            invalidProduct.setCategoryId(999L);
            invalidProduct.setPrice(new BigDecimal("100.00"));

            when(categoryMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.createProduct(invalidProduct));

            assertTrue(exception.getMessage().contains("分类不存在"));
        }

        @Test
        @DisplayName("应抛出异常 - when category is disabled")
        void shouldThrowException_whenCategoryIsDisabled() {
            // Arrange
            Category disabledCategory = new Category();
            disabledCategory.setId(2L);
            disabledCategory.setName("已禁用分类");
            disabledCategory.setStatus(0);

            Product invalidProduct = new Product();
            invalidProduct.setSku("SKU999");
            invalidProduct.setName("测试商品");
            invalidProduct.setCategoryId(2L);
            invalidProduct.setPrice(new BigDecimal("100.00"));

            when(categoryMapper.selectById(2L)).thenReturn(disabledCategory);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.createProduct(invalidProduct));

            assertTrue(exception.getMessage().contains("分类已禁用"));
        }

        @Test
        @DisplayName("应抛出异常 - when warning stock is negative")
        void shouldThrowException_whenWarningStockIsNegative() {
            // Arrange
            Product invalidProduct = new Product();
            invalidProduct.setSku("SKU999");
            invalidProduct.setName("测试商品");
            invalidProduct.setCategoryId(1L);
            invalidProduct.setPrice(new BigDecimal("100.00"));
            invalidProduct.setWarningStock(-1);

            when(categoryMapper.selectById(1L)).thenReturn(testCategory);

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(invalidProduct));

            assertTrue(exception.getMessage().contains("预警库存不能为负数"));
        }

        @Test
        @DisplayName("应成功创建 - when warning stock is zero")
        void shouldCreateProduct_whenWarningStockIsZero() {
            // Arrange
            Product newProduct = new Product();
            newProduct.setSku("SKU002");
            newProduct.setName("测试商品");
            newProduct.setCategoryId(1L);
            newProduct.setPrice(new BigDecimal("100.00"));
            newProduct.setWarningStock(0);

            when(categoryMapper.selectById(1L)).thenReturn(testCategory);
            when(productMapper.selectCount(any())).thenReturn(0L);
            when(productMapper.insert(any(Product.class))).thenReturn(1);

            // Act
            boolean result = productService.createProduct(newProduct);

            // Assert
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("更新商品测试 (Update Product)")
    class UpdateProductTests {

        @Test
        @DisplayName("应成功更新商品信息 - when valid data provided")
        void shouldUpdateProductSuccessfully_whenValidDataProvided() {
            // Arrange
            Product updateData = new Product();
            updateData.setId(1L);
            updateData.setSku("SKU001");
            updateData.setName("iPhone 15 Pro");
            updateData.setPrice(new BigDecimal("6999.00"));

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(productMapper.selectCount(any())).thenReturn(0L);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            // Act
            boolean result = productService.updateProduct(updateData);

            // Assert
            assertTrue(result);
            verify(productMapper, times(1)).updateById(any(Product.class));
        }

        @Test
        @DisplayName("应抛出异常 - when updating with duplicate SKU")
        void shouldThrowException_whenUpdatingWithDuplicateSku() {
            // Arrange
            Product anotherProduct = new Product();
            anotherProduct.setId(2L);
            anotherProduct.setSku("SKU002");

            Product updateData = new Product();
            updateData.setId(1L);
            updateData.setSku("SKU002"); // 尝试更新为另一个商品的SKU

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(productMapper.selectCount(any())).thenReturn(1L);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.updateProduct(updateData));

            assertTrue(exception.getMessage().contains("SKU已存在"));
        }

        @Test
        @DisplayName("应允许更新自己的SKU - when SKU belongs to same product")
        void shouldAllowUpdate_whenSkuBelongsToSameProduct() {
            // Arrange
            Product updateData = new Product();
            updateData.setId(1L);
            updateData.setSku("SKU001"); // 保持自己的SKU
            updateData.setName("iPhone 15 Pro");

            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            // Act
            boolean result = productService.updateProduct(updateData);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("应抛出异常 - when product not found")
        void shouldThrowException_whenProductNotFound() {
            // Arrange
            Product updateData = new Product();
            updateData.setId(999L);

            when(productMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.updateProduct(updateData));

            assertTrue(exception.getMessage().contains("商品不存在"));
        }

        @Test
        @DisplayName("应抛出异常 - when updating price to negative")
        void shouldThrowException_whenUpdatingPriceToNegative() {
            // Arrange
            Product updateData = new Product();
            updateData.setId(1L);
            updateData.setPrice(new BigDecimal("-100.00"));

            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act & Assert
            Exception exception = assertThrows(IllegalArgumentException.class,
                () -> productService.updateProduct(updateData));

            assertTrue(exception.getMessage().contains("价格不能为负数"));
        }
    }

    @Nested
    @DisplayName("删除商品测试 (Delete Product)")
    class DeleteProductTests {

        @Test
        @DisplayName("应成功删除商品 - when no associations exist")
        void shouldDeleteProductSuccessfully_whenNoAssociationsExist() {
            // Arrange
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(inventoryService.hasInventoryRecords(1L)).thenReturn(false);
            when(inventoryService.hasInboundOutboundRecords(1L)).thenReturn(false);
            when(productMapper.deleteById(1L)).thenReturn(1);

            // Act
            boolean result = productService.deleteProduct(1L);

            // Assert
            assertTrue(result);
            verify(productMapper, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("应抛出异常 - when product has inventory records")
        void shouldThrowException_whenProductHasInventoryRecords() {
            // Arrange
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(inventoryService.hasInventoryRecords(1L)).thenReturn(true);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.deleteProduct(1L));

            assertTrue(exception.getMessage().contains("有库存记录"));
            verify(productMapper, never()).deleteById(any());
        }

        @Test
        @DisplayName("应抛出异常 - when product has inbound or outbound records")
        void shouldThrowException_whenProductHasInboundOutboundRecords() {
            // Arrange
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(inventoryService.hasInventoryRecords(1L)).thenReturn(false);
            when(inventoryService.hasInboundOutboundRecords(1L)).thenReturn(true);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.deleteProduct(1L));

            assertTrue(exception.getMessage().contains("有出入库记录"));
            verify(productMapper, never()).deleteById(any());
        }

        @Test
        @DisplayName("应抛出异常 - when product not found")
        void shouldThrowException_whenDeletingNonExistentProduct() {
            // Arrange
            when(productMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.deleteProduct(999L));

            assertTrue(exception.getMessage().contains("商品不存在"));
        }
    }

    @Nested
    @DisplayName("查询商品测试 (Query Product)")
    class QueryProductTests {

        @Test
        @DisplayName("应返回商品详情 - when product exists")
        void shouldReturnProduct_whenProductExists() {
            // Arrange
            when(productMapper.selectById(1L)).thenReturn(testProduct);

            // Act
            Product result = productService.getProductById(1L);

            // Assert
            assertNotNull(result);
            assertEquals("SKU001", result.getSku());
            assertEquals("iPhone 15", result.getName());
        }

        @Test
        @DisplayName("应返回null - when product does not exist")
        void shouldReturnNull_whenProductNotExists() {
            // Arrange
            when(productMapper.selectById(999L)).thenReturn(null);

            // Act
            Product result = productService.getProductById(999L);

            // Assert
            assertNull(result);
        }

        @Test
        @DisplayName("应返回分页商品列表 - when querying with pagination")
        void shouldReturnPagedProducts_whenQueryingWithPagination() {
            // Arrange
            Page<Product> page = new Page<>(1, 10);
            List<Product> products = Arrays.asList(testProduct);
            page.setRecords(products);
            page.setTotal(1);

            when(productMapper.selectPage(any(), any())).thenReturn(page);

            // Act
            Page<Product> result = productService.getProducts(1, 10, null, null, null, null);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("应按名称模糊搜索 - when name filter provided")
        void shouldSearchByName_whenNameFilterProvided() {
            // Arrange
            Page<Product> page = new Page<>(1, 10);
            List<Product> products = Arrays.asList(testProduct);
            page.setRecords(products);

            when(productMapper.selectPage(any(), any())).thenReturn(page);

            // Act
            Page<Product> result = productService.getProducts(1, 10, "iPhone", null, null, null);

            // Assert
            assertNotNull(result);
            verify(productMapper, times(1)).selectPage(any(), any());
        }

        @Test
        @DisplayName("应按SKU模糊搜索 - when SKU filter provided")
        void shouldSearchBySku_whenSkuFilterProvided() {
            // Arrange
            Page<Product> page = new Page<>(1, 10);
            List<Product> products = Arrays.asList(testProduct);
            page.setRecords(products);

            when(productMapper.selectPage(any(), any())).thenReturn(page);

            // Act
            Page<Product> result = productService.getProducts(1, 10, null, "SKU001", null, null);

            // Assert
            assertNotNull(result);
            verify(productMapper, times(1)).selectPage(any(), any());
        }

        @Test
        @DisplayName("应按分类筛选 - when category filter provided")
        void shouldFilterByCategory_whenCategoryFilterProvided() {
            // Arrange
            Page<Product> page = new Page<>(1, 10);
            List<Product> products = Arrays.asList(testProduct);
            page.setRecords(products);

            when(productMapper.selectPage(any(), any())).thenReturn(page);

            // Act
            Page<Product> result = productService.getProducts(1, 10, null, null, 1L, null);

            // Assert
            assertNotNull(result);
            verify(productMapper, times(1)).selectPage(any(), any());
        }

        @Test
        @DisplayName("应按状态筛选 - when status filter provided")
        void shouldFilterByStatus_whenStatusFilterProvided() {
            // Arrange
            Page<Product> page = new Page<>(1, 10);
            List<Product> products = Arrays.asList(testProduct);
            page.setRecords(products);

            when(productMapper.selectPage(any(), any())).thenReturn(page);

            // Act
            Page<Product> result = productService.getProducts(1, 10, null, null, null, 1);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getRecords().get(0).getStatus());
        }

        @Test
        @DisplayName("应返回空列表 - when no products match filter")
        void shouldReturnEmptyList_whenNoProductsMatch() {
            // Arrange
            Page<Product> emptyPage = new Page<>(1, 10);
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);

            when(productMapper.selectPage(any(), any())).thenReturn(emptyPage);

            // Act
            Page<Product> result = productService.getProducts(1, 10, "不存在", null, null, null);

            // Assert
            assertNotNull(result);
            assertTrue(result.getRecords().isEmpty());
            assertEquals(0, result.getTotal());
        }
    }

    @Nested
    @DisplayName("状态切换测试 (Toggle Status)")
    class ToggleStatusTests {

        @Test
        @DisplayName("应成功启用商品 - when disabling enabled product")
        void shouldEnableProduct_whenCurrentStatusIsDisabled() {
            // Arrange
            testProduct.setStatus(0);
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            // Act
            boolean result = productService.toggleStatus(1L);

            // Assert
            assertTrue(result);
            verify(productMapper, times(1)).updateById(argThat(p -> p.getStatus() == 1));
        }

        @Test
        @DisplayName("应成功禁用商品 - when enabling disabled product")
        void shouldDisableProduct_whenCurrentStatusIsEnabled() {
            // Arrange
            testProduct.setStatus(1);
            when(productMapper.selectById(1L)).thenReturn(testProduct);
            when(productMapper.updateById(any(Product.class))).thenReturn(1);

            // Act
            boolean result = productService.toggleStatus(1L);

            // Assert
            assertTrue(result);
            verify(productMapper, times(1)).updateById(argThat(p -> p.getStatus() == 0));
        }

        @Test
        @DisplayName("应抛出异常 - when toggling non-existent product")
        void shouldThrowException_whenTogglingNonExistentProduct() {
            // Arrange
            when(productMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.toggleStatus(999L));

            assertTrue(exception.getMessage().contains("商品不存在"));
        }
    }

    @Nested
    @DisplayName("SKU检查测试 (Check SKU)")
    class CheckSkuTests {

        @Test
        @DisplayName("应返回true - when SKU exists")
        void shouldReturnTrue_whenSkuExists() {
            // Arrange
            when(productMapper.selectCount(any())).thenReturn(1L);

            // Act
            boolean exists = productService.checkSkuExists("SKU001");

            // Assert
            assertTrue(exists);
        }

        @Test
        @DisplayName("应返回false - when SKU does not exist")
        void shouldReturnFalse_whenSkuNotExists() {
            // Arrange
            when(productMapper.selectCount(any())).thenReturn(0L);

            // Act
            boolean exists = productService.checkSkuExists("SKU999");

            // Assert
            assertFalse(exists);
        }

        @Test
        @DisplayName("应排除当前商品 - when checking SKU for existing product")
        void shouldExcludeCurrentProduct_whenCheckingSkuForExistingProduct() {
            // Arrange
            when(productMapper.selectCount(any())).thenReturn(0L);

            // Act
            boolean exists = productService.checkSkuExists("SKU001", 1L);

            // Assert
            assertFalse(exists);
            verify(productMapper, times(1)).selectCount(argThat(wrapper ->
                wrapper.toString().contains("id") || wrapper.toString().contains("1")
            ));
        }
    }

    @Nested
    @DisplayName("批量删除测试 (Batch Delete)")
    class BatchDeleteTests {

        @Test
        @DisplayName("应成功批量删除商品 - when all products have no associations")
        void shouldBatchDeleteSuccessfully_whenAllProductsHaveNoAssociations() {
            // Arrange
            List<Long> ids = Arrays.asList(1L, 2L, 3L);
            when(productMapper.selectBatchIds(ids)).thenReturn(Arrays.asList(testProduct));
            when(inventoryService.hasInventoryRecords(anyLong())).thenReturn(false);
            when(inventoryService.hasInboundOutboundRecords(anyLong())).thenReturn(false);
            when(productMapper.deleteBatchIds(ids)).thenReturn(3);

            // Act
            int count = productService.batchDelete(ids);

            // Assert
            assertEquals(3, count);
            verify(productMapper, times(1)).deleteBatchIds(ids);
        }

        @Test
        @DisplayName("应抛出异常 - when any product has associations")
        void shouldThrowException_whenAnyProductHasAssociations() {
            // Arrange
            List<Long> ids = Arrays.asList(1L, 2L);
            when(productMapper.selectBatchIds(ids)).thenReturn(Arrays.asList(testProduct));
            when(inventoryService.hasInventoryRecords(1L)).thenReturn(true);

            // Act & Assert
            Exception exception = assertThrows(RuntimeException.class,
                () -> productService.batchDelete(ids));

            assertTrue(exception.getMessage().contains("无法删除"));
            verify(productMapper, never()).deleteBatchIds(any());
        }

        @Test
        @DisplayName("应返回0 - when empty list provided")
        void shouldReturnZero_whenEmptyListProvided() {
            // Act
            int count = productService.batchDelete(Collections.emptyList());

            // Assert
            assertEquals(0, count);
            verify(productMapper, never()).deleteBatchIds(any());
        }
    }
}
