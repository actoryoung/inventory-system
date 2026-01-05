/**
 * 商品接口类型定义
 */

/**
 * 商品实体
 */
export interface Product {
  id?: number
  sku: string
  name: string
  categoryId: number
  categoryName?: string
  unit?: string
  price: number
  costPrice?: number
  specification?: string
  description?: string
  warningStock: number
  stockQuantity?: number
  status?: number
  remark?: string
  createdAt?: string
  updatedAt?: string
}

/**
 * 商品表单数据
 */
export interface ProductFormData {
  id?: number
  sku: string
  name: string
  categoryId: number
  unit?: string
  price: number
  costPrice?: number
  specification?: string
  description?: string
  warningStock: number
  status?: number
  remark?: string
}

/**
 * 商品查询参数
 */
export interface ProductQuery {
  name?: string
  sku?: string
  categoryId?: number
  status?: number
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
