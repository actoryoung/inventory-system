-- =====================================================
-- 库存表 (Inventory Management)
-- =====================================================

-- 创建库存表
CREATE TABLE IF NOT EXISTS t_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    warehouse_id BIGINT NOT NULL DEFAULT 1 COMMENT '仓库ID',
    quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    warning_stock INT NOT NULL DEFAULT 10 COMMENT '预警值',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id)
        REFERENCES t_product(id) ON DELETE CASCADE,
    CONSTRAINT uk_inventory_product_warehouse UNIQUE (product_id, warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 创建索引
CREATE INDEX idx_inventory_product ON t_inventory(product_id);
CREATE INDEX idx_inventory_warehouse ON t_inventory(warehouse_id);
