/**
 * 出库管理类型定义
 * Outbound Management Type Definitions
 */

/**
 * 出库单状态枚举
 */
export enum OutboundStatus {
  PENDING = 0,    // 待审核
  APPROVED = 1,   // 已审核
  VOID = 2        // 已作废
}

/**
 * 出库单状态文本映射
 */
export const OutboundStatusText: Record<OutboundStatus, string> = {
  [OutboundStatus.PENDING]: '待审核',
  [OutboundStatus.APPROVED]: '已审核',
  [OutboundStatus.VOID]: '已作废'
}

/**
 * 状态描述映射
 */
export const OutboundStatusMap: Record<OutboundStatus, string> = {
  [OutboundStatus.PENDING]: '待审核',
  [OutboundStatus.APPROVED]: '已审核',
  [OutboundStatus.VOID]: '已作废'
}

/**
 * 状态类型映射
 */
export const OutboundStatusTypeMap: Record<OutboundStatus, 'success' | 'warning' | 'danger' | 'info'> = {
  [OutboundStatus.PENDING]: 'warning',
  [OutboundStatus.APPROVED]: 'success',
  [OutboundStatus.VOID]: 'danger'
}

/**
 * 出库单状态类型
 */
export type OutboundStatusType = 'pending' | 'approved' | 'void'

/**
 * 出库单基础接口
 */
export interface Outbound {
  id: number
  outboundNo: string              // 出库单号
  productId: number               // 商品ID
  productName?: string            // 商品名称（非数据库字段）
  productSku?: string             // 商品SKU（非数据库字段）
  quantity: number                // 出库数量
  receiver: string                // 收货人
  receiverPhone?: string          // 收货人电话
  outboundDate: string            // 出库日期
  status: OutboundStatus          // 状态
  remark?: string                 // 备注
  createdBy?: string              // 创建人
  createdAt: string               // 创建时间
  updatedAt?: string              // 更新时间
  approvedBy?: string             // 审核人
  approvedAt?: string             // 审核时间
}

/**
 * 出库单表单接口
 */
export interface OutboundForm {
  productId: number               // 商品ID
  quantity: number                // 出库数量
  receiver: string                // 收货人
  receiverPhone?: string          // 收货人电话
  outboundDate: string            // 出库日期
  remark?: string                 // 备注
}

/**
 * 出库单查询参数接口
 */
export interface OutboundQuery {
  page: number                    // 页码
  size: number                    // 每页大小
  productId?: number              // 商品ID（可选）
  status?: OutboundStatus         // 状态（可选）
  startDate?: string              // 开始日期（可选）
  endDate?: string                // 结束日期（可选）
}

/**
 * 分页结果接口
 */
export interface PageResult<T> {
  records: T[]                    // 数据列表
  total: number                   // 总记录数
  page: number                    // 当前页码
  size: number                    // 每页大小
}

/**
 * 出库单统计信息
 */
export interface OutboundStatistics {
  totalCount: number              // 总出库单数
  pendingCount: number            // 待审核数量
  approvedCount: number           // 已审核数量
  voidCount: number               // 已作废数量
  todayOutboundCount: number      // 今日出库数量
  todayOutboundQuantity: number   // 今日出库总数量
}
