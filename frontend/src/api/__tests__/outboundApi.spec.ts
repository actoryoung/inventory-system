/**
 * 出库 API 测试（重写，对齐当前 outbound 方法签名）
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import request from '@/utils/request'
import {
  getOutboundList,
  getOutboundDetail,
  createOutbound,
  updateOutbound,
  deleteOutbound,
  approveOutbound,
  voidOutbound,
  exportOutboundList
} from '../outbound'

vi.mock('@/utils/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn()
  }
}))

const mockedRequest = vi.mocked(request)

describe('outbound API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getOutboundList 调用 GET /api/outbound 并携带参数', async () => {
    mockedRequest.get.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { records: [], total: 0, page: 1, size: 10 }
    })

    await getOutboundList({ page: 1, size: 10 })

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/outbound', { params: { page: 1, size: 10 } })
  })

  it('getOutboundDetail 调用 GET /api/outbound/{id}', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: { id: 1 } })

    const res = await getOutboundDetail(1)

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/outbound/1')
    expect(res.data.id).toBe(1)
  })

  it('createOutbound 调用 POST /api/outbound', async () => {
    mockedRequest.post.mockResolvedValue({ code: 200, message: 'ok', data: { id: 5, outboundNo: 'OUT202608060005' } })

    const res = await createOutbound({ productId: 1, quantity: 3, receiver: '张三', outboundDate: '2026-08-06 10:00:00' })

    expect(mockedRequest.post).toHaveBeenCalledWith('/api/outbound', expect.any(Object))
    expect(res.data.outboundNo).toBe('OUT202608060005')
  })

  it('updateOutbound 调用 PUT /api/outbound/{id}', async () => {
    mockedRequest.put.mockResolvedValue({ code: 200, message: 'ok', data: { success: true } })

    await updateOutbound(5, { productId: 1, quantity: 2, receiver: '张三', outboundDate: '2026-08-06 10:00:00' })

    expect(mockedRequest.put).toHaveBeenCalledWith('/api/outbound/5', expect.any(Object))
  })

  it('deleteOutbound 调用 DELETE /api/outbound/{id}', async () => {
    mockedRequest.delete.mockResolvedValue({ code: 200, message: 'ok', data: { success: true } })

    await deleteOutbound(5)

    expect(mockedRequest.delete).toHaveBeenCalledWith('/api/outbound/5')
  })

  it('approveOutbound 调用 PATCH /api/outbound/{id}/approve 并携带默认审核人', async () => {
    mockedRequest.patch.mockResolvedValue({ code: 200, message: 'ok', data: { success: true } })

    await approveOutbound(5)

    expect(mockedRequest.patch).toHaveBeenCalledWith('/api/outbound/5/approve', null, {
      params: { approvedBy: 'system' }
    })
  })

  it('voidOutbound 调用 PATCH /api/outbound/{id}/void', async () => {
    mockedRequest.patch.mockResolvedValue({ code: 200, message: 'ok', data: null })

    await voidOutbound(5)

    expect(mockedRequest.patch).toHaveBeenCalledWith('/api/outbound/5/void')
  })

  it('exportOutboundList 调用 GET /api/outbound/export 并以 blob 响应', async () => {
    mockedRequest.get.mockResolvedValue(new Blob(['data']))

    await exportOutboundList({ page: 1, size: 10 })

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/outbound/export', {
      params: { page: 1, size: 10 },
      responseType: 'blob'
    })
  })
})
