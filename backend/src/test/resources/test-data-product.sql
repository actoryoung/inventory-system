-- ============================================================================
-- 商品管理模块测试数据
-- Test Data for Product Management Module
-- ============================================================================
-- 说明：此文件包含商品管理模块的所有测试数据
-- 使用方法：在测试前执行此脚本初始化测试数据
-- ============================================================================

-- 清理测试数据（如果存在）
DELETE FROM t_inventory WHERE product_id IN (SELECT id FROM t_product WHERE sku LIKE 'TEST_%');
DELETE FROM t_product WHERE sku LIKE 'TEST_%';

-- ============================================================================
-- 1. 分类测试数据
-- ============================================================================
-- 注意：假设 t_category 表已存在，这里只插入测试用的分类

INSERT INTO t_category (id, name, parent_id, sort, status, created_at, updated_at) VALUES
(100, '测试分类-电子产品', NULL, 1, 1, NOW(), NOW()),
(101, '测试分类-手机配件', 100, 1, 1, NOW(), NOW()),
(102, '测试分类-已禁用分类', NULL, 2, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- 2. 正常商品测试数据
-- ============================================================================

-- 商品1：完整信息的正常商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, specification, description, warning_stock, status, remark, created_at, updated_at) VALUES
(1000, 'TEST_PROD_001', '测试商品-完整信息', 100, '台', 5999.00, 4500.00, '标准版', '这是一个完整的测试商品', 10, 1, '测试用', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 商品2：最小必填项商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(1001, 'TEST_PROD_002', '测试商品-最小信息', 100, '件', 100.00, 0.00, 0, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 商品3：禁用状态的商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(1002, 'TEST_PROD_003', '测试商品-已禁用', 100, '盒', 50.00, 30.00, 5, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 商品4：预警库存为0的商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(1003, 'TEST_PROD_004', '测试商品-零预警', 101, '个', 10.00, 5.00, 0, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 商品5：高价值商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(1004, 'TEST_PROD_005', '测试商品-高价值', 100, '台', 99999.99, 80000.00, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 商品6：已禁用分类的商品（业务异常数据）
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(1005, 'TEST_PROD_006', '测试商品-已禁用分类', 102, '件', 20.00, 10.00, 5, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- 3. 批量操作测试数据
-- ============================================================================

-- 批量测试商品（10条）
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(2001, 'TEST_BATCH_001', '批量测试商品1', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(2002, 'TEST_BATCH_002', '批量测试商品2', 100, '件', 200.00, 100.00, 10, 1, NOW(), NOW()),
(2003, 'TEST_BATCH_003', '批量测试商品3', 100, '件', 300.00, 150.00, 10, 1, NOW(), NOW()),
(2004, 'TEST_BATCH_004', '批量测试商品4', 101, '件', 400.00, 200.00, 10, 1, NOW(), NOW()),
(2005, 'TEST_BATCH_005', '批量测试商品5', 101, '件', 500.00, 250.00, 10, 1, NOW(), NOW()),
(2006, 'TEST_BATCH_006', '批量测试商品6', 101, '件', 600.00, 300.00, 10, 1, NOW(), NOW()),
(2007, 'TEST_BATCH_007', '批量测试商品7', 100, '件', 700.00, 350.00, 10, 1, NOW(), NOW()),
(2008, 'TEST_BATCH_008', '批量测试商品8', 100, '件', 800.00, 400.00, 10, 1, NOW(), NOW()),
(2009, 'TEST_BATCH_009', '批量测试商品9', 101, '件', 900.00, 450.00, 10, 1, NOW(), NOW()),
(2010, 'TEST_BATCH_010', '批量测试商品10', 101, '件', 1000.00, 500.00, 10, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- 4. 边界条件测试数据
-- ============================================================================

-- 价格边界测试
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(3001, 'TEST_BOUNDARY_001', '边界测试-价格0', 100, '件', 0.00, 0.00, 0, 1, NOW(), NOW()),
(3002, 'TEST_BOUNDARY_002', '边界测试-价格0.01', 100, '件', 0.01, 0.00, 0, 1, NOW(), NOW()),
(3003, 'TEST_BOUNDARY_003', '边界测试-价格最大值', 100, '件', 99999999.99, 99999999.99, 0, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 预警库存边界测试
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(3004, 'TEST_BOUNDARY_004', '边界测试-预警0', 100, '件', 100.00, 50.00, 0, 1, NOW(), NOW()),
(3005, 'TEST_BOUNDARY_005', '边界测试-预警1', 100, '件', 100.00, 50.00, 1, 1, NOW(), NOW()),
(3006, 'TEST_BOUNDARY_006', '边界测试-预警大值', 100, '件', 100.00, 50.00, 999999, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- 5. 关联测试数据（用于删除约束测试）
-- ============================================================================

-- 商品7：有库存记录的商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(4001, 'TEST_ASSOC_001', '测试商品-有库存', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 初始化该商品的库存（模拟有库存记录）
INSERT INTO t_inventory (id, product_id, warehouse_id, quantity, warning_stock, created_at, updated_at) VALUES
(50001, 4001, 1, 100, 10, NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- 商品8：有入库记录的商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(4002, 'TEST_ASSOC_002', '测试商品-有入库', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 初始化该商品的入库记录（模拟有入库记录）
INSERT INTO t_inbound (id, inbound_no, product_id, quantity, supplier, inbound_date, status, created_at, updated_at) VALUES
(60001, 'IN20260104001', 4002, 50, '测试供应商', NOW(), 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- 商品9：有出库记录的商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(4003, 'TEST_ASSOC_003', '测试商品-有出库', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 初始化该商品的出库记录（模拟有出库记录）
INSERT INTO t_outbound (id, outbound_no, product_id, quantity, receiver, outbound_date, status, created_at, updated_at) VALUES
(70001, 'OUT20260104001', 4003, 30, '测试客户', NOW(), 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE quantity=VALUES(quantity);

-- 商品10：无任何关联的商品（可正常删除）
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(4004, 'TEST_ASSOC_004', '测试商品-可删除', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- 6. 搜索功能测试数据
-- ============================================================================

-- 搜索测试商品（包含不同名称）
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(5001, 'TEST_SEARCH_IPHONE', 'iPhone 15 Pro Max', 100, '台', 9999.00, 8000.00, 5, 1, NOW(), NOW()),
(5002, 'TEST_SEARCH_MACBOOK', 'MacBook Pro M3', 100, '台', 15999.00, 12000.00, 3, 1, NOW(), NOW()),
(5003, 'TEST_SEARCH_AIRPODS', 'AirPods Pro 2', 101, '副', 1999.00, 1200.00, 20, 1, NOW(), NOW()),
(5004, 'TEST_SEARCH_IPAD', 'iPad Air 5', 100, '台', 4799.00, 3500.00, 10, 1, NOW(), NOW()),
(5005, 'TEST_SEARCH_WATCH', 'Apple Watch Series 9', 101, '块', 3199.00, 2500.00, 15, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- 7. 分页测试数据（总计50条）
-- ============================================================================

-- 生成50条测试商品数据
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(6001, 'TEST_PAGE_001', '分页测试商品1', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6002, 'TEST_PAGE_002', '分页测试商品2', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6003, 'TEST_PAGE_003', '分页测试商品3', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6004, 'TEST_PAGE_004', '分页测试商品4', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6005, 'TEST_PAGE_005', '分页测试商品5', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6006, 'TEST_PAGE_006', '分页测试商品6', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6007, 'TEST_PAGE_007', '分页测试商品7', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6008, 'TEST_PAGE_008', '分页测试商品8', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6009, 'TEST_PAGE_009', '分页测试商品9', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6010, 'TEST_PAGE_010', '分页测试商品10', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6011, 'TEST_PAGE_011', '分页测试商品11', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6012, 'TEST_PAGE_012', '分页测试商品12', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6013, 'TEST_PAGE_013', '分页测试商品13', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6014, 'TEST_PAGE_014', '分页测试商品14', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6015, 'TEST_PAGE_015', '分页测试商品15', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6016, 'TEST_PAGE_016', '分页测试商品16', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6017, 'TEST_PAGE_017', '分页测试商品17', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6018, 'TEST_PAGE_018', '分页测试商品18', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6019, 'TEST_PAGE_019', '分页测试商品19', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6020, 'TEST_PAGE_020', '分页测试商品20', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6021, 'TEST_PAGE_021', '分页测试商品21', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6022, 'TEST_PAGE_022', '分页测试商品22', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6023, 'TEST_PAGE_023', '分页测试商品23', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6024, 'TEST_PAGE_024', '分页测试商品24', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6025, 'TEST_PAGE_025', '分页测试商品25', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6026, 'TEST_PAGE_026', '分页测试商品26', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6027, 'TEST_PAGE_027', '分页测试商品27', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6028, 'TEST_PAGE_028', '分页测试商品28', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6029, 'TEST_PAGE_029', '分页测试商品29', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6030, 'TEST_PAGE_030', '分页测试商品30', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6031, 'TEST_PAGE_031', '分页测试商品31', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6032, 'TEST_PAGE_032', '分页测试商品32', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6033, 'TEST_PAGE_033', '分页测试商品33', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6034, 'TEST_PAGE_034', '分页测试商品34', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6035, 'TEST_PAGE_035', '分页测试商品35', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6036, 'TEST_PAGE_036', '分页测试商品36', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6037, 'TEST_PAGE_037', '分页测试商品37', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6038, 'TEST_PAGE_038', '分页测试商品38', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6039, 'TEST_PAGE_039', '分页测试商品39', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6040, 'TEST_PAGE_040', '分页测试商品40', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6041, 'TEST_PAGE_041', '分页测试商品41', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6042, 'TEST_PAGE_042', '分页测试商品42', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6043, 'TEST_PAGE_043', '分页测试商品43', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6044, 'TEST_PAGE_044', '分页测试商品44', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6045, 'TEST_PAGE_045', '分页测试商品45', 101, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6046, 'TEST_PAGE_046', '分页测试商品46', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6047, 'TEST_PAGE_047', '分页测试商品47', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6048, 'TEST_PAGE_048', '分页测试商品48', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6049, 'TEST_PAGE_049', '分页测试商品49', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(6050, 'TEST_PAGE_050', '分页测试商品50', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- 8. 状态测试数据
-- ============================================================================

-- 不同状态的商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(7001, 'TEST_STATUS_ENABLED_01', '状态测试-启用1', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(7002, 'TEST_STATUS_ENABLED_02', '状态测试-启用2', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(7003, 'TEST_STATUS_DISABLED_01', '状态测试-禁用1', 100, '件', 100.00, 50.00, 10, 0, NOW(), NOW()),
(7004, 'TEST_STATUS_DISABLED_02', '状态测试-禁用2', 100, '件', 100.00, 50.00, 10, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- 9. SKU唯一性测试数据
-- ============================================================================

-- 用于测试SKU唯一性的商品
INSERT INTO t_product (id, sku, name, category_id, unit, price, cost_price, warning_stock, status, created_at, updated_at) VALUES
(8001, 'TEST_UNIQUE_SKU_001', 'SKU唯一性测试1', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(8002, 'TEST_UNIQUE_SKU_002', 'SKU唯一性测试2', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW()),
(8003, 'TEST_UNIQUE_SKU_003', 'SKU唯一性测试3', 100, '件', 100.00, 50.00, 10, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================================
-- 测试数据统计查询（用于验证）
-- ============================================================================

-- 统计查询：总商品数
-- SELECT COUNT(*) as total_products FROM t_product WHERE sku LIKE 'TEST_%';

-- 统计查询：启用商品数
-- SELECT COUNT(*) as enabled_products FROM t_product WHERE sku LIKE 'TEST_%' AND status = 1;

-- 统计查询：禁用商品数
-- SELECT COUNT(*) as disabled_products FROM t_product WHERE sku LIKE 'TEST_%' AND status = 0;

-- 统计查询：有库存记录的商品数
-- SELECT COUNT(*) as products_with_inventory FROM t_product p
-- INNER JOIN t_inventory i ON p.id = i.product_id
-- WHERE p.sku LIKE 'TEST_%';

-- ============================================================================
-- 说明：测试数据使用说明
-- ============================================================================
--
-- 1. 正常场景测试数据：ID范围 1000-1999
--    - 完整信息商品、最小信息商品、禁用商品等
--
-- 2. 批量操作测试数据：ID范围 2001-2999
--    - 10条商品用于批量删除、批量导入等测试
--
-- 3. 边界条件测试数据：ID范围 3001-3999
--    - 价格边界、预警库存边界等
--
-- 4. 关联测试数据：ID范围 4001-4999
--    - 有库存记录、有入库/出库记录的商品（用于删除约束测试）
--
-- 5. 搜索功能测试数据：ID范围 5001-5999
--    - 包含不同名称的商品用于搜索测试
--
-- 6. 分页测试数据：ID范围 6001-6999
--    - 50条商品用于分页查询测试
--
-- 7. 状态测试数据：ID范围 7001-7999
--    - 启用/禁用状态的商品用于状态切换测试
--
-- 8. SKU唯一性测试数据：ID范围 8001-8999
--    - 用于测试SKU唯一性约束
--
-- 测试完成后，可使用以下SQL清理所有测试数据：
-- DELETE FROM t_inventory WHERE product_id IN (SELECT id FROM t_product WHERE sku LIKE 'TEST_%');
-- DELETE FROM t_inbound WHERE product_id IN (SELECT id FROM t_product WHERE sku LIKE 'TEST_%');
-- DELETE FROM t_outbound WHERE product_id IN (SELECT id FROM t_product WHERE sku LIKE 'TEST_%');
-- DELETE FROM t_product WHERE sku LIKE 'TEST_%';
-- DELETE FROM t_category WHERE name LIKE '测试分类%';
-- ============================================================================
