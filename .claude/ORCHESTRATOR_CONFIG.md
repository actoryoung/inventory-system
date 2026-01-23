# 进销存管理系统 - Orchestrator 配置

本文档定义了自动化开发流程的配置和执行计划，支持**主 Agent 派发任务给不同 Agent 并调用不同模型**。

## 项目信息

| 属性 | 值 |
|------|-----|
| 项目名称 | 进销存管理系统 (Inventory System) |
| 技术栈 | Vue 3 + Element Plus + Spring Boot + MySQL |
| 开发模式 | 规范驱动开发 (Spec-Driven Development) |
| 主控代理 | Orchestrator (glm-4.7) |
| 子代理 | glm-4.6 (并发 ≤ 4) / glm-4.5-air (并发 > 4) |

## 功能模块清单

基于 `.claude/specs/feature/` 中的规范文档：

| ID | 功能模块 | 规范文件 | 优先级 | 状态 |
|----|---------|---------|-------|------|
| F1 | ProductModule | product-spec.md | P0 | 规范完成 ✅ |
| F2 | InboundModule | inbound-spec.md | P0 | 规范完成 ✅ |
| F3 | OutboundModule | outbound-spec.md | P0 | 规范完成 ✅ |
| F4 | InventoryModule | inventory-spec.md | P0 | 规范完成 ✅ |
| F5 | ReportModule | report-spec.md | P1 | 待生成 |

## Agent 与模型分配配置

### 通用 Agent 配置

| Agent | 用途 | 推荐模型 | 理由 |
|-------|------|---------|------|
| spec-writer | 编写功能规范 | glm-4.6 | 规范编写需要准确性 |
| test-writer | 生成测试代码 | glm-4.6 | 测试代码生成 |
| code-writer | 实现业务代码 | glm-4.6 | 代码实现效率优先 |
| code-reviewer | 代码审查 | glm-4.6 | 代码质量检查 |
| debugger | 问题诊断 | glm-4.6 | 问题排查 |
| refactor-agent | 代码重构 | glm-4.6 | 重构任务相对标准化 |

### 领域专用 Agent 配置

| Agent | 用途 | 推荐模型 | 理由 |
|-------|------|---------|------|
| inventory-writer | 商品/库存管理 | glm-4.6 | 业务逻辑标准化 |
| inout-writer | 入库/出库管理 | glm-4.6 | 业务逻辑标准化 |
| report-writer | 报表统计分析 | glm-4.6 | 统计分析功能 |

### 模型选择策略

```yaml
model_selection:
  # 主控代理（始终使用最强模型）
  orchestrator:
    model: "glm-4.7"
    use_case: "任务分解、代理调度、结果整合"

  # 子代理（根据并发数动态选择）
  sub_agents:
    normal:
      model: "glm-4.6"
      condition: "并发数 ≤ 4"
      use_case: "标准开发任务"

    high_concurrency:
      model: "glm-4.5-air"
      condition: "并发数 > 4"
      use_case: "高并发场景自动降级"
```

### 并发负载降级策略

```
当前并发子代理数 → Task model 参数 → 实际使用模型
─────────────────┼─────────────────┼──────────────
      ≤ 4        →   "glm-4.6"    →   GLM-4.6
      > 4        →   "glm-4.5-air" →   GLM-4.5-Air
```

## 自动化流程配置

### 流程模板：规范驱动开发（GLM 模型版）

```yaml
workflow: spec_driven_development_glm
steps:
  - name: "编写规范"
    agent: spec-writer
    model: glm-4.6
    input:
      - 设计文档路径
      - 功能需求描述
    output:
      - .claude/specs/{type}/{name}-spec.md
    validation:
      - 规范完整性检查
      - 测试用例覆盖度检查

  - name: "生成测试"
    agent: test-writer
    model: glm-4.6
    input:
      - 规范文件路径
    output:
      - tests/{module}.test.js
    validation:
      - 测试代码语法检查
      - 测试用例与规范对照

  - name: "实现代码"
    agent: code-writer
    model: glm-4.6
    input:
      - 规范文件路径
      - 测试文件路径
    output:
      - src/{module}.js
    validation:
      - 代码通过测试
      - 代码符合规范

  - name: "代码审查"
    agent: code-reviewer
    model: glm-4.6
    input:
      - 实现代码路径
      - 规范文件路径
    output:
      - .claude/reviews/{module}-review.md
    validation:
      - 审查通过标准
      - 无阻塞性问题
```

### 并行任务派发配置

```yaml
parallel_execution:
  # 可并行的任务组
  groups:
    - name: "frontend_modules"
      agents:
        - agent: inventory-writer
          model: glm-4.6
          task: "商品管理前端模块"
        - agent: inout-writer
          model: glm-4.6
          task: "出入库前端模块"
      mode: "parallel"

    - name: "backend_apis"
      agents:
        - agent: code-writer
          model: glm-4.6
          task: "商品 API"
        - agent: code-writer
          model: glm-4.6
          task: "出入库 API"
      mode: "parallel"

  # 并发数 > 4 时自动降级到 glm-4.5-air
  concurrency_limit: 4
  fallback_model: "glm-4.5-air"
```

### 检查点配置

