---
name: agent-collection
description: 进销存系统专用 Agent 索引
version: 1.0
project: inventory-system
---

# Agent Collection Index - 进销存系统

进销存管理系统的专用 Agent 集合，针对 Vue 3 + Spring Boot 技术栈优化。

## Agent Map

### 主控代理

| Agent | 触发条件 | 文件 |
|-------|----------|------|
| **orchestrator** | 复杂多步骤任务、需要多 Agent 协作 | `orchestrator.md` |

---

### 业务代理（进销存专用）

| Agent | 触发条件 | 文件 |
|-------|----------|------|
| **inventory-writer** | 实现商品、库存相关功能 | `inventory-writer.md` |
| **inout-writer** | 实现入库、出库相关功能 | `inout-writer.md` |
| **report-writer** | 实现统计报表相关功能 | `report-writer.md` |

---

### 通用代理

| Agent | 触发条件 | 文件 |
|-------|----------|------|
| **spec-writer** | 创建规范、基于规范生成测试 | `spec-writer.md` |
| **test-writer** | 编写测试、补充测试用例 | `test-writer.md` |
| **code-reviewer** | PR 审查、代码质量检查 | `code-reviewer.md` |
| **debugger** | Bug 诊断、错误分析 | `debugger.md` |
| **refactor-agent** | 代码重构、结构优化 | `refactor-agent.md` |

---

## Quick Reference - 进销存系统

### 按任务类型选择 Agent

| 任务类型 | 推荐流程 | 涉及 Agent |
|---------|----------|------------|
| **商品管理功能** | spec-writer → inventory-writer → test-writer | 商品规范 → 商品代码 → 测试 |
| **入库出库功能** | spec-writer → inout-writer → test-writer | 入出库规范 → 入出库代码 → 测试 |
| **统计报表功能** | spec-writer → report-writer → test-writer | 报表规范 → 报表代码 → 测试 |
| **Bug 修复** → debugger → code-writer | 探索 → 诊断 → 修复 |

### 技术栈

**前端：**
- Vue 3 (Composition API)
- Element Plus (UI 组件库)
- TailwindCSS (样式)
- Vite (构建工具)

**后端：**
- Spring Boot 2.5
- MyBatis-Plus (ORM)
- MySQL 8.0
- Redis (缓存)

---

## 进销存业务知识

### 核心实体

| 实体 | 说明 | 关键字段 |
|------|------|----------|
| **商品 (Product)** | 基础商品信息 | id, name, sku, category, price, unit |
| **库存 (Inventory)** | 商品库存数量 | id, productId, quantity, warehouseId |
| **入库单 (Inbound)** | 商品入库记录 | id, inboundNo, productId, quantity, supplier |
| **出库单 (Outbound)** | 商品出库记录 | id, outboundNo, productId, quantity, receiver |

### 业务规则

1. **库存扣减**：出库时库存不足则不允许出库
2. **库存预警**：库存低于阈值时触发预警
3. **入库增加库存**：入库成功后自动增加对应商品库存
4. **出库减少库存**：出库成功后自动减少对应商品库存
5. **出入库记录**：所有出入库操作必须有记录

### 报表统计

- **库存汇总**：按商品分类、仓库统计库存数量和金额
- **出入库统计**：按时间段统计出入库数量和金额
- **库存周转率**：计算商品库存周转情况

---

## 相关文件

- `.claude/agents/*.md` - 各 Agent 的完整定义
- `.claude/snippets/inventory/` - 进销存专用代码片段
- `.claude/snippets/vue/` - Vue 3 前端代码片段
- `.claude/snippets/springboot/` - Spring Boot 后端代码片段
