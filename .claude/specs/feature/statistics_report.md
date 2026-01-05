# 统计报表功能规范

> 模块名称：统计报表 (Statistics & Reports)
> 版本：v1.0
> 创建日期：2026-01-04
> 状态：待实现

---

## 1. 功能概述

统计报表模块提供数据可视化和统计分析功能，帮助用户了解库存状况、出入库趋势等关键业务指标。

### 核心功能
- 数据看板（关键指标卡片）
- 出入库趋势图表
- 库存分类占比分析
- 低库存预警列表
- 数据导出功能

---

## 2. 功能详情

### 2.1 数据看板

展示关键业务指标，包括：

| 指标 | 说明 | 数据来源 |
|------|------|----------|
| 总商品数 | 系统中启用状态的商品总数 | t_product |
| 总库存量 | 所有商品的库存数量总和 | t_inventory |
| 库存总额 | 所有商品的成本金额总和 | t_inventory × t_product.cost_price |
| 低库存数量 | 库存低于预警值的商品数 | t_inventory |

### 2.2 出入库趋势图

**图表类型：** 折线图 / 柱状图

**数据维度：**
- 时间范围：最近30天
- 数据项：每日入库数量、每日出库数量
- X轴：日期
- Y轴：数量

**数据来源：** t_inbound、t_outbound（已审核的记录）

### 2.3 库存分类占比

**图表类型：** 饼图

**数据维度：**
- 分类：一级分类
- 数值：该分类下商品的库存数量总和
- 显示：分类名称、数量、占比

**数据来源：** t_inventory JOIN t_product JOIN t_category

### 2.4 低库存预警列表

展示所有库存低于预警值的商品：

| 列 | 说明 |
|------|------|
| 商品SKU | 商品编码 |
| 商品名称 | 商品名称 |
| 分类 | 所属分类 |
| 当前库存 | 实际库存数量 |
| 预警值 | 预警库存值 |
| 差值 | 预警值 - 当前库存 |
| 状态 | 低库存（红色标签） |

### 2.5 数据导出

支持导出功能：
- 导出格式：Excel (.xlsx)
- 导出内容：库存汇总报表
- 包含字段：商品SKU、商品名称、分类、库存数量、成本价、库存金额

---

## 3. API 接口设计

### 3.1 获取看板数据
```
GET /api/statistics/dashboard
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalProducts": 100,
    "totalQuantity": 50000,
    "totalAmount": 1000000.00,
    "lowStockCount": 5
  }
}
```

### 3.2 获取出入库趋势
```
GET /api/statistics/trend?days=30
```

**查询参数：**
- `days`: 天数（默认30，最大90）

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dates": ["2026-01-01", "2026-01-02", ...],
    "inboundQuantities": [100, 200, ...],
    "outboundQuantities": [50, 80, ...]
  }
}
```

### 3.3 获取库存分类占比
```
GET /api/statistics/category-distribution
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "categoryName": "电子产品",
      "quantity": 10000,
      "percentage": 40.5
    },
    ...
  ]
}
```

### 3.4 获取低库存列表
```
GET /api/statistics/low-stock
```

**响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "productId": 1,
      "productSku": "SKU001",
      "productName": "商品A",
      "categoryName": "分类1",
      "quantity": 5,
      "warningStock": 20,
      "shortage": 15
    },
    ...
  ]
}
```

---

## 4. 前端页面设计

### 4.1 页面布局

```
┌─────────────────────────────────────────────────┐
│                    统计报表                      │
├─────────────────────────────────────────────────┤
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐        │
│  │总商品数│  │总库存量│  │库存总额│  │低库存数│       │  <- 指标卡片
│  └──────┘  └──────┘  └──────┘  └──────┘        │
├─────────────────────────────────────────────────┤
│  ┌──────────────────────┐  ┌─────────────────┐  │
│  │    出入库趋势图       │  │   库存分类占比   │  │  <- 图表
│  │                      │  │                 │  │
│  └──────────────────────┘  └─────────────────┘  │
├─────────────────────────────────────────────────┤
│  低库存预警列表                                    │  <- 数据表格
│  ┌───────────────────────────────────────────┐  │
│  │ SKU  │ 名称 │ 分类 │ 库存 │ 预警 │ 差值 │  │
│  ├──────┼──────┼──────┼──────┼──────┼──────┤  │
│  │ ...  │ ...  │ ...  │ ...  │ ...  │ ...  │  │
│  └───────────────────────────────────────────┘  │
│                                         [导出]  │
└─────────────────────────────────────────────────┘
```

