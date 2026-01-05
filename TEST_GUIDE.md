# 商品分类管理模块测试指南

## 测试概述

本模块为商品分类管理功能生成了完整的测试用例，涵盖单元测试、集成测试、组件测试和 API 测试。

## 测试文件结构

```
inventory-system/
├── backend/
│   ├── src/
│   │   ├── test/
│   │   │   ├── java/com/inventory/
│   │   │   │   ├── service/
│   │   │   │   │   └── CategoryServiceTest.java          # 服务层单元测试
│   │   │   │   └── controller/
│   │   │   │       └── CategoryControllerTest.java       # 控制器集成测试
│   │   │   └── resources/
│   │   │       ├── test-data-category.sql                # 测试数据脚本
│   │   │       └── application-test.yml                  # 测试环境配置
│   │   └── pom.xml                                       # Maven 配置
│
└── frontend/
    ├── src/
    │   ├── views/category/__tests__/
    │   │   └── CategoryList.spec.ts                      # 组件测试
    │   ├── api/__tests__/
    │   │   └── categoryApi.spec.ts                       # API 测试
    │   └── tests/
    │       └── setup.ts                                  # 测试环境配置
    ├── package.json                                      # NPM 配置
    └── vitest.config.ts                                  # Vitest 配置
```

## 后端测试运行指南

### 1. 环境准备

```bash
cd backend
```

### 2. 运行所有测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 运行所有测试（单元 + 集成）
mvn clean test verify
```

### 3. 运行单个测试类

```bash
# 运行服务层测试
mvn test -Dtest=CategoryServiceTest

# 运行控制器测试
mvn test -Dtest=CategoryControllerTest
```

### 4. 运行单个测试方法

```bash
mvn test -Dtest=CategoryServiceTest#shouldCreateLevel1CategorySuccessfully
```

### 5. 生成测试覆盖率报告

```bash
# 生成覆盖率报告
mvn clean test jacoco:report

# 查看报告
open target/site/jacoco/index.html
```

### 6. 测试数据库初始化

```bash
# 使用 H2 内存数据库（自动初始化）
# 或使用 MySQL 数据库：
mysql -u root -p inventory_system < src/test/resources/test-data-category.sql
```

## 前端测试运行指南

### 1. 环境准备

```bash
cd frontend
npm install
```

### 2. 运行所有测试

```bash
# 运行测试（监听模式）
npm run test

# 运行测试（单次运行）
npm run test:run

# 运行测试（带 UI 界面）
npm run test:ui
```

### 3. 生成测试覆盖率报告

```bash
npm run test:coverage

# 查看覆盖率报告
open coverage/index.html
```

### 4. 运行单个测试文件

```bash
npx vitest src/views/category/__tests__/CategoryList.spec.ts
npx vitest src/api/__tests__/categoryApi.spec.ts
```

### 5. 运行匹配特定模式的测试

```bash
# 运行所有 API 测试
npx vitest api

