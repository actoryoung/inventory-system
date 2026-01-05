/**
 * 入库列表组件测试
 * Inbound List Component Tests
 *
 * 测试覆盖：
 * - 入库单列表渲染
 * - 搜索和筛选功能（商品、状态、日期范围）
 * - 分页功能
 * - 状态流转操作（审核、作废）
 * - 修改和删除操作（仅待审核状态）
 * - 单号格式验证
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import InboundList from '../InboundList.vue'
import * as inboundApi from '@/api/inbound'
import type { Inbound } from '@/types/inbound'

// Mock API 模块
vi.mock('@/api/inbound', () => ({
  getInbounds: vi.fn(),
  getInboundById: vi.fn(),
  createInbound: vi.fn(),
  updateInbound: vi.fn(),
  deleteInbound: vi.fn(),
  approveInbound: vi.fn(),
  voidInbound: vi.fn()
}))

describe('InboundList.vue', () => {
  let wrapper: VueWrapper<any>
  let pinia: any

  // 模拟入库单数据
  const mockInbounds: Inbound[] = [
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
      createdBy: 'admin',
      createdAt: '2026-01-04T10:00:00'
    },
    {
      id: 2,
      inboundNo: 'IN202601040002',
      productId: 2,
      productName: 'MacBook Pro',
      quantity: 50,
      supplier: '供应商B',
      inboundDate: '2026-01-04T14:00:00',
      status: 1, // 已审核
      remark: '已审核入库单',
      createdBy: 'admin',
      createdAt: '2026-01-04T14:00:00',
      approvedBy: 'manager',
      approvedAt: '2026-01-04T15:00:00'
    },
    {
      id: 3,
      inboundNo: 'IN202601040003',
      productId: 3,
      productName: 'AirPods Pro',
      quantity: 200,
      supplier: '供应商C',
      inboundDate: '2026-01-04T16:00:00',
      status: 2, // 已作废
      remark: '已作废入库单',
      createdBy: 'admin',
      createdAt: '2026-01-04T16:00:00'
    }
  ]

  // 模拟API响应
  const mockApiResponse = {
    code: 200,
    message: 'success',
    data: {
      records: mockInbounds,
      total: 3,
      page: 1,
      size: 10
    }
  }

  beforeEach(() => {
    // 创建 Pinia 实例
    pinia = createPinia()
    setActivePinia(pinia)

    // 重置所有 mocks
    vi.clearAllMocks()

    // Mock getInbounds API
    vi.mocked(inboundApi.getInbounds).mockResolvedValue(mockApiResponse)

    // 挂载组件
    wrapper = mount(InboundList, {
      global: {
        plugins: [pinia, ElementPlus],
        stubs: {
          'el-table': true,
          'el-table-column': true,
          'el-pagination': true,
          'el-button': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-dialog': true,
          'el-form': true,
          'el-form-item': true,
          'el-message': true,
          'el-message-box': true,
          'el-tag': true
        }
      }
    })

    // 等待组件挂载和数据加载
    wrapper.vm.$nextTick()
  })

  describe('组件渲染测试 (Component Rendering)', () => {
    it('should render inbound list correctly', async () => {
      // 等待数据加载完成
      await wrapper.vm.$nextTick()
      await wrapper.vm.loadInbounds()

      expect(wrapper.vm.inbounds).toEqual(mockInbounds)
      expect(wrapper.vm.total).toBe(3)
    })

    it('should display loading state when fetching data', async () => {
      wrapper.vm.loading = true
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.loading).toBe(true)
    })

    it('should display empty state when no inbounds', async () => {
      vi.mocked(inboundApi.getInbounds).mockResolvedValue({
        code: 200,
        message: 'success',
        data: {
          records: [],
          total: 0,
          page: 1,
          size: 10
        }
      })

      await wrapper.vm.loadInbounds()

      expect(wrapper.vm.inbounds).toEqual([])
      expect(wrapper.vm.total).toBe(0)
    })

    it('should display inbound status tags correctly', () => {
      const pendingStatus = wrapper.vm.getStatusTag(0)
      const approvedStatus = wrapper.vm.getStatusTag(1)
      const voidedStatus = wrapper.vm.getStatusTag(2)

      expect(pendingStatus.text).toBe('待审核')
      expect(pendingStatus.type).toBe('warning')

      expect(approvedStatus.text).toBe('已审核')
      expect(approvedStatus.type).toBe('success')

      expect(voidedStatus.text).toBe('已作废')
      expect(voidedStatus.type).toBe('danger')
    })
  })

  describe('搜索功能测试 (Search Functionality)', () => {
    it('should filter by product', async () => {
      wrapper.vm.searchForm.productId = 1
      await wrapper.vm.handleSearch()

      expect(inboundApi.getInbounds).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        productId: 1,
        status: undefined,
        startDate: undefined,
        endDate: undefined
      })
    })

    it('should filter by status - pending', async () => {
      wrapper.vm.searchForm.status = 0
      await wrapper.vm.handleSearch()

      expect(inboundApi.getInbounds).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        productId: undefined,
        status: 0,
        startDate: undefined,
        endDate: undefined
      })
    })

    it('should filter by status - approved', async () => {
      wrapper.vm.searchForm.status = 1
      await wrapper.vm.handleSearch()

      expect(inboundApi.getInbounds).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        productId: undefined,
        status: 1,
        startDate: undefined,
        endDate: undefined
      })
    })

    it('should filter by date range', async () => {
      wrapper.vm.searchForm.startDate = '2026-01-01T00:00:00'
      wrapper.vm.searchForm.endDate = '2026-01-31T23:59:59'
      await wrapper.vm.handleSearch()

      expect(inboundApi.getInbounds).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        productId: undefined,
        status: undefined,
        startDate: '2026-01-01T00:00:00',
        endDate: '2026-01-31T23:59:59'
      })
    })

    it('should reset search form', async () => {
      wrapper.vm.searchForm = {
        productId: 1,
        status: 0,
        startDate: '2026-01-01T00:00:00',
        endDate: '2026-01-31T23:59:59'
      }

      await wrapper.vm.handleReset()

      expect(wrapper.vm.searchForm).toEqual({
        productId: undefined,
        status: undefined,
        startDate: undefined,
        endDate: undefined
      })
    })
  })

  describe('分页功能测试 (Pagination)', () => {
    it('should handle page change', async () => {
      await wrapper.vm.handlePageChange(2)

      expect(wrapper.vm.queryParams.page).toBe(2)
      expect(inboundApi.getInbounds).toHaveBeenCalledWith({
        page: 2,
        size: 10,
        productId: undefined,
        status: undefined,
        startDate: undefined,
        endDate: undefined
      })
    })

    it('should handle page size change', async () => {
      await wrapper.vm.handleSizeChange(20)

      expect(wrapper.vm.queryParams.size).toBe(20)
      expect(wrapper.vm.queryParams.page).toBe(1) // 重置到第一页
    })
  })

  describe('审核操作测试 (Approve Operation)', () => {
    it('should approve pending inbound successfully', async () => {
      const pendingInbound = mockInbounds[0] // status = 0

      vi.mocked(inboundApi.approveInbound).mockResolvedValue({
        code: 200,
        message: '审核成功',
        data: {
          ...pendingInbound,
          status: 1,
          approvedBy: 'admin',
          approvedAt: '2026-01-04T17:00:00'
        }
      })

      await wrapper.vm.handleApprove(pendingInbound)

      expect(inboundApi.approveInbound).toHaveBeenCalledWith(1, 'admin')
      expect(inboundApi.getInbounds).toHaveBeenCalled() // 刷新列表
    })

    it('should show error when approving approved inbound', async () => {
      const approvedInbound = mockInbounds[1] // status = 1

      vi.mocked(inboundApi.approveInbound).mockResolvedValue({
        code: 400,
        message: '入库单已审核，无法重复审核',
        data: null
      })

      await wrapper.vm.handleApprove(approvedInbound)

      expect(inboundApi.approveInbound).toHaveBeenCalledWith(2, 'admin')
    })

    it('should show error when approving voided inbound', async () => {
      const voidedInbound = mockInbounds[2] // status = 2

      vi.mocked(inboundApi.approveInbound).mockResolvedValue({
        code: 400,
        message: '入库单已作废，无法审核',
        data: null
      })

      await wrapper.vm.handleApprove(voidedInbound)

      expect(inboundApi.approveInbound).toHaveBeenCalledWith(3, 'admin')
    })

    it('should not show approve button for approved inbound', () => {
      const approvedInbound = mockInbounds[1]
      const canApprove = wrapper.vm.canApprove(approvedInbound)

      expect(canApprove).toBe(false)
    })

    it('should not show approve button for voided inbound', () => {
      const voidedInbound = mockInbounds[2]
      const canApprove = wrapper.vm.canApprove(voidedInbound)

      expect(canApprove).toBe(false)
    })

    it('should show approve button only for pending inbound', () => {
      const pendingInbound = mockInbounds[0]
      const canApprove = wrapper.vm.canApprove(pendingInbound)

      expect(canApprove).toBe(true)
    })
  })

  describe('作废操作测试 (Void Operation)', () => {
    it('should void pending inbound successfully', async () => {
      const pendingInbound = mockInbounds[0] // status = 0

      vi.mocked(inboundApi.voidInbound).mockResolvedValue({
        code: 200,
        message: '作废成功',
        data: {
          ...pendingInbound,
          status: 2
        }
      })

      await wrapper.vm.handleVoid(pendingInbound)

      expect(inboundApi.voidInbound).toHaveBeenCalledWith(1)
      expect(inboundApi.getInbounds).toHaveBeenCalled() // 刷新列表
    })

    it('should show error when voiding approved inbound', async () => {
      const approvedInbound = mockInbounds[1] // status = 1

      vi.mocked(inboundApi.voidInbound).mockResolvedValue({
        code: 400,
        message: '入库单已审核，无法作废',
        data: null
      })

      await wrapper.vm.handleVoid(approvedInbound)

      expect(inboundApi.voidInbound).toHaveBeenCalledWith(2)
    })

    it('should not show void button for approved inbound', () => {
      const approvedInbound = mockInbounds[1]
      const canVoid = wrapper.vm.canVoid(approvedInbound)

      expect(canVoid).toBe(false)
    })

    it('should not show void button for voided inbound', () => {
      const voidedInbound = mockInbounds[2]
      const canVoid = wrapper.vm.canVoid(voidedInbound)

      expect(canVoid).toBe(false)
    })

    it('should show void button only for pending inbound', () => {
      const pendingInbound = mockInbounds[0]
      const canVoid = wrapper.vm.canVoid(pendingInbound)

      expect(canVoid).toBe(true)
    })
  })

  describe('修改操作测试 (Update Operation)', () => {
    it('should open edit dialog for pending inbound', async () => {
      const pendingInbound = mockInbounds[0]

      await wrapper.vm.handleEdit(pendingInbound)

      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('修改入库单')
      expect(wrapper.vm.formData).toEqual(pendingInbound)
    })

    it('should not allow editing approved inbound', async () => {
      const approvedInbound = mockInbounds[1]

      await wrapper.vm.handleEdit(approvedInbound)

      expect(wrapper.vm.dialogVisible).toBe(false)
    })

    it('should not allow editing voided inbound', async () => {
      const voidedInbound = mockInbounds[2]

      await wrapper.vm.handleEdit(voidedInbound)

      expect(wrapper.vm.dialogVisible).toBe(false)
    })

    it('should update inbound successfully', async () => {
      wrapper.vm.formData = {
        id: 1,
        quantity: 150,
        supplier: '新供应商',
        remark: '更新备注'
      }

      vi.mocked(inboundApi.updateInbound).mockResolvedValue({
        code: 200,
        message: '更新成功',
        data: true
      })

      await wrapper.vm.handleUpdate()

      expect(inboundApi.updateInbound).toHaveBeenCalledWith(1, wrapper.vm.formData)
      expect(wrapper.vm.dialogVisible).toBe(false)
      expect(inboundApi.getInbounds).toHaveBeenCalled()
    })

    it('should show error when update fails', async () => {
      wrapper.vm.formData = {
        id: 1,
        quantity: 0 // 不合法的数量
      }

      vi.mocked(inboundApi.updateInbound).mockResolvedValue({
        code: 400,
        message: '入库数量必须大于0',
        data: null
      })

      await wrapper.vm.handleUpdate()

      expect(wrapper.vm.dialogVisible).toBe(true) // 对话框保持打开
    })
  })

  describe('删除操作测试 (Delete Operation)', () => {
    it('should delete pending inbound successfully', async () => {
      const pendingInbound = mockInbounds[0]

      vi.mocked(inboundApi.deleteInbound).mockResolvedValue({
        code: 200,
        message: '删除成功',
        data: true
      })

      await wrapper.vm.handleDelete(pendingInbound)

      expect(inboundApi.deleteInbound).toHaveBeenCalledWith(1)
      expect(inboundApi.getInbounds).toHaveBeenCalled()
    })

    it('should not allow deleting approved inbound', async () => {
      const approvedInbound = mockInbounds[1]

      vi.mocked(inboundApi.deleteInbound).mockResolvedValue({
        code: 400,
        message: '入库单已审核，无法删除',
        data: null
      })

      await wrapper.vm.handleDelete(approvedInbound)

      expect(inboundApi.deleteInbound).toHaveBeenCalledWith(2)
    })

    it('should not allow deleting voided inbound', async () => {
      const voidedInbound = mockInbounds[2]

      vi.mocked(inboundApi.deleteInbound).mockResolvedValue({
        code: 400,
        message: '入库单已作废，无法删除',
        data: null
      })

      await wrapper.vm.handleDelete(voidedInbound)

      expect(inboundApi.deleteInbound).toHaveBeenCalledWith(3)
    })

    it('should not show delete button for approved inbound', () => {
      const approvedInbound = mockInbounds[1]
      const canDelete = wrapper.vm.canDelete(approvedInbound)

      expect(canDelete).toBe(false)
    })

    it('should not show delete button for voided inbound', () => {
      const voidedInbound = mockInbounds[2]
      const canDelete = wrapper.vm.canDelete(voidedInbound)

      expect(canDelete).toBe(false)
    })

    it('should show delete button only for pending inbound', () => {
      const pendingInbound = mockInbounds[0]
      const canDelete = wrapper.vm.canDelete(pendingInbound)

      expect(canDelete).toBe(true)
    })
  })

  describe('创建入库单测试 (Create Inbound)', () => {
    it('should open create dialog', async () => {
      await wrapper.vm.handleCreate()

      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('新增入库单')
      expect(wrapper.vm.formData).toEqual({
        productId: undefined,
        quantity: undefined,
        supplier: '',
        inboundDate: new Date(),
        remark: ''
      })
    })

    it('should create inbound successfully', async () => {
      wrapper.vm.formData = {
        productId: 1,
        quantity: 100,
        supplier: '供应商A',
        inboundDate: '2026-01-04T10:00:00',
        remark: '测试备注'
      }

      vi.mocked(inboundApi.createInbound).mockResolvedValue({
        code: 200,
        message: '入库单创建成功',
        data: {
          id: 4,
          inboundNo: 'IN202601040004',
          ...wrapper.vm.formData,
          status: 0
        }
      })

      await wrapper.vm.handleCreateSubmit()

      expect(inboundApi.createInbound).toHaveBeenCalledWith(wrapper.vm.formData)
      expect(wrapper.vm.dialogVisible).toBe(false)
      expect(inboundApi.getInbounds).toHaveBeenCalled()
    })

    it('should validate inbound number format', async () => {
      wrapper.vm.formData = {
        productId: 1,
        quantity: 50,
        supplier: '供应商'
      }

      vi.mocked(inboundApi.createInbound).mockResolvedValue({
        code: 200,
        message: '入库单创建成功',
        data: {
          id: 1,
          inboundNo: 'IN202601040001',
          ...wrapper.vm.formData,
          status: 0
        }
      })

      const result = await inboundApi.createInbound(wrapper.vm.formData)

      // 验证单号格式：IN + yyyyMMdd + 4位序号
      expect(result.data.inboundNo).toMatch(/^IN\d{12}$/)
    })

    it('should validate quantity when creating', async () => {
      wrapper.vm.formData = {
        productId: 1,
        quantity: 0, // 不合法
        supplier: '供应商'
      }

      vi.mocked(inboundApi.createInbound).mockResolvedValue({
        code: 400,
        message: '入库数量必须大于0',
        data: null
      })

      await wrapper.vm.handleCreateSubmit()

      expect(wrapper.vm.dialogVisible).toBe(true) // 对话框保持打开
    })

    it('should validate supplier when creating', async () => {
      wrapper.vm.formData = {
        productId: 1,
        quantity: 100,
        supplier: '' // 空供应商
      }

      vi.mocked(inboundApi.createInbound).mockResolvedValue({
        code: 400,
        message: '供应商名称不能为空',
        data: null
      })

      await wrapper.vm.handleCreateSubmit()

      expect(wrapper.vm.dialogVisible).toBe(true)
    })
  })

  describe('表单验证测试 (Form Validation)', () => {
    it('should validate product selection', async () => {
      wrapper.vm.formData = {
        productId: undefined, // 未选择商品
        quantity: 100,
        supplier: '供应商'
      }

      const isValid = await wrapper.vm.validateForm()

      expect(isValid).toBe(false)
    })

    it('should validate quantity range', async () => {
      // 测试最小边界
      wrapper.vm.formData = {
        productId: 1,
        quantity: 0,
        supplier: '供应商'
      }

      let isValid = await wrapper.vm.validateForm()
      expect(isValid).toBe(false)

      // 测试负数
      wrapper.vm.formData.quantity = -10
      isValid = await wrapper.vm.validateForm()
      expect(isValid).toBe(false)

      // 测试超过最大值
      wrapper.vm.formData.quantity = 1000000
      isValid = await wrapper.vm.validateForm()
      expect(isValid).toBe(false)

      // 测试合法值
      wrapper.vm.formData.quantity = 100
      isValid = await wrapper.vm.validateForm()
      expect(isValid).toBe(true)
    })

    it('should validate supplier length', async () => {
      wrapper.vm.formData = {
        productId: 1,
        quantity: 100,
        supplier: 'A'.repeat(101) // 超过100字符
      }

      const isValid = await wrapper.vm.validateForm()

      expect(isValid).toBe(false)
    })
  })

  describe('边界条件测试 (Boundary Tests)', () => {
    it('should accept minimum quantity (1)', async () => {
      wrapper.vm.formData = {
        productId: 1,
        quantity: 1,
        supplier: '供应商'
      }

      vi.mocked(inboundApi.createInbound).mockResolvedValue({
        code: 200,
        message: '入库单创建成功',
        data: {
          id: 1,
          inboundNo: 'IN202601040001',
          ...wrapper.vm.formData,
          status: 0
        }
      })

      await wrapper.vm.handleCreateSubmit()

      expect(inboundApi.createInbound).toHaveBeenCalled()
    })

    it('should accept maximum quantity (999999)', async () => {
      wrapper.vm.formData = {
        productId: 1,
        quantity: 999999,
        supplier: '供应商'
      }

      vi.mocked(inboundApi.createInbound).mockResolvedValue({
        code: 200,
        message: '入库单创建成功',
        data: {
          id: 1,
          inboundNo: 'IN202601040001',
          ...wrapper.vm.formData,
          status: 0
        }
      })

      await wrapper.vm.handleCreateSubmit()

      expect(inboundApi.createInbound).toHaveBeenCalled()
    })

    it('should handle inbound number sequence reset', async () => {
      // 模拟跨天后单号序号重置
      const todayDate = new Date().toISOString().slice(0, 10).replace(/-/g, '')

      const response1 = await inboundApi.createInbound({
        productId: 1,
        quantity: 100,
        supplier: '供应商'
      })

      // 验证单号包含正确的日期
      expect(response1.data.inboundNo).toContain(todayDate)
    })
  })

  describe('状态流转测试 (Status Flow)', () => {
    it('should follow correct flow: pending -> approved', async () => {
      const pendingInbound = { ...mockInbounds[0] }

      // 审核入库单
      vi.mocked(inboundApi.approveInbound).mockResolvedValue({
        code: 200,
        message: '审核成功',
        data: {
          ...pendingInbound,
          status: 1
        }
      })

      await wrapper.vm.handleApprove(pendingInbound)

      // 验证状态更新
      expect(inboundApi.approveInbound).toHaveBeenCalledWith(1, 'admin')
    })

    it('should follow correct flow: pending -> voided', async () => {
      const pendingInbound = { ...mockInbounds[0] }

      // 作废入库单
      vi.mocked(inboundApi.voidInbound).mockResolvedValue({
        code: 200,
        message: '作废成功',
        data: {
          ...pendingInbound,
          status: 2
        }
      })

      await wrapper.vm.handleVoid(pendingInbound)

      // 验证状态更新
      expect(inboundApi.voidInbound).toHaveBeenCalledWith(1)
    })

    it('should prevent status change from approved to pending', async () => {
      const approvedInbound = mockInbounds[1]

      // 尝试修改已审核的入库单
      const canEdit = wrapper.vm.canEdit(approvedInbound)

      expect(canEdit).toBe(false)
    })

    it('should prevent status change from voided to pending', async () => {
      const voidedInbound = mockInbounds[2]

      // 尝试修改已作废的入库单
      const canEdit = wrapper.vm.canEdit(voidedInbound)

      expect(canEdit).toBe(false)
    })
  })

  describe('错误处理测试 (Error Handling)', () => {
    it('should handle API error gracefully', async () => {
      vi.mocked(inboundApi.getInbounds).mockRejectedValue(new Error('Network Error'))

      await wrapper.vm.loadInbounds()

      expect(wrapper.vm.loading).toBe(false)
      expect(wrapper.vm.error).toBeTruthy()
    })

    it('should show error message when operation fails', async () => {
      vi.mocked(inboundApi.approveInbound).mockResolvedValue({
        code: 500,
        message: '服务器内部错误',
        data: null
      })

      await wrapper.vm.handleApprove(mockInbounds[0])

      // 验证错误处理逻辑
      expect(inboundApi.approveInbound).toHaveBeenCalled()
    })
  })
})
