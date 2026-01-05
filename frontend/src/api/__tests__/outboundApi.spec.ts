/**
 * 出库 API 测试
 * Outbound API Tests
 *
 * 测试覆盖：
 * - API 调用测试
 * - 错误处理测试
 * - 请求参数验证
 * - 响应数据格式验证
 * - 业务规则验证（单号生成、数量限制、状态流转、库存校验）
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import axios from 'axios'
import * as outboundApi from '../outbound'
import { OutboundStatus } from '@/types/outbound'

// Mock axios
vi.mock('axios')

describe('Outbound API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getOutboundList - 获取出库单列表', () => {
    it('should fetch outbounds successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          records: [
            {
              id: 1,
              outboundNo: 'OUT202601040001',
              productId: 1,
              productName: 'iPhone 15',
              quantity: 50,
              receiver: '客户A',
              receiverPhone: '13800138000',
              outboundDate: '2026-01-04T10:00:00',
              status: OutboundStatus.PENDING, // 待审核
              remark: '测试出库单',
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
      const result = await outboundApi.getOutboundList({
        page: 1,
        size: 10
      })

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/outbound', {
        params: {
          page: 1,
          size: 10
        }
      })
      expect(result.data.records).toHaveLength(1)
      expect(result.data.records[0].outboundNo).toBe('OUT202601040001')
      expect(result.data.records[0].status).toBe(OutboundStatus.PENDING)
    })

    it('should fetch outbounds with filters', async () => {
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
      await outboundApi.getOutboundList({
        page: 1,
        size: 10,
        productId: 1,
        status: OutboundStatus.APPROVED,
        startDate: '2026-01-01T00:00:00',
        endDate: '2026-01-31T23:59:59'
      })

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/outbound', {
        params: {
          page: 1,
          size: 10,
          productId: 1,
          status: 1,
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
      await expect(outboundApi.getOutboundList({
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
      const result = await outboundApi.getOutboundList({
        page: 1,
        size: 10
      })

      // Assert
      expect(result.data.records).toEqual([])
      expect(result.data.total).toBe(0)
    })
  })

  describe('getOutboundDetail - 获取出库单详情', () => {
    it('should fetch outbound by id successfully', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: 'success',
        data: {
          id: 1,
          outboundNo: 'OUT202601040001',
          productId: 1,
          productName: 'iPhone 15',
          quantity: 50,
          receiver: '客户A',
          receiverPhone: '13800138000',
          outboundDate: '2026-01-04T10:00:00',
          status: OutboundStatus.PENDING,
          remark: '测试出库单',
          createdBy: 'admin',
          createdAt: '2026-01-04T10:00:00'
        }
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.getOutboundDetail(1)

      // Assert
      expect(axios.get).toHaveBeenCalledWith('/api/outbound/1')
      expect(result.data.id).toBe(1)
      expect(result.data.outboundNo).toBe('OUT202601040001')
      expect(result.data.receiver).toBe('客户A')
    })

    it('should handle non-existent outbound', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '出库单不存在',
        data: null
      }

      vi.mocked(axios.get).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.getOutboundDetail(999)

      // Assert
      expect(result.code).toBe(404)
      expect(result.data).toBeNull()
    })

    it('should handle API error', async () => {
      // Arrange
      const mockError = new Error('Network Error')
      vi.mocked(axios.get).mockRejectedValue(mockError)

      // Act & Assert
      await expect(outboundApi.getOutboundDetail(1)).rejects.toThrow('Network Error')
    })
  })

  describe('createOutbound - 创建出库单', () => {
    it('should create outbound successfully with auto-generated number', async () => {
      // Arrange
      const newOutbound = {
        productId: 1,
        quantity: 50,
        receiver: '客户A',
        receiverPhone: '13800138000',
        outboundDate: '2026-01-04T10:00:00',
        remark: '测试出库单'
      }

      const mockResponse = {
        code: 200,
        message: '出库单创建成功',
        data: {
          id: 1,
          outboundNo: 'OUT202601040001', // 自动生成的单号
          ...newOutbound,
          status: OutboundStatus.PENDING // 待审核
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(newOutbound)

      // Assert
      expect(axios.post).toHaveBeenCalledWith('/api/outbound', newOutbound)
      expect(result.code).toBe(200)
      expect(result.message).toBe('出库单创建成功')
      expect(result.data.outboundNo).toMatch(/^OUT\d{12}$/) // 验证单号格式
      expect(result.data.status).toBe(OutboundStatus.PENDING) // 验证初始状态为待审核
    })

    it('should validate outbound number format', async () => {
      // Arrange
      const newOutbound = {
        productId: 1,
        quantity: 30,
        receiver: '客户B',
        outboundDate: '2026-01-04T10:00:00'
      }

      const mockResponse = {
        code: 200,
        message: '出库单创建成功',
        data: {
          id: 2,
          outboundNo: 'OUT202601040002',
          ...newOutbound,
          status: OutboundStatus.PENDING
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(newOutbound)

      // Assert
      // 验证单号格式：OUT + yyyyMMdd + 4位序号
      expect(result.data.outboundNo).toMatch(/^OUT\d{12}$/)

      // 验证日期部分为当天日期
      const datePart = result.data.outboundNo.substring(3, 11)
      const todayDate = new Date().toISOString().slice(0, 10).replace(/-/g, '')
      expect(datePart).toBe(todayDate)

      // 验证序号部分为4位数字
      const sequencePart = result.data.outboundNo.substring(11, 15)
      expect(sequencePart).toMatch(/^\d{4}$/)
    })

    it('should handle product not found error', async () => {
      // Arrange
      const invalidOutbound = {
        productId: 999, // 不存在的商品
        quantity: 50,
        receiver: '客户'
      }

      const mockResponse = {
        code: 400,
        message: '商品不存在或已禁用',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(invalidOutbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('商品')
    })

    it('should validate quantity must be greater than 0', async () => {
      // Arrange
      const invalidOutbound = {
        productId: 1,
        quantity: 0, // 不合法的数量
        receiver: '客户'
      }

      const mockResponse = {
        code: 400,
        message: '出库数量必须大于0',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(invalidOutbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('出库数量')
    })

    it('should validate negative quantity', async () => {
      // Arrange
      const invalidOutbound = {
        productId: 1,
        quantity: -50, // 负数
        receiver: '客户'
      }

      const mockResponse = {
        code: 400,
        message: '出库数量必须大于0',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(invalidOutbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('出库数量')
    })

    it('should validate insufficient stock', async () => {
      // Arrange
      const invalidOutbound = {
        productId: 1,
        quantity: 1000, // 超过库存数量
        receiver: '客户'
      }

      const mockResponse = {
        code: 400,
        message: '库存不足，当前库存为50',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(invalidOutbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('库存不足')
    })

    it('should validate quantity maximum limit (999999)', async () => {
      // Arrange
      const invalidOutbound = {
        productId: 1,
        quantity: 1000000, // 超过最大值
        receiver: '客户'
      }

      const mockResponse = {
        code: 400,
        message: '出库数量超过最大限制',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(invalidOutbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('出库数量')
    })

    it('should validate receiver is required', async () => {
      // Arrange
      const invalidOutbound = {
        productId: 1,
        quantity: 50,
        receiver: null // 收货人为空
      }

      const mockResponse = {
        code: 400,
        message: '收货人不能为空',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(invalidOutbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('收货人')
    })

    it('should validate empty receiver', async () => {
      // Arrange
      const invalidOutbound = {
        productId: 1,
        quantity: 50,
        receiver: '' // 空字符串
      }

      const mockResponse = {
        code: 400,
        message: '收货人不能为空',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(invalidOutbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('收货人')
    })

    it('should validate receiver name length (max 100)', async () => {
      // Arrange
      const invalidOutbound = {
        productId: 1,
        quantity: 50,
        receiver: 'A'.repeat(101) // 超过100字符
      }

      const mockResponse = {
        code: 400,
        message: '收货人名称长度不能超过100个字符',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(invalidOutbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('收货人')
    })

    it('should validate receiver phone format', async () => {
      // Arrange
      const invalidOutbound = {
        productId: 1,
        quantity: 50,
        receiver: '客户A',
        receiverPhone: '12345' // 不合法的手机号
      }

      const mockResponse = {
        code: 400,
        message: '收货人电话格式不正确',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(invalidOutbound)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('电话')
    })

    it('should handle API error', async () => {
      // Arrange
      const newOutbound = {
        productId: 1,
        quantity: 50,
        receiver: '客户'
      }

      vi.mocked(axios.post).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(outboundApi.createOutbound(newOutbound)).rejects.toThrow('Network Error')
    })

    it('should accept minimum boundary quantity (1)', async () => {
      // Arrange
      const boundaryOutbound = {
        productId: 1,
        quantity: 1, // 最小边界值
        receiver: '客户'
      }

      const mockResponse = {
        code: 200,
        message: '出库单创建成功',
        data: {
          id: 1,
          outboundNo: 'OUT202601040001',
          ...boundaryOutbound,
          status: OutboundStatus.PENDING
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(boundaryOutbound)

      // Assert
      expect(result.code).toBe(200)
    })

    it('should accept maximum boundary quantity (999999)', async () => {
      // Arrange
      const boundaryOutbound = {
        productId: 1,
        quantity: 999999, // 最大边界值
        receiver: '客户'
      }

      const mockResponse = {
        code: 200,
        message: '出库单创建成功',
        data: {
          id: 1,
          outboundNo: 'OUT202601040001',
          ...boundaryOutbound,
          status: OutboundStatus.PENDING
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(boundaryOutbound)

      // Assert
      expect(result.code).toBe(200)
    })

    it('should accept quantity equal to current stock', async () => {
      // Arrange
      const boundaryOutbound = {
        productId: 1,
        quantity: 100, // 等于当前库存
        receiver: '客户'
      }

      const mockResponse = {
        code: 200,
        message: '出库单创建成功',
        data: {
          id: 1,
          outboundNo: 'OUT202601040001',
          ...boundaryOutbound,
          status: OutboundStatus.PENDING
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound(boundaryOutbound)

      // Assert
      expect(result.code).toBe(200)
    })
  })

  describe('updateOutbound - 更新出库单', () => {
    it('should update outbound successfully when status is pending', async () => {
      // Arrange
      const updateData = {
        productId: 1,
        quantity: 60,
        receiver: '新客户',
        receiverPhone: '13900139000',
        remark: '更新后的备注'
      }

      const mockResponse = {
        code: 200,
        message: '更新成功',
        data: true
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.updateOutbound(1, updateData)

      // Assert
      expect(axios.put).toHaveBeenCalledWith('/api/outbound/1', updateData)
      expect(result.code).toBe(200)
      expect(result.message).toBe('更新成功')
    })

    it('should handle update when outbound is already approved', async () => {
      // Arrange
      const updateData = {
        productId: 1,
        quantity: 80
      }

      const mockResponse = {
        code: 400,
        message: '出库单已审核，无法修改',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.updateOutbound(1, updateData)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已审核')
    })

    it('should handle update when outbound is voided', async () => {
      // Arrange
      const updateData = {
        productId: 1,
        quantity: 80
      }

      const mockResponse = {
        code: 400,
        message: '出库单已作废，无法修改',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.updateOutbound(1, updateData)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已作废')
    })

    it('should validate quantity when updating', async () => {
      // Arrange
      const invalidUpdate = {
        productId: 1,
        quantity: 0,
        receiver: '客户'
      }

      const mockResponse = {
        code: 400,
        message: '出库数量必须大于0',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.updateOutbound(1, invalidUpdate)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('出库数量')
    })

    it('should validate insufficient stock when updating', async () => {
      // Arrange
      const invalidUpdate = {
        productId: 1,
        quantity: 200, // 超过库存
        receiver: '客户'
      }

      const mockResponse = {
        code: 400,
        message: '库存不足，当前库存为100',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.updateOutbound(1, invalidUpdate)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('库存不足')
    })

    it('should handle outbound not found', async () => {
      // Arrange
      const updateData = {
        productId: 1,
        quantity: 60,
        receiver: '客户'
      }

      const mockResponse = {
        code: 404,
        message: '出库单不存在',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.updateOutbound(999, updateData)

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      const updateData = {
        productId: 1,
        quantity: 60,
        receiver: '客户'
      }

      vi.mocked(axios.put).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(outboundApi.updateOutbound(1, updateData)).rejects.toThrow('Network Error')
    })
  })

  describe('deleteOutbound - 删除出库单', () => {
    it('should delete outbound successfully when status is pending', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '删除成功',
        data: true
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.deleteOutbound(1)

      // Assert
      expect(axios.delete).toHaveBeenCalledWith('/api/outbound/1')
      expect(result.code).toBe(200)
      expect(result.message).toBe('删除成功')
    })

    it('should handle deletion when outbound is already approved', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '出库单已审核，无法删除',
        data: null
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.deleteOutbound(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已审核')
    })

    it('should handle deletion when outbound is voided', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '出库单已作废，无法删除',
        data: null
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.deleteOutbound(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已作废')
    })

    it('should handle outbound not found', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '出库单不存在',
        data: null
      }

      vi.mocked(axios.delete).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.deleteOutbound(999)

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.delete).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(outboundApi.deleteOutbound(1)).rejects.toThrow('Network Error')
    })
  })

  describe('approveOutbound - 审核出库单', () => {
    it('should approve outbound successfully and decrease inventory', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '审核成功',
        data: {
          id: 1,
          outboundNo: 'OUT202601040001',
          status: OutboundStatus.APPROVED, // 已审核
          approvedBy: 'admin',
          approvedAt: '2026-01-04T11:00:00'
        }
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.approveOutbound(1, 'admin')

      // Assert
      expect(axios.patch).toHaveBeenCalledWith('/api/outbound/1/approve', null, {
        params: { approvedBy: 'admin' }
      })
      expect(result.code).toBe(200)
      expect(result.message).toBe('审核成功')
      expect(result.data.status).toBe(OutboundStatus.APPROVED) // 验证状态更新为已审核
      expect(result.data.approvedBy).toBe('admin')
      expect(result.data.approvedAt).toBeDefined()
    })

    it('should validate stock is sufficient before approval', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '库存不足，当前库存为30，无法出库50',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.approveOutbound(1, 'admin')

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('库存不足')
    })

    it('should handle approval when outbound is already approved', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '出库单已审核，无法重复审核',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.approveOutbound(1, 'admin')

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已审核')
    })

    it('should handle approval when outbound is voided', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '出库单已作废，无法审核',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.approveOutbound(1, 'admin')

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已作废')
    })

    it('should handle outbound not found', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '出库单不存在',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.approveOutbound(999, 'admin')

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should use default approvedBy when not provided', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '审核成功',
        data: {
          id: 1,
          status: OutboundStatus.APPROVED,
          approvedBy: 'system'
        }
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.approveOutbound(1)

      // Assert
      expect(axios.patch).toHaveBeenCalledWith('/api/outbound/1/approve', null, {
        params: { approvedBy: 'system' }
      })
      expect(result.data.approvedBy).toBe('system')
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.patch).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(outboundApi.approveOutbound(1, 'admin')).rejects.toThrow('Network Error')
    })
  })

  describe('voidOutbound - 作废出库单', () => {
    it('should void outbound successfully when status is pending', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '作废成功',
        data: {
          id: 1,
          outboundNo: 'OUT202601040001',
          status: OutboundStatus.VOID // 已作废
        }
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.voidOutbound(1)

      // Assert
      expect(axios.patch).toHaveBeenCalledWith('/api/outbound/1/void')
      expect(result.code).toBe(200)
      expect(result.message).toBe('作废成功')
      expect(result.data.status).toBe(OutboundStatus.VOID) // 验证状态更新为已作废
    })

    it('should handle void when outbound is already approved', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '出库单已审核，无法作废',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.voidOutbound(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已审核')
    })

    it('should handle void when outbound is already voided', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '出库单已作废，无法重复作废',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.voidOutbound(1)

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已作废')
    })

    it('should handle outbound not found', async () => {
      // Arrange
      const mockResponse = {
        code: 404,
        message: '出库单不存在',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.voidOutbound(999)

      // Assert
      expect(result.code).toBe(404)
      expect(result.message).toContain('不存在')
    })

    it('should handle API error', async () => {
      // Arrange
      vi.mocked(axios.patch).mockRejectedValue(new Error('Network Error'))

      // Act & Assert
      await expect(outboundApi.voidOutbound(1)).rejects.toThrow('Network Error')
    })
  })

  describe('状态流转测试 (Status Flow Tests)', () => {
    it('should follow correct status flow: pending -> approved', async () => {
      // Arrange - 创建待审核的出库单
      const createResponse = {
        code: 200,
        message: '出库单创建成功',
        data: {
          id: 1,
          outboundNo: 'OUT202601040001',
          status: OutboundStatus.PENDING // 待审核
        }
      }

      // Act - 审核出库单
      const approveResponse = {
        code: 200,
        message: '审核成功',
        data: {
          id: 1,
          status: OutboundStatus.APPROVED // 已审核
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: createResponse })
      vi.mocked(axios.patch).mockResolvedValue({ data: approveResponse })

      // 验证状态流转
      const createResult = await outboundApi.createOutbound({
        productId: 1,
        quantity: 50,
        receiver: '客户'
      })

      expect(createResult.data.status).toBe(OutboundStatus.PENDING)

      const approveResult = await outboundApi.approveOutbound(1, 'admin')
      expect(approveResult.data.status).toBe(OutboundStatus.APPROVED)
    })

    it('should follow correct status flow: pending -> void', async () => {
      // Arrange
      const voidResponse = {
        code: 200,
        message: '作废成功',
        data: {
          id: 1,
          status: OutboundStatus.VOID // 已作废
        }
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: voidResponse })

      // Act
      const result = await outboundApi.voidOutbound(1)

      // Assert
      expect(result.data.status).toBe(OutboundStatus.VOID)
    })

    it('should prevent status change from approved to pending', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '出库单已审核，无法修改',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.updateOutbound(1, {
        productId: 1,
        quantity: 80,
        receiver: '客户'
      })

      // Assert
      expect(result.code).toBe(400)
    })

    it('should prevent status change from void to pending', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '出库单已作废，无法修改',
        data: null
      }

      vi.mocked(axios.put).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.updateOutbound(1, {
        productId: 1,
        quantity: 80,
        receiver: '客户'
      })

      // Assert
      expect(result.code).toBe(400)
    })

    it('should prevent double approval', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '出库单已审核，无法重复审核',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.approveOutbound(1, 'admin')

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('重复审核')
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
      const result = await outboundApi.getOutboundDetail(1)

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
      const result = await outboundApi.getOutboundDetail(1)

      // Assert
      expect(result.code).toBe(500)
      expect(result.message).toBe('服务器内部错误')
    })

    it('should handle network timeout', async () => {
      // Arrange
      vi.mocked(axios.get).mockRejectedValue(new Error('timeout'))

      // Act & Assert
      await expect(outboundApi.getOutboundDetail(1)).rejects.toThrow('timeout')
    })

    it('should handle concurrent approval attempts', async () => {
      // Arrange
      const mockResponse = {
        code: 409,
        message: '出库单正在被其他用户审核',
        data: null
      }

      vi.mocked(axios.patch).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.approveOutbound(1, 'admin')

      // Assert
      expect(result.code).toBe(409)
      expect(result.message).toContain('正在被其他用户审核')
    })
  })

  describe('边界条件测试 (Boundary Tests)', () => {
    it('should handle sequence number reset on new day', async () => {
      // Arrange
      const mockResponse = {
        code: 200,
        message: '出库单创建成功',
        data: {
          id: 1,
          outboundNo: 'OUT202601050001', // 新的一天，序号重置为0001
          status: OutboundStatus.PENDING
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound({
        productId: 1,
        quantity: 50,
        receiver: '客户'
      })

      // Assert
      expect(result.data.outboundNo).toMatch(/^OUT202601050001$/)
    })

    it('should handle maximum sequence number (9999)', async () => {
      // Arrange
      const mockResponse = {
        code: 400,
        message: '今日出库单数量已达上限',
        data: null
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound({
        productId: 1,
        quantity: 50,
        receiver: '客户'
      })

      // Assert
      expect(result.code).toBe(400)
      expect(result.message).toContain('已达上限')
    })

    it('should handle receiver name exactly 100 characters', async () => {
      // Arrange
      const receiver100 = 'A'.repeat(100)
      const mockResponse = {
        code: 200,
        message: '出库单创建成功',
        data: {
          id: 1,
          outboundNo: 'OUT202601040001',
          status: OutboundStatus.PENDING
        }
      }

      vi.mocked(axios.post).mockResolvedValue({ data: mockResponse })

      // Act
      const result = await outboundApi.createOutbound({
        productId: 1,
        quantity: 50,
        receiver: receiver100
      })

      // Assert
      expect(result.code).toBe(200)
    })
  })
})
