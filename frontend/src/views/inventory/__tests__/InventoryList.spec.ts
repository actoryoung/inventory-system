/**
 * 库存列表组件测试
 * Inventory List Component Tests
 *
 * 测试覆盖：
 * - 组件渲染测试
 * - 库存列表展示
 * - 低库存标识
 * - 筛选功能（商品名称、分类、低库存）
 * - 库存调整对话框
 * - 分页功能
 * - 错误处理
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { ElTable, ElButton, ElInput, ElSelect, ElDialog, ElMessage } from 'element-plus'
import InventoryList from '../InventoryList.vue'
import * as inventoryApi from '@/api/inventory'

// Mock API
vi.mock('@/api/inventory')

// Mock router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('InventoryList.vue', () => {
  let wrapper: VueWrapper<any>

  const mockInventoryData = [
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
    },
    {
      id: 2,
      productId: 2,
      productName: 'MacBook Pro',
      productSku: 'SKU002',
      categoryId: 1,
      categoryName: '电子产品',
      warehouseId: 1,
      quantity: 5,
      warningStock: 10,
      isLowStock: true
    },
    {
      id: 3,
      productId: 3,
      productName: 'AirPods',
      productSku: 'SKU003',
      categoryId: 2,
      categoryName: '配件',
      warehouseId: 1,
      quantity: 50,
      warningStock: 10,
      isLowStock: false
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()

    // Mock API 响应
    vi.mocked(inventoryApi.getInventoryList).mockResolvedValue({
      code: 200,
      message: 'success',
      data: {
        records: mockInventoryData,
        total: 3,
        page: 1,
        size: 10
      }
    })

    vi.mocked(inventoryApi.getLowStockList).mockResolvedValue({
      code: 200,
      message: 'success',
      data: {
        records: [mockInventoryData[1]],
        total: 1,
        page: 1,
        size: 10
      }
    })

    vi.mocked(inventoryApi.adjustInventory).mockResolvedValue({
      code: 200,
      message: '库存调整成功',
      data: {
        oldQuantity: 100,
        newQuantity: 150
      }
    })
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('组件渲染测试 (Component Rendering)', () => {
    it('should render inventory list component', () => {
      // Arrange & Act
      wrapper = mount(InventoryList, {
        global: {
          components: {
            ElTable,
            ElButton,
            ElInput,
            ElSelect
          }
        }
      })

      // Assert
      expect(wrapper.find('.inventory-container').exists()).toBe(true)
      expect(wrapper.find('.page-title').text()).toContain('库存管理')
    })

    it('should render search bar with filters', () => {
      // Arrange & Act
      wrapper = mount(InventoryList, {
        global: {
          components: {
            ElInput,
            ElSelect,
            ElButton
          }
        }
      })

      // Assert
      expect(wrapper.find('.search-bar').exists()).toBe(true)
      expect(wrapper.find('input[placeholder*="商品名称"]').exists()).toBe(true)
      expect(wrapper.find('input[placeholder*="SKU"]').exists()).toBe(true)
    })

    it('should render inventory table', async () => {
      // Arrange & Act
      wrapper = mount(InventoryList, {
        global: {
          components: {
            ElTable
          }
        }
      })

      await wrapper.vm.$nextTick()

      // Assert
      expect(wrapper.find('.el-table').exists()).toBe(true)
    })
  })

  describe('库存列表展示测试 (Inventory List Display)', () => {
    it('should display inventory items correctly', async () => {
      // Arrange & Act
      wrapper = mount(InventoryList, {
        global: {
          components: {
            ElTable
          }
        }
      })

      await wrapper.vm.loadInventoryList()
      await wrapper.vm.$nextTick()

      // Assert
      expect(wrapper.vm.tableData).toEqual(mockInventoryData)
      expect(wrapper.vm.tableData).toHaveLength(3)
    })

    it('should display product SKU in table', async () => {
      // Arrange & Act
      wrapper = mount(InventoryList)
      await wrapper.vm.loadInventoryList()

      // Assert
      expect(wrapper.vm.tableData[0].productSku).toBe('SKU001')
      expect(wrapper.vm.tableData[1].productSku).toBe('SKU002')
    })

    it('should display quantity and warning stock', async () => {
      // Arrange & Act
      wrapper = mount(InventoryList)
      await wrapper.vm.loadInventoryList()

      // Assert
      expect(wrapper.vm.tableData[0].quantity).toBe(100)
      expect(wrapper.vm.tableData[0].warningStock).toBe(10)
    })

    it('should calculate low stock status correctly', async () => {
      // Arrange & Act
      wrapper = mount(InventoryList)
      await wrapper.vm.loadInventoryList()

      // Assert
      expect(wrapper.vm.tableData[0].isLowStock).toBe(false) // 100 > 10
      expect(wrapper.vm.tableData[1].isLowStock).toBe(true)  // 5 <= 10
    })
  })

  describe('低库存标识测试 (Low Stock Indicator)', () => {
    it('should highlight low stock items', async () => {
      // Arrange & Act
      wrapper = mount(InventoryList)
      await wrapper.vm.loadInventoryList()

      const lowStockItem = wrapper.vm.tableData.find(
        (item: any) => item.quantity <= item.warningStock
      )

      // Assert
      expect(lowStockItem).toBeDefined()
      expect(lowStockItem.isLowStock).toBe(true)
    })

    it('should apply low-stock class to low stock items', async () => {
      // Arrange & Act
      wrapper = mount(InventoryList)
      await wrapper.vm.loadInventoryList()

      const lowStockItem = wrapper.vm.tableData[1]

      // Assert
      expect(lowStockItem.quantity).toBeLessThanOrEqual(lowStockItem.warningStock)
    })

    it('should not highlight items with sufficient stock', async () => {
      // Arrange & Act
      wrapper = mount(InventoryList)
      await wrapper.vm.loadInventoryList()

      const normalStockItem = wrapper.vm.tableData[0]

      // Assert
      expect(normalStockItem.quantity).toBeGreaterThan(normalStockItem.warningStock)
      expect(normalStockItem.isLowStock).toBe(false)
    })
  })

  describe('筛选功能测试 (Filter Functionality)', () => {
    it('should filter by product name', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      wrapper.vm.searchForm.productName = 'iPhone'

      // Act
      await wrapper.vm.handleSearch()

      // Assert
      expect(inventoryApi.getInventoryList).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        productName: 'iPhone',
        categoryId: undefined,
        lowStockOnly: undefined
      })
    })

    it('should filter by SKU', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      wrapper.vm.searchForm.productSku = 'SKU001'

      // Act
      await wrapper.vm.handleSearch()

      // Assert
      expect(inventoryApi.getInventoryList).toHaveBeenCalledWith(
        expect.objectContaining({
          productName: undefined,
          productSku: 'SKU001'
        })
      )
    })

    it('should filter by category', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      wrapper.vm.searchForm.categoryId = 1

      // Act
      await wrapper.vm.handleSearch()

      // Assert
      expect(inventoryApi.getInventoryList).toHaveBeenCalledWith(
        expect.objectContaining({
          categoryId: 1
        })
      )
    })

    it('should filter low stock only', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      wrapper.vm.searchForm.lowStockOnly = true

      // Act
      await wrapper.vm.handleSearch()

      // Assert
      expect(inventoryApi.getInventoryList).toHaveBeenCalledWith(
        expect.objectContaining({
          lowStockOnly: true
        })
      )
    })

    it('should reset filters', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      wrapper.vm.searchForm.productName = 'iPhone'
      wrapper.vm.searchForm.categoryId = 1
      wrapper.vm.searchForm.lowStockOnly = true

      // Act
      await wrapper.vm.handleReset()

      // Assert
      expect(wrapper.vm.searchForm.productName).toBe('')
      expect(wrapper.vm.searchForm.productSku).toBe('')
      expect(wrapper.vm.searchForm.categoryId).toBeUndefined()
      expect(wrapper.vm.searchForm.lowStockOnly).toBe(false)
    })

    it('should apply multiple filters together', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      wrapper.vm.searchForm.productName = 'iPhone'
      wrapper.vm.searchForm.categoryId = 1
      wrapper.vm.searchForm.lowStockOnly = true

      // Act
      await wrapper.vm.handleSearch()

      // Assert
      expect(inventoryApi.getInventoryList).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        productName: 'iPhone',
        categoryId: 1,
        lowStockOnly: true
      })
    })
  })

  describe('库存调整对话框测试 (Adjust Stock Dialog)', () => {
    it('should open adjust dialog when button clicked', async () => {
      // Arrange
      wrapper = mount(InventoryList, {
        global: {
          components: {
            ElDialog
          }
        }
      })

      // Act
      await wrapper.vm.openAdjustDialog(mockInventoryData[0])

      // Assert
      expect(wrapper.vm.adjustDialogVisible).toBe(true)
      expect(wrapper.vm.currentInventory).toEqual(mockInventoryData[0])
    })

    it('should close adjust dialog', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      await wrapper.vm.openAdjustDialog(mockInventoryData[0])

      // Act
      await wrapper.vm.closeAdjustDialog()

      // Assert
      expect(wrapper.vm.adjustDialogVisible).toBe(false)
      expect(wrapper.vm.adjustForm).toEqual({
        type: 'add',
        quantity: null,
        reason: ''
      })
    })

    it('should add stock successfully', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      await wrapper.vm.openAdjustDialog(mockInventoryData[0])

      wrapper.vm.adjustForm = {
        type: 'add',
        quantity: 50,
        reason: '采购入库'
      }

      // Act
      await wrapper.vm.handleAdjustStock()

      // Assert
      expect(inventoryApi.adjustInventory).toHaveBeenCalledWith(1, {
        type: 'add',
        quantity: 50,
        reason: '采购入库'
      })
    })

    it('should reduce stock successfully', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      await wrapper.vm.openAdjustDialog(mockInventoryData[0])

      wrapper.vm.adjustForm = {
        type: 'reduce',
        quantity: 30,
        reason: '销售出库'
      }

      // Act
      await wrapper.vm.handleAdjustStock()

      // Assert
      expect(inventoryApi.adjustInventory).toHaveBeenCalledWith(1, {
        type: 'reduce',
        quantity: 30,
        reason: '销售出库'
      })
    })

    it('should set stock value successfully', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      await wrapper.vm.openAdjustDialog(mockInventoryData[0])

      wrapper.vm.adjustForm = {
        type: 'set',
        quantity: 200,
        reason: '盘点调整'
      }

      // Act
      await wrapper.vm.handleAdjustStock()

      // Assert
      expect(inventoryApi.adjustInventory).toHaveBeenCalledWith(1, {
        type: 'set',
        quantity: 200,
        reason: '盘点调整'
      })
    })

    it('should validate required fields', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      await wrapper.vm.openAdjustDialog(mockInventoryData[0])

      wrapper.vm.adjustForm = {
        type: 'add',
        quantity: null,
        reason: ''
      }

      // Act
      const result = await wrapper.vm.handleAdjustStock()

      // Assert
      expect(result).toBe(false)
      expect(inventoryApi.adjustInventory).not.toHaveBeenCalled()
    })

    it('should handle insufficient stock error', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      await wrapper.vm.openAdjustDialog(mockInventoryData[1]) // 库存为5

      wrapper.vm.adjustForm = {
        type: 'reduce',
        quantity: 10, // 大于当前库存
        reason: '测试'
      }

      vi.mocked(inventoryApi.adjustInventory).mockResolvedValue({
        code: 400,
        message: '库存不足，当前库存：5，需要：10',
        data: null
      })

      // Act
      await wrapper.vm.handleAdjustStock()

      // Assert
      expect(wrapper.vm.errorMessage).toContain('库存不足')
    })
  })

  describe('分页功能测试 (Pagination)', () => {
    it('should load inventory with pagination', async () => {
      // Arrange
      wrapper = mount(InventoryList)

      // Act
      await wrapper.vm.loadInventoryList()

      // Assert
      expect(wrapper.vm.pagination.page).toBe(1)
      expect(wrapper.vm.pagination.size).toBe(10)
      expect(inventoryApi.getInventoryList).toHaveBeenCalledWith({
        page: 1,
        size: 10
      })
    })

    it('should handle page change', async () => {
      // Arrange
      wrapper = mount(InventoryList)

      // Act
      await wrapper.vm.handlePageChange(2)

      // Assert
      expect(wrapper.vm.pagination.page).toBe(2)
      expect(inventoryApi.getInventoryList).toHaveBeenCalledWith({
        page: 2,
        size: 10
      })
    })

    it('should handle page size change', async () => {
      // Arrange
      wrapper = mount(InventoryList)

      // Act
      await wrapper.vm.handleSizeChange(20)

      // Assert
      expect(wrapper.vm.pagination.size).toBe(20)
      expect(wrapper.vm.pagination.page).toBe(1) // 重置到第一页
      expect(inventoryApi.getInventoryList).toHaveBeenCalledWith({
        page: 1,
        size: 20
      })
    })

    it('should display total count correctly', async () => {
      // Arrange & Act
      wrapper = mount(InventoryList)
      await wrapper.vm.loadInventoryList()

      // Assert
      expect(wrapper.vm.pagination.total).toBe(3)
    })
  })

  describe('低库存快速查询测试 (Quick Low Stock Query)', () => {
    it('should load low stock items only', async () => {
      // Arrange
      wrapper = mount(InventoryList)

      // Act
      await wrapper.vm.loadLowStockItems()

      // Assert
      expect(inventoryApi.getLowStockList).toHaveBeenCalledWith({
        page: 1,
        size: 10
      })
      expect(wrapper.vm.tableData[0].isLowStock).toBe(true)
    })

    it('should toggle low stock filter', async () => {
      // Arrange
      wrapper = mount(InventoryList)

      // Act
      await wrapper.vm.toggleLowStockFilter()

      // Assert
      expect(wrapper.vm.searchForm.lowStockOnly).toBe(true)
      expect(inventoryApi.getInventoryList).toHaveBeenCalledWith(
        expect.objectContaining({
          lowStockOnly: true
        })
      )
    })
  })

  describe('加载状态测试 (Loading State)', () => {
    it('should show loading indicator when fetching', async () => {
      // Arrange
      vi.mocked(inventoryApi.getInventoryList).mockImplementation(
        () => new Promise(resolve => {
          setTimeout(() => {
            resolve({
              code: 200,
              message: 'success',
              data: {
                records: mockInventoryData,
                total: 3,
                page: 1,
                size: 10
              }
            })
          }, 100)
        })
      )

      wrapper = mount(InventoryList)

      // Act
      const loadPromise = wrapper.vm.loadInventoryList()
      expect(wrapper.vm.loading).toBe(true)

      await loadPromise
      await wrapper.vm.$nextTick()

      // Assert
      expect(wrapper.vm.loading).toBe(false)
    })

    it('should hide loading indicator after fetch completes', async () => {
      // Arrange
      wrapper = mount(InventoryList)

      // Act
      await wrapper.vm.loadInventoryList()

      // Assert
      expect(wrapper.vm.loading).toBe(false)
    })
  })

  describe('错误处理测试 (Error Handling)', () => {
    it('should handle API error gracefully', async () => {
      // Arrange
      vi.mocked(inventoryApi.getInventoryList).mockRejectedValue(
        new Error('Network Error')
      )

      wrapper = mount(InventoryList)

      // Act
      await wrapper.vm.loadInventoryList()

      // Assert
      expect(wrapper.vm.error).toBeTruthy()
    })

    it('should display error message', async () => {
      // Arrange
      vi.mocked(inventoryApi.getInventoryList).mockRejectedValue(
        new Error('加载失败')
      )

      wrapper = mount(InventoryList)

      // Act
      await wrapper.vm.loadInventoryList()

      // Assert
      expect(wrapper.vm.errorMessage).toContain('加载')
    })
  })

  describe('响应式数据测试 (Reactive Data)', () => {
    it('should update table when search changes', async () => {
      // Arrange
      wrapper = mount(InventoryList)

      // Act
      wrapper.vm.searchForm.productName = 'iPhone'
      await wrapper.vm.handleSearch()
      await wrapper.vm.$nextTick()

      // Assert
      expect(inventoryApi.getInventoryList).toHaveBeenCalled()
    })

    it('should clear search form on reset', async () => {
      // Arrange
      wrapper = mount(InventoryList)
      wrapper.vm.searchForm.productName = 'iPhone'
      wrapper.vm.searchForm.categoryId = 1

      // Act
      await wrapper.vm.handleReset()

      // Assert
      expect(wrapper.vm.searchForm).toEqual({
        productName: '',
        productSku: '',
        categoryId: undefined,
        lowStockOnly: false
      })
    })
  })
})
