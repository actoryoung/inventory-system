-- =====================================================
-- 库存管理测试数据 (Inventory Management Test Data)
-- =====================================================
--
-- 用途：为库存管理模块的单元测试和集成测试提供测试数据
-- 使用方式：
--   1. 单元测试：使用 Mock 对象，本文件仅供参考
--   2. 集成测试：在测试前执行此脚本初始化测试数据
--   3. 清理：测试后执行清理脚本恢复初始状态
--
-- =====================================================

-- 清理现有测试数据（如果存在）
DELETE FROM t_inventory WHERE product_id IN (
    SELECT id FROM t_product WHERE sku LIKE 'TEST_%'
);

DELETE FROM t_product WHERE sku LIKE 'TEST_%';

-- =====================================================
-- 测试场景 1: 正常库存商品
-- =====================================================

-- 测试商品 1: 正常库存商品
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_INV001', '测试商品-正常库存', 6, '台', 5999.00, 4500.00, 10, 1, '用于测试正常库存操作');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
(LAST_INSERT_ID(), 1, 100, 10);

-- 测试商品 2: 零库存商品
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_INV002', '测试商品-零库存', 6, '台', 3999.00, 3000.00, 10, 1, '用于测试零库存操作');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
(LAST_INSERT_ID(), 1, 0, 10);

-- 测试商品 3: 低库存商品（库存数量 = 预警值）
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_INV003', '测试商品-低库存', 6, '台', 2999.00, 2000.00, 10, 1, '用于测试低库存预警');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
(LAST_INSERT_ID(), 1, 10, 10);

-- 测试商品 4: 严重低库存商品（库存数量 < 预警值）
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_INV004', '测试商品-严重低库存', 6, '台', 1999.00, 1500.00, 10, 1, '用于测试严重低库存预警');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
(LAST_INSERT_ID(), 1, 5, 10);

-- =====================================================
-- 测试场景 2: 边界条件测试
-- =====================================================

-- 测试商品 5: 大量库存商品（用于测试大数值）
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_INV005', '测试商品-大量库存', 6, '台', 999.00, 500.00, 100, 1, '用于测试大量库存操作');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
(LAST_INSERT_ID(), 1, 100000, 100);

-- 测试商品 6: 库存为1（最小正值）
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_INV006', '测试商品-库存为1', 6, '台', 99.00, 50.00, 10, 1, '用于测试最小库存值');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
(LAST_INSERT_ID(), 1, 1, 10);

-- 测试商品 7: 预警值为0（无预警）
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_INV007', '测试商品-无预警', 6, '台', 799.00, 600.00, 0, 1, '用于测试无预警设置');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
(LAST_INSERT_ID(), 1, 50, 0);

-- =====================================================
-- 测试场景 3: 按分类统计测试数据
-- =====================================================

-- 电子产品分类测试数据
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_ELEC001', '测试电子产品A', 6, '台', 5999.00, 4500.00, 10, 1, '电子产品测试A'),
('TEST_ELEC002', '测试电子产品B', 6, '台', 4999.00, 3500.00, 10, 1, '电子产品测试B'),
('TEST_ELEC003', '测试电子产品C', 6, '台', 3999.00, 2500.00, 10, 1, '电子产品测试C');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
((SELECT id FROM t_product WHERE sku = 'TEST_ELEC001'), 1, 200, 10),
((SELECT id FROM t_product WHERE sku = 'TEST_ELEC002'), 1, 150, 10),
((SELECT id FROM t_product WHERE sku = 'TEST_ELEC003'), 1, 50, 10);

-- 电脑产品分类测试数据
INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_COMP001', '测试电脑产品A', 7, '台', 12999.00, 10000.00, 5, 1, '电脑产品测试A'),
('TEST_COMP002', '测试电脑产品B', 7, '台', 8999.00, 7000.00, 5, 1, '电脑产品测试B');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
((SELECT id FROM t_product WHERE sku = 'TEST_COMP001'), 1, 80, 5),
((SELECT id FROM t_product WHERE sku = 'TEST_COMP002'), 1, 20, 5);

-- =====================================================
-- 测试场景 4: 禁用商品测试（可选）
-- =====================================================

INSERT INTO t_product (sku, name, category_id, unit, price, cost_price, warning_stock, status, remark) VALUES
('TEST_INV999', '测试商品-已禁用', 6, '台', 6999.00, 5000.00, 10, 0, '用于测试禁用商品的库存');

INSERT INTO t_inventory (product_id, warehouse_id, quantity, warning_stock) VALUES
(LAST_INSERT_ID(), 1, 50, 10);

-- =====================================================
-- 数据验证查询
-- =====================================================

-- 查询所有测试商品
-- SELECT id, sku, name, category_id, status FROM t_product WHERE sku LIKE 'TEST_%' ORDER BY sku;

-- 查询所有测试库存
-- SELECT inv.id, p.sku, p.name, inv.quantity, inv.warning_stock
-- FROM t_inventory inv
-- JOIN t_product p ON inv.product_id = p.id
-- WHERE p.sku LIKE 'TEST_%'
-- ORDER BY p.sku;

-- 查询低库存测试商品
-- SELECT inv.id, p.sku, p.name, inv.quantity, inv.warning_stock,
--        CASE WHEN inv.quantity <= inv.warning_stock THEN '是' ELSE '否' END AS is_low_stock
-- FROM t_inventory inv
-- JOIN t_product p ON inv.product_id = p.id
-- WHERE p.sku LIKE 'TEST_%'
-- ORDER BY inv.quantity;

-- 按分类汇总测试数据
-- SELECT c.name AS category_name,
--        COUNT(*) AS product_count,
--        SUM(inv.quantity) AS total_quantity
-- FROM t_inventory inv
-- JOIN t_product p ON inv.product_id = p.id
-- JOIN t_category c ON p.category_id = c.id
-- WHERE p.sku LIKE 'TEST_%'
-- GROUP BY c.id, c.name
-- ORDER BY total_quantity DESC;

-- =====================================================
-- 清理脚本（测试完成后执行）
-- =====================================================

-- DELETE FROM t_inventory WHERE product_id IN (
--     SELECT id FROM t_product WHERE sku LIKE 'TEST_%'
-- );
-- DELETE FROM t_product WHERE sku LIKE 'TEST_%';
