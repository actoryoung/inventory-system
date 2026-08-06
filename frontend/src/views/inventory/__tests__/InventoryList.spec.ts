/**
 * 库存列表组件测试（重写，对齐当前 InventoryList 组件）
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import InventoryList from '../InventoryList.vue'
import inventoryApi from '@/api/inventory'
import categoryApi from '@/api/category'

vi.mock('@/api/inventory', () => ({
  default: {
    getInventoryList: vi.fn(),
    getInventoryByProduct: vi.fn(),
    adjustInventory: vi.fn(),
    getLowStockList: vi.fn(),
    checkStock: vi.fn(),
    getInventorySummary: vi.fn(),
    batchAdjust: vi.fn()
  }
}))

vi.mock('@/api/category', () => ({
  default: {
    getEnabledTree: vi.fn(),
    getTree: vi.fn()
  }
}))

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() }
  }
})

const mockInventoryApi = vi.mocked(inventoryApi)
const mockCategoryApi = vi.mocked(categoryApi)

const sampleSummary = {
  totalProducts: 10,
  totalQuantity: 150,
  lowStockCount: 3,
  totalAmount: 5000
}

const sampleInventories = [
  {
    id: 1,
    productId: 1,
    productSku: 'SKU001',
    productName: 'iPhone 15 Pro',
    categoryName: '电子产品',
    quantity: 5,
    warningStock: 10,
    isLowStock: true,
    amount: 500
  }
]

function mountInventoryList() {
  return mount(InventoryList, {
    global: {
      plugins: [ElementPlus],
      stubs: { teleport: true }
    }
  })
}

describe('InventoryList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCategoryApi.getEnabledTree.mockResolvedValue({ code: 200, message: 'ok', data: [] })
    mockInventoryApi.getInventorySummary.mockResolvedValue({ code: 200, message: 'ok', data: sampleSummary })
    mockInventoryApi.getInventoryList.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { records: sampleInventories, total: 1, page: 1, size: 10 }
    })
  })

  it('挂载时调用 getInventoryList 并渲染库存行', async () => {
    const wrapper = mountInventoryList()
    await flushPromises()

    expect(mockInventoryApi.getInventoryList).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('iPhone 15 Pro')
    expect(wrapper.text()).toContain('SKU001')
  })

  it('挂载时调用 getInventorySummary 并渲染汇总卡片', async () => {
    const wrapper = mountInventoryList()
    await flushPromises()

    expect(mockInventoryApi.getInventorySummary).toHaveBeenCalled()
    expect(wrapper.text()).toContain('总商品数')
    expect(wrapper.text()).toContain('低库存')
  })

  it('搜索商品名称时重置分页并携带查询参数', async () => {
    const wrapper = mountInventoryList()
    await flushPromises()

    const nameInput = wrapper.find('input[placeholder="商品名称"]')
    await nameInput.setValue('Mac')
    await flushPromises()

    const calls = mockInventoryApi.getInventoryList.mock.calls
    const lastCall = calls[calls.length - 1][0] as any
    expect(lastCall.productName).toBe('Mac')
    expect(lastCall.page).toBe(1)
  })

  it('勾选只看低库存后携带 lowStock 查询参数', async () => {
    const wrapper = mountInventoryList()
    await flushPromises()

    await wrapper.find('input[type="checkbox"]').setValue(true)
    await flushPromises()

    const calls = mockInventoryApi.getInventoryList.mock.calls
    const lastCall = calls[calls.length - 1][0] as any
    expect(lastCall.lowStock).toBe(true)
  })
})
