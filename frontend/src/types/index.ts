/**
 * 类型统一出口
 *
 * 所有 API/视图层从 `@/types` 导入类型，
 * 通用类型（ApiResponse/PageResult）统一定义在 `./common`。
 */
export type { ApiResponse, PageResult } from './common'

export type { Product, ProductFormData, ProductQuery } from './product'

export type { Category, CategoryFormData, CategoryQuery } from './category'

export type {
  Inventory,
  InventoryAdjustData,
  InventoryQuery,
  InventorySummary
} from './inventory'

export type { Inbound, InboundForm, InboundQuery } from './inbound'
export { InboundStatus, InboundStatusMap, InboundStatusTypeMap } from './inbound'

export type { Outbound, OutboundForm, OutboundQuery, OutboundStatusType } from './outbound'
export {
  OutboundStatus,
  OutboundStatusMap,
  OutboundStatusText,
  OutboundStatusTypeMap
} from './outbound'

export type { Dashboard, Trend, CategoryDistribution, LowStock } from './statistics'
