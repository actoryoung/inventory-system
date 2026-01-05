package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inventory.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品 Mapper 接口
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据SKU查询商品
     *
     * @param sku 商品编码
     * @return 商品对象
     */
    @Select("SELECT * FROM t_product WHERE sku = #{sku}")
    Product selectBySku(@Param("sku") String sku);

    /**
     * 检查SKU是否存在（排除指定ID）
     *
     * @param sku 商品编码
     * @param excludeId 排除的商品ID
     * @return 商品对象
     */
    @Select("SELECT * FROM t_product WHERE sku = #{sku} AND id != #{excludeId}")
    Product selectBySkuExcludeId(@Param("sku") String sku, @Param("excludeId") Long excludeId);

    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @Select("SELECT * FROM t_product WHERE category_id = #{categoryId} ORDER BY id DESC")
    List<Product> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据名称模糊搜索商品
     *
     * @param name 商品名称（支持模糊匹配）
     * @return 匹配的商品列表
     */
    @Select("SELECT * FROM t_product WHERE name LIKE CONCAT('%', #{name}, '%') ORDER BY id DESC")
    List<Product> selectByNameLike(@Param("name") String name);

    /**
     * 检查商品是否有库存记录
     *
     * @param productId 商品ID
     * @return 库存记录数量
     */
    @Select("SELECT COUNT(*) FROM t_inventory WHERE product_id = #{productId}")
    int countInventoryRecords(@Param("productId") Long productId);

    /**
     * 检查商品是否有入库记录
     *
     * @param productId 商品ID
     * @return 入库记录数量
     */
    @Select("SELECT COUNT(*) FROM t_inbound WHERE product_id = #{productId}")
    int countInboundRecords(@Param("productId") Long productId);

    /**
     * 检查商品是否有出库记录
     *
     * @param productId 商品ID
     * @return 出库记录数量
     */
    @Select("SELECT COUNT(*) FROM t_outbound WHERE product_id = #{productId}")
    int countOutboundRecords(@Param("productId") Long productId);

    /**
     * 分页查询商品（带分类名称）
     *
     * @param page 分页对象
     * @param name 商品名称（可选）
     * @param sku SKU（可选）
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    IPage<Product> selectProductPageWithCategory(
            Page<Product> page,
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status
    );

    /**
     * 查询低库存商品
     *
     * @return 低库存商品列表
     */
    @Select("SELECT p.* FROM t_product p " +
            "INNER JOIN t_inventory i ON p.id = i.product_id " +
            "WHERE i.quantity <= p.warning_stock " +
            "ORDER BY (i.quantity - p.warning_stock) ASC")
    List<Product> selectLowStockProducts();
}
