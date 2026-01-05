package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inventory.dto.InventoryAdjustDTO;
import com.inventory.entity.Category;
import com.inventory.entity.Inventory;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.InventoryMapper;
import com.inventory.service.*;
import com.inventory.vo.InventoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    private final ProductService productService;
    private final CategoryService categoryService;

    private static final Long DEFAULT_WAREHOUSE_ID = 1L;

    public InventoryServiceImpl(
            ProductService productService,
            CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initInventory(Long productId, Integer quantity) {
        // 检查是否已存在
        int count = this.baseMapper.countByProductAndWarehouse(productId, DEFAULT_WAREHOUSE_ID);
        if (count > 0) {
            throw new BusinessException("库存记录已存在");
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setWarehouseId(DEFAULT_WAREHOUSE_ID);
        inventory.setQuantity(quantity != null ? quantity : 0);
        inventory.setWarningStock(10); // 默认预警值

        this.save(inventory);
        log.info("初始化库存成功，productId={}, quantity={}", productId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(Long productId, Integer quantity) {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        Integer oldQuantity = inventory.getQuantity();
        inventory.setQuantity(oldQuantity + quantity);
        this.updateById(inventory);

        log.info("增加库存成功，productId={}, {} -> {}",
                productId, oldQuantity, inventory.getQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduceStock(Long productId, Integer quantity) {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        if (inventory.getQuantity() < quantity) {
            throw new BusinessException(
                    String.format("库存不足，当前库存：%d，需要：%d",
                            inventory.getQuantity(), quantity));
        }

        Integer oldQuantity = inventory.getQuantity();
        inventory.setQuantity(oldQuantity - quantity);
        this.updateById(inventory);

        log.info("减少库存成功，productId={}, {} -> {}",
                productId, oldQuantity, inventory.getQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long productId, Integer quantity, String reason) {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        Integer oldQuantity = inventory.getQuantity();
        inventory.setQuantity(quantity);
        this.updateById(inventory);

        log.info("调整库存成功，productId={}, {} -> {}, reason={}",
                productId, oldQuantity, quantity, reason);
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
                newQuantity = oldQuantity + dto.getQuantity();
                break;
            case "reduce":
                newQuantity = oldQuantity - dto.getQuantity();
                if (newQuantity < 0) {
                    throw new BusinessException(
                            String.format("库存不足，当前库存：%d，要减少：%d", oldQuantity, dto.getQuantity()));
                }
                break;
            case "set":
                newQuantity = dto.getQuantity();
                if (newQuantity < 0) {
                    throw new BusinessException("库存数量不能为负数");
                }
                break;
            default:
                throw new BusinessException("无效的调整类型");
        }

        inventory.setQuantity(newQuantity);
        this.updateById(inventory);

        log.info("调整库存成功，inventoryId={}, {} -> {}, type={}, reason={}",
                inventoryId, oldQuantity, newQuantity, dto.getType(), dto.getReason());

        Map<String, Object> result = new HashMap<>();
        result.put("oldQuantity", oldQuantity);
        result.put("newQuantity", newQuantity);
        return result;
    }

    @Override
    public Inventory getByProductId(Long productId) {
        return this.baseMapper.selectByProductId(productId);
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

        // 构建查询条件
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();

        // 低库存筛选
        if (lowStock != null && lowStock) {
            wrapper.apply("quantity <= warning_stock");
        }

        wrapper.orderByDesc(Inventory::getUpdatedAt);

        IPage<Inventory> inventoryPage = this.page(pageParam, wrapper);

        // 转换为 VO 并填充商品信息
        IPage<InventoryVO> voPage = new Page<>(inventoryPage.getCurrent(), inventoryPage.getSize(), inventoryPage.getTotal());
        List<InventoryVO> records = inventoryPage.getRecords().stream()
                .map(inv -> {
                    InventoryVO vo = InventoryVO.fromEntity(inv);

                    // 获取商品信息
                    Product product = productService.getById(inv.getProductId());
                    if (product != null) {
                        vo.setProductId(product.getId());
                        vo.setProductSku(product.getSku());
                        vo.setProductName(product.getName());
                        vo.setCategoryId(product.getCategoryId());

                        // 获取分类信息
                        Category category = categoryService.getById(product.getCategoryId());
                        if (category != null) {
                            vo.setCategoryName(category.getName());
                        }

                        // 计算库存金额
                        if (inv.getQuantity() != null && product.getPrice() != null) {
                            vo.setAmount(product.getPrice().multiply(new BigDecimal(inv.getQuantity())));
                        }

                        // 判断是否低库存
                        vo.setIsLowStock(inv.getQuantity() <= (inv.getWarningStock() != null ? inv.getWarningStock() : 0));
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        // 按商品名称和分类过滤
        if (StringUtils.hasText(productName) || categoryId != null) {
            records = records.stream()
                    .filter(vo -> {
                        boolean match = true;

                        if (StringUtils.hasText(productName)) {
                            match = match && (vo.getProductName() != null &&
                                    vo.getProductName().toLowerCase().contains(productName.toLowerCase()));
                        }

                        if (categoryId != null) {
                            match = match && categoryId.equals(vo.getCategoryId());
                        }

                        return match;
                    })
                    .collect(Collectors.toList());
        }

        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public List<InventoryVO> getLowStockList() {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("quantity <= warning_stock");
        wrapper.orderByAsc(Inventory::getQuantity);

        List<Inventory> inventories = this.list(wrapper);

        return inventories.stream()
                .map(inv -> {
                    InventoryVO vo = InventoryVO.fromEntity(inv);

                    Product product = productService.getById(inv.getProductId());
                    if (product != null) {
                        vo.setProductSku(product.getSku());
                        vo.setProductName(product.getName());
                        vo.setCategoryId(product.getCategoryId());

                        Category category = categoryService.getById(product.getCategoryId());
                        if (category != null) {
                            vo.setCategoryName(category.getName());
                        }

                        vo.setIsLowStock(true);
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getSummary() {
        // 总商品数
        long totalProducts = productService.count();

        // 总库存数量
        long totalQuantity = this.list().stream()
                .mapToLong(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0L)
                .sum();

        // 低库存商品数
        long lowStockCount = this.count(new LambdaQueryWrapper<Inventory>()
                .apply("quantity <= warning_stock"));

        // 库存总金额
        BigDecimal totalAmount = this.list().stream()
                .map(inv -> {
                    Product product = productService.getById(inv.getProductId());
                    if (product != null && product.getPrice() != null && inv.getQuantity() != null) {
                        return product.getPrice().multiply(new BigDecimal(inv.getQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", totalProducts);
        summary.put("totalQuantity", totalQuantity);
        summary.put("lowStockCount", lowStockCount);
        summary.put("totalAmount", totalAmount);

        return summary;
    }
}
