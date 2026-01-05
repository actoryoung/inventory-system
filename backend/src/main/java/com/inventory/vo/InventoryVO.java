package com.inventory.vo;

import com.inventory.entity.Inventory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存视图对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "InventoryVO", description = "库存视图对象")
public class InventoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "库存ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "商品ID", example = "1")
    private Long productId;

    @ApiModelProperty(value = "商品编码", example = "SKU001")
    private String productSku;

    @ApiModelProperty(value = "商品名称", example = "iPhone 15 Pro")
    private String productName;

    @ApiModelProperty(value = "分类ID", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "分类名称", example = "电子产品")
    private String categoryName;

    @ApiModelProperty(value = "仓库ID", example = "1")
    private Long warehouseId;

    @ApiModelProperty(value = "库存数量", example = "100")
    private Integer quantity;

    @ApiModelProperty(value = "预警值", example = "10")
    private Integer warningStock;

    @ApiModelProperty(value = "是否低库存", example = "false")
    private Boolean isLowStock;

    @ApiModelProperty(value = "库存金额", example = "799900.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 从 Entity 转换为 VO
     */
    public static InventoryVO fromEntity(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        InventoryVO vo = new InventoryVO();
        vo.setId(inventory.getId());
        vo.setProductId(inventory.getProductId());
        vo.setWarehouseId(inventory.getWarehouseId());
        vo.setQuantity(inventory.getQuantity());
        vo.setWarningStock(inventory.getWarningStock());
        vo.setCreatedAt(inventory.getCreatedAt());
        vo.setUpdatedAt(inventory.getUpdatedAt());
        return vo;
    }
}
