/**
 * 出库管理 API
 * Outbound Management API
 */
import request from '@/utils/request'
import type {
  Outbound,
  OutboundForm,
  OutboundQuery,
  PageResult,
  ApiResponse
} from '@/types/outbound'

/**
 * 获取出库单列表
 */
export function getOutboundList(params: OutboundQuery) {
  return request.get<ApiResponse<PageResult<Outbound>>>('/api/outbound', { params })
}

/**
 * 获取出库单详情
 */
export function getOutboundDetail(id: number) {
  return request.get<ApiResponse<Outbound>>(`/api/outbound/${id}`)
}

/**
 * 创建出库单
 */
export function createOutbound(data: OutboundForm) {
  return request.post<ApiResponse<{ id: number; outboundNo: string }>>('/api/outbound', data)
}

/**
 * 更新出库单
 */
export function updateOutbound(id: number, data: OutboundForm) {
  return request.put<ApiResponse<{ success: boolean }>>(`/api/outbound/${id}`, data)
}

/**
 * 删除出库单
 */
export function deleteOutbound(id: number) {
  return request.delete<ApiResponse<{ success: boolean }>>(`/api/outbound/${id}`)
}

/**
 * 审核出库单
 */
export function approveOutbound(id: number, approvedBy?: string) {
  return request.patch<ApiResponse<{ success: boolean }>>(
    `/api/outbound/${id}/approve`,
    null,
    { params: { approvedBy: approvedBy || 'system' } }
  )
}

/**
 * 作废出库单
 */
export function voidOutbound(id: number) {
  return request.patch<ApiResponse>(`/api/outbound/${id}/void`)
}

/**
 * 导出出库单列表
 */
export function exportOutboundList(params: OutboundQuery) {
  return request.get<Blob>('/api/outbound/export', {
    params,
    responseType: 'blob'
  })
}

export default {
  getOutboundList,
  getOutboundDetail,
  createOutbound,
  updateOutbound,
  deleteOutbound,
  approveOutbound,
  voidOutbound,
  exportOutboundList
}
