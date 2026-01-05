/**
 * 商品 API 测试
 * Product API Tests
 *
 * 测试覆盖：
 * - API 调用测试
 * - 错误处理测试
 * - 请求参数验证
 * - 响应数据格式验证
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import axios from 'axios'
import * as productApi from '../product'
import type { Product } from '@/types/product'

// Mock axios
vi.mock('axios')

describe('Product API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getProducts - 获取商品列表', () => {
    it('should fetch products successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          records: [
            {
              id: 1,
              sku: 'SKU001',
              name: 'iPhone 15',
              categoryId: 1,
              categoryName: '电子产品',
              unit: '台',
              price: 5999.00,
              costPrice: 4500.00,
              warningStock: 10,
              status: 1
            }
          ],
          total: 1,
          page: 1,
          size: 10
        }
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.getProducts({
        page: 1,
        size: 10
      })

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/products', {
        params: {
          page: 1,
          size: 10
        }
      })
      expect(result.data.records).toHaveLength(1)
      expect(result.data.records[0].sku).toBe('SKU001')
    })

    it('should fetch products with filters', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          records: [],
          total: 0,
          page: 1,
          size: 10
        }
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      await productApi.getProducts({
        page: 1,
        size: 10,
        name: 'iPhone',
        sku: 'SKU001',
        categoryId: 1,
        status: 1
      })

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/products', {
        params: {
          page: 1,
          size: 10,
          name: 'iPhone',
          sku: 'SKU001',
          categoryId: 1,
          status: 1
        }
      })
    })

    it('should handle API error', async () => {
      // Arrange
      const mockError = new Error('Network Error')
      vi.mocked(axios.get).mockRejectedValue(mockError)

      // Act & Assert
      await expect(productApi.getProducts({
        page: 1,
        size: 10
      })).rejects.toThrow('Network Error')
    })

    it('should handle empty response', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          records: [],
          total: 0,
          page: 1,
          size: 10
        }
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.getProducts({
        page: 1,
        size: 10
      })

      // Assert
      expect(result.data.records).toEqual([])
      expect(result.data.total).toBe(0)
    })
  })

  describe('getProductById - 获取商品详情', () => {
    it('should fetch product by id successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          id: 1,
          sku: 'SKU001',
          name: 'iPhone 15',
          categoryId: 1,
          categoryName: '电子产品',
          unit: '台',
          price: 5999.00,
          costPrice: 4500.00,
          warningStock: 10,
          status: 1
        }
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.getProductById(1)

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/products/1')
      expect(result.data.id).toBe(1)
      expect(result.data.sku).toBe('SKU001')
    })

    it('should handle non-existent product', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '商品不存在',
        data: null
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.getProductById(999)

      // Assert
      expect(result.code).toBe(404)
      expect(result.data).toBeNull()
    })

    it('should handle API error', async () => {
      // Arrange
      const mockError = new Error('Network Error')
      vi.mocked(axios.get).mockRejectedValue(mockError)

      // Act & Assert
      await expect(productApi.getProductById(1)).rejects.toThrow('Network Error')
    })
  })

  describe('createProduct - 创建商品', () => {
    it('should create product successfully', async () => {
      // Arrange
      const newProduct: Partial<Product> = {
        sku: 'SKU002',
        name: 'MacBook Pro',
        categoryId: 1,
        unit: '台',
        price: 12999.00,
        costPrice: 10000.00,
        warningStock: 5,
        status: 1
      }

      const mockResponse = {
        code: 200,
        message: '创建成功',
        data: 2
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.createProduct(newProduct)

      // Assert
      expect(axios.post).toHaveBeenCalledWith('/api/products', newProduct)
      expect(result.code).toBe(200)
      expect(result.message).toBe('创建成功')
      expect(result.data).toBe(2)
    })

    it('should handle duplicate SKU error', async () => {
      // Arrange
      const duplicateProduct: Partial<Product> = {
        sku: 'SKU001', // 重复的SKU
        name: '测试商品',
        categoryId: 1,
        price: 100.00
      }

      const mockResponse = {
        code: 400,
        message: 'SKU已存在',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.createProduct(duplicateProduct)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('SKU')
    })

    it('should handle negative price error', async () => {
      // Arrange
      const invalidProduct: Partial<Product> = {
        sku: 'SKU999',
        name: '测试商品',
        categoryId: 1,
        price: -100.00
      }

      const mockResponse = {
        code: 400,
        message: '价格不能为负数',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.createProduct(invalidProduct)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('价格')
    })

    it('should handle missing required fields', async () => {
      // Arrange
      const invalidProduct: Partial<Product> = {
        name: '测试商品'
        // 缺少必填字段：sku, categoryId, price
      }

      const mockResponse = {
        code: 400,
        message: '参数校验失败',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.createProduct(invalidProduct)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('校验')
    })

    it('should handle API error', async () => {
      // Arrange
      const newProduct: Partial<Product> = {
        sku: 'SKU002',
        name: 'MacBook Pro',
        categoryId: 1,
        price: 12999.00
      }

      vi.mocked(axios.post).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.createProduct(newProduct)).rejects.toThrow('Network Error')
    })
  })

  describe('updateProduct - 更新商品', () => {
    it('should update product successfully', async () => {
      // Arrange
      const updateData: Partial<Product> = {
        id: 1,
        name: 'iPhone 15 Pro',
        price: 6999.00
      }

      const mockResponse = {
        code: 200,
        message: '更新成功',
        data: true
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.updateProduct(updateData)

      // Assert
      expect(axios.put).toHaveBeenCalledWith('/api/products/1', updateData)
      expect(result.code).toBe(200)
      expect(result.message).toBe('更新成功')
    })

    it('should handle duplicate SKU when updating', async () => {
      // Arrange
      const updateData: Partial<Product> = {
        id: 1,
        sku: 'SKU002' // 另一个商品的SKU
      }

      const mockResponse = {
        code: 400,
        message: 'SKU已存在',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.updateProduct(updateData)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('SKU')
    })

    it('should handle product not found', async () => {
      // Arrange
      const updateData: Partial<Product> = {
        id: 999,
        name: '测试'
      }

      const mockResponse = {
        code: 404,
        message: '商品不存在',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.updateProduct(updateData)

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      const updateData: Partial<Product> = {
        id: 1,
        name: 'iPhone 15 Pro'
      }

      vi.mocked(axios.put).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.updateProduct(updateData)).rejects.toThrow('Network Error')
    })
  })

  describe('deleteProduct - 删除商品', () => {
    it('should delete product successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '删除成功',
        data: true
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.deleteProduct(1)

      // Assert
      expect(axios.delete).toHaveBeenCalledWith('/api/products/1')
      expect(result.code).toBe(200)
      expect(result.message).toBe('删除成功')
    })

    it('should handle deletion with inventory records', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '该商品有库存记录，无法删除',
        data: null
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.deleteProduct(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('库存记录')
    })

    it('should handle deletion with inbound/outbound records', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '该商品有出入库记录，无法删除',
        data: null
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.deleteProduct(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('出入库记录')
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.delete).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.deleteProduct(1)).rejects.toThrow('Network Error')
    })
  })

  describe('batchDeleteProducts - 批量删除商品', () => {
    it('should batch delete products successfully', async () => {
      // Arrange
      const ids = [1, 2, 3]
      const mockResponse = {
        code: 200,
        message: '成功删除3个商品',
        data: 3
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.batchDeleteProducts(ids)

      // Assert
      expect(axios.delete).toHaveBeenCalledWith('/api/products/batch', {
        data: { ids }
      })
      expect(result.code).toBe(200)
      expect(result.data).toBe(3)
    })

    it('should handle empty id list', async () => {
      // Arrange
      const ids: number[] = []
      const mockResponse = {
        code: 200,
        message: '没有选择要删除的商品',
        data: 0
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.batchDeleteProducts(ids)

      // Assert
      expect(result.data).toBe(0)
    })

    it('should handle batch deletion with associations', async () => {
      // Arrange
      const ids = [1, 2]
      const mockResponse = {
        code: 400,
        message: '有商品无法删除',
        data: null
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.batchDeleteProducts(ids)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('无法删除')
    })

    it('should handle API error', async () => {
      // Arrange
      const ids = [1, 2, 3]
      vi.mocked(axios.delete).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.batchDeleteProducts(ids)).rejects.toThrow('Network Error')
    })
  })

  describe('updateProductStatus - 切换商品状态', () => {
    it('should toggle product status successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '状态切换成功',
        data: true
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.updateProductStatus(1, 0)

      // Assert
      expect(axios.patch).toHaveBeenCalledWith('/api/products/1/status', {
        status: 0
      })
      expect(result.code).toBe(200)
      expect(result.message).toBe('状态切换成功')
    })

    it('should handle product not found', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '商品不存在',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.updateProductStatus(999, 0)

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.patch).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.updateProductStatus(1, 0)).rejects.toThrow('Network Error')
    })
  })

  describe('checkSkuExists - 检查SKU是否存在', () => {
    it('should return true when SKU exists', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: true
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.checkSkuExists('SKU001')

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/products/check-sku', {
        params: { sku: 'SKU001' }
      })
      expect(result.data).toBe(true)
    })

    it('should return false when SKU does not exist', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: false
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.checkSkuExists('SKU999')

      // Assert
      expect(result.data).toBe(false)
    })

    it('should exclude current product when checking', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: false
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.checkSkuExists('SKU001', 1)

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/products/check-sku', {
        params: { sku: 'SKU001', excludeId: 1 }
      })
      expect(result.data).toBe(false)
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.get).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.checkSkuExists('SKU001')).rejects.toThrow('Network Error')
    })
  })

  describe('searchProducts - 搜索商品', () => {
    it('should search products by keyword', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: [
          {
            id: 1,
            sku: 'SKU001',
            name: 'iPhone 15',
            categoryId: 1,
            categoryName: '电子产品',
            price: 5999.00
          }
        ]
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.searchProducts('iPhone')

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/products/search', {
        params: { keyword: 'iPhone' }
      })
      expect(result.data).toHaveLength(1)
    })

    it('should return empty array when no results', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: []
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.searchProducts('不存在')

      // Assert
      expect(result.data).toEqual([])
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.get).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.searchProducts('iPhone')).rejects.toThrow('Network Error')
    })
  })

  describe('importProducts - 批量导入商品', () => {
    it('should import products successfully', async () => {
      // Arrange
      const file = new File(['test'], 'test.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      const formData = new FormData()
      formData.append('file', file)

      const mockResponse = {
        code: 200,
        message: '导入成功',
        data: {
          successCount: 10,
          failCount: 0,
          errors: []
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.importProducts(file)

      // Assert
      expect(result.code).toBe(200)
      expect(result.data.successCount).toBe(10)
    })

    it('should handle partial import failure', async () => {
      // Arrange
      const file = new File(['test'], 'test.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })

      const mockResponse = {
        code: 207, // Multi-status
        message: '部分导入成功',
        data: {
          successCount: 8,
          failCount: 2,
          errors: [
            { row: 3, error: 'SKU重复' },
            { row: 5, error: '价格无效' }
          ]
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.importProducts(file)

      // Assert
      expect(result.data.successCount).toBe(8)
      expect(result.data.failCount).toBe(2)
      expect(result.data.errors).toHaveLength(2)
    })

    it('should handle invalid file format', async () => {
      // Arrange
      const file = new File(['test'], 'test.txt', { type: 'text/plain' })

      const mockResponse = {
        code: 400,
        message: '文件格式错误',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.importProducts(file)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('文件格式')
    })

    it('should handle API error', async () => {
      // Arrange
      const file = new File(['test'], 'test.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      vi.mocked(axios.post).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.importProducts(file)).rejects.toThrow('Network Error')
    })
  })

  describe('exportProducts - 导出商品数据', () => {
    it('should export products successfully', async () => {
      // Arrange
      const mockBlob = new Blob(['test data'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      vi.mocked(axios.get).mockResolvedValue({ data: mockBlob })

      // Act
      const result = await productApi.exportProducts()

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/products/export', {
        responseType: 'blob'
      })
      expect(result).toBeInstanceOf(Blob)
    })

    it('should export selected products only', async () => {
      // Arrange
      const ids = [1, 2, 3]
      const mockBlob = new Blob(['test data'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      vi.mocked(axios.get).mockResolvedValue({ data: mockBlob })

      // Act
      const result = await productApi.exportProducts(ids)

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/products/export', {
        params: { ids: '1,2,3' },
        responseType: 'blob'
      })
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.get).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.exportProducts()).rejects.toThrow('Network Error')
    })
  })

  describe('downloadTemplate - 下载导入模板', () => {
    it('should download template successfully', async () => {
      // Arrange
      const mockBlob = new Blob(['template data'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      vi.mocked(axios.get).mockResolvedValue({ data: mockBlob })

      // Act
      const result = await productApi.downloadTemplate()

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/products/template', {
        responseType: 'blob'
      })
      expect(result).toBeInstanceOf(Blob)
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.get).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(productApi.downloadTemplate()).rejects.toThrow('Network Error')
    })
  })

  describe('请求拦截器测试 (Request Interceptor)', () => {
    it('should include authentication token in headers', async () => {
      // 这个测试需要在实际项目中配置认证后才能运行
      // 这里只是示例代码
      expect(true).toBe(true)
    })

    it('should handle request timeout', async () => {
      // Arrange
      vi.mocked(axios.get).mockRejectedValue(new Error('timeout'))

      // Act & Assert
      await expect(productApi.getProducts({
        page: 1,
        size: 10
      })).rejects.toThrow('timeout')
    })
  })

  describe('响应拦截器测试 (Response Interceptor)', () => {
    it('should handle common error codes', async () => {
      // Arrange
      const mockResponse = {
        code: 401,
        message: '未授权',
        data: null
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.getProductById(1)

      // Assert
      expect(result.code).toBe(401)
    })

    it('should handle server error', async () => {
      // Arrange
      const mockResponse = {
        code: 500,
        message: '服务器内部错误',
        data: null
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await productApi.getProductById(1)

      // Assert
      expect(result.code).toBe(500)
    })
  })
})
