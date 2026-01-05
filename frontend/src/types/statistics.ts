/**
 * 统计报表接口类型定义
 */

/**
 * 数据看板
 */
export interface Dashboard {
  totalProducts: number
  totalQuantity: number
  totalAmount: number
  lowStockCount: number
}

/**
 * 出入库趋势
 */
export interface Trend {
  dates: string[]
  inboundQuantities: number[]
  outboundQuantities: number[]
}

/**
 * 分类分布
 */
export interface CategoryDistribution {
  categoryName: string
  quantity: number
  percentage: number
}

/**
 * 低库存预警
 */
export interface LowStock {
  productId: number
  productSku: string
  productName: string
  categoryName: string
  quantity: number
  warningStock: number
  shortage: number
}

/**
 * API 响应格式
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
