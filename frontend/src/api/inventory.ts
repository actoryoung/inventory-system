import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 库存管理类型定义
 */
export interface Inventory {
  id: number
  productId: number
  productName?: string
  productSku?: string
  categoryId?: number
  categoryName?: string
  warehouseId: number
  quantity: number
  warningStock: number
  isLowStock?: boolean
  createdAt?: string
  updatedAt?: string
}

export interface InventoryQuery {
  page?: number
  size?: number
  productName?: string
  categoryId?: number
  lowStockOnly?: boolean
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface AdjustInventoryData {
  type: 'add' | 'reduce' | 'set'
  quantity: number
  reason: string
}

export interface CheckStockData {
  productId: number
  quantity: number
}

export interface InventorySummary {
  categoryId: number
  categoryName: string
  productCount: number
  totalQuantity: number
  totalAmount: number
}

/**
 * 库存管理 API
 */
export const inventoryApi = {
  /**
   * 获取库存列表（分页）
   */
  getInventoryList(params?: InventoryQuery): Promise<ApiResponse<PageResult<Inventory>>> {
    return request.get('/api/inventory', { params })
  },

  /**
   * 获取商品库存
   */
  getInventoryByProduct(productId: number): Promise<ApiResponse<Inventory>> {
    return request.get(`/api/inventory/product/${productId}`)
  },

  /**
   * 调整库存
   */
  adjustInventory(inventoryId: number, data: AdjustInventoryData): Promise<ApiResponse<{ oldQuantity: number; newQuantity: number }>> {
    return request.put(`/api/inventory/${inventoryId}/adjust`, data)
  },

  /**
   * 获取低库存列表
   */
  getLowStockList(params?: { page?: number; size?: number; categoryId?: number }): Promise<ApiResponse<PageResult<Inventory>>> {
    return request.get('/api/inventory/low-stock', { params })
  },

  /**
   * 检查库存是否充足
   */
  checkStock(data: CheckStockData): Promise<ApiResponse<boolean>> {
    return request.post('/api/inventory/check', data)
  },

  /**
   * 获取库存汇总
   */
  getInventorySummary(): Promise<ApiResponse<InventorySummary[]>> {
    return request.get('/api/inventory/summary')
  },

  /**
   * 批量调整库存
   */
  batchAdjust(adjustments: Array<{ inventoryId: number; type: string; quantity: number; reason: string }>): Promise<ApiResponse<{ successCount: number; failCount: number; errors: Array<{ index: number; error: string }> }>> {
    return request.post('/api/inventory/batch-adjust', { adjustments })
  }
}

export default inventoryApi