### 4.2 组件结构

```
views/statistics/
└── Statistics.vue           # 统计报表主页面
    ├── DashboardCards       # 指标卡片组件
    ├── TrendChart           # 趋势图表组件
    ├── CategoryChart        # 分类占比组件
    └── LowStockTable        # 低库存列表组件
```

### 4.3 图表配置

**使用 ECharts 5.x**

**趋势图配置：**
```javascript
{
  title: { text: '出入库趋势（近30天）' },
  tooltip: { trigger: 'axis' },
  legend: { data: ['入库数量', '出库数量'] },
  xAxis: { type: 'category', data: dates },
  yAxis: { type: 'value' },
  series: [
    { name: '入库数量', type: 'line', data: inboundQuantities },
    { name: '出库数量', type: 'line', data: outboundQuantities }
  ]
}
```

**饼图配置：**
```javascript
{
  title: { text: '库存分类占比' },
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical' },
  series: [{
    type: 'pie',
    data: categoryData,
    label: {
      formatter: '{b}: {c} ({d}%)'
    }
  }]
}
```

---

## 5. 数据查询逻辑

### 5.1 看板数据查询

```sql
-- 总商品数
SELECT COUNT(*) FROM t_product WHERE status = 1;

-- 总库存量
SELECT SUM(quantity) FROM t_inventory;

-- 库存总额
SELECT SUM(i.quantity * p.cost_price)
FROM t_inventory i
JOIN t_product p ON i.product_id = p.id;

-- 低库存数量
SELECT COUNT(*)
FROM t_inventory
WHERE quantity < warning_stock;
```

### 5.2 出入库趋势查询

```sql
-- 入库趋势（按日期分组）
SELECT
    DATE(inbound_date) as date,
    SUM(quantity) as total_quantity
FROM t_inbound
WHERE status = 1
    AND inbound_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(inbound_date)
ORDER BY date;

-- 出库趋势（按日期分组）
SELECT
    DATE(outbound_date) as date,
    SUM(quantity) as total_quantity
FROM t_outbound
WHERE status = 1
    AND outbound_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(outbound_date)
ORDER BY date;
```

### 5.3 库存分类占比查询

```sql
SELECT
    c.name as category_name,
    SUM(i.quantity) as total_quantity
FROM t_inventory i
JOIN t_product p ON i.product_id = p.id
JOIN t_category c ON p.category_id = c.id
GROUP BY c.id, c.name
ORDER BY total_quantity DESC;
```

---

## 6. 前端依赖

需要安装以下依赖：

```json
{
  "dependencies": {
    "echarts": "^5.4.3"
  }
}
```

安装命令：
```bash
cd frontend
npm install echarts
```

---

## 7. 异常处理

### 7.1 业务异常

| 异常场景 | 处理方式 |
|----------|----------|
| 无数据 | 显示"暂无数据"提示 |
| 查询失败 | 显示错误提示，使用缓存数据（如有） |
| 导出失败 | 提示用户重试 |

### 7.2 边界条件

- 天数参数范围：1-90天
- 空数据时的图表占位
- 大数据量时的分页处理

---

## 8. 测试用例

### 8.1 正常场景
- 访问统计页面，正确加载所有数据
- 图表正确渲染，数据准确
- 低库存列表正确显示
- 导出功能正常

### 8.2 异常场景
- 无数据时的展示
- API失败时的降级处理
- 导出失败时的错误提示

---

## 9. 实现优先级

| 优先级 | 功能 |
|--------|------|
| P0 | 数据看板（指标卡片） |
| P0 | 低库存预警列表 |
| P1 | 出入库趋势图 |
| P1 | 库存分类占比图 |
| P2 | 数据导出功能 |

---

## 10. 验收标准

- [ ] 看板4个指标正确显示
- [ ] 趋势图正确展示近30天出入库数据
- [ ] 饼图正确展示分类占比
- [ ] 低库存列表正确显示所有低于预警值的商品
- [ ] 图表支持交互（鼠标悬停显示数值）
- [ ] 数据自动刷新（手动刷新按钮）
- [ ] 导出功能正常

---

**文档版本历史**
- v1.0 (2026-01-04): 初始版本
