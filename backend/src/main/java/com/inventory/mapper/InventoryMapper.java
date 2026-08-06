package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 分页查询库存（JOIN 商品表）
     *
     * 将商品名称/分类/低库存过滤条件下推到 SQL，保证分页 total 与过滤后的记录数一致。
     *
     * @param page 分页对象
     * @param productName 商品名称（可选，模糊匹配）
     * @param categoryId 分类ID（可选）
     * @param lowStock 是否只查低库存（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT i.* FROM t_inventory i " +
            "INNER JOIN t_product p ON i.product_id = p.id " +
            "<where>" +
            "  <if test='productName != null and productName != \"\"'>AND p.name LIKE CONCAT('%', #{productName}, '%')</if>" +
            "  <if test='categoryId != null'>AND p.category_id = #{categoryId}</if>" +
            "  <if test='lowStock != null and lowStock'>AND i.quantity &lt;= i.warning_stock</if>" +
            "</where>" +
            "ORDER BY i.updated_at DESC" +
            "</script>")
    IPage<Inventory> selectInventoryPage(
            Page<Inventory> page,
            @Param("productName") String productName,
            @Param("categoryId") Long categoryId,
            @Param("lowStock") Boolean lowStock);
}
