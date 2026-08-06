/**
 * 通用接口类型定义
 */

/**
 * API 响应格式
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
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
