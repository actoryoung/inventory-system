import request from '@/utils/request'
import type { ApiResponse, Product, ProductFormData, ProductQuery, PageResult } from '@/types/product'

/**
 * 商品管理 API
 */
export const productApi = {
  /**
   * 创建商品
   */
  create(data: ProductFormData): Promise<ApiResponse<number>> {
    return request.post('/api/products', data)
  },

  /**
   * 更新商品
   */
  update(id: number, data: ProductFormData): Promise<ApiResponse<boolean>> {
    return request.put(`/api/products/${id}`, data)
  },

  /**
   * 删除商品
   */
  delete(id: number): Promise<ApiResponse<boolean>> {
    return request.delete(`/api/products/${id}`)
  },

  /**
   * 批量删除商品
   */
  batchDelete(ids: number[]): Promise<ApiResponse<number>> {
    return request.delete('/api/products/batch', { data: ids })
  },

  /**
   * 获取商品详情
   */
  getById(id: number): Promise<ApiResponse<Product>> {
    return request.get(`/api/products/${id}`)
  },

  /**
   * 分页查询商品列表
   */
  getList(params?: ProductQuery): Promise<ApiResponse<PageResult<Product>>> {
    return request.get('/api/products', { params })
  },

  /**
   * 搜索商品
   */
  search(keyword: string): Promise<ApiResponse<Product[]>> {
    return request.get('/api/products/search', { params: { keyword } })
  },

  /**
   * 切换商品状态
   */
  toggleStatus(id: number, status: number): Promise<ApiResponse<boolean>> {
    return request.patch(`/api/products/${id}/status`, null, {
      params: { status }
    })
  },

  /**
   * 检查 SKU 是否存在
   */
  checkSkuExists(sku: string, excludeId?: number): Promise<ApiResponse<boolean>> {
    return request.get('/api/products/check-sku', {
      params: { sku, excludeId }
    })
  },

  /**
   * 获取低库存商品列表
   */
  getLowStockProducts(): Promise<ApiResponse<Product[]>> {
    return request.get('/api/products/low-stock')
  }
}

export default productApi
