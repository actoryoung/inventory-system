package com.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inventory.dto.InventoryAdjustDTO;
import com.inventory.entity.Inventory;
import com.inventory.vo.InventoryVO;

import java.util.List;
import java.util.Map;

/**
 * 库存服务接口
 *
 * @author inventory-system
 * @since 2026-01-04
 */
public interface InventoryService extends IService<Inventory> {

    /**
     * 初始化库存
     *
     * @param productId 商品ID
     * @param quantity 初始数量
     */
    void initInventory(Long productId, Integer quantity);

    /**
     * 增加库存
     *
     * @param productId 商品ID
     * @param quantity 增加数量
     */
    void addStock(Long productId, Integer quantity);

    /**
     * 减少库存
     *
     * @param productId 商品ID
     * @param quantity 减少数量
     */
    void reduceStock(Long productId, Integer quantity);

    /**
     * 调整库存
     *
     * @param productId 商品ID
     * @param quantity 新的库存数量
     * @param reason 调整原因
     */
    void adjustStock(Long productId, Integer quantity, String reason);

    /**
     * 调整库存（使用DTO）
     *
     * @param inventoryId 库存ID
     * @param dto 调整数据
     * @return 调整后的库存信息
     */
    Map<String, Object> adjustInventory(Long inventoryId, InventoryAdjustDTO dto);

    /**
     * 根据商品ID获取库存
     *
     * @param productId 商品ID
     * @return 库存对象
     */
    Inventory getByProductId(Long productId);

    /**
     * 检查库存是否充足
     *
     * @param productId 商品ID
     * @param quantity 需要的数量
     * @return 是否充足
     */
    boolean checkStock(Long productId, Integer quantity);

    /**
     * 分页查询库存列表
     *
     * @param productName 商品名称（可选）
     * @param categoryId 分类ID（可选）
     * @param lowStock 是否只查低库存（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<InventoryVO> page(String productName, Long categoryId, Boolean lowStock, int page, int size);

    /**
     * 获取低库存商品列表
     *
     * @return 低库存商品列表
     */
    List<InventoryVO> getLowStockList();

    /**
     * 获取库存汇总统计
     *
     * @return 汇总数据
     */
    Map<String, Object> getSummary();
}
