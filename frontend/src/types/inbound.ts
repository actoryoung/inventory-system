/**
 * 入库单接口类型定义
 */

/**
 * 入库单状态枚举
 */
export enum InboundStatus {
  Pending = 0,    // 待审核
  Approved = 1,   // 已审核
  Void = 2        // 已作废
}

/**
 * 状态描述映射
 */
export const InboundStatusMap: Record<InboundStatus, string> = {
  [InboundStatus.Pending]: '待审核',
  [InboundStatus.Approved]: '已审核',
  [InboundStatus.Void]: '已作废'
}

/**
 * 状态类型映射
 */
export const InboundStatusTypeMap: Record<InboundStatus, 'success' | 'warning' | 'danger' | 'info'> = {
  [InboundStatus.Pending]: 'warning',
  [InboundStatus.Approved]: 'success',
  [InboundStatus.Void]: 'danger'
}

/**
 * 入库单实体
 */
export interface Inbound {
  id?: number
  inboundNo?: string
  productId: number
  productName?: string
  productSku?: string
  quantity: number
  supplier: string
  inboundDate: string
  status?: InboundStatus
  statusDesc?: string
  remark?: string
  createdBy?: string
  createdAt?: string
  approvedBy?: string
  approvedAt?: string
}

/**
 * 入库单表单数据
 */
export interface InboundForm {
  productId: number | undefined
  quantity: number | undefined
  supplier: string
  inboundDate: string
  remark: string
}

/**
 * 入库单查询参数
 */
export interface InboundQuery {
  productId?: number
  status?: InboundStatus
  startDate?: string
  endDate?: string
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
