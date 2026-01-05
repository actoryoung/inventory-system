-- 出库单表
CREATE TABLE IF NOT EXISTS t_outbound (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    outbound_no VARCHAR(20) NOT NULL UNIQUE COMMENT '出库单号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '出库数量',
    receiver VARCHAR(100) NOT NULL COMMENT '收货人',
    receiver_phone VARCHAR(20) COMMENT '收货人电话',
    outbound_date DATETIME NOT NULL COMMENT '出库日期',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-已审核 2-已作废',
    remark VARCHAR(500) COMMENT '备注',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    approved_by VARCHAR(50) COMMENT '审核人',
    approved_at DATETIME COMMENT '审核时间',

    CONSTRAINT fk_outbound_product FOREIGN KEY (product_id)
        REFERENCES t_product(id) ON DELETE RESTRICT,
    CONSTRAINT uk_outbound_no UNIQUE (outbound_no),
    CONSTRAINT chk_outbound_quantity CHECK (quantity > 0),
    CONSTRAINT chk_outbound_status CHECK (status IN (0, 1, 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

-- 索引
CREATE INDEX idx_outbound_product_id ON t_outbound(product_id);
CREATE INDEX idx_outbound_date ON t_outbound(outbound_date);
CREATE INDEX idx_outbound_status ON t_outbound(status);

-- 单号序号表（用于生成出库单号）
CREATE TABLE IF NOT EXISTS t_outbound_sequence (
    seq_date DATE PRIMARY KEY COMMENT '日期',
    seq_value INT NOT NULL DEFAULT 0 COMMENT '序号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单号序号表';
