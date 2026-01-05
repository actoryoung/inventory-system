<template>
  <div class="inbound-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">入库管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleCreate">
        新增入库单
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-select
        v-model="searchForm.productId"
        placeholder="选择商品"
        clearable
        filterable
        style="width: 200px"
        @change="handleSearch"
      >
        <el-option
          v-for="product in productList"
          :key="product.id"
          :label="`${product.sku} - ${product.name}`"
          :value="product.id"
        />
      </el-select>
      <el-select
        v-model="searchForm.status"
        placeholder="选择状态"
        clearable
        style="width: 120px; margin-left: 10px"
        @change="handleSearch"
      >
        <el-option label="待审核" :value="0" />
        <el-option label="已审核" :value="1" />
        <el-option label="已作废" :value="2" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 240px; margin-left: 10px"
        @change="handleDateChange"
      />
      <el-button type="primary" :icon="Search" style="margin-left: 10px" @click="loadInboundList">
        搜索
      </el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 入库单表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="inboundNo" label="入库单号" width="150" />
      <el-table-column prop="productSku" label="商品SKU" width="120" />
      <el-table-column prop="productName" label="商品名称" min-width="150" />
      <el-table-column prop="quantity" label="入库数量" width="100" align="center" />
      <el-table-column prop="supplier" label="供应商" width="150" />
      <el-table-column prop="inboundDate" label="入库日期" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.inboundDate) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="info" size="small" @click="handleView(row)">
            详情
          </el-button>
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.status === 0"
            type="success"
            size="small"
            @click="handleApprove(row)"
          >
            审核
          </el-button>
          <el-button
            v-if="row.status === 0"
            type="danger"
            size="small"
            @click="handleDelete(row)"
          >
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
        @size-change="loadInboundList"
        @current-change="loadInboundList"
      />
    </div>

    <!-- 入库单表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select
            v-model="form.productId"
            placeholder="请选择商品"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="product in enabledProductList"
              :key="product.id"
              :label="`${product.sku} - ${product.name}`"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number
            v-model="form.quantity"
            :min="1"
            :max="999999"
            :precision="0"
            controls-position="right"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="供应商" prop="supplier">
          <el-input
            v-model="form.supplier"
            placeholder="请输入供应商名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="入库日期" prop="inboundDate">
          <el-date-picker
            v-model="form.inboundDate"
            type="datetime"
            placeholder="选择入库日期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="入库单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="入库单号">{{ detail.inboundNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detail.status)" size="small">
            {{ getStatusText(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品SKU">{{ detail.productSku }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detail.productName }}</el-descriptions-item>
        <el-descriptions-item label="入库数量">{{ detail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detail.supplier }}</el-descriptions-item>
        <el-descriptions-item label="入库日期">{{ formatDateTime(detail.inboundDate) }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detail.createdBy }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(detail.createdAt) }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.status === 1" label="审核人">{{ detail.approvedBy }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.status === 1" label="审核时间">
          {{ formatDateTime(detail.approvedAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import inboundApi from '@/api/inbound'
import productApi from '@/api/product'
import type { Inbound, InboundForm, InboundQuery } from '@/types/inbound'
import { InboundStatus, InboundStatusMap, InboundStatusTypeMap } from '@/types/inbound'

// 响应式数据
const loading = ref(false)
const tableData = ref<Inbound[]>([])
const productList = ref<any[]>([])
const enabledProductList = ref<any[]>([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)
const dialogTitle = ref('新增入库单')
const formRef = ref<FormInstance>()
const currentId = ref<number | undefined>()
const dateRange = ref<string[]>()

// 详情数据
const detail = reactive<Partial<Inbound>>({
  inboundNo: '',
  productSku: '',
  productName: '',
  quantity: 0,
  supplier: '',
  inboundDate: '',
  status: InboundStatus.Pending,
  createdBy: '',
  createdAt: '',
  approvedBy: '',
  approvedAt: '',
  remark: ''
})

// 搜索表单
const searchForm = reactive<InboundQuery>({
  productId: undefined,
  status: undefined,
  startDate: undefined,
  endDate: undefined
})

// 表单数据
const form = reactive<InboundForm>({
  productId: undefined,
  quantity: undefined,
  supplier: '',
  inboundDate: '',
  remark: ''
})

// 表单验证规则
const formRules: FormRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [
    { required: true, message: '请输入入库数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '入库数量必须大于0', trigger: 'blur' }
  ],
  supplier: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { min: 1, max: 100, message: '供应商名称长度在1-100字符之间', trigger: 'blur' }
  ],
  inboundDate: [{ required: true, message: '请选择入库日期', trigger: 'change' }]
}

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 加载商品列表
async function loadProducts() {
  try {
    const res = await productApi.getProductList({ page: 1, size: 1000 })
    if (res.code === 200) {
      productList.value = res.data.records || []
      enabledProductList.value = productList.value.filter(p => p.status === 1)
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
  }
}

// 加载入库单列表
async function loadInboundList() {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }

    const res = await inboundApi.getInboundList(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载入库单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
function handleSearch() {
  pagination.page = 1
  loadInboundList()
}

// 处理日期范围变化
function handleDateChange(value: string[]) {
  if (value && value.length === 2) {
    searchForm.startDate = value[0]
    searchForm.endDate = value[1]
  } else {
    searchForm.startDate = undefined
    searchForm.endDate = undefined
  }
  handleSearch()
}

// 处理重置
function handleReset() {
  searchForm.productId = undefined
  searchForm.status = undefined
  searchForm.startDate = undefined
  searchForm.endDate = undefined
  dateRange.value = undefined
  pagination.page = 1
  loadInboundList()
}

// 处理创建
function handleCreate() {
  dialogTitle.value = '新增入库单'
  currentId.value = undefined
  dialogVisible.value = true
}

// 处理查看
async function handleView(row: Inbound) {
  try {
    const res = await inboundApi.getInboundDetail(row.id!)
    if (res.code === 200) {
      Object.assign(detail, res.data)
      detailVisible.value = true
    }
  } catch (error) {
    console.error('获取入库单详情失败:', error)
  }
}

// 处理编辑
function handleEdit(row: Inbound) {
  dialogTitle.value = '编辑入库单'
  currentId.value = row.id
  form.productId = row.productId
  form.quantity = row.quantity
  form.supplier = row.supplier
  form.inboundDate = row.inboundDate
  form.remark = row.remark || ''
  dialogVisible.value = true
}

// 处理删除
async function handleDelete(row: Inbound) {
  try {
    await ElMessageBox.confirm('确定要删除该入库单吗？', '提示', {
      type: 'warning'
    })
    const res = await inboundApi.deleteInbound(row.id!)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadInboundList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除入库单失败:', error)
    }
  }
}

// 处理审核
async function handleApprove(row: Inbound) {
  try {
    await ElMessageBox.confirm('审核通过后将自动增加库存，确定要审核吗？', '提示', {
      type: 'warning'
    })
    const res = await inboundApi.approveInbound(row.id!)
    if (res.code === 200) {
      ElMessage.success('审核成功')
      loadInboundList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核入库单失败:', error)
    }
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    const res = currentId.value
      ? await inboundApi.updateInbound(currentId.value, form)
      : await inboundApi.createInbound(form)

    if (res.code === 200) {
      ElMessage.success(currentId.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadInboundList()
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 对话框关闭
function handleDialogClosed() {
  formRef.value?.resetFields()
  form.productId = undefined
  form.quantity = undefined
  form.supplier = ''
  form.inboundDate = ''
  form.remark = ''
}

// 格式化日期时间
function formatDateTime(dateStr: string | undefined) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取状态文本
function getStatusText(status: number | undefined) {
  if (status === undefined) return '-'
  return InboundStatusMap[status] || '未知'
}

// 获取状态类型
function getStatusType(status: number | undefined) {
  if (status === undefined) return 'info'
  return InboundStatusTypeMap[status] || 'info'
}

// 初始化
onMounted(() => {
  loadProducts()
  loadInboundList()
})
</script>

<style scoped lang="scss">
.inbound-container {
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
</style>
