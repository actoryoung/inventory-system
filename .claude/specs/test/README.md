# 商品管理模块测试快速指南

## 文件位置

### 后端测试
```
backend/src/test/java/com/inventory/
├── service/ProductServiceTest.java          # 服务层单元测试 (45+ tests)
├── controller/ProductControllerTest.java    # 控制器集成测试 (30+ tests)
└── resources/test-data-product.sql          # 测试数据 (100+ records)
```

### 前端测试
```
frontend/src/
├── views/product/__tests__/ProductList.spec.ts   # 组件测试 (35+ tests)
└── api/__tests__/productApi.spec.ts              # API测试 (40+ tests)
```

## 快速开始

### 运行所有测试
```bash
# 后端
cd backend && mvn test

# 前端
cd frontend && npm run test
```

### 运行单个测试文件
```bash
# 后端
mvn test -Dtest=ProductServiceTest

# 前端
npm run test ProductList.spec.ts
```

### 生成测试覆盖率报告
```bash
# 后端
mvn test jacoco:report

# 前端
npm run test:coverage
```

## 测试覆盖

| 类别 | 测试数量 | 覆盖率 |
|------|---------|--------|
| 后端单元测试 | 45+ | 85%+ |
| 后端集成测试 | 30+ | 80%+ |
| 前端组件测试 | 35+ | 75%+ |
| 前端API测试 | 40+ | 85%+ |

## 主要测试场景

### 商品创建
- ✅ 正常创建
- ✅ SKU重复校验
- ✅ 价格负数校验
- ✅ 分类有效性校验
- ✅ 库存自动初始化

### 商品更新
- ✅ 正常更新
- ✅ SKU唯一性保持
- ✅ 价格修改校验

### 商品删除
- ✅ 无关联删除
- ✅ 有库存记录阻止
- ✅ 有出入库记录阻止

### 商品查询
- ✅ 分页查询
- ✅ 名称模糊搜索
- ✅ SKU精确搜索
- ✅ 分类筛选
- ✅ 状态筛选

### 状态管理
- ✅ 启用/禁用切换
- ✅ 批量操作

## 测试数据

测试数据按功能分组：
- **正常数据**: ID 1000-1999 (6条)
- **批量数据**: ID 2001-2999 (10条)
- **边界数据**: ID 3001-3999 (6条)
- **关联数据**: ID 4001-4999 (4条)
- **搜索数据**: ID 5001-5999 (5条)
- **分页数据**: ID 6001-6999 (50条)
- **状态数据**: ID 7001-7999 (4条)
- **SKU数据**: ID 8001-8999 (3条)

**总计**: 100+ 条测试数据

## 故障排除

### 测试失败
1. 检查数据库连接
2. 确认测试数据已加载
3. 查看详细错误日志

### Mock 失败
1. 确认 Mock 对象正确配置
2. 验证 Mock 调用参数
3. 检查返回值设置

### 前端测试
1. 确认依赖已安装 (npm install)
2. 检查 Vue Test Utils 版本
3. 验证组件路径正确

## 详细文档

查看完整测试报告: [product_management_tests_summary.md](./product_management_tests_summary.md)
