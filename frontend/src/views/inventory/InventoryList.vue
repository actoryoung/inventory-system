<template>
  <div class="inventory-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">库存管理</h2>
      <div class="summary-cards">
        <div class="summary-card">
          <div class="label">总商品数</div>
          <div class="value">{{ summary.totalProducts }}</div>
        </div>
        <div class="summary-card">
          <div class="label">总库存</div>
          <div class="value">{{ summary.totalQuantity }}</div>
        </div>
        <div class="summary-card warning">
          <div class="label">低库存</div>
          <div class="value">{{ summary.lowStockCount }}</div>
        </div>
        <div class="summary-card">
          <div class="label">库存总额</div>
          <div class="value">¥{{ formatAmount(summary.totalAmount) }}</div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchForm.productName"
        placeholder="商品名称"
        :prefix-icon="Search"
        clearable
        style="width: 200px"
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
      <el-checkbox
        v-model="searchForm.lowStock"
        style="margin-left: 10px"
        @change="handleSearch"
      >
        只看低库存
      </el-checkbox>
      <el-button type="primary" :icon="Search" style="margin-left: 10px" @click="loadInventory">
        搜索
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 库存表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="productSku" label="SKU编码" width="120" />
      <el-table-column prop="productName" label="商品名称" min-width="150" />
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="quantity" label="库存数量" width="100" align="center">
        <template #default="{ row }">
          <span :class="{ 'low-stock': row.isLowStock }">{{ row.quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="warningStock" label="预警值" width="100" align="center" />
      <el-table-column prop="amount" label="库存金额" width="120" align="right">
        <template #default="{ row }">
          ¥{{ formatAmount(row.amount) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isLowStock" type="danger" size="small">低库存</el-tag>
          <el-tag v-else type="success" size="small">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleAdjust(row)">
            调整
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
        @size-change="loadInventory"
        @current-change="loadInventory"
      />
    </div>

    <!-- 库存调整对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="调整库存"
      width="500px"
    >
      <el-form ref="adjustFormRef" :model="adjustForm" label-width="100px">
        <el-form-item label="调整类型">
          <el-radio-group v-model="adjustForm.type">
            <el-radio label="add">增加</el-radio>
            <el-radio label="reduce">减少</el-radio>
            <el-radio label="set">设置</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number
            v-model="adjustForm.quantity"
            :min="adjustForm.type === 'set' ? 0 : 1"
            :precision="0"
            controls-position="right"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="调整原因" required>
          <el-input
            v-model="adjustForm.reason"
            placeholder="请输入调整原因（如：盘点入库、损耗、报损等）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjustSubmitting" @click="handleAdjustSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import inventoryApi from '@/api/inventory'
import categoryApi from '@/api/category'
import type { Inventory } from '@/types/inventory'

// 响应式数据
const loading = ref(false)
const tableData = ref<Inventory[]>([])
const categoryTree = ref<any[]>([])
const adjustDialogVisible = ref(false)
const adjustSubmitting = ref(false)
const currentInventory = ref<Inventory | null>(null)

// 汇总数据
const summary = reactive({
  totalProducts: 0,
  totalQuantity: 0,
  lowStockCount: 0,
  totalAmount: 0
})

// 搜索表单
const searchForm = reactive({
  productName: '',
  categoryId: undefined as number | undefined,
  lowStock: false
})

// 调整表单
const adjustForm = reactive({
  type: 'add' as 'add' | 'reduce' | 'set',
  quantity: 1,
  reason: ''
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

// 扁平化分类树
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

// 加载汇总数据
async function loadSummary() {
  try {
    const res = await inventoryApi.getInventorySummary()
    if (res.code === 200) {
      const data = res.data
      summary.totalProducts = data.length
      summary.totalQuantity = data.reduce((sum, item) => sum + item.totalQuantity, 0)
      summary.lowStockCount = data.filter(item => item.isLowStock).length
      summary.totalAmount = data.reduce((sum, item) => sum + item.totalAmount, 0)
    }
  } catch (error) {
    console.error('加载汇总失败:', error)
  }
}

// 加载库存列表
async function loadInventory() {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }

    const res = await inventoryApi.getInventoryList(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载库存失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
function handleSearch() {
  pagination.page = 1
  loadInventory()
}

// 处理重置
function handleReset() {
  searchForm.productName = ''
  searchForm.categoryId = undefined
  searchForm.lowStock = false
  pagination.page = 1
  loadInventory()
}

// 格式化金额
function formatAmount(amount: number | undefined) {
  if (amount === undefined) return '0.00'
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 处理调整库存
function handleAdjust(row: Inventory) {
  currentInventory.value = row
  adjustForm.type = 'add'
  adjustForm.quantity = 1
  adjustForm.reason = ''
  adjustDialogVisible.value = true
}

// 提交调整
async function handleAdjustSubmit() {
  if (!adjustForm.reason.trim()) {
    ElMessage.warning('请输入调整原因')
    return
  }

  if (!currentInventory.value) return

  adjustSubmitting.value = true
  try {
    const res = await inventoryApi.adjustInventory(currentInventory.value.id!, adjustForm)
    if (res.code === 200) {
      ElMessage.success(`库存调整成功：${res.data.oldQuantity} → ${res.data.newQuantity}`)
      adjustDialogVisible.value = false
      await loadInventory()
      await loadSummary()
    }
  } catch (error) {
    console.error('调整库存失败:', error)
  } finally {
    adjustSubmitting.value = false
  }
}

// 初始化
onMounted(() => {
  loadCategories()
  loadSummary()
  loadInventory()
})
</script>

<style scoped lang="scss">
.inventory-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.summary-cards {
  display: flex;
  gap: 15px;
}

.summary-card {
  flex: 1;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;

  &.warning {
    border-left-color: #f56c6c;
  }

  .label {
    font-size: 12px;
    color: #909399;
    margin-bottom: 8px;
  }

  .value {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
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
