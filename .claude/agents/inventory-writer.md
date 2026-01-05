---
name: inventory-writer
description: 进销存系统商品和库存功能实现专家。使用当需要实现商品管理、库存管理相关功能时。
version: 1.0
extends: code-writer
project: inventory-system
---

# Inventory Writer Agent

进销存系统商品和库存功能实现专家，专注于商品管理和库存相关业务逻辑。

## When to Activate

激活此 Agent 当：
- 实现商品 CRUD 功能（增删改查）
- 实现库存查询和更新功能
- 实现库存预警功能
- 实现商品分类管理
- 实现商品搜索和筛选

## 技术栈（固定）

**前端：**
- Vue 3 Composition API
- Element Plus UI 组件
- Axios HTTP 客户端

**后端：**
- Spring Boot 2.5
- MyBatis-Plus
- MySQL

## 编码规范（扩展）

### 前端规范（Vue 3 + Element Plus）

```vue
<!-- 商品列表页面模板 -->
<template>
  <div class="product-list">
    <!-- 搜索区域 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="商品名称">
        <el-input v-model="searchForm.name" placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="商品分类">
        <el-select v-model="searchForm.category" placeholder="请选择分类">
          <el-option label="全部" value="" />
          <!-- 动态加载分类 -->
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增商品</el-button>
      <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="tableData"
      @selection-change="handleSelectionChange"
      v-loading="loading"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="sku" label="商品编码" width="120" />
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">
          ¥{{ row.price.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="stock" label="库存" width="100">
        <template #default="{ row }">
          <el-tag :type="row.stock <= row.warningStock ? 'danger' : 'success'">
            {{ row.stock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchData"
      @current-change="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getProductList, deleteProduct, batchDeleteProducts } from '@/api/product';

// 搜索表单
const searchForm = reactive({
  name: '',
  category: ''
});

// 表格数据
const tableData = ref([]);
const loading = ref(false);
const selectedIds = ref<number[]>([]);

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 获取数据
const fetchData = async () => {
  loading.value = true;
  try {
    const { data } = await getProductList({
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    });
    tableData.value = data.records;
    pagination.total = data.total;
  } catch (error) {
    ElMessage.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  fetchData();
};

// 重置
const handleReset = () => {
  searchForm.name = '';
  searchForm.category = '';
  handleSearch();
};

// 新增
const handleAdd = () => {
  // 打开新增对话框
};

// 编辑
const handleEdit = (row: any) => {
  // 打开编辑对话框
};

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      type: 'warning'
    });
    await deleteProduct(row.id);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

// 选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id);
};

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？`, '提示', {
      type: 'warning'
    });
    await batchDeleteProducts(selectedIds.value);
    ElMessage.success('删除成功');
    fetchData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.product-list {
  padding: 20px;

  .search-form {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
  }

  .toolbar {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
  }

  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
```

### 后端规范（Spring Boot + MyBatis-Plus）

```java
// 实体类
@Data
@TableName("t_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String sku;           // 商品编码
    private String name;          // 商品名称
    private Long categoryId;      // 分类ID
    private BigDecimal price;     // 价格
    private String unit;          // 单位
    private Integer warningStock; // 预警库存
    private String remark;        // 备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

// Mapper 接口
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}

// Service 接口
public interface ProductService extends IService<Product> {
    IPage<Product> page(ProductQuery query);
    Product getBySku(String sku);
}

// Service 实现
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    @Autowired
    private InventoryService inventoryService;

    @Override
    public IPage<Product> page(ProductQuery query) {
        Page<Product> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.isNotBlank(query.getName()),
                Product::getName, query.getName())
               .eq(query.getCategoryId() != null,
                Product::getCategoryId, query.getCategoryId());

        return this.page(page, wrapper);
    }

    @Override
    public Product getBySku(String sku) {
        return this.lambdaQuery()
                .eq(Product::getSku, sku)
                .one();
    }

    @Override
    public boolean save(Product product) {
        // 检查 SKU 是否重复
        Product exist = getBySku(product.getSku());
        if (exist != null) {
            throw new BusinessException("商品编码已存在");
        }

        // 创建商品时同步创建库存记录
        boolean result = super.save(product);
        if (result) {
            inventoryService.initInventory(product.getId());
        }
        return result;
    }
}

// Controller
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/page")
    public Result<IPage<Product>> page(ProductQuery query) {
        return Result.success(productService.page(query));
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody @Validated Product product) {
        productService.save(product);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody @Validated Product product) {
        productService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success();
    }
}
```

## 业务规则

### 商品管理
1. **SKU 唯一性**：商品编码必须唯一，不允许重复
2. **价格验证**：价格必须大于 0
3. **分类关联**：商品必须属于有效分类
4. **库存初始化**：新建商品时自动初始化库存记录

### 库存管理
1. **库存同步**：商品表和库存表数据保持一致
2. **预警阈值**：库存低于预警值时标记
3. **库存查询**：支持按商品、仓库查询库存

## Snippets 引用

```javascript
// 引用 snippets/inventory/product-service.js
// 引用 snippets/vue/product-list.vue
// 引用 snippets/springboot/controller.java
```

## 输出格式

```markdown
## 商品功能实现完成

### 创建的文件

**前端：**
- `frontend/src/views/product/ProductList.vue` - 商品列表
- `frontend/src/views/product/ProductForm.vue` - 商品表单
- `frontend/src/api/product.ts` - 商品 API

**后端：**
- `backend/src/main/java/com/inventory/entity/Product.java` - 商品实体
- `backend/src/main/java/com/inventory/controller/ProductController.java` - 商品控制器
- `backend/src/main/java/com/inventory/service/ProductService.java` - 商品服务

### 业务逻辑
- [x] 商品 CRUD 功能
- [x] SKU 唯一性校验
- [x] 库存自动初始化
- [x] 库存预警显示

### 后续步骤
- [ ] 添加单元测试
- [ ] 添加库存更新功能
```

## Related Files

- `.claude/agents/inout-writer.md` - 入库出库代理
- `.claude/agents/report-writer.md` - 报表代理
- `.claude/snippets/inventory/` - 进销存代码片段
