/**
 * 库存 API 测试（重写，对齐当前 inventoryApi 方法签名）
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import request from '@/utils/request'
import { inventoryApi } from '../inventory'

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

describe('inventoryApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getInventoryList 调用 GET /api/inventory 并携带分页参数', async () => {
    mockedRequest.get.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { records: [], total: 0, page: 1, size: 10 }
    })

    const res = await inventoryApi.getInventoryList({ page: 1, size: 10 })

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/inventory', { params: { page: 1, size: 10 } })
    expect(res.code).toBe(200)
  })

  it('getInventoryByProduct 调用 GET /api/inventory/product/{id}', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: null })

    await inventoryApi.getInventoryByProduct(1)

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/inventory/product/1')
  })

  it('adjustInventory 调用 PUT /api/inventory/{id}/adjust', async () => {
    mockedRequest.put.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { oldQuantity: 100, newQuantity: 110 }
    })

    const res = await inventoryApi.adjustInventory(1, { type: 'add', quantity: 10, reason: '盘点' })

    expect(mockedRequest.put).toHaveBeenCalledWith('/api/inventory/1/adjust', {
      type: 'add',
      quantity: 10,
      reason: '盘点'
    })
    expect(res.data.newQuantity).toBe(110)
  })

  it('getLowStockList 调用 GET /api/inventory/low-stock', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: { records: [], total: 0, page: 1, size: 10 } })

    await inventoryApi.getLowStockList({ page: 1, size: 10 })

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/inventory/low-stock', { params: { page: 1, size: 10 } })
  })

  it('checkStock 调用 POST /api/inventory/check', async () => {
    mockedRequest.post.mockResolvedValue({ code: 200, message: 'ok', data: true })

    await inventoryApi.checkStock({ productId: 1, quantity: 5 })

    expect(mockedRequest.post).toHaveBeenCalledWith('/api/inventory/check', { productId: 1, quantity: 5 })
  })

  it('getInventorySummary 调用 GET /api/inventory/summary', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: null })

    await inventoryApi.getInventorySummary()

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/inventory/summary')
  })

  it('batchAdjust 调用 POST /api/inventory/batch-adjust', async () => {
    mockedRequest.post.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { successCount: 1, failCount: 0, errors: [] }
    })

    const adjustments = [{ inventoryId: 1, type: 'add', quantity: 5, reason: '盘点' }]
    await inventoryApi.batchAdjust(adjustments)

    expect(mockedRequest.post).toHaveBeenCalledWith('/api/inventory/batch-adjust', { adjustments })
  })
})
