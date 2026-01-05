package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inventory.dto.ProductDTO;
import com.inventory.entity.Category;
import com.inventory.entity.Inventory;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.*;
import com.inventory.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    private final CategoryService categoryService;
    private final InventoryService inventoryService;

    public ProductServiceImpl(
            CategoryService categoryService,
            InventoryService inventoryService) {
        this.categoryService = categoryService;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductDTO dto) {
        // 1. 校验SKU唯一性
        if (checkSkuExists(dto.getSku(), null)) {
            throw new BusinessException("商品编码已存在");
        }

        // 2. 校验分类是否存在且启用
        Category category = categoryService.getById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("商品分类不存在");
        }
        if (category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException("商品分类已禁用，无法添加商品");
        }

        // 3. 校验价格
        if (dto.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException("销售价格不能为负数");
        }
        if (dto.getCostPrice() != null && dto.getCostPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException("成本价格不能为负数");
        }

        // 4. 创建商品
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        boolean saved = this.save(product);
        if (!saved) {
            throw new BusinessException("商品创建失败");
        }

        // 5. 初始化库存记录
        try {
            inventoryService.initInventory(product.getId(), 0);
        } catch (Exception e) {
            log.error("初始化库存失败", e);
            throw new BusinessException("商品创建成功，但库存初始化失败");
        }

        log.info("创建商品成功，sku={}, id={}", product.getSku(), product.getId());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ProductDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("商品ID不能为空");
        }

        Product exist = this.getById(dto.getId());
        if (exist == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验SKU唯一性（排除当前商品）
        if (checkSkuExists(dto.getSku(), dto.getId())) {
            throw new BusinessException("商品编码已存在");
        }

        // 如果修改了分类，需要校验新分类
        if (!dto.getCategoryId().equals(exist.getCategoryId())) {
            Category category = categoryService.getById(dto.getCategoryId());
            if (category == null) {
                throw new BusinessException("商品分类不存在");
            }
            if (category.getStatus() == null || category.getStatus() != 1) {
                throw new BusinessException("商品分类已禁用");
            }
        }

        // 校验价格
        if (dto.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BusinessException("销售价格不能为负数");
        }

        // 更新商品
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setId(dto.getId());

        boolean updated = this.updateById(product);
        log.info("更新商品成功，id={}, sku={}", product.getId(), product.getSku());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        if (id == null) {
            throw new BusinessException("商品ID不能为空");
        }

        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查是否可以删除
        if (!canDelete(id)) {
            throw new BusinessException("该商品有库存或出入库记录，无法删除");
        }

        boolean deleted = this.removeById(id);
        log.info("删除商品成功，id={}, sku={}", id, product.getSku());
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("商品ID列表不能为空");
        }

        int deletedCount = 0;
        int cannotDeleteCount = 0;

        for (Long id : ids) {
            try {
                if (canDelete(id)) {
                    if (this.removeById(id)) {
                        deletedCount++;
                    }
                } else {
                    cannotDeleteCount++;
                }
            } catch (Exception e) {
                log.error("删除商品失败，id={}", id, e);
                cannotDeleteCount++;
            }
        }

        log.info("批量删除商品完成，成功={}, 无法删除={}", deletedCount, cannotDeleteCount);

        if (cannotDeleteCount > 0) {
            throw new BusinessException(
                String.format("批量删除完成，成功删除 %d 个，%d 个商品因有关联记录无法删除",
                    deletedCount, cannotDeleteCount));
        }

        return deletedCount;
    }

    @Override
    public ProductVO getById(Long id) {
        if (id == null) {
            return null;
        }
        Product product = this.baseMapper.selectById(id);
        if (product == null) {
            return null;
        }

        // 获取分类名称
        Category category = categoryService.getById(product.getCategoryId());
        if (category != null) {
            product.setCategoryName(category.getName());
        }

        // 获取当前库存
        Inventory inventory = inventoryService.getByProductId(id);
        if (inventory != null) {
            product.setStockQuantity(inventory.getQuantity());
        } else {
            product.setStockQuantity(0);
        }

        return ProductVO.fromEntity(product);
    }

    @Override
    public IPage<ProductVO> page(
            String name, String sku, Long categoryId, Integer status, int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }
        if (StringUtils.hasText(sku)) {
            wrapper.like(Product::getSku, sku);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreatedAt);

        IPage<Product> productPage = this.page(pageParam, wrapper);

        // 转换为 VO
        IPage<ProductVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        List<ProductVO> records = productPage.getRecords().stream()
                .map(p -> {
                    // 设置分类名称
                    Category category = categoryService.getById(p.getCategoryId());
                    if (category != null) {
                        p.setCategoryName(category.getName());
                    }
                    // 设置库存
                    Inventory inventory = inventoryService.getByProductId(p.getId());
                    p.setStockQuantity(inventory != null ? inventory.getQuantity() : 0);
                    return ProductVO.fromEntity(p);
                })
                .collect(Collectors.toList());

        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public List<ProductVO> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(Product::getSku, keyword)
                .or()
                .like(Product::getName, keyword));

        List<Product> products = this.list(wrapper);

        return products.stream()
                .map(p -> {
                    Category category = categoryService.getById(p.getCategoryId());
                    if (category != null) {
                        p.setCategoryName(category.getName());
                    }
                    Inventory inventory = inventoryService.getByProductId(p.getId());
                    p.setStockQuantity(inventory != null ? inventory.getQuantity() : 0);
                    return ProductVO.fromEntity(p);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("商品ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效");
        }

        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(status);
        boolean updated = this.updateById(product);
        log.info("切换商品状态成功，id={}, status={}", id, status);
        return updated;
    }

    @Override
    public boolean checkSkuExists(String sku, Long excludeId) {
        if (!StringUtils.hasText(sku)) {
            return false;
        }

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSku, sku);

        if (excludeId != null) {
            wrapper.ne(Product::getId, excludeId);
        }

        return this.count(wrapper) > 0;
    }

    @Override
    public boolean canDelete(Long id) {
        // 检查是否有库存记录
        int inventoryCount = this.baseMapper.countInventoryRecords(id);
        if (inventoryCount > 0) {
            return false;
        }

        // 检查是否有入库记录
        int inboundCount = this.baseMapper.countInboundRecords(id);
        if (inboundCount > 0) {
            return false;
        }

        // 检查是否有出库记录
        int outboundCount = this.baseMapper.countOutboundRecords(id);
        if (outboundCount > 0) {
            return false;
        }

        return true;
    }

    @Override
    public List<ProductVO> getLowStockProducts() {
        List<Product> products = this.baseMapper.selectLowStockProducts();

        return products.stream()
                .map(p -> {
                    Category category = categoryService.getById(p.getCategoryId());
                    if (category != null) {
                        p.setCategoryName(category.getName());
                    }
                    Inventory inventory = inventoryService.getByProductId(p.getId());
                    p.setStockQuantity(inventory != null ? inventory.getQuantity() : 0);
                    return ProductVO.fromEntity(p);
                })
                .collect(Collectors.toList());
    }
}
