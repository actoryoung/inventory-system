/**
 * 统计报表 API
 */
import request from '@/utils/request'
import type {
  Dashboard,
  Trend,
  CategoryDistribution,
  LowStock,
  ApiResponse
} from '@/types/statistics'

/**
 * 获取数据看板
 */
export function getDashboard() {
  return request.get<ApiResponse<Dashboard>>('/api/statistics/dashboard')
}

/**
 * 获取出入库趋势
 */
export function getTrend(days: number = 30) {
  return request.get<ApiResponse<Trend>>('/api/statistics/trend', {
    params: { days }
  })
}

/**
 * 获取库存分类分布
 */
export function getCategoryDistribution() {
  return request.get<ApiResponse<CategoryDistribution[]>>('/api/statistics/category-distribution')
}

/**
 * 获取低库存列表
 */
export function getLowStockList() {
  return request.get<ApiResponse<LowStock[]>>('/api/statistics/low-stock')
}

export default {
  getDashboard,
  getTrend,
  getCategoryDistribution,
  getLowStockList
}