# 运行所有组件测试
npx vitest views
```

## 测试覆盖范围

### 后端单元测试 (CategoryServiceTest.java)

#### 测试套件

1. **分类创建测试** (10 个测试用例)
   - 创建一级分类
   - 创建二级分类
   - 创建三级分类
   - 层级超过限制
   - 同级名称重复
   - 不同层级同名
   - 父分类不存在
   - 名称为空
   - 名称超长

2. **分类更新测试** (3 个测试用例)
   - 更新基本信息
   - 分类不存在
   - 更新后名称重复

3. **分类删除测试** (5 个测试用例)
   - 成功删除
   - 有关联商品
   - 有子分类
   - 分类不存在
   - 删除禁用分类

4. **分类查询测试** (8 个测试用例)
   - 获取列表
   - 按名称搜索
   - 按状态过滤
   - 按层级过滤
   - 获取详情
   - 分类不存在
   - 获取树形结构
   - 树形结构只返回启用分类

5. **分类状态切换测试** (4 个测试用例)
   - 启用到禁用
   - 禁用到启用
   - 分类不存在
   - 切换父分类不影响子分类

6. **分类层级校验测试** (4 个测试用例)
   - 计算一级分类
   - 计算二级分类
   - 计算三级分类
   - 四级分类抛出异常

7. **分类名称唯一性校验测试** (4 个测试用例)
   - 检测同名
   - 检测非同名
   - 更新时排除当前ID
   - 不同父分类同名

8. **边界条件测试** (4 个测试用例)
   - 包含空格的名称
   - 特殊字符
   - 负数排序
   - 零值排序

9. **批量操作测试** (2 个测试用例)
   - 批量创建成功
   - 批量创建部分失败

**总计：44 个单元测试用例**

### 后端集成测试 (CategoryControllerTest.java)

#### 测试套件

1. **POST /api/categories** (7 个测试用例)
   - 创建一级分类
   - 创建二级分类
   - 名称为空返回 400
   - 层级超过返回 400
   - 名称重复返回 400
   - 请求体为空返回 400
   - Content-Type 错误返回 415

2. **PUT /api/categories/{id}** (4 个测试用例)
   - 更新成功
   - 分类不存在返回 404
   - 更新后名称重复返回 400
   - 路径ID与请求体ID不一致返回 400

3. **DELETE /api/categories/{id}** (5 个测试用例)
   - 删除成功
   - 有关联商品返回 400
   - 有子分类返回 400
   - 分类不存在返回 404
   - ID格式无效返回 400

4. **GET /api/categories/{id}** (3 个测试用例)
   - 获取详情成功
   - 分类不存在返回 404
   - ID格式无效返回 400

5. **GET /api/categories** (7 个测试用例)
   - 获取列表成功
   - 支持名称搜索
   - 支持状态过滤
   - 支持层级过滤
   - 支持组合查询
   - 列表为空返回空数组
   - 参数格式无效返回 400

6. **GET /api/categories/tree** (2 个测试用例)
   - 获取树形结构成功
   - 树为空返回空数组

7. **PATCH /api/categories/{id}/status** (3 个测试用例)
   - 切换状态成功
   - 分类不存在返回 404
   - ID格式无效返回 400

8. **请求响应格式验证** (4 个测试用例)
   - 成功响应标准格式
   - 错误响应标准格式
   - 响应包含必要字段
   - 日期时间格式正确

9. **数据库事务测试** (3 个测试用例)
   - 创建失败回滚
   - 更新失败回滚
   - 删除失败回滚

10. **并发请求测试** (2 个测试用例)
    - 并发创建请求
    - 并发更新请求

11. **安全性测试** (3 个测试用例)
    - 防止SQL注入
    - 防止XSS攻击
    - 验证请求体大小限制

12. **性能测试** (2 个测试用例)
    - 列表查询响应时间 < 200ms
    - 树形查询响应时间 < 500ms

**总计：49 个集成测试用例**

### 前端组件测试 (CategoryList.spec.ts)

#### 测试套件

1. **组件初始化** (4 个测试用例)
2. **分类列表渲染** (4 个测试用例)
3. **树形展开/折叠** (3 个测试用例)
4. **搜索功能** (6 个测试用例)
5. **状态切换** (3 个测试用例)
6. **新增分类** (5 个测试用例)
7. **编辑分类** (3 个测试用例)
8. **删除分类** (5 个测试用例)
9. **批量操作** (3 个测试用例)
10. **数据导出** (2 个测试用例)
11. **数据导入** (5 个测试用例)
12. **分页功能** (3 个测试用例)
13. **辅助方法** (3 个测试用例)
14. **错误处理** (2 个测试用例)
15. **性能测试** (1 个测试用例)
16. **可访问性** (2 个测试用例)

**总计：54 个组件测试用例**

### 前端 API 测试 (categoryApi.spec.ts)

#### 测试套件

1. **getCategoryList** (8 个测试用例)
2. **getCategoryTree** (3 个测试用例)
3. **getCategoryById** (3 个测试用例)
4. **createCategory** (6 个测试用例)
5. **updateCategory** (4 个测试用例)
6. **deleteCategory** (5 个测试用例)
7. **updateCategoryStatus** (4 个测试用例)
8. **exportCategories** (3 个测试用例)
9. **importCategories** (5 个测试用例)
10. **batchDeleteCategories** (4 个测试用例)
11. **Token 验证** (3 个测试用例)
12. **错误处理** (4 个测试用例)
13. **请求拦截** (3 个测试用例)
14. **响应拦截** (3 个测试用例)
15. **并发请求** (2 个测试用例)
16. **性能测试** (2 个测试用例)

**总计：62 个 API 测试用例**

## 测试数据说明

### 测试数据范围

测试数据 SQL 脚本 (`test-data-category.sql`) 包含：

1. **基础数据** (ID: 1001-1999)
   - 5 个一级分类
   - 15 个二级分类
   - 20 个三级分类

2. **边界测试数据** (ID: 2001-2999)
   - 名称长度边界
   - 特殊字符
   - 排序边界

3. **约束测试数据** (ID: 3001-3999)
   - 有商品关联的分类
   - 无商品关联的分类
   - 有子分类的分类

4. **唯一性测试数据** (ID: 4001-4999)
   - 不同父分类下的同名分类

5. **层级结构测试数据** (ID: 5001-5999)
   - 完整的三级分类链

6. **性能测试数据** (ID: 6001-6999)
   - 100 个一级分类
   - 50 个二级分类

7. **状态切换测试数据** (ID: 8001-8999)
8. **统计测试数据** (ID: 9001-9999)

### 测试商品数据

- 3 个测试商品 (ID: 9001-9003)
- 关联到分类 3001

## 测试覆盖率目标

| 类型 | 目标 | 说明 |
|------|------|------|
| 语句覆盖率 | >= 80% | 代码语句被执行的比例 |
| 分支覆盖率 | >= 75% | 条件分支被执行的比例 |
| 函数覆盖率 | 100% | 函数被调用的比例 |

## 测试最佳实践

### 1. 测试命名

使用描述性命名，格式：`should + 预期行为 + when + 触发条件`

```java
// ✅ 好的命名
shouldCreateLevel1CategorySuccessfully()
shouldFailWhenCategoryNameDuplicates()

