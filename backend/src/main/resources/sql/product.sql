-- =====================================================
-- 商品表 (Product Management)
-- =====================================================

-- 创建商品表
CREATE TABLE IF NOT EXISTS t_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    sku VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编码（SKU）',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    unit VARCHAR(20) COMMENT '计量单位',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '销售价格',
    cost_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '成本价格',
    specification VARCHAR(200) COMMENT '商品规格',
    description TEXT COMMENT '商品描述',
    warning_stock INT NOT NULL DEFAULT 0 COMMENT '预警库存',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
        REFERENCES t_category(id) ON DELETE RESTRICT,
    CONSTRAINT uk_product_sku UNIQUE (sku)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 创建索引
CREATE INDEX idx_product_category ON t_product(category_id);
CREATE INDEX idx_product_status ON t_product(status);
CREATE INDEX idx_product_name ON t_product(name);
CREATE INDEX idx_product_sku ON t_product(sku);

-- =====================================================
-- 初始化示例数据
-- =====================================================

-- 插入示例商品
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, specification, description, warning_stock, status, remark) VALUES
('SKU001', 'iPhone 15 Pro', 6, '台', 7999.00, 6000.00, '256GB 深空黑色', '苹果最新款智能手机', 10, 1, '热销商品'),
('SKU002', 'MacBook Pro 14寸', 7, '台', 14999.00, 12000.00, 'M3 Pro芯片 16GB 512GB', '苹果专业笔记本电脑', 5, 1, '专业工作站'),
('SKU003', 'AirPods Pro 2', 6, '副', 1899.00, 1200.00, 'USB-C版', '苹果无线降噪耳机', 20, 1, '热销配件'),
('SKU004', 'iPad Air 5', 7, '台', 4799.00, 3500.00, '64GB WiFi版 蓝色', '苹果平板电脑', 15, 1, ''),
('SKU005', '小米14 Pro', 6, '台', 4999.00, 3500.00, '16GB 512GB 钛金属', '小米旗舰手机', 10, 1, '性价比之选'),
('SKU006', '华为MateBook X Pro', 7, '台', 8999.00, 7000.00, '16GB 1TB i7', '华为轻薄本', 5, 1, '');

-- 食品类商品
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status) VALUES
('FOOD001', '可口可乐 330ml', 11, '罐', 3.00, 2.00, 50, 1),
('FOOD002', '乐事薯片 原味', 11, '包', 8.00, 5.00, 30, 1),
('FOOD003', '农夫山泉 550ml', 11, '瓶', 2.00, 1.50, 100, 1);

-- 服装类商品
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status) VALUES
('CLOTH001', '纯棉T恤 白色', 14, '件', 59.00, 35.00, 20, 1),
('CLOTH002', '牛仔裤 男款', 14, '条', 199.00, 120.00, 10, 1);
