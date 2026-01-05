# 商品分类管理 - 测试运行指南

## 环境要求

- Java 8+
- Maven 3.6+

## 快速开始

### 1. 安装 Maven

#### Windows
```powershell
# 使用 Chocolatey
choco install maven

# 或者手动下载安装
# 1. 下载 https://maven.apache.org/download.cgi
# 2. 解压到 C:\Program Files\Apache\maven
# 3. 添加到环境变量 PATH: C:\Program Files\Apache\maven\bin
# 4. 验证: mvn -version
```

#### Linux/Mac
```bash
# 使用包管理器
# Ubuntu/Debian
sudo apt-get install maven

# macOS (Homebrew)
brew install maven

# 验证
mvn -version
```

### 2. 运行测试

```bash
# 进入后端目录
cd backend

# 清理并编译
mvn clean compile

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=CategoryServiceTest
mvn test -Dtest=CategoryControllerTest

# 运行测试并生成覆盖率报告
mvn clean test jacoco:report

# 查看覆盖率报告
# 报告位置: backend/target/site/jacoco/index.html
```

### 3. 测试输出示例

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.inventory.service.CategoryServiceTest
[INFO] Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Running com.inventory.controller.CategoryControllerTest
[INFO] Tests run: 49, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 93, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## 测试说明

### 测试类型

1. **单元测试** (CategoryServiceTest)
   - 使用 Mockito 模拟依赖
   - 测试业务逻辑
   - 无需数据库
   - 运行速度快

2. **集成测试** (CategoryControllerTest)
   - 使用 @SpringBootTest
   - 测试完整流程
   - 使用 H2 内存数据库
   - 测试 API 端点

### 测试覆盖的功能

#### 分类创建 (10 个测试)
- ✅ 创建一级分类
- ✅ 创建二级分类
- ✅ 创建三级分类
- ❌ 尝试创建四级分类（应失败）
- ❌ 创建重复名称分类（应失败）
- ✅ 不同级创建同名分类
- ❌ 空名称（应失败）
- ❌ 超长名称（应失败）
- ❌ 父分类不存在（应失败）
- ✅ 自定义排序号

#### 分类更新 (3 个测试)
- ✅ 更新分类基本信息
- ❌ 更新不存在的分类（应失败）
- ❌ 更新后名称重复（应失败）

#### 分类删除 (5 个测试)
- ✅ 删除无商品关联的分类
- ❌ 删除有商品关联的分类（应失败）
- ❌ 删除有子分类的分类（应失败）
- ❌ 删除不存在的分类（应失败）
- ✅ 删除禁用的分类

#### 分类查询 (8 个测试)
- ✅ 获取分类列表
- ✅ 按名称搜索
- ✅ 按状态过滤
- ✅ 按层级过滤
- ✅ 获取分类详情
- ✅ 获取分类树
- ✅ 获取启用的分类树
- ✅ 获取子分类

#### 状态切换 (4 个测试)
- ✅ 启用分类
- ✅ 禁用分类
- ❌ 切换不存在分类的状态（应失败）
- ✅ 切换状态后查询验证

#### 层级校验 (4 个测试)
- ✅ 计算一级分类层级
- ✅ 计算二级分类层级
- ✅ 计算三级分类层级
- ❌ 尝试计算四级分类（应失败）

#### 名称唯一性 (4 个测试)
- ✅ 同级分类名称唯一
- ❌ 同级创建重复名称（应失败）
- ✅ 不同级可以同名
- ✅ 更新时排除当前ID

#### 批量操作 (2 个测试)
- ✅ 批量创建分类
- ⚠️ 部分失败处理

## 常见问题

### Q: 测试失败怎么办？
A: 检查测试日志，查看具体失败原因。常见问题：
- 依赖未下载完整：运行 `mvn clean install`
- 端口冲突：修改 application-test.yml 中的端口
- H2 数据库问题：删除 target 目录重新测试

### Q: 如何只运行一个测试方法？
A:
```bash
mvn test -Dtest=CategoryServiceTest#testCreateRootCategory
```

### Q: 测试太慢怎么办？
A: 单元测试很快（秒级），集成测试较慢（10秒级）。
可以只运行单元测试：
```bash
mvn test -Dtest=*ServiceTest
```

### Q: 如何查看详细日志？
A: 在 pom.xml 中调整日志级别，或运行：
```bash
mvn test -X
```

## 下一步

测试通过后，可以：

1. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

2. **访问 API 文档**
   ```
   http://localhost:8080/doc.html
   ```

3. **使用 Postman/curl 测试 API**
   ```bash
   curl -X POST http://localhost:8080/api/categories \
     -H "Content-Type: application/json" \
     -d '{"name":"电子产品","sortOrder":1,"status":1}'
   ```

4. **开始前端开发**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
