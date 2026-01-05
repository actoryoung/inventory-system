<template>
  <div class="product-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">商品管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增商品</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.name"
        placeholder="商品名称"
        :prefix-icon="Search"
        clearable
        style="width: 200px"
        @input="handleSearch"
      />
      <el-input
        v-model="searchForm.sku"
        placeholder="SKU编码"
        clearable
        style="width: 180px; margin-left: 10px"
        @input="handleSearch"
      />
      <el-select
        v-model="searchForm.categoryId"
        placeholder="选择分类"
        clearable
        filterable
        style="width: 150px; margin-left: 10px"
        @change="handleSearch"
      >
        <el-option
          v-for="cat in categoryTree"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
      <el-select
        v-model="searchForm.status"
        placeholder="选择状态"
        clearable
        style="width: 120px; margin-left: 10px"
        @change="handleSearch"
      >
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" style="margin-left: 10px" @click="loadProducts">
        搜索
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 商品表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%; margin-top: 20px"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="sku" label="SKU编码" width="120" />
      <el-table-column prop="name" label="商品名称" min-width="150" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="price" label="销售价格" width="100" align="right">
        <template #default="{ row }">
          ¥{{ row.price.toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="stockQuantity" label="库存" width="80" align="center">
        <template #default="{ row }">
          <span :class="{ 'low-stock': row.stockQuantity <= row.warningStock }">
            {{ row.stockQuantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="warningStock" label="预警值" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadProducts"
        @current-change="loadProducts"
      />
    </div>

    <!-- 商品表单对话框 -->
    <ProductForm
      v-model="formVisible"
      :form-data="formData"
      :category-tree="categoryTree"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import productApi from '@/api/product'
import categoryApi from '@/api/category'
import type { Product, ProductFormData } from '@/types/product'
import ProductForm from './ProductForm.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<Product[]>([])
const categoryTree = ref<any[]>([])
const selectedRows = ref<Product[]>([])
const formVisible = ref(false)
const formData = ref<ProductFormData>({
  sku: '',
  name: '',
  categoryId: 0,
  unit: '',
  price: 0,
  costPrice: 0,
  warningStock: 0,
  status: 1
})

// 搜索表单
const searchForm = reactive({
  name: '',
  sku: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 加载分类树
async function loadCategories() {
  try {
    const res = await categoryApi.getEnabledTree()
    if (res.code === 200) {
      categoryTree.value = flattenCategories(res.data || [])
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 扁平化分类树（用于下拉选择）
function flattenCategories(categories: any[]): any[] {
  const result: any[] = []
  categories.forEach((cat) => {
    result.push(cat)
    if (cat.children && cat.children.length > 0) {
      result.push(...flattenCategories(cat.children))
    }
  })
  return result
}

// 加载商品列表
async function loadProducts() {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }

    const res = await productApi.getList(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载商品失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
function handleSearch() {
  pagination.page = 1
  loadProducts()
}

// 处理重置
function handleReset() {
  searchForm.name = ''
  searchForm.sku = ''
  searchForm.categoryId = undefined
  searchForm.status = undefined
  pagination.page = 1
  loadProducts()
}

// 处理选择变化
function handleSelectionChange(selection: Product[]) {
  selectedRows.value = selection
}

// 处理新增
function handleAdd() {
  formData.value = {
    sku: '',
    name: '',
    categoryId: 0,
    unit: '',
    price: 0,
    costPrice: 0,
    warningStock: 0,
    status: 1
  }
  formVisible.value = true
}

// 处理编辑
function handleEdit(row: Product) {
  formData.value = {
    id: row.id,
    sku: row.sku,
    name: row.name,
    categoryId: row.categoryId,
    unit: row.unit,
    price: row.price,
    costPrice: row.costPrice,
    specification: row.specification,
    description: row.description,
    warningStock: row.warningStock,
    status: row.status,
    remark: row.remark
  }
  formVisible.value = true
}

// 处理删除
async function handleDelete(row: Product) {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品"${row.name}"吗？删除后不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const res = await productApi.delete(row.id!)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadProducts()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 处理状态切换
async function handleStatusChange(row: Product) {
  try {
    const res = await productApi.toggleStatus(row.id!, row.status!)
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      // 失败时恢复原状态
      row.status = row.status === 1 ? 0 : 1
    }
  } catch (error) {
    // 失败时恢复原状态
    row.status = row.status === 1 ? 0 : 1
    console.error('状态更新失败:', error)
  }
}

// 处理表单提交
async function handleFormSubmit(data: ProductFormData) {
  loading.value = true
  try {
    let res
    if (data.id) {
      res = await productApi.update(data.id, data)
    } else {
      res = await productApi.create(data)
    }

    if (res.code === 200) {
      ElMessage.success(data.id ? '更新成功' : '创建成功')
      formVisible.value = false
      await loadProducts()
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped lang="scss">
.product-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.low-stock {
  color: #f56c6c;
  font-weight: bold;
}
</style>
