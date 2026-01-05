-- 入库单表
CREATE TABLE IF NOT EXISTS t_inbound (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    inbound_no VARCHAR(20) NOT NULL UNIQUE COMMENT '入库单号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '入库数量',
    supplier VARCHAR(100) NOT NULL COMMENT '供应商',
    inbound_date DATETIME NOT NULL COMMENT '入库日期',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核 1-已审核 2-已作废',
    remark VARCHAR(500) COMMENT '备注',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    approved_by VARCHAR(50) COMMENT '审核人',
    approved_at DATETIME COMMENT '审核时间',

    CONSTRAINT fk_inbound_product FOREIGN KEY (product_id)
        REFERENCES t_product(id) ON DELETE RESTRICT,
    CONSTRAINT uk_inbound_no UNIQUE (inbound_no),
    CONSTRAINT chk_inbound_quantity CHECK (quantity > 0),
    CONSTRAINT chk_inbound_status CHECK (status IN (0, 1, 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

-- 索引
CREATE INDEX idx_inbound_product_id ON t_inbound(product_id);
CREATE INDEX idx_inbound_date ON t_inbound(inbound_date);
CREATE INDEX idx_inbound_status ON t_inbound(status);

-- 单号序号表（用于生成入库单号）
CREATE TABLE IF NOT EXISTS t_inbound_sequence (
    seq_date DATE PRIMARY KEY COMMENT '日期',
    seq_value INT NOT NULL DEFAULT 0 COMMENT '序号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单号序号表';
