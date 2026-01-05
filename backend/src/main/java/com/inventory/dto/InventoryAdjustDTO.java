package com.inventory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 库存调整数据传输对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "InventoryAdjustDTO", description = "库存调整DTO")
public class InventoryAdjustDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 调整类型：add-增加，reduce-减少，set-设置
     */
    @NotNull(message = "调整类型不能为空")
    @Pattern(regexp = "add|reduce|set", message = "调整类型只能是 add、reduce 或 set")
    @ApiModelProperty(value = "调整类型", required = true, example = "add")
    private String type;

    /**
     * 调整数量
     */
    @NotNull(message = "调整数量不能为空")
    @ApiModelProperty(value = "调整数量", required = true, example = "10")
    private Integer quantity;

    /**
     * 调整原因
     */
    @NotNull(message = "调整原因不能为空")
    @ApiModelProperty(value = "调整原因", required = true, example = "盘点入库")
    private String reason;
}
