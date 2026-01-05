<template>
  <div class="statistics-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">统计报表</h2>
      <el-button type="primary" :icon="Refresh" @click="refreshData" :loading="loading">
        刷新数据
      </el-button>
    </div>

    <!-- 数据看板 -->
    <div class="dashboard-cards">
      <div class="card-item blue">
        <div class="card-icon">
          <el-icon :size="32"><Goods /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">总商品数</div>
          <div class="card-value">{{ dashboard.totalProducts }}</div>
        </div>
      </div>
      <div class="card-item green">
        <div class="card-icon">
          <el-icon :size="32"><Box /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">总库存量</div>
          <div class="card-value">{{ formatNumber(dashboard.totalQuantity) }}</div>
        </div>
      </div>
      <div class="card-item purple">
        <div class="card-icon">
          <el-icon :size="32"><Coin /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">库存总额</div>
          <div class="card-value">¥{{ formatAmount(dashboard.totalAmount) }}</div>
        </div>
      </div>
      <div class="card-item red">
        <div class="card-icon">
          <el-icon :size="32"><Warning /></el-icon>
        </div>
        <div class="card-content">
          <div class="card-label">低库存数量</div>
          <div class="card-value">{{ dashboard.lowStockCount }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-container">
        <div class="chart-header">
          <h3>出入库趋势（近30天）</h3>
        </div>
        <div ref="trendChartRef" class="chart-content" v-loading="trendLoading"></div>
      </div>
      <div class="chart-container">
        <div class="chart-header">
          <h3>库存分类占比</h3>
        </div>
        <div ref="categoryChartRef" class="chart-content" v-loading="categoryLoading"></div>
      </div>
    </div>

    <!-- 低库存预警列表 -->
    <div class="low-stock-section">
      <div class="section-header">
        <h3>低库存预警列表</h3>
        <el-tag type="danger" size="small" v-if="lowStockList.length > 0">
          共 {{ lowStockList.length }} 件商品库存不足
        </el-tag>
      </div>
      <el-table
        :data="lowStockList"
        border
        stripe
        style="width: 100%"
        v-loading="lowStockLoading"
      >
        <el-table-column prop="productSku" label="商品SKU" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="quantity" label="当前库存" width="100" align="center">
          <template #default="{ row }">
            <span class="low-stock-number">{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningStock" label="预警值" width="100" align="center" />
        <el-table-column prop="shortage" label="缺货数量" width="100" align="center">
          <template #default="{ row }">
            <span class="shortage-number">{{ row.shortage }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!lowStockLoading && lowStockList.length === 0" description="暂无低库存商品" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Goods, Box, Coin, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import statisticsApi from '@/api/statistics'
import type { Dashboard, Trend, CategoryDistribution, LowStock } from '@/types/statistics'

// 响应式数据
const loading = ref(false)
const trendLoading = ref(false)
const categoryLoading = ref(false)
const lowStockLoading = ref(false)

const dashboard = ref<Dashboard>({
  totalProducts: 0,
  totalQuantity: 0,
  totalAmount: 0,
  lowStockCount: 0
})

const trendData = ref<Trend>({
  dates: [],
  inboundQuantities: [],
  outboundQuantities: []
})

const categoryData = ref<CategoryDistribution[]>([])
const lowStockList = ref<LowStock[]>([])

// 图表引用
const trendChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null

// 加载看板数据
async function loadDashboard() {
  try {
    const res = await statisticsApi.getDashboard()
    if (res.code === 200) {
      dashboard.value = res.data
    }
  } catch (error) {
    console.error('加载看板数据失败:', error)
  }
}

// 加载趋势数据
async function loadTrend() {
  trendLoading.value = true
  try {
    const res = await statisticsApi.getTrend(30)
    if (res.code === 200) {
      trendData.value = res.data
      renderTrendChart()
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  } finally {
    trendLoading.value = false
  }
}

// 加载分类分布数据
async function loadCategoryDistribution() {
  categoryLoading.value = true
  try {
    const res = await statisticsApi.getCategoryDistribution()
    if (res.code === 200) {
      categoryData.value = res.data
      renderCategoryChart()
    }
  } catch (error) {
    console.error('加载分类分布失败:', error)
  } finally {
    categoryLoading.value = false
  }
}

// 加载低库存列表
async function loadLowStockList() {
  lowStockLoading.value = true
  try {
    const res = await statisticsApi.getLowStockList()
    if (res.code === 200) {
      lowStockList.value = res.data
    }
  } catch (error) {
    console.error('加载低库存列表失败:', error)
  } finally {
    lowStockLoading.value = false
  }
}

// 渲染趋势图
function renderTrendChart() {
  if (!trendChartRef.value) return

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['入库数量', '出库数量'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: trendData.value.dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '数量'
    },
    series: [
      {
        name: '入库数量',
        type: 'line',
        smooth: true,
        data: trendData.value.inboundQuantities,
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      },
      {
        name: '出库数量',
        type: 'line',
        smooth: true,
        data: trendData.value.outboundQuantities,
        itemStyle: { color: '#E6A23C' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }
          ])
        }
      }
    ]
  }

  trendChart.setOption(option)
}

