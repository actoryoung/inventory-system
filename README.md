# 进销存管理系统 (Inventory Management System)

> 基于 AipexBase 的库存管理演示项目

## 项目概述

这是一个展示企业进销存管理能力的演示项目，包含商品管理、入库出库、库存预警和统计报表功能。

## 技术栈

**前端：**
- Vue 3
- Element Plus
- TailwindCSS
- Vite

**后端：**
- Spring Boot 2.5
- MyBatis-Plus
- MySQL 8.0
- Redis

## 功能模块

### 1. 商品管理
- 商品列表展示
- 商品新增/编辑/删除
- 商品分类管理
- 商品搜索

### 2. 入库管理
- 入库单创建
- 入库记录查询
- 批量入库

### 3. 出库管理
- 出库单创建
- 出库记录查询
- 库存扣减

### 4. 库存预警
- 低库存商品预警
- 预警阈值设置
- 预警通知

### 5. 统计报表
- 库存汇总
- 入库出库统计
- 图表展示

## 项目结构

```
inventory-system/
├── frontend/           # Vue 3 前端
│   ├── src/
│   │   ├── views/     # 页面组件
│   │   ├── components/# 通用组件
│   │   ├── api/       # API 接口
│   │   └── router/    # 路由配置
│   └── package.json
├── backend/           # Spring Boot 后端
│   ├── src/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── entity/
│   │   └── mapper/
│   └── pom.xml
├── .claude/           # AI 辅助开发配置
└── README.md
```

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
mvn spring-boot:run
```

## 开发进度

- [x] 项目初始化
- [x] 数据库设计
- [x] 后端 API 开发
- [x] 前端页面开发
- [x] 功能测试
- [ ] 在线部署

## 对应客户需求

此项目可复用于：
- 企业库存管理系统
- 商品订单管理
- 仓库管理系统
- 进销存一体化系统
