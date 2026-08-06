package com.inventory.event;

import com.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 库存事件监听器
 *
 * 监听 {@link ProductCreatedEvent}，自动初始化库存记录。
 * 使用同步 {@link EventListener}，保证在商品创建事务内完成库存初始化，
 * 任一失败整体回滚，维持数据一致性。
 *
 * @author inventory-system
 * @since 2026-08-06
 */
@Slf4j
@Component
public class InventoryEventListener {

    private final InventoryService inventoryService;

    public InventoryEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @EventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("收到商品创建事件，初始化库存，productId={}", event.getProductId());
        inventoryService.initInventory(event.getProductId(), 0);
    }
}
