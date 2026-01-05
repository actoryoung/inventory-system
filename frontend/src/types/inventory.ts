/**
 * 库存接口类型定义
 */

/**
 * 库存实体
 */
export interface Inventory {
  id?: number
  productId: number
  productSku?: string
  productName?: string
  categoryId?: number
  categoryName?: string
  warehouseId?: number
  quantity: number
  warningStock: number
  isLowStock?: boolean
  amount?: number
  createdAt?: string
  updatedAt?: string
}

/**
 * 库存调整数据
 */
export interface InventoryAdjustData {
  type: 'add' | 'reduce' | 'set'
  quantity: number
  reason: string
}

/**
 * 库存查询参数
 */
export interface InventoryQuery {
  productName?: string
  categoryId?: number
  lowStock?: boolean
  page?: number
  size?: number
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

/**
 * API 响应格式
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
