package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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
     * 原子增加库存
     *
     * @param id 库存ID
     * @param quantity 增加数量
     * @return 影响行数
     */
    @Update("UPDATE t_inventory SET quantity = quantity + #{quantity} WHERE id = #{id}")
    int incrementStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 原子扣减库存（防止超卖）
     *
     * 仅当当前库存 >= 扣减数量时更新成功。
     *
     * @param id 库存ID
     * @param quantity 扣减数量
     * @return 影响行数（0 表示库存不足）
     */
    @Update("UPDATE t_inventory SET quantity = quantity - #{quantity} WHERE id = #{id} AND quantity >= #{quantity}")
    int decrementStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 设置库存为指定值
     *
     * @param id 库存ID
     * @param quantity 目标库存值
     * @return 影响行数
     */
    @Update("UPDATE t_inventory SET quantity = #{quantity} WHERE id = #{id}")
    int setStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 分页查询库存（JOIN 商品表）
     *
     * 将商品名称/分类/低库存过滤条件下推到 SQL，保证分页 total 与过滤后的记录数一致。
     * 低库存判定使用 t_product.warning_stock（预警值唯一数据源）。
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
            "  <if test='lowStock != null and lowStock'>AND i.quantity &lt;= p.warning_stock</if>" +
            "</where>" +
            "ORDER BY i.updated_at DESC" +
            "</script>")
    IPage<Inventory> selectInventoryPage(
            Page<Inventory> page,
            @Param("productName") String productName,
            @Param("categoryId") Long categoryId,
            @Param("lowStock") Boolean lowStock);

    /**
     * 查询低库存列表（JOIN 商品表，预警值取 t_product.warning_stock）
     *
     * @return 低库存库存记录
     */
    @Select("SELECT i.* FROM t_inventory i " +
            "INNER JOIN t_product p ON i.product_id = p.id " +
            "WHERE i.quantity <= p.warning_stock " +
            "ORDER BY i.quantity ASC")
    List<Inventory> selectLowStockInventories();

    /**
     * 统计低库存商品数（JOIN 商品表，预警值取 t_product.warning_stock）
     *
     * @return 低库存数量
     */
    @Select("SELECT COUNT(*) FROM t_inventory i " +
            "INNER JOIN t_product p ON i.product_id = p.id " +
            "WHERE i.quantity <= p.warning_stock")
    Long countLowStock();
}
