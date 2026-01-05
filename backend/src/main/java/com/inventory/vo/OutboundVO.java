package com.inventory.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出库单视图对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "OutboundVO对象", description = "出库单视图对象")
public class OutboundVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 出库单ID
     */
    @ApiModelProperty(value = "出库单ID", example = "1")
    private Long id;

    /**
     * 出库单号
     */
    @ApiModelProperty(value = "出库单号", example = "OUT202601040001")
    private String outboundNo;

    /**
     * 商品ID
     */
    @ApiModelProperty(value = "商品ID", example = "1")
    private Long productId;

    /**
     * 商品名称（非数据库字段）
     */
    @ApiModelProperty(value = "商品名称")
    private String productName;

    /**
     * 商品SKU（非数据库字段）
     */
    @ApiModelProperty(value = "商品SKU")
    private String productSku;

    /**
     * 出库数量
     */
    @ApiModelProperty(value = "出库数量", example = "50")
    private Integer quantity;

    /**
     * 收货人
     */
    @ApiModelProperty(value = "收货人", example = "客户A")
    private String receiver;

    /**
     * 收货人电话
     */
    @ApiModelProperty(value = "收货人电话", example = "13800138000")
    private String receiverPhone;

    /**
     * 出库日期
     */
    @ApiModelProperty(value = "出库日期", example = "2026-01-04T10:00:00")
    private LocalDateTime outboundDate;

    /**
     * 状态：0-待审核，1-已审核，2-已作废
     */
    @ApiModelProperty(value = "状态：0-待审核，1-已审核，2-已作废", example = "0")
    private Integer status;

    /**
     * 状态文本（非数据库字段）
     */
    @ApiModelProperty(value = "状态文本")
    private String statusText;

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
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
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
     * 从实体转换为VO
     */
    public static OutboundVO fromEntity(com.inventory.entity.Outbound outbound) {
        OutboundVO vo = new OutboundVO();
        vo.setId(outbound.getId());
        vo.setOutboundNo(outbound.getOutboundNo());
        vo.setProductId(outbound.getProductId());
        vo.setQuantity(outbound.getQuantity());
        vo.setReceiver(outbound.getReceiver());
        vo.setReceiverPhone(outbound.getReceiverPhone());
        vo.setOutboundDate(outbound.getOutboundDate());
        vo.setStatus(outbound.getStatus());
        vo.setRemark(outbound.getRemark());
        vo.setCreatedBy(outbound.getCreatedBy());
        vo.setCreatedAt(outbound.getCreatedAt());
        vo.setUpdatedAt(outbound.getUpdatedAt());
        vo.setApprovedBy(outbound.getApprovedBy());
        vo.setApprovedAt(outbound.getApprovedAt());

        // 设置状态文本
        if (outbound.getStatus() != null) {
            switch (outbound.getStatus()) {
                case com.inventory.entity.Outbound.STATUS_PENDING:
                    vo.setStatusText("待审核");
                    break;
                case com.inventory.entity.Outbound.STATUS_APPROVED:
                    vo.setStatusText("已审核");
                    break;
                case com.inventory.entity.Outbound.STATUS_VOID:
                    vo.setStatusText("已作废");
                    break;
            }
        }

        return vo;
    }
}
