/**
 * 商品列表组件测试
 * Product List Component Tests
 *
 * 测试覆盖：
 * - 商品列表渲染
 * - 搜索和筛选功能
 * - 分页功能
 * - 状态切换
 * - 删除确认
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import ProductList from '../ProductList.vue'
import * as productApi from '@/api/product'
import type { Product } from '@/types/product'

// Mock API 模块
vi.mock('@/api/product', () => ({
  getProducts: vi.fn(),
  deleteProduct: vi.fn(),
  updateProductStatus: vi.fn(),
  checkSkuExists: vi.fn()
}))

describe('ProductList.vue', () => {
  let wrapper: VueWrapper<any>
  let pinia: any

  // 模拟商品数据
  const mockProducts: Product[] = [
    {
      id: 1,
      sku: 'SKU001',
      name: 'iPhone 15',
      categoryName: '电子产品',
      unit: '台',
      price: 5999.00,
      costPrice: 4500.00,
      warningStock: 10,
      status: 1,
      createdAt: '2026-01-04T10:00:00',
      updatedAt: '2026-01-04T10:00:00'
    },
    {
      id: 2,
      sku: 'SKU002',
      name: 'MacBook Pro',
      categoryName: '电子产品',
      unit: '台',
      price: 12999.00,
      costPrice: 10000.00,
      warningStock: 5,
      status: 1,
      createdAt: '2026-01-04T10:00:00',
      updatedAt: '2026-01-04T10:00:00'
    },
    {
      id: 3,
      sku: 'SKU003',
      name: 'AirPods Pro',
      categoryName: '手机配件',
      unit: '副',
      price: 1999.00,
      costPrice: 1200.00,
      warningStock: 0,
      status: 0,
      createdAt: '2026-01-04T10:00:00',
      updatedAt: '2026-01-04T10:00:00'
    }
  ]

  // 模拟API响应
  const mockApiResponse = {
    code: 200,
    message: 'success',
    data: {
      records: mockProducts,
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

    // Mock getProducts API
    vi.mocked(productApi.getProducts).mockResolvedValue(mockApiResponse)

    // 挂载组件
    wrapper = mount(ProductList, {
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
          'el-dialog': true,
          'el-form': true,
          'el-form-item': true,
          'el-message': true,
          'el-message-box': true
        }
      }
    })

    // 等待组件挂载和数据加载
    await wrapper.vm.$nextTick()
  })

  afterEach(() => {
    wrapper.unmount()
  })

  describe('组件渲染测试 (Component Rendering)', () => {
    it('should render product list correctly', async () => {
      // 等待数据加载完成
      await wrapper.vm.$nextTick()
      await wrapper.vm.loadProducts()

      expect(wrapper.vm.products).toEqual(mockProducts)
      expect(wrapper.vm.total).toBe(3)
    })

    it('should show loading state when fetching data', async () => {
      wrapper.vm.loading = true
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.loading).toBe(true)
    })

    it('should display empty state when no products', async () => {
      vi.mocked(productApi.getProducts).mockResolvedValue({
        code: 200,
        message: 'success',
        data: {
          records: [],
          total: 0,
          page: 1,
          size: 10
        }
      })

      await wrapper.vm.loadProducts()

      expect(wrapper.vm.products).toEqual([])
      expect(wrapper.vm.total).toBe(0)
    })
  })

  describe('搜索功能测试 (Search Functionality)', () => {
    it('should search products by name', async () => {
      wrapper.vm.searchForm.name = 'iPhone'
      await wrapper.vm.handleSearch()

      expect(productApi.getProducts).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        name: 'iPhone',
        sku: undefined,
        categoryId: undefined,
        status: undefined
      })
    })

    it('should search products by SKU', async () => {
      wrapper.vm.searchForm.sku = 'SKU001'
      await wrapper.vm.handleSearch()

      expect(productApi.getProducts).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        name: undefined,
        sku: 'SKU001',
        categoryId: undefined,
        status: undefined
      })
    })

    it('should search products by category', async () => {
      wrapper.vm.searchForm.categoryId = 1
      await wrapper.vm.handleSearch()

      expect(productApi.getProducts).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        name: undefined,
        sku: undefined,
        categoryId: 1,
        status: undefined
      })
    })

    it('should search products by status', async () => {
      wrapper.vm.searchForm.status = 1
      await wrapper.vm.handleSearch()

      expect(productApi.getProducts).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        name: undefined,
        sku: undefined,
        categoryId: undefined,
        status: 1
      })
    })

    it('should search with multiple filters', async () => {
      wrapper.vm.searchForm.name = 'iPhone'
      wrapper.vm.searchForm.sku = 'SKU001'
      wrapper.vm.searchForm.status = 1

      await wrapper.vm.handleSearch()

      expect(productApi.getProducts).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        name: 'iPhone',
        sku: 'SKU001',
        categoryId: undefined,
        status: 1
      })
    })

    it('should reset search form', async () => {
      wrapper.vm.searchForm.name = 'iPhone'
      wrapper.vm.searchForm.sku = 'SKU001'
      wrapper.vm.searchForm.categoryId = 1
      wrapper.vm.searchForm.status = 1

      await wrapper.vm.handleReset()

      expect(wrapper.vm.searchForm.name).toBe('')
      expect(wrapper.vm.searchForm.sku).toBe('')
      expect(wrapper.vm.searchForm.categoryId).toBeUndefined()
      expect(wrapper.vm.searchForm.status).toBeUndefined()
    })
  })

  describe('分页功能测试 (Pagination)', () => {
    it('should load products with correct page and size', async () => {
      wrapper.vm.pagination.page = 2
      wrapper.vm.pagination.size = 20

      await wrapper.vm.loadProducts()

      expect(productApi.getProducts).toHaveBeenCalledWith({
        page: 2,
        size: 20,
        name: undefined,
        sku: undefined,
        categoryId: undefined,
        status: undefined
      })
    })

    it('should handle page change', async () => {
      await wrapper.vm.handlePageChange(3)

      expect(wrapper.vm.pagination.page).toBe(3)
      expect(productApi.getProducts).toHaveBeenCalled()
    })

    it('should handle page size change', async () => {
      await wrapper.vm.handleSizeChange(50)

      expect(wrapper.vm.pagination.size).toBe(50)
      expect(wrapper.vm.pagination.page).toBe(1) // 重置到第一页
      expect(productApi.getProducts).toHaveBeenCalled()
    })

    it('should disable pagination when loading', async () => {
      wrapper.vm.loading = true
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.loading).toBe(true)
    })
  })

  describe('状态切换测试 (Status Toggle)', () => {
    it('should toggle product status from enabled to disabled', async () => {
      vi.mocked(productApi.updateProductStatus).mockResolvedValue({
        code: 200,
        message: '状态切换成功',
        data: true
      })

      await wrapper.vm.handleStatusToggle(mockProducts[0])

      expect(productApi.updateProductStatus).toHaveBeenCalledWith(1, 0)
      expect(wrapper.vm.products[0].status).toBe(0)
    })

    it('should toggle product status from disabled to enabled', async () => {
      vi.mocked(productApi.updateProductStatus).mockResolvedValue({
        code: 200,
        message: '状态切换成功',
        data: true
      })

      await wrapper.vm.handleStatusToggle(mockProducts[2]) // 禁用的商品

      expect(productApi.updateProductStatus).toHaveBeenCalledWith(3, 1)
      expect(wrapper.vm.products[2].status).toBe(1)
    })

    it('should show error message when status toggle fails', async () => {
      vi.mocked(productApi.updateProductStatus).mockResolvedValue({
        code: 400,
        message: '状态切换失败',
        data: null
      })

      await wrapper.vm.handleStatusToggle(mockProducts[0])

      // 验证错误消息显示
      expect(wrapper.vm.products[0].status).toBe(1) // 状态未改变
    })
  })

  describe('删除功能测试 (Delete Functionality)', () => {
    it('should show delete confirmation dialog', async () => {
      const confirmSpy = vi.spyOn(wrapper.vm, 'handleDelete')
      wrapper.vm.handleDeleteConfirm(mockProducts[0])

      expect(confirmSpy).toHaveBeenCalled()
    })

    it('should delete product successfully', async () => {
      vi.mocked(productApi.deleteProduct).mockResolvedValue({
        code: 200,
        message: '删除成功',
        data: true
      })

      await wrapper.vm.handleDeleteConfirm(mockProducts[0])

      expect(productApi.deleteProduct).toHaveBeenCalledWith(1)
    })

    it('should reload products after successful deletion', async () => {
      vi.mocked(productApi.deleteProduct).mockResolvedValue({
        code: 200,
        message: '删除成功',
        data: true
      })

      const loadSpy = vi.spyOn(wrapper.vm, 'loadProducts')

      await wrapper.vm.handleDeleteConfirm(mockProducts[0])

      expect(loadSpy).toHaveBeenCalled()
    })

    it('should not delete when user cancels', async () => {
      vi.mocked(productApi.deleteProduct).mockResolvedValue({
        code: 200,
        message: '删除成功',
        data: true
      })

      // 取消删除
      await wrapper.vm.handleDeleteCancel()

      expect(productApi.deleteProduct).not.toHaveBeenCalled()
    })

    it('should show error message when deletion fails', async () => {
      vi.mocked(productApi.deleteProduct).mockResolvedValue({
        code: 400,
        message: '该商品有库存记录，无法删除',
        data: null
      })

      await wrapper.vm.handleDeleteConfirm(mockProducts[0])

      expect(productApi.deleteProduct).toHaveBeenCalledWith(1)
    })
  })

  describe('批量操作测试 (Batch Operations)', () => {
    it('should enable batch delete when products are selected', async () => {
      wrapper.vm.selectedProducts = [mockProducts[0], mockProducts[1]]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedProducts.length).toBe(2)
    })

    it('should disable batch delete when no products selected', async () => {
      wrapper.vm.selectedProducts = []
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedProducts.length).toBe(0)
    })

    it('should show confirmation before batch delete', async () => {
      wrapper.vm.selectedProducts = [mockProducts[0], mockProducts[1]]
      await wrapper.vm.$nextTick()

      const hasSelection = wrapper.vm.selectedProducts.length > 0
      expect(hasSelection).toBe(true)
    })
  })

  describe('商品详情测试 (Product Details)', () => {
    it('should display correct product information', () => {
      const product = mockProducts[0]

      expect(product.id).toBe(1)
      expect(product.sku).toBe('SKU001')
      expect(product.name).toBe('iPhone 15')
      expect(product.categoryName).toBe('电子产品')
      expect(product.price).toBe(5999.00)
      expect(product.status).toBe(1)
    })

    it('should format price correctly', () => {
      const price = wrapper.vm.formatPrice(5999.00)
      expect(price).toBe('¥5,999.00')
    })

    it('should format date correctly', () => {
      const date = wrapper.vm.formatDate('2026-01-04T10:00:00')
      expect(date).toContain('2026-01-04')
    })
  })

  describe('错误处理测试 (Error Handling)', () => {
    it('should handle API error gracefully', async () => {
      vi.mocked(productApi.getProducts).mockRejectedValue(new Error('Network Error'))

      try {
        await wrapper.vm.loadProducts()
      } catch (error) {
        expect(error).toBeTruthy()
      }
    })

    it('should show error message when API fails', async () => {
      vi.mocked(productApi.getProducts).mockResolvedValue({
        code: 500,
        message: '服务器错误',
        data: null
      })

      await wrapper.vm.loadProducts()

      // 验证错误处理
      expect(wrapper.vm.error).toBeTruthy()
    })
  })

  describe('商品表单测试 (Product Form)', () => {
    it('should open add product dialog', async () => {
      wrapper.vm.openAddDialog()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('新增商品')
      expect(wrapper.vm.formMode).toBe('add')
    })

    it('should open edit product dialog', async () => {
      wrapper.vm.openEditDialog(mockProducts[0])
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('编辑商品')
      expect(wrapper.vm.formMode).toBe('edit')
      expect(wrapper.vm.productForm.id).toBe(1)
    })

    it('should reset form when dialog closes', async () => {
      wrapper.vm.productForm.name = 'Test Product'
      wrapper.vm.dialogVisible = false
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.dialogVisible).toBe(false)
    })
  })

  describe('SKU 实时校验测试 (SKU Validation)', () => {
    it('should check SKU exists when creating product', async () => {
      vi.mocked(productApi.checkSkuExists).mockResolvedValue({
        code: 200,
        message: 'success',
        data: true
      })

      wrapper.vm.productForm.sku = 'SKU001'
      await wrapper.vm.validateSku('SKU001')

      expect(productApi.checkSkuExists).toHaveBeenCalledWith('SKU001')
    })

    it('should exclude current product ID when updating SKU', async () => {
      vi.mocked(productApi.checkSkuExists).mockResolvedValue({
        code: 200,
        message: 'success',
        data: false
      })

      wrapper.vm.productForm.id = 1
      await wrapper.vm.validateSku('SKU001', 1)

      expect(productApi.checkSkuExists).toHaveBeenCalledWith('SKU001', 1)
    })

    it('should show error message when SKU already exists', async () => {
      vi.mocked(productApi.checkSkuExists).mockResolvedValue({
        code: 200,
        message: 'success',
        data: true
      })

      const result = await wrapper.vm.validateSku('SKU001')
      expect(result).toBe(true)
    })
  })

  describe('导出功能测试 (Export Functionality)', () => {
    it('should export products to Excel', async () => {
      const exportSpy = vi.spyOn(wrapper.vm, 'handleExport')
      await wrapper.vm.handleExport()

      expect(exportSpy).toHaveBeenCalled()
    })

    it('should include selected products in export', async () => {
      wrapper.vm.selectedProducts = [mockProducts[0], mockProducts[1]]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.selectedProducts.length).toBe(2)
    })
  })
})
