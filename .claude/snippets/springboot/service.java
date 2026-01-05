# Spring Boot Service 层片段

## 商品服务 (ProductService.java)

```java
package com.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.entity.Product;
import com.inventory.dto.ProductQuery;
import com.inventory.dto.ProductDTO;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询
     */
    IPage<Product> page(ProductQuery query);

    /**
     * 根据SKU查询
     */
    Product getBySku(String sku);

    /**
     * 检查SKU是否存在
     */
    boolean checkSkuExists(String sku);

    /**
     * 创建商品并初始化库存
     */
    boolean createWithStock(ProductDTO dto);

    /**
     * 更新商品状态
     */
    boolean updateStatus(Long id, Integer status);
}
```

## 商品服务实现 (ProductServiceImpl.java)

```java
package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inventory.entity.Product;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.ProductService;
import com.inventory.service.InventoryService;
import com.inventory.dto.ProductQuery;
import com.inventory.dto.ProductDTO;
import com.inventory.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 商品服务实现
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    @Autowired
    private InventoryService inventoryService;

    @Override
    public IPage<Product> page(ProductQuery query) {
        Page<Product> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 搜索条件
        wrapper.like(StringUtils.hasText(query.getName()),
                Product::getName, query.getName())
               .eq(StringUtils.hasText(query.getSku()),
                Product::getSku, query.getSku())
               .eq(query.getCategoryId() != null,
                Product::getCategoryId, query.getCategoryId())
               .eq(query.getStatus() != null,
                Product::getStatus, query.getStatus());

        // 排序
        wrapper.orderByDesc(Product::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public Product getBySku(String sku) {
        return this.lambdaQuery()
                .eq(Product::getSku, sku)
                .one();
    }

    @Override
    public boolean checkSkuExists(String sku) {
        return this.lambdaQuery()
                .eq(Product::getSku, sku)
                .count() > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createWithStock(ProductDTO dto) {
        // 1. 检查SKU是否重复
        if (checkSkuExists(dto.getSku())) {
            throw new BusinessException("商品编码已存在");
        }

        // 2. 创建商品
        Product product = new Product();
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setCategoryId(dto.getCategoryId());
        product.setUnit(dto.getUnit());
        product.setPrice(dto.getPrice());
        product.setCostPrice(dto.getCostPrice());
        product.setWarningStock(dto.getWarningStock());
        product.setSpecification(dto.getSpecification());
        product.setDescription(dto.getDescription());
        product.setRemark(dto.getRemark());
        product.setStatus(1);

        boolean saved = this.save(product);
        if (!saved) {
            throw new BusinessException("商品创建失败");
        }

        // 3. 初始化库存
        inventoryService.initInventory(product.getId(), dto.getInitStock());

        log.info("创建商品成功，sku={}, id={}", product.getSku(), product.getId());
        return true;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(status);
        return this.updateById(product);
    }
}
```

## 库存服务 (InventoryService.java)

```java
package com.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.inventory.entity.Inventory;

/**
 * 库存服务接口
 */
public interface InventoryService extends IService<Inventory> {

    /**
     * 初始化库存
     */
    void initInventory(Long productId, Integer quantity);

    /**
     * 增加库存
     */
    void addStock(Long productId, Integer quantity);

    /**
     * 减少库存
     */
    void reduceStock(Long productId, Integer quantity);

    /**
     * 调整库存
     */
    void adjustStock(Long productId, Integer quantity, String reason);

    /**
     * 根据商品ID获取库存
     */
    Inventory getByProductId(Long productId);

    /**
     * 检查库存是否充足
     */
    boolean checkStock(Long productId, Integer quantity);
}
```

## 库存服务实现 (InventoryServiceImpl.java)

```java
package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inventory.entity.Inventory;
import com.inventory.mapper.InventoryMapper;
import com.inventory.service.InventoryService;
import com.inventory.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存服务实现
 */
@Slf4j
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory>
        implements InventoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initInventory(Long productId, Integer quantity) {
        // 检查是否已存在
        Inventory exist = getByProductId(productId);
        if (exist != null) {
            throw new BusinessException("库存记录已存在");
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setWarehouseId(1L); // 默认仓库
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

        // 检查是否触发预警
        // TODO: 发送预警通知
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
    public Inventory getByProductId(Long productId) {
        return this.lambdaQuery()
                .eq(Inventory::getProductId, productId)
                .one();
    }

    @Override
    public boolean checkStock(Long productId, Integer quantity) {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            return false;
        }
        return inventory.getQuantity() >= quantity;
    }
}
```
