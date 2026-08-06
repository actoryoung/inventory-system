package com.inventory.event;

/**
 * 商品创建事件
 *
 * 商品创建成功后发布，用于解耦"创建商品时初始化库存"的强依赖。
 * 由 {@link InventoryEventListener} 监听并初始化库存记录。
 *
 * @author inventory-system
 * @since 2026-08-06
 */
public class ProductCreatedEvent {

    private final Long productId;

    public ProductCreatedEvent(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
