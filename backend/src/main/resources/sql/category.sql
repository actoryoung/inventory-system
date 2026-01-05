-- =====================================================
-- 商品分类表 (Category Management)
-- =====================================================

-- 创建商品分类表
CREATE TABLE IF NOT EXISTS t_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID',
    level TINYINT NOT NULL DEFAULT 1 COMMENT '层级(1-3)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id)
        REFERENCES t_category(id) ON DELETE CASCADE,
    CONSTRAINT uk_category_name_parent UNIQUE (name, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 创建索引
CREATE INDEX idx_category_parent ON t_category(parent_id);
CREATE INDEX idx_category_level ON t_category(level);
CREATE INDEX idx_category_status ON t_category(status);

-- =====================================================
-- 初始化默认分类数据
-- =====================================================

-- 一级分类
INSERT INTO t_category (name, parent_id, level, sort_order, status) VALUES
('电子产品', NULL, 1, 1, 1),
('食品饮料', NULL, 1, 2, 1),
('服装鞋帽', NULL, 1, 3, 1),
('家居用品', NULL, 1, 4, 1),
('图书文具', NULL, 1, 5, 1);

-- 二级分类 - 电子产品
INSERT INTO t_category (name, parent_id, level, sort_order, status) VALUES
('手机', 1, 2, 1, 1),
('电脑', 1, 2, 2, 1),
('数码配件', 1, 2, 3, 1),
('摄影器材', 1, 2, 4, 1);

-- 三级分类 - 手机
INSERT INTO t_category (name, parent_id, level, sort_order, status) VALUES
('智能手机', 6, 3, 1, 1),
('功能手机', 6, 3, 2, 1),
('对讲机', 6, 3, 3, 1);

-- 三级分类 - 电脑
INSERT INTO t_category (name, parent_id, level, sort_order, status) VALUES
('笔记本', 7, 3, 1, 1),
('台式机', 7, 3, 2, 1),
('平板电脑', 7, 3, 3, 1);

-- 二级分类 - 食品饮料
INSERT INTO t_category (name, parent_id, level, sort_order, status) VALUES
('零食', 2, 2, 1, 1),
('饮料', 2, 2, 2, 1),
('生鲜', 2, 2, 3, 1),
('调味品', 2, 2, 4, 1);

-- 二级分类 - 服装鞋帽
INSERT INTO t_category (name, parent_id, level, sort_order, status) VALUES
('男装', 3, 2, 1, 1),
('女装', 3, 2, 2, 1),
('鞋类', 3, 2, 3, 1),
('配饰', 3, 2, 4, 1);

-- 二级分类 - 家居用品
INSERT INTO t_category (name, parent_id, level, sort_order, status) VALUES
('厨房用品', 4, 2, 1, 1),
('家纺', 4, 2, 2, 1),
('灯具', 4, 2, 3, 1);

-- 二级分类 - 图书文具
INSERT INTO t_category (name, parent_id, level, sort_order, status) VALUES
('图书', 5, 2, 1, 1),
('办公用品', 5, 2, 2, 1),
('文具', 5, 2, 3, 1);
