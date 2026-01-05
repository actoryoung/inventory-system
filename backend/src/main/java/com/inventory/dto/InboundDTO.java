package com.inventory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入库单数据传输对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "InboundDTO对象", description = "入库单数据传输对象")
public class InboundDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @ApiModelProperty(value = "入库数量", required = true)
    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
    @Max(value = 999999, message = "入库数量不能超过999999")
    private Integer quantity;

    @ApiModelProperty(value = "供应商", required = true)
    @NotBlank(message = "供应商不能为空")
    @Size(min = 1, max = 100, message = "供应商名称长度必须在1-100字符之间")
    private String supplier;

    @ApiModelProperty(value = "入库日期", required = true)
    @NotNull(message = "入库日期不能为空")
    private LocalDateTime inboundDate;

    @ApiModelProperty(value = "备注")
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}
