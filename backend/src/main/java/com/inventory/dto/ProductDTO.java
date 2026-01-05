package com.inventory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品数据传输对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "ProductDTO", description = "商品数据传输对象")
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID（更新时需要）
     */
    @ApiModelProperty(value = "商品ID", example = "1")
    private Long id;

    /**
     * 商品编码（SKU）
     */
    @NotBlank(message = "商品编码不能为空")
    @Size(max = 50, message = "商品编码长度不能超过50个字符")
    @ApiModelProperty(value = "商品编码（SKU）", required = true, example = "SKU001")
    private String sku;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100个字符")
    @ApiModelProperty(value = "商品名称", required = true, example = "iPhone 15 Pro")
    private String name;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    @ApiModelProperty(value = "分类ID", required = true, example = "1")
    private Long categoryId;

    /**
     * 计量单位
     */
    @Size(max = 20, message = "计量单位长度不能超过20个字符")
    @ApiModelProperty(value = "计量单位", example = "台")
    private String unit;

    /**
     * 销售价格
     */
    @NotNull(message = "销售价格不能为空")
    @DecimalMin(value = "0.00", message = "销售价格不能为负数")
    @ApiModelProperty(value = "销售价格", required = true, example = "7999.00")
    private BigDecimal price;

    /**
     * 成本价格
     */
    @DecimalMin(value = "0.00", message = "成本价格不能为负数")
    @ApiModelProperty(value = "成本价格", example = "6000.00")
    private BigDecimal costPrice;

    /**
     * 商品规格
     */
    @Size(max = 200, message = "商品规格长度不能超过200个字符")
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
    @NotNull(message = "预警库存不能为空")
    @Min(value = 0, message = "预警库存不能为负数")
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
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @ApiModelProperty(value = "备注")
    private String remark;
}
