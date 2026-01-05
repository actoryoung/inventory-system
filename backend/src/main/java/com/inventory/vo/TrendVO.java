package com.inventory.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 出入库趋势VO
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "TrendVO对象", description = "出入库趋势")
public class TrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("日期列表")
    private List<String> dates;

    @ApiModelProperty("入库数量列表")
    private List<Integer> inboundQuantities;

    @ApiModelProperty("出库数量列表")
    private List<Integer> outboundQuantities;
}
