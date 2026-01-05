package com.inventory.vo;

import com.inventory.entity.Product;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品视图对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "ProductVO", description = "商品视图对象")
public class ProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "商品编码（SKU）", example = "SKU001")
    private String sku;

    @ApiModelProperty(value = "商品名称", example = "iPhone 15 Pro")
    private String name;

    @ApiModelProperty(value = "分类ID", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "分类名称", example = "电子产品")
    private String categoryName;

    @ApiModelProperty(value = "计量单位", example = "台")
    private String unit;

    @ApiModelProperty(value = "销售价格", example = "7999.00")
    private BigDecimal price;

    @ApiModelProperty(value = "成本价格", example = "6000.00")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "商品规格", example = "256GB 深空黑色")
    private String specification;

    @ApiModelProperty(value = "商品描述")
    private String description;

    @ApiModelProperty(value = "预警库存", example = "10")
    private Integer warningStock;

    @ApiModelProperty(value = "当前库存")
    private Integer stockQuantity;

    @ApiModelProperty(value = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 从 Entity 转换为 VO
     */
    public static ProductVO fromEntity(Product product) {
        if (product == null) {
            return null;
        }
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setSku(product.getSku());
        vo.setName(product.getName());
        vo.setCategoryId(product.getCategoryId());
        vo.setCategoryName(product.getCategoryName());
        vo.setUnit(product.getUnit());
        vo.setPrice(product.getPrice());
        vo.setCostPrice(product.getCostPrice());
        vo.setSpecification(product.getSpecification());
        vo.setDescription(product.getDescription());
        vo.setWarningStock(product.getWarningStock());
        vo.setStockQuantity(product.getStockQuantity());
        vo.setStatus(product.getStatus());
        vo.setRemark(product.getRemark());
        vo.setCreatedAt(product.getCreatedAt());
        vo.setUpdatedAt(product.getUpdatedAt());
        return vo;
    }
}
