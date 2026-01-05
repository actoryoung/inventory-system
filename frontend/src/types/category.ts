/**
 * 分类接口类型定义
 */

/**
 * 分类实体
 */
export interface Category {
  id?: number
  name: string
  parentId?: number | null
  level?: number
  sortOrder?: number
  status?: number
  createdAt?: string
  updatedAt?: string
  children?: Category[]
}

/**
 * 分类表单数据
 */
export interface CategoryFormData {
  id?: number
  name: string
  parentId?: number | null
  level?: number
  sortOrder: number
  status?: number
}

/**
 * 分类查询参数
 */
export interface CategoryQuery {
  name?: string
  level?: number
  status?: number
}

/**
 * API 响应格式
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
