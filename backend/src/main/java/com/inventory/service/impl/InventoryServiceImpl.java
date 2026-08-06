package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inventory.dto.InventoryAdjustDTO;
import com.inventory.entity.Category;
import com.inventory.entity.Inventory;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.CategoryMapper;
import com.inventory.mapper.InventoryMapper;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.InventoryService;
import com.inventory.vo.InventoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存服务实现
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory>
        implements InventoryService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    private static final Long DEFAULT_WAREHOUSE_ID = 1L;

    public InventoryServiceImpl(
            ProductMapper productMapper,
            CategoryMapper categoryMapper) {
        this.productMapper = productMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initInventory(Long productId, Integer quantity) {
        // 检查是否已存在
        int count = this.baseMapper.countByProductAndWarehouse(productId, DEFAULT_WAREHOUSE_ID);
        if (count > 0) {
            throw new BusinessException("库存记录已存在");
        }

        // 预警值以商品为准（t_product.warning_stock 为唯一数据源，库存冗余存储过渡期）
        Integer warningStock = 0;
        Product product = productMapper.selectById(productId);
        if (product != null && product.getWarningStock() != null) {
            warningStock = product.getWarningStock();
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setWarehouseId(DEFAULT_WAREHOUSE_ID);
        inventory.setQuantity(quantity != null ? quantity : 0);
        inventory.setWarningStock(warningStock);

        this.save(inventory);
        log.info("初始化库存成功，productId={}, quantity={}, warningStock={}", productId, quantity, warningStock);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(Long productId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new BusinessException("增加数量不能为负数");
        }

        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        // 原子增加，避免并发丢失更新
        int rows = this.baseMapper.incrementStock(inventory.getId(), quantity);
        if (rows == 0) {
            throw new BusinessException("库存更新失败");
        }

        log.info("增加库存成功，productId={}, +{}", productId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduceStock(Long productId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new BusinessException("扣减数量不能为负数");
        }

        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        // 原子扣减，WHERE quantity >= #{quantity} 防止超卖
        int rows = this.baseMapper.decrementStock(inventory.getId(), quantity);
        if (rows == 0) {
            throw new BusinessException(
                    String.format("库存不足，当前库存：%d，需要：%d",
                            inventory.getQuantity(), quantity));
        }

        log.info("减少库存成功，productId={}, -{}", productId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long productId, Integer quantity, String reason) {
        if (quantity == null || quantity < 0) {
            throw new BusinessException("库存数量不能为负数");
        }

        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        // 直接设置目标值
        int rows = this.baseMapper.setStock(inventory.getId(), quantity);
        if (rows == 0) {
            throw new BusinessException("库存更新失败");
        }

        log.info("调整库存成功，productId={} -> {}, reason={}",
                productId, quantity, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> adjustInventory(Long inventoryId, InventoryAdjustDTO dto) {
        Inventory inventory = this.getById(inventoryId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        Integer oldQuantity = inventory.getQuantity();
        Integer newQuantity;

        switch (dto.getType()) {
            case "add":
                // 允许负数做"反向调整"，但调整后结果不能为负
                newQuantity = oldQuantity + dto.getQuantity();
                if (newQuantity < 0) {
                    throw new BusinessException("调整后库存不能为负数");
                }
                this.baseMapper.incrementStock(inventoryId, dto.getQuantity());
                break;
            case "reduce":
                // 原子扣减，不足则失败
                int rows = this.baseMapper.decrementStock(inventoryId, dto.getQuantity());
                if (rows == 0) {
                    throw new BusinessException(
                            String.format("库存不足，当前库存：%d，要减少：%d", oldQuantity, dto.getQuantity()));
                }
                newQuantity = oldQuantity - dto.getQuantity();
                break;
            case "set":
                if (dto.getQuantity() < 0) {
                    throw new BusinessException("库存数量不能为负数");
                }
                this.baseMapper.setStock(inventoryId, dto.getQuantity());
                newQuantity = dto.getQuantity();
                break;
            default:
                throw new BusinessException("无效的调整类型");
        }

        log.info("调整库存成功，inventoryId={}, {} -> {}, type={}, reason={}",
                inventoryId, oldQuantity, newQuantity, dto.getType(), dto.getReason());

        Map<String, Object> result = new HashMap<>();
        result.put("oldQuantity", oldQuantity);
        result.put("newQuantity", newQuantity);
        return result;
    }

    @Override
    public Inventory getByProductId(Long productId) {
        return this.baseMapper.selectByProductId(productId, DEFAULT_WAREHOUSE_ID);
    }

    @Override
    public boolean checkStock(Long productId, Integer quantity) {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            return false;
        }
        return inventory.getQuantity() >= quantity;
    }

    @Override
    public IPage<InventoryVO> page(String productName, Long categoryId, Boolean lowStock, int page, int size) {
        Page<Inventory> pageParam = new Page<>(page, size);

        // 过滤条件下推到 SQL（JOIN t_product），保证分页 total 与过滤结果一致
        IPage<Inventory> inventoryPage =
                this.baseMapper.selectInventoryPage(pageParam, productName, categoryId, lowStock);

        List<InventoryVO> records = inventoryPage.getRecords().stream()
                .map(InventoryVO::fromEntity)
                .collect(Collectors.toList());

        // 批量加载商品与分类，消除 N+1 查询
        enrichWithProductAndCategory(records);

        IPage<InventoryVO> voPage = new Page<>(inventoryPage.getCurrent(), inventoryPage.getSize(), inventoryPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public List<InventoryVO> getLowStockList() {
        // JOIN t_product，预警值取 t_product.warning_stock
        List<Inventory> inventories = this.baseMapper.selectLowStockInventories();

        List<InventoryVO> records = inventories.stream()
                .map(InventoryVO::fromEntity)
                .collect(Collectors.toList());

        // 批量加载商品与分类，消除 N+1 查询
        enrichWithProductAndCategory(records);

        records.forEach(vo -> vo.setIsLowStock(true));
        return records;
    }

    /**
     * 批量填充商品/分类信息（消除逐条查询的 N+1）
     */
    private void enrichWithProductAndCategory(List<InventoryVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        List<Long> productIds = records.stream()
                .map(InventoryVO::getProductId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return;
        }

        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, java.util.function.Function.identity()));

        List<Long> categoryIds = productMap.values().stream()
                .map(Product::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Category> categoryMap = categoryIds.isEmpty()
                ? new HashMap<>()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, java.util.function.Function.identity()));

        for (InventoryVO vo : records) {
            Product product = productMap.get(vo.getProductId());
            if (product == null) {
                continue;
            }
            vo.setProductId(product.getId());
            vo.setProductSku(product.getSku());
            vo.setProductName(product.getName());
            vo.setCategoryId(product.getCategoryId());

            Category category = categoryMap.get(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }

            // 计算库存金额
            if (vo.getQuantity() != null && product.getPrice() != null) {
                vo.setAmount(product.getPrice().multiply(new BigDecimal(vo.getQuantity())));
            }

            // 预警值以商品为准（唯一数据源）
            Integer productWarningStock = product.getWarningStock() != null ? product.getWarningStock() : 0;
            vo.setWarningStock(productWarningStock);
            vo.setIsLowStock(vo.getQuantity() <= productWarningStock);
        }
    }

    @Override
    public Map<String, Object> getSummary() {
        // 全部使用聚合 SQL 在数据库层计算，避免全表扫描 + N+1
        long totalProducts = productMapper.selectCount(null);
        long totalQuantity = this.baseMapper.sumQuantity() != null ? this.baseMapper.sumQuantity() : 0L;
        long lowStockCount = this.baseMapper.countLowStock() != null ? this.baseMapper.countLowStock() : 0L;
        BigDecimal totalAmount = this.baseMapper.sumAmount() != null ? this.baseMapper.sumAmount() : BigDecimal.ZERO;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", totalProducts);
        summary.put("totalQuantity", totalQuantity);
        summary.put("lowStockCount", lowStockCount);
        summary.put("totalAmount", totalAmount);

        return summary;
    }

    @Override
    public List<Inventory> listByProductIds(java.util.Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return this.baseMapper.selectByProductIds(productIds);
    }
}
