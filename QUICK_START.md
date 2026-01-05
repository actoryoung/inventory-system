# 快速开始 - Spec-Driven + TDD 开发

本文档说明如何使用 Spec-Driven + TDD 规范进行开发。

---

## 什么是 Spec-Driven + TDD？

```
传统开发流程：
设计 → 写代码 → 写测试 → 修复问题

Spec-Driven + TDD 流程：
写规范(SPEC) → 写测试(TEST) → 写代码(CODE) → 验证
     ↑_______________|              ↓
     |                              通过
     |______________________________|
              不通过，重新实现
```

**优势：**
1. 先明确规范，避免返工
2. 测试先行，确保代码质量
3. 规范文档可作为项目文档

---

## 开发一个模块的完整流程

### 示例：开发"商品管理"模块

#### 第 1 步：创建规范 (SPEC)

```bash
# 在项目中执行
/spec action=create type=feature name=product_management
```

**系统会引导你填写：**
1. 功能描述
2. 实体属性
3. 业务规则
4. API 接口定义

**生成的规范文件：**
`.claude/specs/feature/product_management.md`

---

#### 第 2 步：生成测试 (TEST)

```bash
# 基于规范生成测试用例
/spec action=generate-tests name=product_management
```

**系统会：**
1. 解析规范中的业务规则
2. 生成对应的测试用例
3. 创建测试文件

**生成的测试文件示例：**
```
backend/src/test/java/com/inventory/ProductServiceTest.java

测试用例：
✓ testCreateProduct_Success()
✓ testCreateProduct_DuplicateSku_Fail()
✓ testCreateProduct_InvalidCategory_Fail()
✓ testUpdateProduct_Success()
✓ testDeleteProduct_Success()
✓ testGetProductPage_Success()
```

---

#### 第 3 步：实现代码 (CODE)

**方式 1：使用专用 Agent（推荐）**

```bash
# 调用 inventory-writer Agent
# Agent 会参考规范和测试来实现代码
```

**方式 2：手动编码**

参考以下文件：
- `.claude/snippets/vue/product-list.vue` - 前端模板
- `.claude/snippets/springboot/entity.java` - 后端模板
- `.claude/snippets/inventory/business-rules.md` - 业务规则

---

#### 第 4 步：运行测试 (TEST)

```bash
# 运行所有测试
/test
```

**如果测试全部通过：**
✅ 模块开发完成

**如果有测试失败：**
1. 查看失败原因
2. 修改代码
3. 重新运行测试
4. 重复直到全部通过

---

## 常用命令

| 命令 | 说明 | 示例 |
|------|------|------|
| `/spec action=create` | 创建规范 | `/spec action=create type=feature name=xxx` |
| `/spec action=generate-tests` | 生成测试 | `/spec action=generate-tests name=xxx` |
| `/spec action=view` | 查看规范 | `/spec action=view name=xxx` |
| `/test` | 运行测试 | `/test` |
| `/module action=create` | 快速生成模块 | `/module action=create type=product` |

---

## 规范类型说明

### 1. Feature Spec（功能规范）
适用于：完整的功能模块

```bash
/spec action=create type=feature name=product_management
```

**包含内容：**
- 功能概述
- 用户故事
- 实体定义
- 业务规则
- API 接口
- 验收标准

---

### 2. Function Spec（函数规范）
适用于：单个函数/方法

```bash
/spec action=create type=function name=generate_inbound_no
```

**包含内容：**
- 函数描述
- 输入参数
- 输出值
- 业务规则
- 边界条件
- 错误处理

---

### 3. API Spec（接口规范）
适用于：单个 API 端点

```bash
/spec action=create type=api name=products_post
```

**包含内容：**
- 端点路径和方法
- 请求参数
- 响应格式
- 状态码定义
- 错误处理

---

## 测试驱动开发 (TDD) 循环

