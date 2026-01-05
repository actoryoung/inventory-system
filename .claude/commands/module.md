# /module

进销存系统模块快速开发命令。

## 描述

该命令帮助快速生成进销存系统的标准模块，包括前端页面、后端接口、数据库表等。

## 使用场景

- 快速创建商品管理模块
- 快速创建入库出库模块
- 快速创建报表统计模块

## 参数

| 参数 | 类型 | 必填 | 描述 | 可选值 |
|------|------|------|------|--------|
| action | string | 是 | 操作类型 | `create`, `list` |
| type | string | create 时必填 | 模块类型 | `product`, `inventory`, `inbound`, `outbound`, `report` |
| name | string | 否 | 模块名称（自定义） | - |

## 模块类型说明

### 1. product - 商品管理模块
**生成内容：**
- 前端：商品列表页、商品表单页、API 接口
- 后端：Product 实体、Controller、Service、Mapper
- 数据库：t_product 表 SQL

### 2. inventory - 库存管理模块
**生成内容：**
- 前端：库存列表页、库存调整页、API 接口
- 后端：Inventory 实体、Controller、Service、Mapper
- 数据库：t_inventory 表 SQL

### 3. inbound - 入库管理模块
**生成内容：**
- 前端：入库单列表页、入库单表单页、API 接口
- 后端：Inbound 实体、Controller、Service、Mapper
- 数据库：t_inbound 表 SQL
- 业务逻辑：入库自动增加库存

### 4. outbound - 出库管理模块
**生成内容：**
- 前端：出库单列表页、出库单表单页、API 接口
- 后端：Outbound 实体、Controller、Service、Mapper
- 数据库：t_outbound 表 SQL
- 业务逻辑：出库自动减少库存（库存校验）

### 5. report - 报表统计模块
**生成内容：**
- 前端：数据看板、库存报表、出入库报表
- 后端：ReportService、统计查询
- 图表：ECharts 配置

## 执行流程

### 创建模块 (`action: create`)

```
1. 确认模块类型
   ↓
2. 调用 inventory-generator 生成代码
   ↓
3. 创建前端文件
   ↓
4. 创建后端文件
   ↓
5. 生成数据库表 SQL
   ↓
6. 返回创建清单
```

### 示例用法

```
用户: /module action=create type=product

Claude: 正在创建商品管理模块...

[调用 inventory-generator skill]

✓ 前端文件：
  - frontend/src/views/product/ProductList.vue
  - frontend/src/views/product/ProductForm.vue
  - frontend/src/api/product.ts

✓ 后端文件：
  - backend/src/main/java/com/inventory/entity/Product.java
  - backend/src/main/java/com/inventory/controller/ProductController.java
  - backend/src/main/java/com/inventory/service/ProductService.java

✓ 数据库：
  - backend/src/main/resources/sql/product.sql

模块创建完成！下一步：执行 product.sql 初始化数据库表
```

## 相关资源

- `.claude/skills/inventory-generator.js` - 代码生成器
- `.claude/agents/inventory-writer.md` - 商品库存代理
- `.claude/agents/inout-writer.md` - 入库出库代理
