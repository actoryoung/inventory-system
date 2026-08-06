package com.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inventory.dto.ProductDTO;
import com.inventory.entity.Product;
import com.inventory.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author inventory-system
 * @since 2026-01-04
 */
public interface ProductService extends IService<Product> {

    /**
     * 创建商品
     *
     * @param dto 商品数据传输对象
     * @return 创建的商品ID
     */
    Long create(ProductDTO dto);

    /**
     * 更新商品
     *
     * @param dto 商品数据传输对象
     * @return 是否成功
     */
    boolean update(ProductDTO dto);

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 批量删除商品
     *
     * @param ids 商品ID列表
     * @return 删除数量
     */
    int batchDelete(List<Long> ids);

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @return 商品视图对象
     */
    ProductVO getById(Long id);

    /**
     * 分页查询商品
     *
     * @param name 商品名称（可选）
     * @param sku SKU（可选）
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<ProductVO> page(String name, String sku, Long categoryId, Integer status, int page, int size);

    /**
     * 搜索商品
     *
     * @param keyword 关键词（匹配SKU或名称）
     * @return 商品列表
     */
    List<ProductVO> search(String keyword);

    /**
     * 切换商品状态
     *
     * @param id 商品ID
     * @param status 状态：0-禁用，1-启用
     * @return 是否成功
     */
    boolean toggleStatus(Long id, Integer status);

    /**
     * 检查SKU是否存在
     *
     * @param sku 商品编码
     * @param excludeId 排除的商品ID（更新时使用）
     * @return 是否存在
     */
    boolean checkSkuExists(String sku, Long excludeId);

    /**
     * 检查是否可以删除商品
     *
     * @param id 商品ID
     * @return 是否可以删除
     */
    boolean canDelete(Long id);

    /**
     * 获取低库存商品列表
     *
     * @return 低库存商品列表
     */
    List<ProductVO> getLowStockProducts();
}