// ❌ 不好的命名
testCreate()
test1()
```

### 2. 测试结构

使用 AAA 模式 (Arrange-Act-Assert)

```java
@Test
void shouldUpdateCategorySuccessfully() {
    // Arrange - 准备测试数据
    Category updateCategory = new Category();
    updateCategory.setId(1L);
    updateCategory.setName("更新后");

    // Act - 执行被测方法
    boolean result = categoryService.updateCategory(updateCategory);

    // Assert - 验证结果
    assertThat(result).isTrue();
}
```

### 3. Mock 使用

- 只 mock 外部依赖
- 不 mock 业务逻辑
- 验证 mock 调用次数

### 4. 测试独立性

- 每个测试独立运行
- 测试之间无依赖
- 使用 `@BeforeEach` 初始化

## 常见问题

### Q1: 测试数据库连接失败

**A:** 检查 `application-test.yml` 配置，确保 H2 数据库配置正确。

### Q2: 前端测试报错 "Cannot find module"

**A:** 确保已安装所有依赖：
```bash
cd frontend
npm install
```

### Q3: 测试覆盖率未达标

**A:** 查看覆盖率报告，补充缺失的测试用例：
```bash
# 后端
open target/site/jacoco/index.html

# 前端
open coverage/index.html
```

### Q4: Mockito 版本兼容性问题

**A:** 确保 `pom.xml` 中的 Mockito 版本与 JUnit 5 兼容。

## 测试报告

### 后端测试报告

- 单元测试报告：`target/surefire-reports/`
- 集成测试报告：`target/failsafe-reports/`
- 覆盖率报告：`target/site/jacoco/index.html`

### 前端测试报告

- 测试报告：`coverage/index.html`
- 覆盖率报告：`coverage/`

## 持续集成

### CI/CD 配置示例

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 8
        uses: actions/setup-java@v2
        with:
          java-version: '8'
      - name: Run tests
        run: |
          cd backend
          mvn test

  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '16'
      - name: Run tests
        run: |
          cd frontend
          npm install
          npm run test:run
```

## 测试维护

### 测试更新时机

1. 新增功能时添加测试
2. 修复 Bug 时添加回归测试
3. 重构代码时更新测试
4. 定期审查和优化测试

### 测试清理

- 删除过时的测试
- 合并重复的测试
- 优化慢速测试
- 提高测试可读性

## 总结

本测试套件提供了：

- ✅ **209 个测试用例**
- ✅ **完整的测试覆盖**
- ✅ **规范的测试结构**
- ✅ **详细的测试数据**
- ✅ **清晰的文档说明**

遵循 TDD 原则，确保代码质量和可维护性！
