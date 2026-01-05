package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@TableName("t_product")
@ApiModel(value = "Product对象", description = "商品")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "商品ID", example = "1")
    private Long id;

    /**
     * 商品编码（SKU）
     */
    @ApiModelProperty(value = "商品编码（SKU）", required = true, example = "SKU001")
    private String sku;

    /**
     * 商品名称
     */
    @ApiModelProperty(value = "商品名称", required = true, example = "iPhone 15 Pro")
    private String name;

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID", required = true, example = "1")
    private Long categoryId;

    /**
     * 计量单位
     */
    @ApiModelProperty(value = "计量单位", example = "台")
    private String unit;

    /**
     * 销售价格
     */
    @ApiModelProperty(value = "销售价格", required = true, example = "7999.00")
    private BigDecimal price;

    /**
     * 成本价格
     */
    @ApiModelProperty(value = "成本价格", example = "6000.00")
    private BigDecimal costPrice;

    /**
     * 商品规格
     */
    @ApiModelProperty(value = "商品规格", example = "256GB 深空黑色")
    private String specification;

    /**
     * 商品描述
     */
    @ApiModelProperty(value = "商品描述")
    private String description;

    /**
     * 预警库存
     */
    @ApiModelProperty(value = "预警库存", example = "10")
    private Integer warningStock;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 分类名称（非数据库字段）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    /**
     * 当前库存（非数据库字段）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "当前库存")
    private Integer stockQuantity;

    /**
     * 是否启用
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否启用", hidden = true)
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 是否库存不足
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否库存不足", hidden = true)
    public boolean isLowStock(Integer currentStock) {
        return currentStock != null && currentStock <= this.warningStock;
    }
}
