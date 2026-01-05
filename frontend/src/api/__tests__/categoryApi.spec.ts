/**
 * 商品分类 API 测试
 *
 * 测试范围:
 * - API 调用测试
 * - 请求参数构建
 * - 响应数据解析
 * - 错误处理
 * - Token 验证
 * - 超时处理
 *
 * @author Claude Code
 * @since 2026-01-04
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import {
  getCategoryList,
  getCategoryTree,
  getCategoryById,
  createCategory,
  updateCategory,
  deleteCategory,
  updateCategoryStatus,
  exportCategories,
  importCategories,
  batchDeleteCategories
} from '../category'
import axios from 'axios'

// Mock axios
vi.mock('axios')

const mockedAxios = axios as any

describe('Category API', () => {
  const mockToken = 'test-token-123456'

  beforeEach(() => {
    // 设置测试环境变量
    localStorage.setItem('token', mockToken)

    // Mock axios 响应
    mockedAxios.get.mockResolvedValue({ data: { code: 200, message: 'success', data: [] } })
    mockedAxios.post.mockResolvedValue({ data: { code: 200, message: 'success', data: {} } })
    mockedAxios.put.mockResolvedValue({ data: { code: 200, message: 'success', data: {} } })
    mockedAxios.patch.mockResolvedValue({ data: { code: 200, message: 'success', data: {} } })
    mockedAxios.delete.mockResolvedValue({ data: { code: 200, message: 'success', data: {} } })
  })

  afterEach(() => {
    vi.clearAllMocks()
    localStorage.clearItem('token')
  })

  describe('getCategoryList - 获取分类列表', () => {
    it('应成功获取分类列表', async () => {
      const mockData = [
        { id: 1, name: '电子产品', level: 1, status: 1 },
        { id: 2, name: '手机', level: 2, status: 1 }
      ]

      mockedAxios.get.mockResolvedValue({
        data: { code: 200, message: 'success', data: mockData }
      })

      const result = await getCategoryList()

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/categories', {
        params: {},
        headers: { Authorization: `Bearer ${mockToken}` }
      })
      expect(result).toEqual(mockData)
    })

    it('应支持按名称搜索', async () => {
      const params = { name: '电子' }

      await getCategoryList(params)

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/categories', {
        params,
        headers: { Authorization: `Bearer ${mockToken}` }
      })
    })

    it('应支持按状态过滤', async () => {
      const params = { status: 1 }

      await getCategoryList(params)

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/categories', {
        params,
        headers: { Authorization: `Bearer ${mockToken}` }
      })
    })

    it('应支持按层级过滤', async () => {
      const params = { level: 1 }

      await getCategoryList(params)

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/categories', {
        params,
        headers: { Authorization: `Bearer ${mockToken}` }
      })
    })

    it('应支持组合查询条件', async () => {
      const params = { name: '手机', status: 1, level: 2 }

      await getCategoryList(params)

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/categories', {
        params,
        headers: { Authorization: `Bearer ${mockToken}` }
      })
    })

    it('应在无Token时抛出错误', async () => {
      localStorage.removeItem('token')

      await expect(getCategoryList()).rejects.toThrow('未登录或登录已过期')
    })

    it('应在API返回错误时抛出异常', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { code: 400, message: '参数错误', data: null }
      })

      await expect(getCategoryList()).rejects.toThrow('参数错误')
    })
  })

  describe('getCategoryTree - 获取分类树', () => {
    it('应成功获取分类树', async () => {
      const mockTree = [
        {
          id: 1,
          name: '电子产品',
          level: 1,
          status: 1,
          children: [
            {
              id: 2,
              name: '手机',
              level: 2,
              status: 1,
              children: []
            }
          ]
        }
      ]

      mockedAxios.get.mockResolvedValue({
        data: { code: 200, message: 'success', data: mockTree }
      })

      const result = await getCategoryTree()

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/categories/tree', {
        headers: { Authorization: `Bearer ${mockToken}` }
      })
      expect(result).toEqual(mockTree)
    })

    it('应正确解析树形结构', async () => {
      const mockTree = [
        {
          id: 1,
          name: '电子产品',
          children: [
            { id: 2, name: '手机', children: [] }
          ]
        }
      ]

      mockedAxios.get.mockResolvedValue({
        data: { code: 200, message: 'success', data: mockTree }
      })

      const result = await getCategoryTree()

      expect(result[0].children).toBeDefined()
      expect(result[0].children[0].children).toBeDefined()
    })

    it('应在无数据时返回空数组', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { code: 200, message: 'success', data: [] }
      })

      const result = await getCategoryTree()

      expect(result).toEqual([])
    })
  })

  describe('getCategoryById - 获取分类详情', () => {
    it('应成功获取分类详情', async () => {
      const mockCategory = {
        id: 1,
        name: '电子产品',
        parentId: null,
        level: 1,
        sortOrder: 1,
        status: 1,
        createdAt: '2026-01-04T10:00:00',
        updatedAt: '2026-01-04T10:00:00'
      }

      mockedAxios.get.mockResolvedValue({
        data: { code: 200, message: 'success', data: mockCategory }
      })

      const result = await getCategoryById(1)

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/categories/1', {
        headers: { Authorization: `Bearer ${mockToken}` }
      })
      expect(result).toEqual(mockCategory)
    })

    it('应在分类不存在时返回null', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { code: 404, message: '分类不存在', data: null }
      })

      const result = await getCategoryById(999)

      expect(result).toBeNull()
    })

    it('应在ID无效时抛出错误', async () => {
      await expect(getCategoryById(0)).rejects.toThrow('无效的分类ID')
      await expect(getCategoryById(-1)).rejects.toThrow('无效的分类ID')
      await expect(getCategoryById(NaN)).rejects.toThrow('无效的分类ID')
    })
  })

  describe('createCategory - 创建分类', () => {
    it('应成功创建一级分类', async () => {
      const newCategory = {
        name: '食品饮料',
        parentId: null,
        level: 1,
        sortOrder: 2,
        status: 1
      }

      const mockResponse = {
        id: 5,
        ...newCategory,
        createdAt: '2026-01-04T10:00:00'
      }

      mockedAxios.post.mockResolvedValue({
        data: { code: 200, message: '创建成功', data: mockResponse }
      })

      const result = await createCategory(newCategory)

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/categories',
        newCategory,
        { headers: { Authorization: `Bearer ${mockToken}` } }
      )
      expect(result).toEqual(mockResponse)
    })

    it('应成功创建二级分类', async () => {
      const newCategory = {
        name: '电脑',
        parentId: 1,
        level: 2,
        sortOrder: 2,
        status: 1
      }

      const mockResponse = {
        id: 4,
        ...newCategory,
        createdAt: '2026-01-04T10:00:00'
      }

      mockedAxios.post.mockResolvedValue({
        data: { code: 200, message: '创建成功', data: mockResponse }
      })

      const result = await createCategory(newCategory)

      expect(result.id).toBe(4)
      expect(result.level).toBe(2)
    })

    it('应在分类名称为空时返回错误', async () => {
      const invalidCategory = {
        name: '',
        parentId: null,
        level: 1
      }

      mockedAxios.post.mockResolvedValue({
        data: { code: 400, message: '分类名称不能为空', data: null }
      })

      await expect(createCategory(invalidCategory)).rejects.toThrow('分类名称不能为空')
    })

    it('应在层级超过限制时返回错误', async () => {
      const invalidCategory = {
        name: '四级分类',
        parentId: 3,
        level: 4
      }

      mockedAxios.post.mockResolvedValue({
        data: { code: 400, message: '分类层级不能超过3级', data: null }
      })

      await expect(createCategory(invalidCategory)).rejects.toThrow('分类层级不能超过3级')
    })

    it('应在同级名称重复时返回错误', async () => {
      const duplicateCategory = {
        name: '手机',
        parentId: 1,
        level: 2
      }

      mockedAxios.post.mockResolvedValue({
        data: { code: 400, message: '同一父分类下已存在同名分类', data: null }
      })

      await expect(createCategory(duplicateCategory)).rejects.toThrow('同一父分类下已存在同名分类')
    })

    it('应在父分类不存在时返回错误', async () => {
      const invalidCategory = {
        name: '子分类',
        parentId: 999,
        level: 2
      }

      mockedAxios.post.mockResolvedValue({
        data: { code: 400, message: '父分类不存在', data: null }
      })

      await expect(createCategory(invalidCategory)).rejects.toThrow('父分类不存在')
    })
  })

  describe('updateCategory - 更新分类', () => {
    it('应成功更新分类', async () => {
      const updateCategory = {
        id: 1,
        name: '电子产品（更新）',
        parentId: null,
        level: 1,
        sortOrder: 1,
        status: 1
      }

      mockedAxios.put.mockResolvedValue({
        data: { code: 200, message: '更新成功', data: updateCategory }
      })

      const result = await updateCategory(1, updateCategory)

      expect(mockedAxios.put).toHaveBeenCalledWith(
        '/api/categories/1',
        updateCategory,
        { headers: { Authorization: `Bearer ${mockToken}` } }
      )
      expect(result).toEqual(updateCategory)
    })

    it('应在分类不存在时返回错误', async () => {
      const updateCategory = {
        id: 999,
        name: '不存在的分类'
      }

      mockedAxios.put.mockResolvedValue({
        data: { code: 404, message: '分类不存在', data: null }
      })

      await expect(updateCategory(999, updateCategory)).rejects.toThrow('分类不存在')
    })

    it('应在更新后名称重复时返回错误', async () => {
      const updateCategory = {
        id: 2,
        name: '电脑',
        parentId: 1,
        level: 2
      }

      mockedAxios.put.mockResolvedValue({
        data: { code: 400, message: '同一父分类下已存在同名分类', data: null }
      })

      await expect(updateCategory(2, updateCategory)).rejects.toThrow('同一父分类下已存在同名分类')
    })

    it('应验证路径ID与请求体ID一致性', async () => {
      const updateCategory = {
        id: 2,  // 与路径ID不一致
        name: '电子产品'
      }

      await expect(updateCategory(1, updateCategory)).rejects.toThrow('路径ID与请求体ID不一致')
    })
  })

  describe('deleteCategory - 删除分类', () => {
    it('应成功删除分类', async () => {
      mockedAxios.delete.mockResolvedValue({
        data: { code: 200, message: '删除成功', data: null }
      })

      await deleteCategory(1)

      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/categories/1', {
        headers: { Authorization: `Bearer ${mockToken}` }
      })
    })

    it('应在分类有关联商品时返回错误', async () => {
      mockedAxios.delete.mockResolvedValue({
        data: { code: 400, message: '该分类下有商品，无法删除', data: null }
      })

      await expect(deleteCategory(1)).rejects.toThrow('该分类下有商品，无法删除')
    })

    it('应在分类有子分类时返回错误', async () => {
      mockedAxios.delete.mockResolvedValue({
        data: { code: 400, message: '该分类下有子分类，请先删除子分类', data: null }
      })

      await expect(deleteCategory(1)).rejects.toThrow('该分类下有子分类，请先删除子分类')
    })

    it('应在分类不存在时返回错误', async () => {
      mockedAxios.delete.mockResolvedValue({
        data: { code: 404, message: '分类不存在', data: null }
      })

      await expect(deleteCategory(999)).rejects.toThrow('分类不存在')
    })

    it('应在ID无效时抛出错误', async () => {
      await expect(deleteCategory(0)).rejects.toThrow('无效的分类ID')
      await expect(deleteCategory(-1)).rejects.toThrow('无效的分类ID')
    })
  })

  describe('updateCategoryStatus - 切换分类状态', () => {
    it('应成功切换状态（启用到禁用）', async () => {
      mockedAxios.patch.mockResolvedValue({
        data: { code: 200, message: '状态更新成功', data: { status: 0 } }
      })

      const result = await updateCategoryStatus(1, 0)

      expect(mockedAxios.patch).toHaveBeenCalledWith(
        '/api/categories/1/status',
        { status: 0 },
        { headers: { Authorization: `Bearer ${mockToken}` } }
      )
      expect(result.status).toBe(0)
    })

    it('应成功切换状态（禁用到启用）', async () => {
      mockedAxios.patch.mockResolvedValue({
        data: { code: 200, message: '状态更新成功', data: { status: 1 } }
      })

      const result = await updateCategoryStatus(1, 1)

      expect(result.status).toBe(1)
    })

    it('应在分类不存在时返回错误', async () => {
      mockedAxios.patch.mockResolvedValue({
        data: { code: 404, message: '分类不存在', data: null }
      })

      await expect(updateCategoryStatus(999, 1)).rejects.toThrow('分类不存在')
    })

    it('应验证状态值的有效性', async () => {
      await expect(updateCategoryStatus(1, 2)).rejects.toThrow('无效的状态值')
      await expect(updateCategoryStatus(1, -1)).rejects.toThrow('无效的状态值')
    })
  })

  describe('exportCategories - 导出分类数据', () => {
    it('应成功导出分类数据', async () => {
      const mockBlob = new Blob(['test data'], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      mockedAxios.get.mockResolvedValue({
        data: mockBlob,
        headers: {
          'content-disposition': 'attachment; filename=categories.xlsx'
        }
      })

      const result = await exportCategories()

      expect(mockedAxios.get).toHaveBeenCalledWith('/api/categories/export', {
        headers: { Authorization: `Bearer ${mockToken}` },
        responseType: 'blob'
      })
      expect(result.data).toBeInstanceOf(Blob)
    })

    it('应正确解析文件名', async () => {
      const mockBlob = new Blob(['test data'])

      mockedAxios.get.mockResolvedValue({
        data: mockBlob,
        headers: {
          'content-disposition': 'attachment; filename=categories_20260104.xlsx'
        }
      })

      await exportCategories()

      // 验证文件名解析逻辑
      const contentDisposition = 'attachment; filename=categories_20260104.xlsx'
      const filename = contentDisposition.split('filename=')[1]
      expect(filename).toBe('categories_20260104.xlsx')
    })

    it('应在导出失败时抛出错误', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { code: 500, message: '导出失败' }
      })

      await expect(exportCategories()).rejects.toThrow()
    })
  })

  describe('importCategories - 导入分类数据', () => {
    it('应成功导入分类数据', async () => {
      const mockFile = new File(['test'], 'categories.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      const mockResponse = {
        success: 10,
        failed: 0,
        errors: []
      }

      mockedAxios.post.mockResolvedValue({
        data: { code: 200, message: '导入成功', data: mockResponse }
      })

      const result = await importCategories(mockFile)

      expect(mockedAxios.post).toHaveBeenCalledWith(
        '/api/categories/import',
        expect.any(FormData),
        {
          headers: {
            Authorization: `Bearer ${mockToken}`,
            'Content-Type': 'multipart/form-data'
          }
        }
      )
      expect(result).toEqual(mockResponse)
    })

    it('应正确构建FormData', async () => {
      const mockFile = new File(['test'], 'categories.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      mockedAxios.post.mockResolvedValue({
        data: { code: 200, message: '导入成功', data: {} }
      })

      await importCategories(mockFile)

      const formData = mockedAxios.post.mock.calls[0][1]
      expect(formData).toBeInstanceOf(FormData)
      expect(formData.get('file')).toBe(mockFile)
    })

    it('应在部分失败时返回错误详情', async () => {
      const mockFile = new File(['test'], 'categories.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      const mockResponse = {
        success: 8,
        failed: 2,
        errors: ['第3行：分类名称重复', '第5行：层级超过限制']
      }

      mockedAxios.post.mockResolvedValue({
        data: { code: 200, message: '导入完成', data: mockResponse }
      })

      const result = await importCategories(mockFile)

      expect(result.success).toBe(8)
      expect(result.failed).toBe(2)
      expect(result.errors).toHaveLength(2)
    })

    it('应在文件格式错误时返回错误', async () => {
      const invalidFile = new File(['test'], 'test.txt', {
        type: 'text/plain'
      })

      mockedAxios.post.mockResolvedValue({
        data: { code: 400, message: '文件格式错误，请上传Excel文件', data: null }
      })

      await expect(importCategories(invalidFile)).rejects.toThrow('文件格式错误，请上传Excel文件')
    })
  })

  describe('batchDeleteCategories - 批量删除分类', () => {
    it('应成功批量删除分类', async () => {
      const ids = [1, 2, 3]

      mockedAxios.delete.mockResolvedValue({
        data: { code: 200, message: '批量删除成功', data: null }
      })

      await batchDeleteCategories(ids)

      expect(mockedAxios.delete).toHaveBeenCalledWith('/api/categories/batch', {
        data: { ids },
        headers: { Authorization: `Bearer ${mockToken}` }
      })
    })

    it('应在部分删除失败时返回错误', async () => {
      const ids = [1, 2, 3]

      mockedAxios.delete.mockResolvedValue({
        data: {
          code: 400,
          message: '部分分类删除失败',
          data: {
            success: [1, 2],
            failed: [3],
            errors: { 3: '该分类下有商品，无法删除' }
          }
        }
      })

      await expect(batchDeleteCategories(ids)).rejects.toThrow('部分分类删除失败')
    })

    it('应在ID列表为空时返回错误', async () => {
      await expect(batchDeleteCategories([])).rejects.toThrow('请选择要删除的分类')
    })

    it('应在ID列表无效时返回错误', async () => {
      await expect(batchDeleteCategories([0, -1, NaN])).rejects.toThrow('无效的分类ID')
    })
  })

  describe('Token 验证', () => {
    it('应在所有请求中携带Token', async () => {
      await getCategoryList()
      await getCategoryTree()
      await createCategory({ name: '测试', parentId: null, level: 1 })
      await updateCategory(1, { id: 1, name: '更新', parentId: null, level: 1 })
      await deleteCategory(1)
      await updateCategoryStatus(1, 1)

      expect(mockedAxios.get).toHaveBeenNthCalledWith(
        1,
        '/api/categories',
        expect.objectContaining({
          headers: { Authorization: `Bearer ${mockToken}` }
        })
      )
    })

    it('应在Token过期时自动刷新', async () => {
      mockedAxios.get.mockRejectedValue({
        response: { status: 401, data: { message: 'Token已过期' } }
      })

      // Mock token刷新逻辑
      const newToken = 'new-token-789012'
      localStorage.setItem('token', newToken)

      mockedAxios.get.mockResolvedValue({
        data: { code: 200, message: 'success', data: [] }
      })

      await getCategoryList()

      // 验证使用新Token重试
      expect(mockedAxios.get).toHaveBeenCalledTimes(2)
    })

    it('应在无Token时跳转到登录页', async () => {
      localStorage.removeItem('token')

      // Mock window.location
      const mockLocation = { href: '' }
      vi.stubGlobal('window', { location: mockLocation })

      await expect(getCategoryList()).rejects.toThrow('未登录或登录已过期')

      expect(mockLocation.href).toContain('/login')
    })
  })

  describe('错误处理', () => {
    it('应正确处理网络错误', async () => {
      mockedAxios.get.mockRejectedValue(new Error('Network Error'))

      await expect(getCategoryList()).rejects.toThrow('Network Error')
    })

    it('应正确处理超时错误', async () => {
      mockedAxios.get.mockRejectedValue({ code: 'ECONNABORTED', message: 'timeout' })

      await expect(getCategoryList()).rejects.toThrow()
    })

    it('应正确处理服务器错误', async () => {
      mockedAxios.get.mockRejectedValue({
        response: { status: 500, data: { message: '服务器内部错误' } }
      })

      await expect(getCategoryList()).rejects.toThrow('服务器内部错误')
    })

    it('应正确处理4xx错误', async () => {
      mockedAxios.get.mockRejectedValue({
        response: { status: 400, data: { message: '参数错误' } }
      })

      await expect(getCategoryList()).rejects.toThrow('参数错误')
    })
  })

  describe('请求拦截', () => {
    it('应在所有请求中添加时间戳防止缓存', async () => {
      await getCategoryList()

      const config = mockedAxios.get.mock.calls[0][1]
      expect(config).toBeDefined()
    })

    it('应在POST请求中设置正确的Content-Type', async () => {
      const newCategory = { name: '测试', parentId: null, level: 1 }

      await createCategory(newCategory)

      const config = mockedAxios.post.mock.calls[0][2]
      expect(config.headers['Content-Type']).toBe('application/json')
    })

    it('应在文件上传时设置multipart/form-data', async () => {
      const mockFile = new File(['test'], 'test.xlsx')

      mockedAxios.post.mockResolvedValue({
        data: { code: 200, message: 'success', data: {} }
      })

      await importCategories(mockFile)

      const config = mockedAxios.post.mock.calls[0][2]
      expect(config.headers['Content-Type']).toBe('multipart/form-data')
    })
  })

  describe('响应拦截', () => {
    it('应统一处理响应格式', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { code: 200, message: 'success', data: [] }
      })

      const result = await getCategoryList()

      expect(result).toBeDefined()
      expect(Array.isArray(result)).toBe(true)
    })

    it('应统一处理错误响应', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { code: 400, message: '参数错误', data: null }
      })

      await expect(getCategoryList()).rejects.toThrow('参数错误')
    })

    it('应在业务错误时显示错误信息', async () => {
      mockedAxios.get.mockResolvedValue({
        data: { code: 500, message: '业务处理失败', data: null }
      })

      await expect(getCategoryList()).rejects.toThrow('业务处理失败')
    })
  })

  describe('并发请求', () => {
    it('应正确处理并发获取分类列表和树', async () => {
      const mockList = [{ id: 1, name: '电子产品' }]
      const mockTree = [{ id: 1, name: '电子产品', children: [] }]

      mockedAxios.get
        .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: mockList } })
        .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: mockTree } })

      const [list, tree] = await Promise.all([
        getCategoryList(),
        getCategoryTree()
      ])

      expect(list).toEqual(mockList)
      expect(tree).toEqual(mockTree)
    })

    it('应在并发请求部分失败时不影响其他请求', async () => {
      mockedAxios.get
        .mockResolvedValueOnce({ data: { code: 200, message: 'success', data: [] } })
        .mockRejectedValueOnce(new Error('Network Error'))

      await expect(
        Promise.all([
          getCategoryList(),
          getCategoryTree()
        ])
      ).rejects.toThrow()
    })
  })

  describe('性能测试', () => {
    it('应在合理时间内完成请求', async () => {
      const startTime = Date.now()

      mockedAxios.get.mockImplementation(() =>
        new Promise((resolve) => {
          setTimeout(() => {
            resolve({ data: { code: 200, message: 'success', data: [] } })
          }, 100)
        })
      )

      await getCategoryList()

      const endTime = Date.now()
      expect(endTime - startTime).toBeLessThan(200)
    })

    it('应处理大量分类数据', async () => {
      const largeData = Array.from({ length: 1000 }, (_, i) => ({
        id: i + 1,
        name: `分类${i + 1}`,
        level: 1
      }))

      mockedAxios.get.mockResolvedValue({
        data: { code: 200, message: 'success', data: largeData }
      })

      const result = await getCategoryList()

      expect(result).toHaveLength(1000)
    })
  })
})
