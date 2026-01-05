package com.inventory.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据看板VO
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "DashboardVO对象", description = "数据看板")
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("总商品数")
    private Integer totalProducts;

    @ApiModelProperty("总库存量")
    private Integer totalQuantity;

    @ApiModelProperty("库存总额")
    private Double totalAmount;

    @ApiModelProperty("低库存数量")
    private Integer lowStockCount;
}
