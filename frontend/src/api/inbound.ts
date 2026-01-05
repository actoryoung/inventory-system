/**
 * 入库管理 API
 */
import request from '@/utils/request'
import type {
  Inbound,
  InboundForm,
  InboundQuery,
  PageResult,
  ApiResponse
} from '@/types/inbound'

/**
 * 获取入库单列表
 */
export function getInboundList(params: InboundQuery) {
  return request.get<ApiResponse<PageResult<Inbound>>>('/api/inbound', { params })
}

/**
 * 获取入库单详情
 */
export function getInboundDetail(id: number) {
  return request.get<ApiResponse<Inbound>>(`/api/inbound/${id}`)
}

/**
 * 创建入库单
 */
export function createInbound(data: InboundForm) {
  return request.post<ApiResponse<{ id: number }>>('/api/inbound', data)
}

/**
 * 更新入库单
 */
export function updateInbound(id: number, data: InboundForm) {
  return request.put<ApiResponse<{ success: boolean }>>(`/api/inbound/${id}`, data)
}

/**
 * 删除入库单
 */
export function deleteInbound(id: number) {
  return request.delete<ApiResponse<{ success: boolean }>>(`/api/inbound/${id}`)
}

/**
 * 审核入库单
 */
export function approveInbound(id: number, approvedBy?: string) {
  return request.patch<ApiResponse<{ success: boolean }>>(
    `/api/inbound/${id}/approve`,
    null,
    { params: { approvedBy: approvedBy || 'system' } }
  )
}

/**
 * 作废入库单
 */
export function voidInbound(id: number) {
  return request.patch<ApiResponse>(`/api/inbound/${id}/void`)
}

export default {
  getInboundList,
  getInboundDetail,
  createInbound,
  updateInbound,
  deleteInbound,
  approveInbound,
  voidInbound
}
