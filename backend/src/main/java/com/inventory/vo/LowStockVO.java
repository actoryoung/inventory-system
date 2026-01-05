package com.inventory.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 低库存预警VO
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "LowStockVO对象", description = "低库存预警")
public class LowStockVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品SKU")
    private String productSku;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("当前库存")
    private Integer quantity;

    @ApiModelProperty("预警值")
    private Integer warningStock;

    @ApiModelProperty("缺货数量")
    private Integer shortage;
}
