# 进销存管理系统 (Inventory Management System)

> 基于 AipexBase 的进销存管理演示项目
> 技术栈：Vue 3 + Element Plus + Spring Boot + MyBatis-Plus + MySQL

## 项目概述

这是一个完整的进销存管理系统演示项目，包含商品管理、库存管理、入库出库管理和统计报表等功能。

## 技术栈

**前端：**
- Vue 3 (Composition API + TypeScript)
- Element Plus (UI 组件库)
- TailwindCSS (样式)
- ECharts (数据可视化)
- Axios (HTTP 客户端)
- Vite (构建工具)

**后端：**
- Spring Boot 2.5
- MyBatis-Plus (ORM)
- MySQL 8.0
- Redis (缓存)
- Knife4j (API 文档)

## 核心功能

### 1. 商品管理
- 商品 CRUD 操作
- SKU 唯一性校验
- 商品分类管理
- 商品启用/禁用
- 批量导入/导出

### 2. 库存管理
- 实时库存查询
- 库存预警提示
- 库存调整功能
- 多仓库支持

### 3. 入库管理
- 入库单创建
- 自动生成入库单号
- 入库自动增加库存
- 供应商管理

### 4. 出库管理
- 出库单创建
- 自动生成出库单号
- 出库自动减少库存
- 库存不足校验

### 5. 统计报表
- 库存汇总报表
- 出入库趋势图
- 低库存预警列表
- 数据导出

## 业务规则

### 商品管理
- SKU 必须唯一
- 价格必须 >= 0
- 商品必须有有效分类
- 新增商品时自动初始化库存

### 库存管理
- 库存数量 >= 0
- 库存低于预警值时触发预警
- 入库增加库存，出库减少库存

### 入库业务
- 单号格式：IN + yyyyMMdd + 4位序号
- 入库数量必须 > 0
- 入库成功后自动增加库存

### 出库业务
- 单号格式：OUT + yyyyMMdd + 4位序号
- 出库数量必须 > 0 且 <= 当前库存
- 出库成功后自动减少库存

## 项目结构

```
inventory-system/
├── frontend/                    # Vue 3 前端项目
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   │   ├── product/        # 商品管理
│   │   │   ├── inventory/      # 库存管理
│   │   │   ├── inbound/        # 入库管理
│   │   │   └── outbound/       # 出库管理
│   │   ├── components/         # 通用组件
│   │   ├── api/                # API 接口
│   │   ├── router/             # 路由配置
│   │   └── stores/             # 状态管理
│   └── package.json
├── backend/                     # Spring Boot 后端项目
│   ├── src/main/java/com/inventory/
│   │   ├── entity/             # 实体类
│   │   ├── controller/         # 控制器
│   │   ├── service/            # 服务层
│   │   ├── mapper/             # 数据访问层
│   │   └── config/             # 配置类
│   └── pom.xml
└── .claude/                    # AI 辅助开发配置
    ├── agents/                 # 专用代理
    ├── commands/               # 自定义命令
    ├── skills/                 # 技能脚本
    └── snippets/               # 代码片段
```

## AI 辅助开发配置

本项目配置了专用的 AI 辅助开发工具：

### 专用 Agents
- **inventory-writer**: 商品和库存功能实现专家
- **inout-writer**: 入库出库功能实现专家
- **report-writer**: 统计报表功能实现专家

### 自定义命令
- `/module`: 快速生成标准模块

### 代码片段
- Vue 3 商品列表/表单模板
- Spring Boot 实体/服务层模板
- 进销存业务规则文档

## 快速开始

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 后端启动
```bash
cd backend
# 配置数据库连接：修改 application-mysql.yml
mvn spring-boot:run
```

### 数据库初始化
```bash
# 导入 SQL 脚本
mysql -u root -p inventory_system < backend/src/main/resources/sql/init.sql
```

## API 文档

后端启动后访问：http://localhost:8080/doc.html

## 开发指南

### 使用 AI 辅助开发

1. **创建新模块**：使用 `/module` 命令
   ```
   /module action=create type=product
   ```

2. **实现功能**：使用专用 Agent
   - 商品功能：调用 `inventory-writer`
   - 入出库功能：调用 `inout-writer`
   - 报表功能：调用 `report-writer`

3. **参考代码片段**：查看 `.claude/snippets/` 目录

## 数据库表结构

### t_product (商品表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| sku | VARCHAR(50) | 商品编码（唯一） |
| name | VARCHAR(100) | 商品名称 |
| category_id | BIGINT | 分类ID |
| price | DECIMAL(10,2) | 销售价格 |
| cost_price | DECIMAL(10,2) | 成本价格 |
| unit | VARCHAR(20) | 计量单位 |
| warning_stock | INT | 预警库存 |
| status | TINYINT | 状态：0-禁用 1-启用 |

### t_inventory (库存表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| product_id | BIGINT | 商品ID |
| warehouse_id | BIGINT | 仓库ID |
| quantity | INT | 库存数量 |
| warning_stock | INT | 预警值 |

### t_inbound (入库单表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| inbound_no | VARCHAR(20) | 入库单号 |
| product_id | BIGINT | 商品ID |
| quantity | INT | 入库数量 |
| supplier | VARCHAR(100) | 供应商 |
| inbound_date | DATETIME | 入库日期 |
| status | TINYINT | 状态 |

### t_outbound (出库单表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| outbound_no | VARCHAR(20) | 出库单号 |
| product_id | BIGINT | 商品ID |
| quantity | INT | 出库数量 |
| receiver | VARCHAR(100) | 收货人 |
| outbound_date | DATETIME | 出库日期 |
| status | TINYINT | 状态 |

## 开发进度

- [ ] 项目初始化
- [ ] 数据库设计与创建
- [ ] 后端 API 开发
  - [ ] 商品管理 API
  - [ ] 库存管理 API
  - [ ] 入库管理 API
  - [ ] 出库管理 API
  - [ ] 统计报表 API
- [ ] 前端页面开发
  - [ ] 商品管理页面
  - [ ] 库存管理页面
  - [ ] 入库管理页面
  - [ ] 出库管理页面
  - [ ] 统计报表页面
- [ ] 功能测试
- [ ] 在线部署

## 参考资料

- Element Plus: https://element-plus.org/
- Vue 3: https://vuejs.org/
- MyBatis-Plus: https://baomidou.com/
- ECharts: https://echarts.apache.org/
