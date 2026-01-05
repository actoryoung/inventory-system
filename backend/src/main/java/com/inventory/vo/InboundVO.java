package com.inventory.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入库单视图对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "InboundVO对象", description = "入库单视图对象")
public class InboundVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("入库单号")
    private String inboundNo;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品SKU")
    private String productSku;

    @ApiModelProperty("入库数量")
    private Integer quantity;

    @ApiModelProperty("供应商")
    private String supplier;

    @ApiModelProperty("入库日期")
    private LocalDateTime inboundDate;

    @ApiModelProperty("状态：0-待审核 1-已审核 2-已作废")
    private Integer status;

    @ApiModelProperty("状态描述")
    private String statusDesc;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建人")
    private String createdBy;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("审核人")
    private String approvedBy;

    @ApiModelProperty("审核时间")
    private LocalDateTime approvedAt;

    /**
     * 从实体转换为VO
     */
    public static InboundVO fromEntity(com.inventory.entity.Inbound inbound) {
        InboundVO vo = new InboundVO();
        vo.setId(inbound.getId());
        vo.setInboundNo(inbound.getInboundNo());
        vo.setProductId(inbound.getProductId());
        vo.setQuantity(inbound.getQuantity());
        vo.setSupplier(inbound.getSupplier());
        vo.setInboundDate(inbound.getInboundDate());
        vo.setStatus(inbound.getStatus());
        vo.setStatusDesc(inbound.getStatusDesc());
        vo.setRemark(inbound.getRemark());
        vo.setCreatedBy(inbound.getCreatedBy());
        vo.setCreatedAt(inbound.getCreatedAt());
        vo.setApprovedBy(inbound.getApprovedBy());
        vo.setApprovedAt(inbound.getApprovedAt());
        return vo;
    }
}
