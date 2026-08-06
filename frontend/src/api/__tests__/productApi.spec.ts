/**
 * 商品 API 测试（重写，对齐当前 productApi 方法签名）
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import request from '@/utils/request'
import { productApi } from '../product'

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

describe('productApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getList 调用 GET /api/products 并携带查询参数', async () => {
    mockedRequest.get.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { records: [], total: 0, page: 1, size: 10 }
    })

    const res = await productApi.getList({ page: 1, size: 10, name: 'iPhone' })

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/products', {
      params: { page: 1, size: 10, name: 'iPhone' }
    })
    expect(res.code).toBe(200)
  })

  it('create 调用 POST /api/products 并返回新建 ID', async () => {
    mockedRequest.post.mockResolvedValue({ code: 200, message: 'ok', data: 2 })

    const res = await productApi.create({
      sku: 'SKU002',
      name: 'MacBook Pro',
      categoryId: 1,
      price: 12999,
      warningStock: 5
    })

    expect(mockedRequest.post).toHaveBeenCalledWith('/api/products', expect.objectContaining({ sku: 'SKU002' }))
    expect(res.data).toBe(2)
  })

  it('update 调用 PUT /api/products/{id}', async () => {
    mockedRequest.put.mockResolvedValue({ code: 200, message: 'ok', data: true })

    await productApi.update(1, {
      sku: 'SKU001',
      name: 'iPhone 15 Pro',
      categoryId: 1,
      price: 6999,
      warningStock: 10
    })

    expect(mockedRequest.put).toHaveBeenCalledWith('/api/products/1', expect.any(Object))
  })

  it('delete 调用 DELETE /api/products/{id}', async () => {
    mockedRequest.delete.mockResolvedValue({ code: 200, message: 'ok', data: true })

    await productApi.delete(1)

    expect(mockedRequest.delete).toHaveBeenCalledWith('/api/products/1')
  })

  it('batchDelete 调用 DELETE /api/products/batch 并携带 ids', async () => {
    mockedRequest.delete.mockResolvedValue({ code: 200, message: 'ok', data: 2 })

    await productApi.batchDelete([1, 2])

    expect(mockedRequest.delete).toHaveBeenCalledWith('/api/products/batch', { data: [1, 2] })
  })

  it('getById 调用 GET /api/products/{id}', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: { id: 1 } })

    const res = await productApi.getById(1)

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/products/1')
    expect(res.data.id).toBe(1)
  })

  it('search 调用 GET /api/products/search 并携带关键词', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: [] })

    await productApi.search('iPhone')

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/products/search', { params: { keyword: 'iPhone' } })
  })

  it('toggleStatus 调用 PATCH /api/products/{id}/status 并携带状态', async () => {
    mockedRequest.patch.mockResolvedValue({ code: 200, message: 'ok', data: true })

    await productApi.toggleStatus(1, 0)

    expect(mockedRequest.patch).toHaveBeenCalledWith('/api/products/1/status', null, { params: { status: 0 } })
  })

  it('checkSkuExists 调用 GET /api/products/check-sku 并携带 excludeId', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: true })

    await productApi.checkSkuExists('SKU001', 2)

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/products/check-sku', {
      params: { sku: 'SKU001', excludeId: 2 }
    })
  })

  it('getLowStockProducts 调用 GET /api/products/low-stock', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: [] })

    await productApi.getLowStockProducts()

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/products/low-stock')
  })
})
