-- ================================================================================
-- 商品分类模块测试数据
-- ================================================================================
-- 用途: 单元测试、集成测试的测试数据
-- 创建日期: 2026-01-04
-- 说明: 包含一级、二级、三级分类数据，以及边界测试数据
-- ================================================================================

-- 清理测试数据（如果存在）
DELETE FROM t_product WHERE category_id IN (SELECT id FROM t_category WHERE id >= 1000);
DELETE FROM t_category WHERE id >= 1000;

-- ================================================================================
-- 1. 一级分类测试数据
-- ================================================================================
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1001, '电子产品', NULL, 1, 1, 1, NOW(), NOW()),
(1002, '食品饮料', NULL, 1, 2, 1, NOW(), NOW()),
(1003, '服装鞋帽', NULL, 1, 3, 1, NOW(), NOW()),
(1004, '家居用品', NULL, 1, 4, 0, NOW(), NOW()),  -- 禁用状态
(1005, '图书文具', NULL, 1, 5, 1, NOW(), NOW());

-- ================================================================================
-- 2. 二级分类测试数据
-- ================================================================================
-- 电子产品子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1101, '手机', 1001, 2, 1, 1, NOW(), NOW()),
(1102, '电脑', 1001, 2, 2, 1, NOW(), NOW()),
(1103, '配件', 1001, 2, 3, 1, NOW(), NOW()),
(1104, '平板', 1001, 2, 4, 0, NOW(), NOW()),  -- 禁用状态
(1105, '智能穿戴', 1001, 2, 5, 1, NOW(), NOW());

-- 食品饮料子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1201, '零食', 1002, 2, 1, 1, NOW(), NOW()),
(1202, '饮料', 1002, 2, 2, 1, NOW(), NOW()),
(1203, '生鲜', 1002, 2, 3, 1, NOW(), NOW());

-- 服装鞋帽子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1301, '男装', 1003, 2, 1, 1, NOW(), NOW()),
(1302, '女装', 1003, 2, 2, 1, NOW(), NOW()),
(1303, '童装', 1003, 2, 3, 1, NOW(), NOW()),
(1304, '鞋靴', 1003, 2, 4, 1, NOW(), NOW());

-- 图书文具子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1501, '图书', 1005, 2, 1, 1, NOW(), NOW()),
(1502, '文具', 1005, 2, 2, 1, NOW(), NOW());

-- ================================================================================
-- 3. 三级分类测试数据
-- ================================================================================
-- 手机子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1111, '智能手机', 1101, 3, 1, 1, NOW(), NOW()),
(1112, '功能手机', 1101, 3, 2, 1, NOW(), NOW()),
(1113, '游戏手机', 1101, 3, 3, 0, NOW(), NOW());  -- 禁用状态

-- 电脑子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1121, '笔记本', 1102, 3, 1, 1, NOW(), NOW()),
(1122, '台式机', 1102, 3, 2, 1, NOW(), NOW()),
(1123, '平板电脑', 1102, 3, 3, 1, NOW(), NOW());

-- 配件子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1131, '充电器', 1103, 3, 1, 1, NOW(), NOW()),
(1132, '耳机', 1103, 3, 2, 1, NOW(), NOW()),
(1133, '保护壳', 1103, 3, 3, 1, NOW(), NOW()),
(1134, '数据线', 1103, 3, 4, 1, NOW(), NOW());

-- 零食子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1211, '薯片', 1201, 3, 1, 1, NOW(), NOW()),
(1212, '饼干', 1201, 3, 2, 1, NOW(), NOW()),
(1213, '糖果', 1201, 3, 3, 1, NOW(), NOW());

-- 饮料子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1221, '碳酸饮料', 1202, 3, 1, 1, NOW(), NOW()),
(1222, '果汁', 1202, 3, 2, 1, NOW(), NOW()),
(1223, '茶饮', 1202, 3, 3, 1, NOW(), NOW());

-- 男装子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1311, 'T恤', 1301, 3, 1, 1, NOW(), NOW()),
(1312, '衬衫', 1301, 3, 2, 1, NOW(), NOW()),
(1313, '牛仔裤', 1301, 3, 3, 1, NOW(), NOW());

