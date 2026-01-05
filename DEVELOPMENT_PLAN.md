# 进销存管理系统 - 详细开发计划

> 基于规格驱动开发 (Spec-Driven Development) + 测试驱动开发 (TDD)
> 创建日期：2026-01-04

---

## 开发方法论选择

### 推荐使用：Spec-Driven + TDD

**理由：**
1. **项目特点**：进销存系统业务规则复杂（库存校验、出入库流程等），先明确规范可以避免返工
2. **演示性质**：作为作品集项目，规范文档可以展示你的工程化能力
3. **已有工具**：`.claude` 配置已包含 `/spec` 命令和 spec-writer Agent

**开发流程：**
```
SPEC（规范）→ TEST（测试）→ CODE（实现）→ REVIEW（审查）
```

---

## 总体开发计划

### 阶段划分

| 阶段 | 内容 | 预计时间 |
|------|------|----------|
| **阶段 0：环境准备** | 数据库、前后端项目初始化 | 0.5 天 |
| **阶段 1：基础模块** | 商品管理、分类管理 | 1.5 天 |
| **阶段 2：核心业务** | 库存管理、入库出库 | 2 天 |
| **阶段 3：统计报表** | 数据看板、报表功能 | 1 天 |
| **阶段 4：测试优化** | 集成测试、性能优化 | 0.5 天 |
| **阶段 5：部署上线** | 在线部署、演示视频 | 0.5 天 |

**总计：约 6 天**

---

## 阶段 0：环境准备（0.5 天）

### 任务清单

- [ ] **数据库初始化**
  - [ ] 创建 MySQL 数据库 `inventory_system`
  - [ ] 导入初始化 SQL 脚本
  - [ ] 验证表结构创建成功

- [ ] **后端项目初始化**
  - [ ] 创建 Spring Boot 项目结构
  - [ ] 配置 application.yml（数据库连接）
  - [ ] 配置 MyBatis-Plus
  - [ ] 配置跨域、日期格式等

- [ ] **前端项目初始化**
  - [ ] 创建 Vue 3 + Vite 项目
  - [ ] 安装 Element Plus、Axios、ECharts
  - [ ] 配置路由、状态管理
  - [ ] 配置 Axios 拦截器

- [ ] **公共配置**
  - [ ] 统一返回格式 Result<T>
  - [ ] 全局异常处理
  - [ ] 前端通用组件（布局、表格等）

### 验收标准
- [ ] 后端能成功启动，访问 http://localhost:8080 返回正常
- [ ] 前端能成功启动，访问 http://localhost:5173 显示页面
- [ ] 数据库表全部创建成功

---

## 阶段 1：基础模块（1.5 天）

### 模块 1.1：商品分类管理（0.3 天）

#### 开发流程（Spec-Driven + TDD）

**Step 1: 创建规范 (SPEC)**
```
使用：/spec action=create type=feature name=category_management
```

**规范内容应包含：**
- 功能描述：商品分类的增删改查
- 实体属性：id, name, code, sort, status
- 业务规则：
  - 分类名称不能重复
  - 分类编码不能重复
  - 删除分类前需检查是否有关联商品
- API 接口定义

**Step 2: 生成测试 (TEST)**
```
使用：/spec action=generate-tests name=category_management
```

**测试用例：**
- [ ] 创建分类 - 成功
- [ ] 创建分类 - 名称重复失败
- [ ] 创建分类 - 编码重复失败
- [ ] 更新分类 - 成功
- [ ] 删除分类 - 无关联商品成功
- [ ] 删除分类 - 有关联商品失败
- [ ] 查询分类列表 - 分页正常

**Step 3: 实现代码 (CODE)**
```
使用：调用 inventory-writer Agent
```

**创建文件：**
- 后端：Category.java, CategoryController.java, CategoryService.java
- 前端：CategoryList.vue, CategoryForm.vue, category.ts

**Step 4: 运行测试 (TEST)**
```
使用：/test
```

**验收：**
- [ ] 所有测试用例通过
- [ ] 前端页面功能正常

---

### 模块 1.2：商品管理（1.2 天）

#### 开发流程

**Step 1: 创建规范 (SPEC)**
```
使用：/spec action=create type=feature name=product_management
```

**规范内容：**
- 功能描述：商品 CRUD、批量操作、导入导出
- 实体属性：id, sku, name, categoryId, price, unit, warningStock, status
- 业务规则：
  - SKU 全局唯一
  - 价格 >= 0
  - 新增商品时初始化库存记录
- API 接口定义（8-10 个接口）

**Step 2: 生成测试 (TEST)**
```
使用：/spec action=generate-tests name=product_management
```

**测试用例（约 15 个）：**
- 创建商品（正常/重复SKU/无效分类/负价格）
- 更新商品
- 删除商品
- 批量删除
- 分页查询（带筛选条件）
- SKU 唯一性校验
- 库存自动初始化

