package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入库单实体
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@TableName("t_inbound")
@ApiModel(value = "Inbound对象", description = "入库单")
public class Inbound implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("入库单号")
    private String inboundNo;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("入库数量")
    private Integer quantity;

    @ApiModelProperty("供应商")
    private String supplier;

    @ApiModelProperty("入库日期")
    private LocalDateTime inboundDate;

    @ApiModelProperty("状态：0-待审核 1-已审核 2-已作废")
    private Integer status;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建人")
    private String createdBy;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("更新时间")
    private LocalDateTime updatedAt;

    @ApiModelProperty("审核人")
    private String approvedBy;

    @ApiModelProperty("审核时间")
    private LocalDateTime approvedAt;

    /**
     * 状态枚举
     */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_VOID = 2;

    /**
     * 判断是否为待审核状态
     */
    public boolean isPending() {
        return STATUS_PENDING == this.status;
    }

    /**
     * 判断是否为已审核状态
     */
    public boolean isApproved() {
        return STATUS_APPROVED == this.status;
    }

    /**
     * 判断是否为已作废状态
     */
    public boolean isVoid() {
        return STATUS_VOID == this.status;
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        switch (this.status) {
            case STATUS_PENDING:
                return "待审核";
            case STATUS_APPROVED:
                return "已审核";
            case STATUS_VOID:
                return "已作废";
            default:
                return "未知";
        }
    }
}