-- 女装子分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(1321, '连衣裙', 1302, 3, 1, 1, NOW(), NOW()),
(1322, '套装', 1302, 3, 2, 1, NOW(), NOW()),
(1323, '半身裙', 1302, 3, 3, 1, NOW(), NOW());

-- ================================================================================
-- 4. 边界测试数据
-- ================================================================================
-- 分类名称长度边界测试
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(2001, 'A', NULL, 1, 100, 1, NOW(), NOW()),  -- 最短名称（1个字符）
(2002, '12345678901234567890123456789012345678901234567890', NULL, 1, 101, 1, NOW(), NOW());  -- 最长名称（50个字符）

-- 特殊字符测试
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(2003, '手机/平板', NULL, 1, 102, 1, NOW(), NOW()),
(2004, '服装（新款）', NULL, 1, 103, 1, NOW(), NOW()),
(2005, '图书&音像', NULL, 1, 104, 1, NOW(), NOW());

-- 排序边界测试
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(2006, '测试分类1', NULL, 1, -1, 1, NOW(), NOW()),  -- 负数排序
(2007, '测试分类2', NULL, 1, 0, 1, NOW(), NOW()),   -- 零值排序
(2008, '测试分类3', NULL, 1, 9999, 1, NOW(), NOW());  -- 极大值排序

-- ================================================================================
-- 5. 关联商品测试数据（用于删除约束测试）
-- ================================================================================
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(3001, '有商品关联的分类', NULL, 1, 200, 1, NOW(), NOW()),
(3002, '无商品关联的分类', NULL, 1, 201, 1, NOW(), NOW()),
(3003, '有子分类的分类', NULL, 1, 202, 1, NOW(), NOW());

INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(3010, '子分类A', 3003, 2, 1, 1, NOW(), NOW());

-- 插入测试商品（关联到分类3001）
INSERT INTO t_product (id, sku, name, category_id, price, cost_price, unit, warning_stock, status, created_at, updated_at) VALUES
(9001, 'TEST001', '测试商品1', 3001, 99.00, 50.00, '件', 10, 1, NOW(), NOW()),
(9002, 'TEST002', '测试商品2', 3001, 199.00, 100.00, '件', 5, 1, NOW(), NOW()),
(9003, 'TEST003', '测试商品3', 3001, 299.00, 150.00, '件', 20, 1, NOW(), NOW());

-- ================================================================================
-- 6. 名称唯一性测试数据
-- ================================================================================
-- 不同父分类下的同名分类（应允许）
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(4001, '通用分类', NULL, 1, 300, 1, NOW(), NOW()),
(4002, '通用分类', 1001, 2, 300, 1, NOW(), NOW()),  -- 父分类不同，允许同名
(4003, '通用分类', 1002, 2, 301, 1, NOW(), NOW());  -- 父分类不同，允许同名

-- ================================================================================
-- 7. 层级结构完整性测试数据
-- ================================================================================
-- 完整的三级分类链
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(5001, '一级测试分类A', NULL, 1, 400, 1, NOW(), NOW()),
(5011, '二级测试分类A1', 5001, 2, 1, 1, NOW(), NOW()),
(5012, '二级测试分类A2', 5001, 2, 2, 1, NOW(), NOW()),
(5021, '三级测试分类A1-1', 5011, 3, 1, 1, NOW(), NOW()),
(5022, '三级测试分类A1-2', 5011, 3, 2, 1, NOW(), NOW()),
(5023, '三级测试分类A2-1', 5012, 3, 1, 1, NOW(), NOW());

-- ================================================================================
-- 8. 性能测试数据（大量分类数据）
-- ================================================================================
-- 创建大量一级分类（用于性能测试）
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at)
SELECT
    6000 + seq,
    CONCAT('性能测试分类', seq),
    NULL,
    1,
    seq,
    1,
    NOW(),
    NOW()