**Step 3: 实现代码 (CODE)**
```
使用：调用 inventory-writer Agent
参考：.claude/snippets/vue/product-list.vue
参考：.claude/snippets/springboot/entity.java
```

**后端文件：**
```
backend/src/main/java/com/inventory/
├── entity/Product.java
├── controller/ProductController.java
├── service/
│   ├── ProductService.java
│   └── impl/ProductServiceImpl.java
├── mapper/ProductMapper.java
└── dto/ProductDTO.java
```

**前端文件：**
```
frontend/src/
├── views/product/
│   ├── ProductList.vue      # 列表页
│   └── ProductForm.vue      # 表单页
├── api/product.ts
└── types/product.ts
```

**Step 4: 运行测试 (TEST)**
```
使用：/test
```

**验收：**
- [ ] 所有测试通过
- [ ] 前端列表可正常增删改查
- [ ] 库存自动初始化成功

---

## 阶段 2：核心业务（2 天）

### 模块 2.1：库存管理（0.5 天）

#### 开发流程

**Step 1: 创建规范**
```
使用：/spec action=create type=feature name=inventory_management
```

**规范内容：**
- 功能描述：库存查询、库存调整、低库存预警
- 实体属性：id, productId, warehouseId, quantity, warningStock
- 业务规则：
  - 库存 >= 0
  - 低于预警值时标记
- API 接口定义

**Step 2: 生成测试**
```
使用：/spec action=generate-tests name=inventory_management
```

**测试用例：**
- 查询库存
- 调整库存（增加/减少/设值）
- 低库存查询
- 库存不足校验

**Step 3: 实现代码**
```
使用：调用 inventory-writer Agent
```

**后端文件：**
```
backend/src/main/java/com/inventory/
├── entity/Inventory.java
├── service/InventoryService.java
├── controller/InventoryController.java
```

**前端文件：**
```
frontend/src/views/inventory/
├── InventoryList.vue
└── StockAdjustDialog.vue
```

**Step 4: 运行测试**

---

### 模块 2.2：入库管理（0.7 天）

#### 开发流程

**Step 1: 创建规范**
```
使用：/spec action=create type=feature name=inbound_management
```

**规范内容：**
- 功能描述：入库单 CRUD、自动生成单号
- 实体属性：id, inboundNo, productId, quantity, supplier, inboundDate, status
- 业务规则：
  - 单号格式：IN + yyyyMMdd + 4位序号
  - 入库数量 > 0
  - 入库成功后自动增加库存
  - 全部在事务中执行
- API 接口定义

**Step 2: 生成测试**
```
使用：/spec action=generate-tests name=inbound_management
```

**测试用例：**
- 创建入库单（正常/商品不存在/负数量）
- 单号自动生成（不重复/每天重置）
- 入库后库存自动增加
- 事务回滚测试

**Step 3: 实现代码**
```
使用：调用 inout-writer Agent
参考：.claude/snippets/springboot/service.java
参考：.claude/snippets/inventory/business-rules.md
```

**关键代码：**
```java
@Transactional(rollbackFor = Exception.class)
public void createInbound(Inbound inbound) {
    // 1. 生成单号
    inbound.setInboundNo(generateInboundNo());

    // 2. 验证商品
    Product product = productService.getById(inbound.getProductId());
    if (product == null) {
        throw new BusinessException("商品不存在");
    }

    // 3. 保存入库单
    this.save(inbound);

    // 4. 增加库存
    inventoryService.addStock(inbound.getProductId(), inbound.getQuantity());
}
```

**Step 4: 运行测试**

---

### 模块 2.3：出库管理（0.8 天）

#### 开发流程

**Step 1: 创建规范**
```
使用：/spec action=create type=feature name=outbound_management
```

**规范内容：**
- 功能描述：出库单 CRUD、自动生成单号
- 实体属性：id, outboundNo, productId, quantity, receiver, outboundDate, status
- 业务规则：
  - 单号格式：OUT + yyyyMMdd + 4位序号
  - 出库数量 > 0 且 <= 当前库存
  - 出库成功后自动减少库存
  - 全部在事务中执行
- API 接口定义

**Step 2: 生成测试**
```
使用：/spec action=generate-tests name=outbound_management
```

**测试用例：**
- 创建出库单（正常/商品不存在/库存不足/数量超限）
- 单号自动生成
- 出库后库存自动减少
- 事务回滚测试

**Step 3: 实现代码**
```
使用：调用 inout-writer Agent
```

**关键代码：**
```java
@Transactional(rollbackFor = Exception.class)
public void createOutbound(Outbound outbound) {
    // 1. 生成单号
    outbound.setOutboundNo(generateOutboundNo());

    // 2. 验证商品
    Product product = productService.getById(outbound.getProductId());
    if (product == null) {
        throw new BusinessException("商品不存在");
    }

    // 3. 检查库存
    if (!inventoryService.checkStock(outbound.getProductId(), outbound.getQuantity())) {
        throw new BusinessException("库存不足");
    }

    // 4. 保存出库单
    this.save(outbound);

    // 5. 减少库存
    inventoryService.reduceStock(outbound.getProductId(), outbound.getQuantity());
}
```

