package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存实体
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@TableName("t_inventory")
@ApiModel(value = "Inventory对象", description = "库存")
public class Inventory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID", example = "1")
    private Long id;

    /**
     * 商品ID
     */
    @ApiModelProperty(value = "商品ID", example = "1")
    private Long productId;

    /**
     * 仓库ID
     */
    @ApiModelProperty(value = "仓库ID", example = "1")
    private Long warehouseId;

    /**
     * 库存数量
     */
    @ApiModelProperty(value = "库存数量", example = "100")
    private Integer quantity;

    /**
     * 预警值
     */
    @ApiModelProperty(value = "预警值", example = "10")
    private Integer warningStock;

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
}
