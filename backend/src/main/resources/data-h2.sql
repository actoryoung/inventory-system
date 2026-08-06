-- =====================================================
-- H2 内存数据库初始化数据（dev 模式）
-- 与 sql/category.sql、sql/product.sql 的示例数据保持一致
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

-- 示例商品
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, specification, description, warning_stock, status, remark) VALUES
('SKU001', 'iPhone 15 Pro', 6, '台', 7999.00, 6000.00, '256GB 深空黑色', '苹果最新款智能手机', 10, 1, '热销商品'),
('SKU002', 'MacBook Pro 14寸', 7, '台', 14999.00, 12000.00, 'M3 Pro芯片 16GB 512GB', '苹果专业笔记本电脑', 5, 1, '专业工作站'),
('SKU003', 'AirPods Pro 2', 6, '副', 1899.00, 1200.00, 'USB-C版', '苹果无线降噪耳机', 20, 1, '热销配件'),
('SKU004', 'iPad Air 5', 7, '台', 4799.00, 3500.00, '64GB WiFi版 蓝色', '苹果平板电脑', 15, 1, ''),
('SKU005', '小米14 Pro', 6, '台', 4999.00, 3500.00, '16GB 512GB 钛金属', '小米旗舰手机', 10, 1, '性价比之选'),
('SKU006', '华为MateBook X Pro', 7, '台', 8999.00, 7000.00, '16GB 1TB i7', '华为轻薄本', 5, 1, '');

INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status) VALUES
('FOOD001', '可口可乐 330ml', 11, '罐', 3.00, 2.00, 50, 1),
('FOOD002', '乐事薯片 原味', 11, '包', 8.00, 5.00, 30, 1),
('FOOD003', '农夫山泉 550ml', 11, '瓶', 2.00, 1.50, 100, 1);

INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status) VALUES
('CLOTH001', '纯棉T恤 白色', 14, '件', 59.00, 35.00, 20, 1),
('CLOTH002', '牛仔裤 男款', 14, '条', 199.00, 120.00, 10, 1);

-- 示例库存（初始为 0，与创建商品时自动初始化保持一致）
INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock)
SELECT id, 1, 0, warning_stock FROM t_product;
