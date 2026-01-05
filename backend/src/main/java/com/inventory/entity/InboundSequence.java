package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 入库单号序号实体
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@TableName("t_inbound_sequence")
@ApiModel(value = "InboundSequence对象", description = "入库单号序号")
public class InboundSequence implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("日期")
    @TableId(type = IdType.INPUT)
    private LocalDate seqDate;

    @ApiModelProperty("序号")
    private Integer seqValue;
}
