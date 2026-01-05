package com.inventory.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 库存分类分布VO
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "CategoryDistributionVO对象", description = "库存分类分布")
public class CategoryDistributionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("库存数量")
    private Integer quantity;

    @ApiModelProperty("占比（百分比）")
    private Double percentage;
}