```
┌─────────────────────────────────────┐
│  1. RED：编写一个失败的测试          │
│     先写测试，测试失败（因为还没实现） │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  2. GREEN：编写最少代码使测试通过    │
│     只写刚好能通过测试的代码          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  3. REFACTOR：重构代码              │
│     优化代码结构，保持测试通过        │
└─────────────────────────────────────┘
              ↓
           回到 1，继续下一个测试
```

---

## 开发检查清单

开发每个模块时，按顺序检查：

### 规范阶段 (SPEC)
- [ ] 功能描述清晰
- [ ] 实体属性完整
- [ ] 业务规则明确
- [ ] API 接口定义完整
- [ ] 边界条件已考虑

### 测试阶段 (TEST)
- [ ] 正常场景测试
- [ ] 异常场景测试
- [ ] 边界条件测试
- [ ] 业务规则测试

### 实现阶段 (CODE)
- [ ] 代码符合规范
- [ ] 异常处理完善
- [ ] 日志记录完整
- [ ] 注释清晰

### 验证阶段
- [ ] 所有测试通过
- [ ] 手动测试通过
- [ ] 代码审查通过

---

## 示例：完整开发流程

### 场景：开发"生成入库单号"功能

#### 1. 创建函数规范

```bash
/spec action=create type=function name=generate_inbound_no
```

**规范内容：**
```markdown
## 函数描述
生成入库单号，格式：IN + yyyyMMdd + 4位序号

## 输入参数
无

## 输出值
String - 入库单号，如 "IN202601040001"

## 业务规则
1. 日期部分使用当天日期
2. 序号每天从0001重新开始
3. 查询当天最大序号，+1得到新序号
4. 单号必须唯一

## 边界条件
- 当天第一个单据：序号为0001
- 跨天：序号重新从0001开始

## 错误处理
- 数据库异常：抛出 SystemException
```

#### 2. 生成测试

```bash
/spec action=generate-tests name=generate_inbound_no
```

**生成的测试用例：**
```java
@Test
public void testGenerateInboundNo_FirstOfToday() {
    // 当天第一个单据
    String result = service.generateInboundNo();
    assertEquals("IN202601040001", result);
}

@Test
public void testGenerateInboundNo_Sequential() {
    // 连续生成
    String result1 = service.generateInboundNo();
    String result2 = service.generateInboundNo();
    assertEquals("IN202601040001", result1);
    assertEquals("IN202601040002", result2);
}
```

#### 3. 实现代码

```java
public String generateInboundNo() {
    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String prefix = "IN" + date;

    // 查询今天最大序号
    LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
    wrapper.likeRight(Inbound::getInboundNo, prefix)
           .orderByDesc(Inbound::getId)
           .last("LIMIT 1");

    Inbound last = this.getOne(wrapper);

    int seq = 1;
    if (last != null) {
        String lastNo = last.getInboundNo();
        String lastSeq = lastNo.substring(prefix.length());
        seq = Integer.parseInt(lastSeq) + 1;
    }

    return prefix + String.format("%04d", seq);
}
```

#### 4. 运行测试

```bash
/test
```

**结果：**
```
✅ testGenerateInboundNo_FirstOfToday PASSED
✅ testGenerateInboundNo_Sequential PASSED
```

---

## 常见问题

### Q1: 规范写错了怎么办？
**A:** 使用 `/spec action=update name=xxx` 更新规范

### Q2: 测试一直失败怎么办？
**A:** 检查以下几点：
1. 规范是否正确
2. 业务规则理解是否有误
3. 代码实现是否正确
4. 是否有环境问题（数据库、配置等）

### Q3: 是否必须严格按 Spec → Test → Code 顺序？
**A:** 推荐按顺序，但可以根据实际情况调整。关键是：
- 先明确需求（规范）
- 测试覆盖完整
- 代码质量过关

---

## 下一步

现在你可以开始开发了！

```bash
cd C:\Users\lenovo\Desktop\portfolio-projects\inventory-system

# 开始第一个模块
/spec action=create type=feature name=category_management
```

祝你开发顺利！
