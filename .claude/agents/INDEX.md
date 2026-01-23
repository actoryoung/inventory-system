---
name: agent-collection
description: 进销存系统专用 Agent 索引（支持 GLM 多模型分配）
version: 2.0
project: inventory-system
---

# Agent Collection Index - 进销存系统

进销存管理系统的专用 Agent 集合，针对 Vue 3 + Spring Boot 技术栈优化。**支持智能模型分配，主 Agent 使用 glm-4.7，子代理使用 glm-4.6，并发 > 4 时降级到 glm-4.5-air。**

## Agent Map

### 主控代理

| Agent | 推荐模型 | 触发条件 | 文件 |
|-------|---------|----------|------|
| **orchestrator** | glm-4.7 | 复杂多步骤任务、需要多 Agent 协作 | `orchestrator.md` |

---

### 业务代理（进销存专用）

| Agent | 推荐模型 | 触发条件 | 文件 |
|-------|---------|----------|------|
| **inventory-writer** | glm-4.6 | 实现商品、库存相关功能 | `inventory-writer.md` |
| **inout-writer** | glm-4.6 | 实现入库、出库相关功能 | `inout-writer.md` |
| **report-writer** | glm-4.6 | 实现统计报表相关功能 | `report-writer.md` |

---

### 通用代理

| Agent | 推荐模型 | 触发条件 | 文件 |
|-------|---------|----------|------|
| **spec-writer** | glm-4.6 | 创建规范、需要深度分析 | `spec-writer.md` |
| **test-writer** | glm-4.6 | 编写测试、补充测试用例 | `test-writer.md` |
| **code-writer** | glm-4.6 | 代码实现、平衡速度质量 | `code-writer.md` |
| **code-reviewer** | glm-4.6 | PR 审查、代码质量检查 | `code-reviewer.md` |
| **debugger** | glm-4.6 | Bug 诊断、错误分析 | `debugger.md` |
| **refactor-agent** | glm-4.6 | 代码重构、结构优化 | `refactor-agent.md` |

---

## Quick Reference - 进销存系统

### 按任务类型选择 Agent 和模型

| 任务类型 | 推荐流程 | 模型分配 |
|---------|----------|----------|
| **商品管理功能** | spec-writer → inventory-writer → test-writer → code-reviewer | glm-4.6 全流程 |
| **入库出库功能** | spec-writer → inout-writer → test-writer → code-reviewer | glm-4.6 全流程 |
| **统计报表功能** | spec-writer → report-writer → test-writer → code-reviewer | glm-4.6 全流程 |
| **Bug 修复** | Explore → debugger → code-writer | glm-4.6 全流程 |
| **代码审查** | code-reviewer | glm-4.6 |
| **规范驱动开发** | spec-writer → test-writer → code-writer → /test | glm-4.6 全流程 |

### GLM 模型选择指南

| 模型 | 成本 | 速度 | 适用场景 |
|------|------|------|----------|
| **glm-4.7** | 高 | 慢 | 主控代理调度（Orchestrator 专用） |
| **glm-4.6** | 中 | 中 | 代码实现、测试编写、规范编写、标准开发任务 |
| **glm-4.5-air** | 低 | 快 | 高并发场景（并发子代理数 > 4 时自动降级） |

### 并发负载降级策略

```
当前并发子代理数 → Task model 参数 → 实际使用模型
─────────────────┼─────────────────┼──────────────
      ≤ 4        →   "glm-4.6"    →   GLM-4.6
      > 4        →   "glm-4.5-air" →   GLM-4.5-Air
```

### 技术栈

**前端：**
- Vue 3 (Composition API + TypeScript)
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

## GLM 多模型派发能力

Orchestrator 主 Agent (使用 glm-4.7) 支持将任务派发给不同的子 Agent，并为每个 Agent 选择最合适的模型：

```
用户任务: "开发完整的进销存系统"
    ↓
┌─────────────────────────────────────┐
│   Orchestrator (主控代理)             │
│   模型: glm-4.7                      │
│                                     │
│  任务分解:                           │
│  1. 商品管理模块 → inventory-writer  │
│  2. 入库模块 → inout-writer          │
│  3. 出库模块 → inout-writer          │
│  4. 报表模块 → report-writer         │
│                                     │
│  模型分配:                           │
│  - 并发 ≤ 4: glm-4.6                │
│  - 并发 > 4: glm-4.5-air (自动降级) │
└─────────────────────────────────────┘
    ↓
并行执行多个 Agent（每个使用 glm-4.6）
    ↓
整合结果返回用户
```

### 调用示例

```bash
# 使用 Orchestrator 进行多 Agent 协作（自动模型分配）
As an orchestrator, develop the following features in parallel:
1. Product management module (use inventory-writer with glm-4.6)
2. Inbound management module (use inout-writer with glm-4.6)
3. Outbound management module (use inout-writer with glm-4.6)
4. Report statistics module (use report-writer with glm-4.6)

# 并发数 > 4 时自动降级到 glm-4.5-air

# 指定使用特定模型
As an orchestrator, execute code review using glm-4.6 for maximum quality
```

---

## 相关文件

- `.claude/agents/*.md` - 各 Agent 的完整定义
- `.claude/agents/orchestrator.md` - 主控代理配置（glm-4.7）
- `.claude/ORCHESTRATOR_CONFIG.md` - Orchestrator 配置和多模型分配策略
- `.claude/snippets/inventory/` - 进销存专用代码片段
- `.claude/snippets/vue/` - Vue 3 前端代码片段
- `.claude/snippets/springboot/` - Spring Boot 后端代码片段
- `.claude/skills/` - 通用技能脚本（code-generator, git-helper, tdd-helper）