// 渲染分类占比图
function renderCategoryChart() {
  if (!categoryChartRef.value) return

  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: categoryData.value.map(item => ({
          value: item.quantity,
          name: item.categoryName
        }))
      }
    ]
  }

  categoryChart.setOption(option)
}

// 刷新所有数据
async function refreshData() {
  loading.value = true
  try {
    await Promise.all([
      loadDashboard(),
      loadTrend(),
      loadCategoryDistribution(),
      loadLowStockList()
    ])
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    loading.value = false
  }
}

// 格式化数字
function formatNumber(num: number) {
  return num.toLocaleString('zh-CN')
}

// 格式化金额
function formatAmount(amount: number) {
  return amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 窗口大小变化时重新渲染图表
function handleResize() {
  trendChart?.resize()
  categoryChart?.resize()
}

// 初始化
onMounted(() => {
  loadDashboard()
  loadTrend()
  loadCategoryDistribution()
  loadLowStockList()
  window.addEventListener('resize', handleResize)
})

// 清理
onBeforeUnmount(() => {
  trendChart?.dispose()
  categoryChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.statistics-container {
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

// 数据看板卡片
.dashboard-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.card-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 4px solid;

  &.blue {
    border-left-color: #409eff;
    .card-icon {
      background: rgba(64, 158, 255, 0.1);
      color: #409eff;
    }
  }

  &.green {
    border-left-color: #67c23a;
    .card-icon {
      background: rgba(103, 194, 58, 0.1);
      color: #67c23a;
    }
  }

  &.purple {
    border-left-color: #9c27b0;
    .card-icon {
      background: rgba(156, 39, 176, 0.1);
      color: #9c27b0;
    }
  }

  &.red {
    border-left-color: #f56c6c;
    .card-icon {
      background: rgba(245, 108, 108, 0.1);
      color: #f56c6c;
    }
  }

  .card-icon {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    margin-right: 15px;
  }

  .card-content {
    flex: 1;
  }

  .card-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 8px;
  }

  .card-value {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
}

// 图表区域
.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.chart-container {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;

  .chart-header {
    padding: 15px 20px;
    border-bottom: 1px solid #e8e8e8;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .chart-content {
    height: 350px;
  }
}

// 低库存区域
.low-stock-section {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 20px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }
}

.low-stock-number {
  color: #f56c6c;
  font-weight: bold;
}

.shortage-number {
  color: #e6a23c;
  font-weight: bold;
}

// 响应式
@media (max-width: 1200px) {
  .dashboard-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-cards {
    grid-template-columns: 1fr;
  }
}
</style>