| 检查点ID | 触发时机 | 验证内容 | 回退目标 |
|---------|---------|---------|---------|
| CP_SPEC | 规范生成后 | 规范完整性、可测试性 | 重新生成规范 |
| CP_TEST | 测试生成后 | 测试覆盖度、语法正确 | 重新生成测试 |
| CP_CODE | 代码实现后 | 测试通过、规范符合 | 重新实现代码 |
| CP_REVIEW | 代码审查后 | 代码质量、无阻塞性问题 | 修改代码 |

### 错误处理配置

```yaml
error_handling:
  L1_surface:
    action: "直接修复"
    max_retries: 0
    notify_user: false

  L2_logic:
    action: "重新调用代理"
    max_retries: 2
    notify_user: "重试2次后失败时通知"

  L3_design:
    action: "回退检查点"
    max_retries: 1
    notify_user: true
    fallback_checkpoint: "CP_SPEC"

  L4_understanding:
    action: "请求用户澄清"
    max_retries: 1
    notify_user: true
    fallback_checkpoint: "开始"
```

## 开发计划

### Phase 1: 核心功能 (当前阶段)

**目标**: 实现进销存基础功能

| 任务 | 规范 | 测试 | 代码 | 审查 | 状态 |
|------|------|------|------|------|------|
| ProductModule | ✅ | ⏳ | ⏳ | ⏳ | 待开始 |
| InboundModule | ✅ | ⏳ | ⏳ | ⏳ | 待开始 |
| OutboundModule | ✅ | ⏳ | ⏳ | ⏳ | 待开始 |
| InventoryModule | ✅ | ⏳ | ⏳ | ⏳ | 待开始 |

### Phase 2: 报表统计

**目标**: 实现数据统计和报表

| 任务 | 规范 | 测试 | 代码 | 审查 | 状态 |
|------|------|------|------|------|------|
| ReportModule | ⏳ | ⏳ | ⏳ | ⏳ | 待开始 |

## 触发命令

### 开始新功能开发（单 Agent 单模型）
```
As an Orchestrator, start spec-driven development for:
- Feature: <功能名称>
- Spec template: feature-spec.md
- Priority: <P0/P1/P2>
- Requirements: <需求描述>
- Model: glm-4.6 (可选，不指定则使用默认配置)
```

### 并行开发多个模块（多 Agent 多模型）
```
As an Orchestrator, execute parallel development:
- Features: [<功能1>, <功能2>, <功能3>]
- Agents:
    - Feature1: inventory-writer (glm-4.6)
    - Feature2: inout-writer (glm-4.6)
    - Feature3: report-writer (glm-4.6)
- Mode: parallel
- Dependencies: <依赖关系描述>

# 并发数 > 4 时自动降级到 glm-4.5-air
```

### 指定模型执行特定任务
```
As an Orchestrator, execute with specific model:
- Agent: code-reviewer
- Model: glm-4.6
- Target: <审查目标文件>
```

### 继续未完成的功能
```
As an Orchestrator, resume development for:
- Feature: <功能名称>
- Current checkpoint: <检查点ID>
- Last error: <错误描述>
- Fallback model: glm-4.6 (可选)
```

## 验证清单

每个功能完成后必须验证：

- [ ] 规范文档完整（所有字段已填写）
- [ ] 测试用例覆盖所有业务规则
- [ ] 测试代码可运行
- [ ] 实现代码通过所有测试
- [ ] 代码符合规范要求
- [ ] 代码审查通过（无阻塞性问题）
- [ ] 文档已更新（如需要）

## 输出产物

每个功能开发完成后应产生：

```
.claude/
├── specs/feature/
│   └── {feature}-spec.md          # 功能规范
├── reviews/
│   └── {feature}-review.md        # 代码审查报告
├── ORCHESTRATOR_LOG.md            # 执行日志
└── ORCHESTRATOR_CONFIG.md         # 本配置文件

src/
└── {module}.js                    # 实现代码

tests/
└── {module}.test.js               # 测试代码
```

## 快捷指令

```bash
# 开发单个功能（使用默认模型配置）
@orchestrator develop ProductModule

# 并行开发多个模块（自动分配模型，并发>4时降级）
@orchestrator develop-parallel ProductModule,InboundModule,OutboundModule,InventoryModule,ReportModule

# 指定使用特定模型
@orchestrator develop ReportModule --model glm-4.6

# 继续未完成的功能
@orchestrator resume InboundModule

# 查看开发状态
@orchestrator status
```

## 模型选择指南

| 场景 | 推荐模型 | 成本 | 速度 | 质量 |
|------|---------|------|------|------|
| 主控调度 | glm-4.7 | 高 | 慢 | 最高 |
| 编写功能规范 | glm-4.6 | 中 | 中 | 高 |
| 编写测试 | glm-4.6 | 中 | 中 | 高 |
| 实现代码 | glm-4.6 | 中 | 快 | 高 |
| 代码审查 | glm-4.6 | 中 | 慢 | 高 |
| 简单重构 | glm-4.6 | 中 | 快 | 高 |
| 复杂调试 | glm-4.6 | 中 | 慢 | 高 |
| 报表统计 | glm-4.6 | 中 | 慢 | 高 |
| 高并发场景(>4) | glm-4.5-air | 低 | 最快 | 中 |
