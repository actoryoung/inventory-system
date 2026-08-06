/**
 * 入库 API 测试（重写，对齐当前 inbound 方法签名）
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import request from '@/utils/request'
import {
  getInboundList,
  getInboundDetail,
  createInbound,
  updateInbound,
  deleteInbound,
  approveInbound,
  voidInbound
} from '../inbound'

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

describe('inbound API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getInboundList 调用 GET /api/inbound 并携带参数', async () => {
    mockedRequest.get.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { records: [], total: 0, page: 1, size: 10 }
    })

    await getInboundList({ page: 1, size: 10 })

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/inbound', { params: { page: 1, size: 10 } })
  })

  it('getInboundDetail 调用 GET /api/inbound/{id}', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: { id: 1 } })

    const res = await getInboundDetail(1)

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/inbound/1')
    expect(res.data.id).toBe(1)
  })

  it('createInbound 调用 POST /api/inbound', async () => {
    mockedRequest.post.mockResolvedValue({ code: 200, message: 'ok', data: { id: 5 } })

    const res = await createInbound({ productId: 1, quantity: 10, supplier: '测试供应商', inboundDate: '2026-08-06 10:00:00' })

    expect(mockedRequest.post).toHaveBeenCalledWith('/api/inbound', expect.any(Object))
    expect(res.data.id).toBe(5)
  })

  it('updateInbound 调用 PUT /api/inbound/{id}', async () => {
    mockedRequest.put.mockResolvedValue({ code: 200, message: 'ok', data: { success: true } })

    await updateInbound(5, { productId: 1, quantity: 20, supplier: '测试供应商', inboundDate: '2026-08-06 10:00:00' })

    expect(mockedRequest.put).toHaveBeenCalledWith('/api/inbound/5', expect.any(Object))
  })

  it('deleteInbound 调用 DELETE /api/inbound/{id}', async () => {
    mockedRequest.delete.mockResolvedValue({ code: 200, message: 'ok', data: { success: true } })

    await deleteInbound(5)

    expect(mockedRequest.delete).toHaveBeenCalledWith('/api/inbound/5')
  })

  it('approveInbound 调用 PATCH /api/inbound/{id}/approve 并携带默认审核人', async () => {
    mockedRequest.patch.mockResolvedValue({ code: 200, message: 'ok', data: { success: true } })

    await approveInbound(5)

    expect(mockedRequest.patch).toHaveBeenCalledWith('/api/inbound/5/approve', null, {
      params: { approvedBy: 'system' }
    })
  })

  it('approveInbound 支持自定义审核人', async () => {
    mockedRequest.patch.mockResolvedValue({ code: 200, message: 'ok', data: { success: true } })

    await approveInbound(5, 'admin')

    expect(mockedRequest.patch).toHaveBeenCalledWith('/api/inbound/5/approve', null, {
      params: { approvedBy: 'admin' }
    })
  })

  it('voidInbound 调用 PATCH /api/inbound/{id}/void', async () => {
    mockedRequest.patch.mockResolvedValue({ code: 200, message: 'ok', data: null })

    await voidInbound(5)

    expect(mockedRequest.patch).toHaveBeenCalledWith('/api/inbound/5/void')
  })
})
