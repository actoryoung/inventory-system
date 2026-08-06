-- =====================================================
-- H2 内存数据库 Schema（dev 模式）
-- 注意：H2 不兼容 MySQL 的 ENGINE/CHARSET/COMMENT 语法，
--       此处为 H2 专用简化 DDL。
-- =====================================================

DROP TABLE IF EXISTS t_inventory;
DROP TABLE IF EXISTS t_inbound;
DROP TABLE IF EXISTS t_outbound;
DROP TABLE IF EXISTS t_inbound_sequence;
DROP TABLE IF EXISTS t_outbound_sequence;
DROP TABLE IF EXISTS t_product;
DROP TABLE IF EXISTS t_category;

-- 商品分类表
CREATE TABLE t_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT,
    level TINYINT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    unit VARCHAR(20),
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cost_price DECIMAL(10,2) DEFAULT 0.00,
    specification VARCHAR(200),
    description TEXT,
    warning_stock INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 库存表
CREATE TABLE t_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL DEFAULT 1,
    quantity INT NOT NULL DEFAULT 0,
    warning_stock INT NOT NULL DEFAULT 10,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 入库单表
CREATE TABLE t_inbound (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inbound_no VARCHAR(20) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    supplier VARCHAR(100) NOT NULL,
    inbound_date DATETIME NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    created_by VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    approved_by VARCHAR(50),
    approved_at DATETIME
);

-- 出库单表
CREATE TABLE t_outbound (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    outbound_no VARCHAR(20) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    receiver VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20),
    outbound_date DATETIME NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    created_by VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    approved_by VARCHAR(50),
    approved_at DATETIME
);

-- 单号序号表
CREATE TABLE t_inbound_sequence (
    seq_date DATE PRIMARY KEY,
    seq_value INT NOT NULL DEFAULT 0
);

CREATE TABLE t_outbound_sequence (
    seq_date DATE PRIMARY KEY,
    seq_value INT NOT NULL DEFAULT 0
);

-- 索引
CREATE INDEX idx_inventory_product ON t_inventory(product_id);
CREATE INDEX idx_inventory_warehouse ON t_inventory(warehouse_id);
CREATE INDEX idx_product_category ON t_product(category_id);
CREATE INDEX idx_product_name ON t_product(name);
CREATE INDEX idx_inbound_product ON t_inbound(product_id);
CREATE INDEX idx_inbound_date ON t_inbound(inbound_date);
CREATE INDEX idx_outbound_product ON t_outbound(product_id);
CREATE INDEX idx_outbound_date ON t_outbound(outbound_date);