**Step 4: 运行测试**

---

## 阶段 3：统计报表（1 天）

### 模块 3.1：数据看板（0.5 天）

#### 开发流程

**Step 1: 创建规范**
```
使用：/spec action=create type=feature name=dashboard
```

**规范内容：**
- 功能描述：数据汇总、图表展示
- 数据指标：
  - 商品总数、库存总量、库存总额、低库存数量
  - 出入库趋势图（近30天）
  - 库存分类占比饼图
- API 接口定义

**Step 2: 实现代码**
```
使用：调用 report-writer Agent
参考：.claude/agents/report-writer.md（包含完整图表代码）
```

**前端文件：**
```
frontend/src/views/report/Dashboard.vue
```

**图表配置：**
- ECharts 折线图（出入库趋势）
- ECharts 饼图（库存分类占比）
- 汇总卡片

---

### 模块 3.2：库存报表（0.3 天）

#### 功能
- 库存汇总报表（按分类）
- 低库存预警列表
- Excel 导出

---

### 模块 3.3：出入库报表（0.2 天）

#### 功能
- 出入库统计（按时间段）
- 出入库明细查询
- 数据导出

---

## 阶段 4：测试优化（0.5 天）

### 集成测试
- [ ] 完整业务流程测试（商品→入库→库存→出库）
- [ ] 边界条件测试
- [ ] 并发测试（库存扣减）

### 性能优化
- [ ] 数据库索引优化
- [ ] 查询性能优化
- [ ] 前端渲染优化

### Bug 修复
- [ ] 修复已知问题
- [ ] 代码重构优化

---

## 阶段 5：部署上线（0.5 天）

### 在线部署

**前端部署（选一个）：**
- Vercel: `npm run build` → 上传 dist
- Netlify: 拖拽上传
- Gitee Pages: 推送到 gitee 仓库

**后端部署（选一个）：**
- Railway.app: 直接连接 GitHub 仓库
- Render: 类似 Railway
- 内网穿透（演示用）

### 演示视频
- [ ] 录制功能演示（2-3 分钟）
- [ ] 添加解说字幕
- [ ] 上传 B 站

### 作品集更新
- [ ] 更新 README.md（添加在线演示链接）
- [ ] 录制 GIF 动图
- [ ] 写技术博客（可选）

---

## 开发检查清单

### 每个模块完成后检查

**代码质量：**
- [ ] 代码符合规范
- [ ] 有必要的注释
- [ ] 异常处理完善
- [ ] 日志记录完整

**功能完整性：**
- [ ] 所有功能点已实现
- [ ] 业务规则正确执行
- [ ] 边界条件处理正确

**测试覆盖：**
- [ ] 单元测试通过
- [ ] 集成测试通过
- [ ] 手动测试通过

**文档：**
- [ ] API 文档更新
- [ ] 代码注释完整
- [ ] 用户说明（如需要）

---

## 常见问题处理

### 1. 库存并发问题
**问题：** 多人同时出库可能导致库存扣减错误

**解决方案：**
```java
// 使用数据库行锁
@Transactional
public void reduceStock(Long productId, Integer quantity) {
    Inventory inventory = inventoryMapper.selectForUpdate(productId);
    // 库存扣减逻辑
}
```

### 2. 单号重复问题
**问题：** 并发创建单据可能导致单号重复

**解决方案：**
```java
// 使用数据库序列或分布式 ID
String inboundNo = "IN" + DateUtil.format(new Date(), "yyyyMMdd")
    + String.format("%04d", redisService.incr("inbound:seq:" + date));
```

### 3. 事务回滚问题
**问题：** 异常被捕获后事务不回滚

**解决方案：**
```java
@Transactional(rollbackFor = Exception.class)
public void createInbound(Inbound inbound) {
    // 手动抛出 BusinessException
    throw new BusinessException("库存不足");
}
```

---

## 开发进度跟踪

| 模块 | 状态 | 完成时间 |
|------|------|----------|
| 环境准备 | ⬜ | |
| 分类管理 | ⬜ | |
| 商品管理 | ⬜ | |
| 库存管理 | ⬜ | |
| 入库管理 | ⬜ | |
| 出库管理 | ⬜ | |
| 数据看板 | ⬜ | |
| 库存报表 | ⬜ | |
| 出入库报表 | ⬜ | |
| 测试优化 | ⬜ | |
| 部署上线 | ⬜ | |

---

## 快速命令参考

```bash
# 创建规范
/spec action=create type=feature name=xxx

# 生成测试
/spec action=generate-tests name=xxx

# 运行测试
/test

# 创建模块
/module action=create type=product

# 代码审查
/review-pr
```

---

> 开发愉快！如有问题，随时查阅 `.claude/` 目录下的配置和文档。
