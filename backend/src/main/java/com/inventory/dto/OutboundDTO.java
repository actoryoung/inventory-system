package com.inventory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出库单数据传输对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "OutboundDTO对象", description = "出库单数据传输对象")
public class OutboundDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true, example = "1")
    private Long productId;

    /**
     * 出库数量
     */
    @NotNull(message = "出库数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    @Max(value = 999999, message = "出库数量不能超过999999")
    @ApiModelProperty(value = "出库数量", required = true, example = "50")
    private Integer quantity;

    /**
     * 收货人
     */
    @NotBlank(message = "收货人不能为空")
    @ApiModelProperty(value = "收货人", required = true, example = "客户A")
    private String receiver;

    /**
     * 收货人电话
     */
    @ApiModelProperty(value = "收货人电话", example = "13800138000")
    private String receiverPhone;

    /**
     * 出库日期
     */
    @NotNull(message = "出库日期不能为空")
    @ApiModelProperty(value = "出库日期", required = true, example = "2026-01-04T10:00:00")
    private LocalDateTime outboundDate;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
}
