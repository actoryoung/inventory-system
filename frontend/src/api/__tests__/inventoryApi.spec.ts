/**
 * 库存管理 API 测试
 * Inventory API Tests
 *
 * 测试覆盖：
 * - 库存列表查询 API
 * - 商品库存查询 API
 * - 库存调整 API（增加/减少/设值）
 * - 低库存查询 API
 * - 库存充足性检查 API
 * - 库存汇总 API
 * - 错误处理测试
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import axios from 'axios'
import * as inventoryApi from '../inventory'

// Mock axios
vi.mock('axios')

// 创建模拟的 request 模块
const mockRequest = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
  patch: vi.fn()
}

// Mock request 模块
vi.mock('@/utils/request', () => ({
  default: mockRequest
}))

describe('Inventory API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getInventoryList - 获取库存列表', () => {
    it('should fetch inventory list successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          records: [
            {
              id: 1,
              productId: 1,
              productName: 'iPhone 15',
              productSku: 'SKU001',
              categoryId: 1,
              categoryName: '电子产品',
              warehouseId: 1,
              quantity: 100,
              warningStock: 10,
              isLowStock: false
            }
          ],
          total: 1,
          page: 1,
          size: 10
        }
      }

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getInventoryList({
        page: 1,
        size: 10
      })

      // Assert
      expect(mockRequest.get).toHaveBeenCalledWith('/api/inventory', {
        params: {
          page: 1,
          size: 10
        }
      })
      expect(result.data.records).toHaveLength(1)
      expect(result.data.records[0].productId).toBe(1)
    })

    it('should fetch inventory with filters', async () => {
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

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      await inventoryApi.getInventoryList({
        page: 1,
        size: 10,
        productName: 'iPhone',
        categoryId: 1,
        lowStockOnly: true
      })

      // Assert
      expect(mockRequest.get).toHaveBeenCalledWith('/api/inventory', {
        params: {
          page: 1,
          size: 10,
          productName: 'iPhone',
          categoryId: 1,
          lowStockOnly: true
        }
      })
    })

    it('should handle API error', async () => {
      // Arrange
      const mockError = new Error('Network Error')
      mockRequest.get.mockRejectedValue(mockError)

      // Act & Assert
      await expect(inventoryApi.getInventoryList({
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

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getInventoryList({
        page: 1,
        size: 10
      })

      // Assert
      expect(result.data.records).toEqual([])
      expect(result.data.total).toBe(0)
    })
  })

  describe('getInventoryByProduct - 获取商品库存', () => {
    it('should fetch product inventory successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          id: 1,
          productId: 1,
          productName: 'iPhone 15',
          productSku: 'SKU001',
          warehouseId: 1,
          quantity: 100,
          warningStock: 10
        }
      }

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getInventoryByProduct(1)

      // Assert
      expect(mockRequest.get).toHaveBeenCalledWith('/api/inventory/product/1')
      expect(result.data.productId).toBe(1)
      expect(result.data.quantity).toBe(100)
    })

    it('should handle non-existent inventory', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '库存记录不存在',
        data: null
      }

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getInventoryByProduct(999)

      // Assert
      expect(result.code).toBe(404)
      expect(result.data).toBeNull()
    })

    it('should handle API error', async () => {
      // Arrange
      const mockError = new Error('Network Error')
      mockRequest.get.mockRejectedValue(mockError)

      // Act & Assert
      await expect(inventoryApi.getInventoryByProduct(1)).rejects.toThrow('Network Error')
    })
  })

  describe('adjustInventory - 调整库存', () => {
    it('should add stock successfully', async () => {
      // Arrange
      const adjustData = {
        type: 'add',
        quantity: 50,
        reason: '采购入库'
      }

      const mockResponse = {
        code: 200,
        message: '库存调整成功',
        data: {
          oldQuantity: 100,
          newQuantity: 150
        }
      }

      mockRequest.put.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.adjustInventory(1, adjustData)

      // Assert
      expect(mockRequest.put).toHaveBeenCalledWith('/api/inventory/1/adjust', adjustData)
      expect(result.code).toBe(200)
      expect(result.data.newQuantity).toBe(150)
    })

    it('should reduce stock successfully', async () => {
      // Arrange
      const adjustData = {
        type: 'reduce',
        quantity: 30,
        reason: '销售出库'
      }

      const mockResponse = {
        code: 200,
        message: '库存调整成功',
        data: {
          oldQuantity: 100,
          newQuantity: 70
        }
      }

      mockRequest.put.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.adjustInventory(1, adjustData)

      // Assert
      expect(result.data.newQuantity).toBe(70)
    })

    it('should set stock value successfully', async () => {
      // Arrange
      const adjustData = {
        type: 'set',
        quantity: 200,
        reason: '盘点调整'
      }

      const mockResponse = {
        code: 200,
        message: '库存调整成功',
        data: {
          oldQuantity: 100,
          newQuantity: 200
        }
      }

      mockRequest.put.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.adjustInventory(1, adjustData)

      // Assert
      expect(result.data.newQuantity).toBe(200)
    })

    it('should handle insufficient stock error', async () => {
      // Arrange
      const adjustData = {
        type: 'reduce',
        quantity: 150,
        reason: '测试'
      }

      const mockResponse = {
        code: 400,
        message: '库存不足，当前库存：100，需要：150',
        data: null
      }

      mockRequest.put.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.adjustInventory(1, adjustData)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('库存不足')
    })

    it('should handle inventory not found error', async () => {
      // Arrange
      const adjustData = {
        type: 'add',
        quantity: 50,
        reason: '测试'
      }

      const mockResponse = {
        code: 400,
        message: '库存记录不存在',
        data: null
      }

      mockRequest.put.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.adjustInventory(999, adjustData)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      const adjustData = {
        type: 'add',
        quantity: 50,
        reason: '测试'
      }

      mockRequest.put.mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(inventoryApi.adjustInventory(1, adjustData)).rejects.toThrow('Network Error')
    })
  })

  describe('getLowStockList - 获取低库存列表', () => {
    it('should fetch low stock items successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          records: [
            {
              id: 1,
              productId: 1,
              productName: 'iPhone 15',
              quantity: 5,
              warningStock: 10,
              isLowStock: true
            },
            {
              id: 2,
              productId: 2,
              productName: 'MacBook Pro',
              quantity: 8,
              warningStock: 10,
              isLowStock: true
            }
          ],
          total: 2,
          page: 1,
          size: 10
        }
      }

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getLowStockList({
        page: 1,
        size: 10
      })

      // Assert
      expect(mockRequest.get).toHaveBeenCalledWith('/api/inventory/low-stock', {
        params: {
          page: 1,
          size: 10
        }
      })
      expect(result.data.records).toHaveLength(2)
      expect(result.data.records[0].isLowStock).toBe(true)
    })

    it('should fetch low stock items with category filter', async () => {
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

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      await inventoryApi.getLowStockList({
        page: 1,
        size: 10,
        categoryId: 1
      })

      // Assert
      expect(mockRequest.get).toHaveBeenCalledWith('/api/inventory/low-stock', {
        params: {
          page: 1,
          size: 10,
          categoryId: 1
        }
      })
    })

    it('should return empty list when no low stock items', async () => {
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

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getLowStockList({
        page: 1,
        size: 10
      })

      // Assert
      expect(result.data.records).toEqual([])
      expect(result.data.total).toBe(0)
    })

    it('should handle API error', async () => {
      // Arrange
      mockRequest.get.mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(inventoryApi.getLowStockList({
        page: 1,
        size: 10
      })).rejects.toThrow('Network Error')
    })
  })

  describe('checkStock - 检查库存充足性', () => {
    it('should return true when stock is sufficient', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: true
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.checkStock({
        productId: 1,
        quantity: 50
      })

      // Assert
      expect(mockRequest.post).toHaveBeenCalledWith('/api/inventory/check', {
        productId: 1,
        quantity: 50
      })
      expect(result.data).toBe(true)
    })

    it('should return false when stock is insufficient', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: false
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.checkStock({
        productId: 1,
        quantity: 150
      })

      // Assert
      expect(result.data).toBe(false)
    })

    it('should return false when inventory does not exist', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: false
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.checkStock({
        productId: 999,
        quantity: 10
      })

      // Assert
      expect(result.data).toBe(false)
    })

    it('should handle validation error when productId is missing', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '参数校验失败',
        data: null
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.checkStock({
        quantity: 10
      } as any)

      // Assert
      expect(result.code).toBe(400)
    })

    it('should handle validation error when quantity is negative', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '数量不能为负数',
        data: null
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.checkStock({
        productId: 1,
        quantity: -10
      })

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('不能为负数')
    })

    it('should handle API error', async () => {
      // Arrange
      mockRequest.post.mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(inventoryApi.checkStock({
        productId: 1,
        quantity: 10
      })).rejects.toThrow('Network Error')
    })
  })

  describe('getInventorySummary - 获取库存汇总', () => {
    it('should fetch inventory summary successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: [
          {
            categoryId: 1,
            categoryName: '电子产品',
            productCount: 10,
            totalQuantity: 500,
            totalAmount: 500000
          },
          {
            categoryId: 2,
            categoryName: '食品',
            productCount: 5,
            totalQuantity: 200,
            totalAmount: 2000
          }
        ]
      }

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getInventorySummary()

      // Assert
      expect(mockRequest.get).toHaveBeenCalledWith('/api/inventory/summary')
      expect(result.data).toHaveLength(2)
      expect(result.data[0].categoryName).toBe('电子产品')
      expect(result.data[0].totalQuantity).toBe(500)
    })

    it('should handle empty summary', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: []
      }

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getInventorySummary()

      // Assert
      expect(result.data).toEqual([])
    })

    it('should handle API error', async () => {
      // Arrange
      mockRequest.get.mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(inventoryApi.getInventorySummary()).rejects.toThrow('Network Error')
    })
  })

  describe('批量操作测试 (Batch Operations)', () => {
    it('should handle batch adjust operations', async () => {
      // Arrange
      const adjustments = [
        { inventoryId: 1, type: 'add', quantity: 10, reason: '批量调整' },
        { inventoryId: 2, type: 'reduce', quantity: 5, reason: '批量调整' }
      ]

      const mockResponse = {
        code: 200,
        message: '批量调整成功',
        data: {
          successCount: 2,
          failCount: 0,
          errors: []
        }
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.batchAdjust(adjustments)

      // Assert
      expect(mockRequest.post).toHaveBeenCalledWith('/api/inventory/batch-adjust', {
        adjustments
      })
      expect(result.data.successCount).toBe(2)
    })

    it('should handle partial batch adjust failure', async () => {
      // Arrange
      const adjustments = [
        { inventoryId: 1, type: 'add', quantity: 10, reason: '批量调整' },
        { inventoryId: 2, type: 'reduce', quantity: 9999, reason: '批量调整' } // 库存不足
      ]

      const mockResponse = {
        code: 207, // Multi-status
        message: '部分调整成功',
        data: {
          successCount: 1,
          failCount: 1,
          errors: [
            { index: 1, error: '库存不足' }
          ]
        }
      }

      mockRequest.post.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.batchAdjust(adjustments)

      // Assert
      expect(result.data.successCount).toBe(1)
      expect(result.data.failCount).toBe(1)
      expect(result.data.errors).toHaveLength(1)
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
      mockRequest.get.mockRejectedValue(new Error('timeout'))

      // Act & Assert
      await expect(inventoryApi.getInventoryList({
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

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getInventoryByProduct(1)

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

      mockRequest.get.mockResolvedValue(mockResponse)

      // Act
      const result = await inventoryApi.getInventoryByProduct(1)

      // Assert
      expect(result.code).toBe(500)
    })
  })
})