FROM (
    SELECT @row := @row + 1 AS seq
    FROM (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
          SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
         (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
          SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t2,
         (SELECT @row := 0) t3
    LIMIT 100
) seq_table;

-- 为前10个性能测试分类创建二级分类
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at)
SELECT
    7000 + seq,
    CONCAT('子分类', seq),
    6000 + FLOOR((seq - 1) / 5) + 1,
    2,
    seq,
    1,
    NOW(),
    NOW()
FROM (
    SELECT @row2 := @row2 + 1 AS seq
    FROM (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
          SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) t1,
         (SELECT @row2 := 0) t2
    LIMIT 50
) seq_table;

-- ================================================================================
-- 9. 状态切换测试数据
-- ================================================================================
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(8001, '待启用分类', NULL, 1, 500, 0, NOW(), NOW()),  -- 初始禁用
(8002, '待禁用分类', NULL, 1, 501, 1, NOW(), NOW());  -- 初始启用

-- ================================================================================
-- 10. 统计测试数据
-- ================================================================================
-- 用于测试分类统计功能的数据
INSERT INTO t_category (id, name, parent_id, level, sort_order, status, created_at, updated_at) VALUES
(9001, '统计测试分类1', NULL, 1, 600, 1, NOW(), NOW()),
(9002, '统计测试分类2', NULL, 1, 601, 1, NOW(), NOW()),
(9003, '统计测试分类3', NULL, 1, 602, 0, NOW(), NOW());

-- ================================================================================
-- 数据验证查询
-- ================================================================================

-- 验证一级分类数量
-- SELECT COUNT(*) as '一级分类数量' FROM t_category WHERE level = 1 AND parent_id IS NULL;
-- 预期结果: >= 120

-- 验证二级分类数量
-- SELECT COUNT(*) as '二级分类数量' FROM t_category WHERE level = 2;
-- 预期结果: >= 30

-- 验证三级分类数量
-- SELECT COUNT(*) as '三级分类数量' FROM t_category WHERE level = 3;
-- 预期结果: >= 20

-- 验证启用状态分类数量
-- SELECT COUNT(*) as '启用分类数量' FROM t_category WHERE status = 1;
-- 预期结果: 大部分分类

-- 验证禁用状态分类数量
-- SELECT COUNT(*) as '禁用分类数量' FROM t_category WHERE status = 0;
-- 预期结果: 少量分类

-- 验证有关联商品的分类
-- SELECT COUNT(DISTINCT c.id) as '有商品关联分类数量'
-- FROM t_category c
-- INNER JOIN t_product p ON p.category_id = c.id
-- WHERE c.id >= 1000;
-- 预期结果: 1 (分类3001)

-- 验证有子分类的分类
-- SELECT COUNT(DISTINCT parent_id) as '有子分类的分类数量'
-- FROM t_category
-- WHERE parent_id IS NOT NULL AND parent_id >= 1000;
-- 预期结果: >= 15

-- 查看分类树结构示例
-- SELECT
--     c1.id as '一级ID',
--     c1.name as '一级名称',
--     c2.id as '二级ID',
--     c2.name as '二级名称',
--     c3.id as '三级ID',
--     c3.name as '三级名称'
-- FROM t_category c1
-- LEFT JOIN t_category c2 ON c2.parent_id = c1.id
-- LEFT JOIN t_category c3 ON c3.parent_id = c2.id
-- WHERE c1.level = 1 AND c1.id BETWEEN 1001 AND 1005
-- ORDER BY c1.id, c2.id, c3.id
-- LIMIT 20;

-- ================================================================================
-- 测试数据使用说明
-- ================================================================================
-- 1. 基础功能测试:
--    - 使用分类ID 1001-1999: 测试基本CRUD操作
--    - 使用分类ID 2001-2999: 测试边界条件
--    - 使用分类ID 3001-3999: 测试删除约束
--    - 使用分类ID 4001-4999: 测试名称唯一性
--    - 使用分类ID 5001-5999: 测试层级结构
--    - 使用分类ID 6001-6999: 测试性能（大量数据）
--    - 使用分类ID 8001-8999: 测试状态切换
--    - 使用分类ID 9001-9999: 测试统计功能
--
-- 2. 测试前准备:
--    - 执行完整脚本以初始化所有测试数据
--    - 或根据测试需要选择性执行特定部分的SQL
--
-- 3. 测试后清理:
--    - DELETE FROM t_product WHERE category_id >= 1000;
--    - DELETE FROM t_category WHERE id >= 1000;
--
-- 4. 注意事项:
--    - 所有测试数据ID从1000开始，避免与生产数据冲突
--    - 商品数据ID从9000开始，避免与分类数据冲突
--    - 性能测试数据会生成大量记录，根据需要调整数量
--    - 每次测试前确保数据已清理，避免数据污染
-- ================================================================================
