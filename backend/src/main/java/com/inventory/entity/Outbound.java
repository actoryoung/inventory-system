package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出库单实体
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@TableName("t_outbound")
@ApiModel(value = "Outbound对象", description = "出库单")
public class Outbound implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 出库单状态常量
     */
    public static final int STATUS_PENDING = 0;    // 待审核
    public static final int STATUS_APPROVED = 1;   // 已审核
    public static final int STATUS_VOID = 2;       // 已作废

    /**
     * 出库单ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "出库单ID", example = "1")
    private Long id;

    /**
     * 出库单号
     */
    @ApiModelProperty(value = "出库单号", required = true, example = "OUT202601040001")
    private String outboundNo;

    /**
     * 商品ID
     */
    @ApiModelProperty(value = "商品ID", required = true, example = "1")
    private Long productId;

    /**
     * 出库数量
     */
    @ApiModelProperty(value = "出库数量", required = true, example = "50")
    private Integer quantity;

    /**
     * 收货人
     */
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
    @ApiModelProperty(value = "出库日期", required = true, example = "2026-01-04T10:00:00")
    private LocalDateTime outboundDate;

    /**
     * 状态：0-待审核，1-已审核，2-已作废
     */
    @ApiModelProperty(value = "状态：0-待审核，1-已审核，2-已作废", example = "0")
    private Integer status;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createdBy;

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
     * 审核人
     */
    @ApiModelProperty(value = "审核人")
    private String approvedBy;

    /**
     * 审核时间
     */
    @ApiModelProperty(value = "审核时间")
    private LocalDateTime approvedAt;

    /**
     * 是否待审核
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否待审核", hidden = true)
    public boolean isPending() {
        return this.status != null && this.status == STATUS_PENDING;
    }

    /**
     * 是否已审核
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否已审核", hidden = true)
    public boolean isApproved() {
        return this.status != null && this.status == STATUS_APPROVED;
    }

    /**
     * 是否已作废
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否已作废", hidden = true)
    public boolean isVoid() {
        return this.status != null && this.status == STATUS_VOID;
    }
}
