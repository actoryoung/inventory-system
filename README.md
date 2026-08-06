# 进销存管理系统 (Inventory Management System)

> 基于 Vue 3 + Element Plus + Spring Boot 的进销存管理演示项目

## 项目概述

这是一个功能完整的进销存管理系统演示项目，包含商品管理、分类管理、库存管理、入库出库和统计报表功能。前后端分离，覆盖进销存核心业务闭环。

## 技术栈

**前端：**
- Vue 3（Composition API + TypeScript）
- Element Plus（按需引入，`unplugin-vue-components`）
- ECharts（数据可视化）
- Axios（HTTP 客户端）
- Vue Router
- Vite（构建工具）
- Vitest + @vue/test-utils（单元测试）

**后端：**
- Spring Boot 2.5.14
- MyBatis-Plus 3.5.2（ORM）
- MySQL 8.0（生产）/ H2（开发，内存模式）
- Knife4j（API 文档）
- Lombok
- JUnit 5 + Mockito + AssertJ（单元测试）

## 功能模块

### 1. 商品管理
- 商品 CRUD 操作
- SKU 唯一性校验
- 商品分类关联
- 商品启用/禁用
- 分页与搜索

### 2. 分类管理
- 分类树形结构
- 分类 CRUD 操作
- 商品关联分类

### 3. 库存管理
- 实时库存查询
- 库存预警提示
- 库存调整功能
- 多仓库支持

### 4. 入库管理
- 入库单创建
- 自动生成入库单号（`IN + yyyyMMdd + 序号`）
- 入库自动增加库存
- 供应商管理

### 5. 出库管理
- 出库单创建
- 自动生成出库单号（`OUT + yyyyMMdd + 序号`）
- 出库自动减少库存
- 库存不足校验

### 6. 统计报表
- 概览指标卡（商品总数、库存总量、库存价值、预警数量）
- 出入库趋势图
- 分类统计图表
- 低库存预警列表

## 项目结构

```
inventory-system/
├── frontend/                    # Vue 3 前端
│   └── src/
│       ├── views/               # 页面组件
│       │   ├── product/         # 商品管理
│       │   ├── category/        # 分类管理
│       │   ├── inventory/       # 库存管理
│       │   ├── inbound/         # 入库管理
│       │   ├── outbound/        # 出库管理
│       │   └── statistics/      # 统计报表
│       ├── components/          # 通用组件
│       ├── api/                 # API 接口层
│       ├── types/               # TypeScript 类型定义
│       ├── utils/               # 工具函数
│       └── router/              # 路由配置
├── backend/                     # Spring Boot 后端
│   └── src/main/java/com/inventory/
│       ├── controller/          # 控制器
│       ├── service/             # 服务层
│       │   └── impl/            # 服务实现
│       ├── mapper/              # 数据访问层
│       ├── entity/              # 实体类
│       ├── dto/                 # 请求参数
│       ├── vo/                  # 返回对象
│       ├── config/              # 配置类
│       ├── common/              # 通用返回/常量
│       ├── exception/           # 异常处理
│       └── event/               # 事件
├── scripts/                     # 系统验证脚本
│   ├── verify-system.ps1        # Windows 验证脚本
│   └── verify-system.sh         # Linux/macOS 验证脚本
└── .claude/                     # AI 辅助开发配置
```

## 快速开始

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173（已配置 `/api` 代理到后端 8080 端口）。

### 后端启动（开发环境，H2 内存数据库）

```bash
cd backend
mvn spring-boot:run
```

- dev profile 默认启用，使用 H2 内存数据库（`MODE=MySQL`），启动时自动建表并导入示例数据
- 后端端口：8080
- H2 控制台：http://localhost:8080/h2-console

### 生产环境（MySQL）

```bash
cd backend
# 配置数据库连接：修改 application.yml 或 application-prod.yml
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 运行测试

```bash
# 后端测试（65 个用例）
cd backend && mvn test

# 前端测试（59 个用例）
cd frontend && npm run test:run
```

## API 文档

后端启动后访问 Knife4j 接口文档：http://localhost:8080/doc.html

## 开发进度

- [x] 项目初始化
- [x] 数据库设计
- [x] 后端 API 开发
- [x] 前端页面开发
- [x] 功能测试（后端 65 + 前端 59）
- [ ] 在线部署

## 对应客户需求

此项目可复用于：
- 企业库存管理系统
- 商品订单管理
- 仓库管理系统
- 进销存一体化系统
