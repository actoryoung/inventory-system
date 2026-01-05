# Vue 3 商品列表页面片段

```vue
<template>
  <div class="product-list">
    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="商品名称">
        <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable />
      </el-form-item>
      <el-form-item label="商品编码">
        <el-input v-model="searchForm.sku" placeholder="请输入商品编码" clearable />
      </el-form-item>
      <el-form-item label="分类">
        <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
          <el-option
            v-for="item in categoryList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增商品
      </el-button>
      <el-button type="success" @click="handleImport">
        <el-icon><Upload /></el-icon>
        导入
      </el-button>
      <el-button type="warning" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出
      </el-button>
      <el-button type="danger" :disabled="!selectedIds.length" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      ref="tableRef"
      :data="tableData"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      border
      stripe
    >
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="sku" label="商品编码" width="130" show-overflow-tooltip />
      <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="price" label="价格" width="100" align="right">
        <template #default="{ row }">
          <span class="price">¥{{ row.price?.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" align="center" />
      <el-table-column prop="stock" label="库存" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStockType(row)">
            {{ row.stock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="warningStock" label="预警值" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleStock(row)">库存</el-button>
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

    <!-- 商品表单对话框 -->
    <product-form
      v-model="formVisible"
      :data="currentRow"
      @refresh="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Upload, Download, Delete } from '@element-plus/icons-vue';
import { getProductList, deleteProduct, batchDeleteProducts, updateStatus } from '@/api/product';
import { getCategoryList } from '@/api/category';
import ProductForm from './ProductForm.vue';

const tableRef = ref();
const loading = ref(false);
const formVisible = ref(false);
const currentRow = ref<any>(null);
const selectedIds = ref<number[]>([]);

const searchForm = reactive({
  name: '',
  sku: '',
  categoryId: null as number | null
});

const tableData = ref([]);
const categoryList = ref([]);

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 获取库存状态类型
const getStockType = (row: any) => {
  if (row.stock <= 0) return 'info';
  if (row.stock <= row.warningStock) return 'danger';
  if (row.stock <= row.warningStock * 2) return 'warning';
  return 'success';
};

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

// 获取分类列表
const fetchCategories = async () => {
  const { data } = await getCategoryList({ page: 1, size: 1000 });
  categoryList.value = data.records;
};

// 搜索
const handleSearch = () => {
  pagination.page = 1;
  fetchData();
};

// 重置
const handleReset = () => {
  searchForm.name = '';
  searchForm.sku = '';
  searchForm.categoryId = null;
  handleSearch();
};

// 新增
const handleAdd = () => {
  currentRow.value = null;
  formVisible.value = true;
};

// 编辑
const handleEdit = (row: any) => {
  currentRow.value = { ...row };
  formVisible.value = true;
};

// 库存调整
const handleStock = (row: any) => {
  // 打开库存调整对话框
};

// 状态切换
const handleStatusChange = async (row: any) => {
  try {
    await updateStatus({ id: row.id, status: row.status });
    ElMessage.success('状态更新成功');
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1;
    ElMessage.error('状态更新失败');
  }
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

// 导入
const handleImport = () => {
  // 导入逻辑
};

// 导出
const handleExport = () => {
  // 导出逻辑
};

onMounted(() => {
  fetchCategories();
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
    margin-bottom: 16px;
  }

  .toolbar {
    background: #fff;
    padding: 16px 20px;
    border-radius: 4px;
    margin-bottom: 16px;

    .el-button {
      margin-right: 10px;
    }
  }

  .price {
    color: #F56C6C;
    font-weight: 500;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
```
