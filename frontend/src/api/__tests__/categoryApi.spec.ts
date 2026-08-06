/**
 * 分类 API 测试（重写，对齐当前 categoryApi 方法签名）
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import request from '@/utils/request'
import { categoryApi } from '../category'

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

describe('categoryApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getTree 调用 GET /api/categories/tree', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: [] })

    await categoryApi.getTree()

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/categories/tree')
  })

  it('getEnabledTree 调用 GET /api/categories/tree/enabled', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: [] })

    await categoryApi.getEnabledTree()

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/categories/tree/enabled')
  })

  it('getList 调用 GET /api/categories 并携带参数', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: [] })

    await categoryApi.getList({ name: '电子' })

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/categories', { params: { name: '电子' } })
  })

  it('create 调用 POST /api/categories', async () => {
    mockedRequest.post.mockResolvedValue({ code: 200, message: 'ok', data: 3 })

    const res = await categoryApi.create({ name: '电脑', parentId: null, sortOrder: 0, status: 1 })

    expect(mockedRequest.post).toHaveBeenCalledWith('/api/categories', expect.any(Object))
    expect(res.data).toBe(3)
  })

  it('update 调用 PUT /api/categories/{id}', async () => {
    mockedRequest.put.mockResolvedValue({ code: 200, message: 'ok', data: true })

    await categoryApi.update(3, { name: '电脑外设', parentId: null, sortOrder: 0, status: 1 })

    expect(mockedRequest.put).toHaveBeenCalledWith('/api/categories/3', expect.any(Object))
  })

  it('delete 调用 DELETE /api/categories/{id}', async () => {
    mockedRequest.delete.mockResolvedValue({ code: 200, message: 'ok', data: true })

    await categoryApi.delete(3)

    expect(mockedRequest.delete).toHaveBeenCalledWith('/api/categories/3')
  })

  it('toggleStatus 调用 PATCH /api/categories/{id}/status', async () => {
    mockedRequest.patch.mockResolvedValue({ code: 200, message: 'ok', data: true })

    await categoryApi.toggleStatus(3, 0)

    expect(mockedRequest.patch).toHaveBeenCalledWith('/api/categories/3/status', null, { params: { status: 0 } })
  })

  it('checkNameDuplicate 调用 GET /api/categories/check-name 并携带排除 ID', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: false })

    await categoryApi.checkNameDuplicate('电脑', 1, 3)

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/categories/check-name', {
      params: { name: '电脑', parentId: 1, excludeId: 3 }
    })
  })

  it('canDelete 调用 GET /api/categories/{id}/can-delete', async () => {
    mockedRequest.get.mockResolvedValue({ code: 200, message: 'ok', data: true })

    await categoryApi.canDelete(3)

    expect(mockedRequest.get).toHaveBeenCalledWith('/api/categories/3/can-delete')
  })
})
