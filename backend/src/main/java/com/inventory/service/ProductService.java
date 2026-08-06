package com.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    default boolean createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("商品不能为空");
        }
        ProductDTO dto = new ProductDTO();
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setCategoryId(product.getCategoryId());
        dto.setUnit(product.getUnit());
        dto.setPrice(product.getPrice());
        dto.setCostPrice(product.getCostPrice());
        dto.setSpecification(product.getSpecification());
        dto.setDescription(product.getDescription());
        dto.setWarningStock(product.getWarningStock());
        dto.setStatus(product.getStatus());
        dto.setRemark(product.getRemark());
        return create(dto) != null;
    }

    default boolean updateProduct(Product product) {
        if (product == null || product.getId() == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setCategoryId(product.getCategoryId());
        dto.setUnit(product.getUnit());
        dto.setPrice(product.getPrice());
        dto.setCostPrice(product.getCostPrice());
        dto.setSpecification(product.getSpecification());
        dto.setDescription(product.getDescription());
        dto.setWarningStock(product.getWarningStock());
        dto.setStatus(product.getStatus());
        dto.setRemark(product.getRemark());
        return update(dto);
    }

    default boolean deleteProduct(Long id) {
        return delete(id);
    }

    default Product getProductById(Long id) {
        ProductVO vo = getById(id);
        if (vo == null) {
            return null;
        }
        Product product = new Product();
        product.setId(vo.getId());
        product.setSku(vo.getSku());
        product.setName(vo.getName());
        product.setCategoryId(vo.getCategoryId());
        product.setCategoryName(vo.getCategoryName());
        product.setUnit(vo.getUnit());
        product.setPrice(vo.getPrice());
        product.setCostPrice(vo.getCostPrice());
        product.setSpecification(vo.getSpecification());
        product.setDescription(vo.getDescription());
        product.setWarningStock(vo.getWarningStock());
        product.setStockQuantity(vo.getStockQuantity());
        product.setStatus(vo.getStatus());
        product.setRemark(vo.getRemark());
        product.setCreatedAt(vo.getCreatedAt());
        product.setUpdatedAt(vo.getUpdatedAt());
        return product;
    }

    default Page<Product> getProducts(int pageNum, int pageSize, String name, String sku, Long categoryId, Integer status) {
        IPage<ProductVO> voPage = page(name, sku, categoryId, status, pageNum, pageSize);
        Page<Product> productPage = new Page<>(voPage.getCurrent(), voPage.getSize(), voPage.getTotal());
        java.util.List<Product> records = voPage.getRecords().stream().map(vo -> {
            Product product = new Product();
            product.setId(vo.getId());
            product.setSku(vo.getSku());
            product.setName(vo.getName());
            product.setCategoryId(vo.getCategoryId());
            product.setCategoryName(vo.getCategoryName());
            product.setUnit(vo.getUnit());
            product.setPrice(vo.getPrice());
            product.setCostPrice(vo.getCostPrice());
            product.setSpecification(vo.getSpecification());
            product.setDescription(vo.getDescription());
            product.setWarningStock(vo.getWarningStock());
            product.setStockQuantity(vo.getStockQuantity());
            product.setStatus(vo.getStatus());
            product.setRemark(vo.getRemark());
            product.setCreatedAt(vo.getCreatedAt());
            product.setUpdatedAt(vo.getUpdatedAt());
            return product;
        }).collect(java.util.stream.Collectors.toList());
        productPage.setRecords(records);
        return productPage;
    }

    default java.util.List<Product> searchProducts(String keyword) {
        return search(keyword).stream().map(vo -> {
            Product product = new Product();
            product.setId(vo.getId());
            product.setSku(vo.getSku());
            product.setName(vo.getName());
            product.setCategoryId(vo.getCategoryId());
            product.setCategoryName(vo.getCategoryName());
            product.setUnit(vo.getUnit());
            product.setPrice(vo.getPrice());
            product.setCostPrice(vo.getCostPrice());
            product.setSpecification(vo.getSpecification());
            product.setDescription(vo.getDescription());
            product.setWarningStock(vo.getWarningStock());
            product.setStockQuantity(vo.getStockQuantity());
            product.setStatus(vo.getStatus());
            product.setRemark(vo.getRemark());
            product.setCreatedAt(vo.getCreatedAt());
            product.setUpdatedAt(vo.getUpdatedAt());
            return product;
        }).collect(java.util.stream.Collectors.toList());
    }

    default boolean toggleStatus(Long id) {
        Product product = getById((java.io.Serializable) id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        int targetStatus = product.getStatus() != null && product.getStatus() == 1 ? 0 : 1;
        return toggleStatus(id, targetStatus);
    }

    default boolean checkSkuExists(String sku) {
        return checkSkuExists(sku, null);
    }
}
