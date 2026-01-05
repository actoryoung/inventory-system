/**
 * 入库 API 测试
 * Inbound API Tests
 *
 * 测试覆盖：
 * - API 调用测试
 * - 错误处理测试
 * - 请求参数验证
 * - 响应数据格式验证
 * - 业务规则验证（单号生成、数量限制、状态流转）
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import axios from 'axios'
import * as inboundApi from '../inbound'
import type { Inbound } from '@/types/inbound'

// Mock axios
vi.mock('axios')

describe('Inbound API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getInbounds - 获取入库单列表', () => {
    it('should fetch inbounds successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          records: [
            {
              id: 1,
              inboundNo: 'IN202601040001',
              productId: 1,
              productName: 'iPhone 15',
              quantity: 100,
              supplier: '供应商A',
              inboundDate: '2026-01-04T10:00:00',
              status: 0, // 待审核
              remark: '测试入库单',
              createdAt: '2026-01-04T10:00:00'
            }
          ],
          total: 1,
          page: 1,
          size: 10
        }
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.getInbounds({
        page: 1,
        size: 10
      })

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/inbound', {
        params: {
          page: 1,
          size: 10
        }
      })
      expect(result.data.records).toHaveLength(1)
      expect(result.data.records[0].inboundNo).toBe('IN202601040001')
    })

    it('should fetch inbounds with filters', async () => {
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
      await inboundApi.getInbounds({
        page: 1,
        size: 10,
        productId: 1,
        status: 0,
        startDate: '2026-01-01T00:00:00',
        endDate: '2026-01-31T23:59:59'
      })

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/inbound', {
        params: {
          page: 1,
          size: 10,
          productId: 1,
          status: 0,
          startDate: '2026-01-01T00:00:00',
          endDate: '2026-01-31T23:59:59'
        }
      })
    })

    it('should handle API error', async () => {
      // Arrange
      const mockError = new Error('Network Error')
      vi.mocked(axios.get).mockRejectedValue(mockError)

      // Act & Assert
      await expect(inboundApi.getInbounds({
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
      const result = await inboundApi.getInbounds({
        page: 1,
        size: 10
      })

      // Assert
      expect(result.data.records).toEqual([])
      expect(result.data.total).toBe(0)
    })
  })

  describe('getInboundById - 获取入库单详情', () => {
    it('should fetch inbound by id successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          id: 1,
          inboundNo: 'IN202601040001',
          productId: 1,
          productName: 'iPhone 15',
          quantity: 100,
          supplier: '供应商A',
          inboundDate: '2026-01-04T10:00:00',
          status: 0,
          remark: '测试入库单',
          createdBy: 'admin',
          createdAt: '2026-01-04T10:00:00'
        }
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.getInboundById(1)

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/inbound/1')
      expect(result.data.id).toBe(1)
      expect(result.data.inboundNo).toBe('IN202601040001')
    })

    it('should handle non-existent inbound', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '入库单不存在',
        data: null
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.getInboundById(999)

      // Assert
      expect(result.code).toBe(404)
      expect(result.data).toBeNull()
    })

    it('should handle API error', async () => {
      // Arrange
      const mockError = new Error('Network Error')
      vi.mocked(axios.get).mockRejectedValue(mockError)

      // Act & Assert
      await expect(inboundApi.getInboundById(1)).rejects.toThrow('Network Error')
    })
  })

  describe('createInbound - 创建入库单', () => {
    it('should create inbound successfully with auto-generated number', async () => {
      // Arrange
      const newInbound = {
        productId: 1,
        quantity: 100,
        supplier: '供应商A',
        inboundDate: '2026-01-04T10:00:00',
        remark: '测试入库单'
      }

      const mockResponse = {
        code: 200,
        message: '入库单创建成功',
        data: {
          id: 1,
          inboundNo: 'IN202601040001', // 自动生成的单号
          ...newInbound,
          status: 0 // 待审核
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(newInbound)

      // Assert
      expect(axios.post).toHaveBeenCalledWith('/api/inbound', newInbound)
      expect(result.code).toBe(200)
      expect(result.message).toBe('入库单创建成功')
      expect(result.data.inboundNo).toMatch(/^IN\d{12}$/) // 验证单号格式
      expect(result.data.status).toBe(0) // 验证初始状态为待审核
    })

    it('should validate inbound number format', async () => {
      // Arrange
      const newInbound = {
        productId: 1,
        quantity: 50,
        supplier: '供应商B',
        inboundDate: '2026-01-04T10:00:00'
      }

      const mockResponse = {
        code: 200,
        message: '入库单创建成功',
        data: {
          id: 2,
          inboundNo: 'IN202601040002',
          ...newInbound,
          status: 0
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(newInbound)

      // Assert
      // 验证单号格式：IN + yyyyMMdd + 4位序号
      expect(result.data.inboundNo).toMatch(/^IN\d{12}$/)

      // 验证日期部分为当天日期
      const datePart = result.data.inboundNo.substring(2, 10)
      const todayDate = new Date().toISOString().slice(0, 10).replace(/-/g, '')
      expect(datePart).toBe(todayDate)

      // 验证序号部分为4位数字
      const sequencePart = result.data.inboundNo.substring(10, 14)
      expect(sequencePart).toMatch(/^\d{4}$/)
    })

    it('should handle product not found error', async () => {
      // Arrange
      const invalidInbound = {
        productId: 999, // 不存在的商品
        quantity: 100,
        supplier: '供应商'
      }

      const mockResponse = {
        code: 400,
        message: '商品不存在或已禁用',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(invalidInbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('商品')
    })

    it('should validate quantity must be greater than 0', async () => {
      // Arrange
      const invalidInbound = {
        productId: 1,
        quantity: 0, // 不合法的数量
        supplier: '供应商'
      }

      const mockResponse = {
        code: 400,
        message: '入库数量必须大于0',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(invalidInbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('入库数量')
    })

    it('should validate negative quantity', async () => {
      // Arrange
      const invalidInbound = {
        productId: 1,
        quantity: -50, // 负数
        supplier: '供应商'
      }

      const mockResponse = {
        code: 400,
        message: '入库数量必须大于0',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(invalidInbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('入库数量')
    })

    it('should validate quantity maximum limit (999999)', async () => {
      // Arrange
      const invalidInbound = {
        productId: 1,
        quantity: 1000000, // 超过最大值
        supplier: '供应商'
      }

      const mockResponse = {
        code: 400,
        message: '入库数量超过最大限制',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(invalidInbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('入库数量')
    })

    it('should validate supplier is required', async () => {
      // Arrange
      const invalidInbound = {
        productId: 1,
        quantity: 100,
        supplier: null // 供应商为空
      }

      const mockResponse = {
        code: 400,
        message: '供应商名称不能为空',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(invalidInbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('供应商')
    })

    it('should validate empty supplier', async () => {
      // Arrange
      const invalidInbound = {
        productId: 1,
        quantity: 100,
        supplier: '' // 空字符串
      }

      const mockResponse = {
        code: 400,
        message: '供应商名称不能为空',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(invalidInbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('供应商')
    })

    it('should handle API error', async () => {
      // Arrange
      const newInbound = {
        productId: 1,
        quantity: 100,
        supplier: '供应商'
      }

      vi.mocked(axios.post).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(inboundApi.createInbound(newInbound)).rejects.toThrow('Network Error')
    })

    it('should accept minimum boundary quantity (1)', async () => {
      // Arrange
      const boundaryInbound = {
        productId: 1,
        quantity: 1, // 最小边界值
        supplier: '供应商'
      }

      const mockResponse = {
        code: 200,
        message: '入库单创建成功',
        data: {
          id: 1,
          inboundNo: 'IN202601040001',
          ...boundaryInbound,
          status: 0
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(boundaryInbound)

      // Assert
      expect(result.code).toBe(200)
    })

    it('should accept maximum boundary quantity (999999)', async () => {
      // Arrange
      const boundaryInbound = {
        productId: 1,
        quantity: 999999, // 最大边界值
        supplier: '供应商'
      }

      const mockResponse = {
        code: 200,
        message: '入库单创建成功',
        data: {
          id: 1,
          inboundNo: 'IN202601040001',
          ...boundaryInbound,
          status: 0
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.createInbound(boundaryInbound)

      // Assert
      expect(result.code).toBe(200)
    })
  })

  describe('updateInbound - 更新入库单', () => {
    it('should update inbound successfully when status is pending', async () => {
      // Arrange
      const updateData = {
        id: 1,
        quantity: 150,
        supplier: '新供应商',
        remark: '更新后的备注'
      }

      const mockResponse = {
        code: 200,
        message: '更新成功',
        data: true
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.updateInbound(1, updateData)

      // Assert
      expect(axios.put).toHaveBeenCalledWith('/api/inbound/1', updateData)
      expect(result.code).toBe(200)
      expect(result.message).toBe('更新成功')
    })

    it('should handle update when inbound is already approved', async () => {
      // Arrange
      const updateData = {
        id: 1,
        quantity: 200
      }

      const mockResponse = {
        code: 400,
        message: '入库单已审核，无法修改',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.updateInbound(1, updateData)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已审核')
    })

    it('should handle update when inbound is voided', async () => {
      // Arrange
      const updateData = {
        id: 1,
        quantity: 200
      }

      const mockResponse = {
        code: 400,
        message: '入库单已作废，无法修改',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.updateInbound(1, updateData)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已作废')
    })

    it('should validate quantity when updating', async () => {
      // Arrange
      const invalidUpdate = {
        id: 1,
        quantity: 0
      }

      const mockResponse = {
        code: 400,
        message: '入库数量必须大于0',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.updateInbound(1, invalidUpdate)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('入库数量')
    })

    it('should handle inbound not found', async () => {
      // Arrange
      const updateData = {
        id: 999,
        quantity: 100
      }

      const mockResponse = {
        code: 404,
        message: '入库单不存在',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.updateInbound(999, updateData)

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      const updateData = {
        id: 1,
        quantity: 150
      }

      vi.mocked(axios.put).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(inboundApi.updateInbound(1, updateData)).rejects.toThrow('Network Error')
    })
  })

  describe('deleteInbound - 删除入库单', () => {
    it('should delete inbound successfully when status is pending', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '删除成功',
        data: true
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.deleteInbound(1)

      // Assert
      expect(axios.delete).toHaveBeenCalledWith('/api/inbound/1')
      expect(result.code).toBe(200)
      expect(result.message).toBe('删除成功')
    })

    it('should handle deletion when inbound is already approved', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '入库单已审核，无法删除',
        data: null
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.deleteInbound(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已审核')
    })

    it('should handle deletion when inbound is voided', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '入库单已作废，无法删除',
        data: null
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.deleteInbound(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已作废')
    })

    it('should handle inbound not found', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '入库单不存在',
        data: null
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.deleteInbound(999)

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.delete).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(inboundApi.deleteInbound(1)).rejects.toThrow('Network Error')
    })
  })

  describe('approveInbound - 审核入库单', () => {
    it('should approve inbound successfully and increase inventory', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '审核成功',
        data: {
          id: 1,
          inboundNo: 'IN202601040001',
          status: 1, // 已审核
          approvedBy: 'admin',
          approvedAt: '2026-01-04T11:00:00'
        }
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.approveInbound(1, 'admin')

      // Assert
      expect(axios.patch).toHaveBeenCalledWith('/api/inbound/1/approve', {
        approvedBy: 'admin'
      })
      expect(result.code).toBe(200)
      expect(result.message).toBe('审核成功')
      expect(result.data.status).toBe(1) // 验证状态更新为已审核
      expect(result.data.approvedBy).toBe('admin')
      expect(result.data.approvedAt).toBeDefined()
    })

    it('should handle approval when inbound is already approved', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '入库单已审核，无法重复审核',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.approveInbound(1, 'admin')

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已审核')
    })

    it('should handle approval when inbound is voided', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '入库单已作废，无法审核',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.approveInbound(1, 'admin')

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已作废')
    })

    it('should handle inbound not found', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '入库单不存在',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.approveInbound(999, 'admin')

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.patch).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(inboundApi.approveInbound(1, 'admin')).rejects.toThrow('Network Error')
    })
  })

  describe('voidInbound - 作废入库单', () => {
    it('should void inbound successfully when status is pending', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '作废成功',
        data: {
          id: 1,
          inboundNo: 'IN202601040001',
          status: 2 // 已作废
        }
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.voidInbound(1)

      // Assert
      expect(axios.patch).toHaveBeenCalledWith('/api/inbound/1/void')
      expect(result.code).toBe(200)
      expect(result.message).toBe('作废成功')
      expect(result.data.status).toBe(2) // 验证状态更新为已作废
    })

    it('should handle void when inbound is already approved', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '入库单已审核，无法作废',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.voidInbound(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已审核')
    })

    it('should handle void when inbound is already voided', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '入库单已作废，无法重复作废',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.voidInbound(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已作废')
    })

    it('should handle inbound not found', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '入库单不存在',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.voidInbound(999)

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.patch).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(inboundApi.voidInbound(1)).rejects.toThrow('Network Error')
    })
  })

  describe('状态流转测试 (Status Flow Tests)', () => {
    it('should follow correct status flow: pending -> approved', async () => {
      // Arrange - 创建待审核的入库单
      const createResponse = {
        code: 200,
        message: '入库单创建成功',
        data: {
          id: 1,
          inboundNo: 'IN202601040001',
          status: 0 // 待审核
        }
      }

      // Act - 审核入库单
      const approveResponse = {
        code: 200,
        message: '审核成功',
        data: {
          id: 1,
          status: 1 // 已审核
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: createResponse })
      vi.mocked(axios.patch).mockResolvedValue({ data: approveResponse })

      // 验证状态流转
      const createResult = await inboundApi.createInbound({
        productId: 1,
        quantity: 100,
        supplier: '供应商'
      })

      expect(createResult.data.status).toBe(0)

      const approveResult = await inboundApi.approveInbound(1, 'admin')
      expect(approveResult.data.status).toBe(1)
    })

    it('should follow correct status flow: pending -> voided', async () => {
      // Arrange
      const voidResponse = {
        code: 200,
        message: '作废成功',
        data: {
          id: 1,
          status: 2 // 已作废
        }
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: voidResponse })

      // Act
      const result = await inboundApi.voidInbound(1)

      // Assert
      expect(result.data.status).toBe(2)
    })

    it('should prevent status change from approved to pending', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '入库单已审核，无法修改',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.updateInbound(1, {
        id: 1,
        quantity: 200
      })

      // Assert
      expect(result.code).toBe(400)
    })

    it('should prevent status change from voided to pending', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '入库单已作废，无法修改',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.updateInbound(1, {
        id: 1,
        quantity: 200
      })

      // Assert
      expect(result.code).toBe(400)
    })
  })

  describe('错误处理测试 (Error Handling)', () => {
    it('should handle 401 unauthorized', async () => {
      // Arrange
      const mockResponse = {
        code: 401,
        message: '未授权',
        data: null
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.getInboundById(1)

      // Assert
      expect(result.code).toBe(401)
      expect(result.message).toBe('未授权')
    })

    it('should handle 500 server error', async () => {
      // Arrange
      const mockResponse = {
        code: 500,
        message: '服务器内部错误',
        data: null
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await inboundApi.getInboundById(1)

      // Assert
      expect(result.code).toBe(500)
      expect(result.message).toBe('服务器内部错误')
    })

    it('should handle network timeout', async () => {
      // Arrange
      vi.mocked(axios.get).mockRejectedValue(new Error('timeout'))

      // Act & Assert
      await expect(inboundApi.getInboundById(1)).rejects.toThrow('timeout')
    })
  })
})
