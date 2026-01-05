package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 库存 Mapper 接口
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 根据商品ID获取库存
     *
     * @param productId 商品ID
     * @return 库存对象
     */
    @Select("SELECT * FROM t_inventory WHERE product_id = #{productId} AND warehouse_id = 1")
    Inventory selectByProductId(@Param("productId") Long productId);

    /**
     * 检查库存记录是否已存在
     *
     * @param productId 商品ID
     * @param warehouseId 仓库ID
     * @return 存在的数量
     */
    @Select("SELECT COUNT(*) FROM t_inventory WHERE product_id = #{productId} AND warehouse_id = #{warehouseId}")
    int countByProductAndWarehouse(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);
}
