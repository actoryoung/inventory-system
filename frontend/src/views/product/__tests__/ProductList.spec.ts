/**
 * 商品列表组件测试（重写，对齐当前 ProductList 组件）
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import ProductList from '../ProductList.vue'
import productApi from '@/api/product'
import categoryApi from '@/api/category'

vi.mock('@/api/product', () => ({
  default: {
    getList: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    toggleStatus: vi.fn(),
    checkSkuExists: vi.fn(),
    getLowStockProducts: vi.fn()
  }
}))

vi.mock('@/api/category', () => ({
  default: {
    getEnabledTree: vi.fn(),
    getTree: vi.fn(),
    getList: vi.fn()
  }
}))

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
    ElMessageBox: { confirm: vi.fn().mockResolvedValue('confirm') }
  }
})

const mockProductApi = vi.mocked(productApi)
const mockCategoryApi = vi.mocked(categoryApi)

const sampleProducts = [
  {
    id: 1,
    sku: 'SKU001',
    name: 'iPhone 15 Pro',
    categoryId: 1,
    categoryName: '电子产品',
    unit: '台',
    price: 7999,
    costPrice: 6000,
    warningStock: 10,
    status: 1
  }
]

function mountProductList() {
  return mount(ProductList, {
    global: {
      plugins: [ElementPlus],
      stubs: { teleport: true }
    }
  })
}

describe('ProductList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCategoryApi.getEnabledTree.mockResolvedValue({ code: 200, message: 'ok', data: [] })
    mockProductApi.getList.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { records: sampleProducts, total: 1, page: 1, size: 10 }
    })
  })

  it('挂载时调用 getList 并渲染商品行', async () => {
    const wrapper = mountProductList()
    await flushPromises()

    expect(mockProductApi.getList).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('iPhone 15 Pro')
    expect(wrapper.text()).toContain('SKU001')
  })

  it('挂载时加载分类树', async () => {
    mountProductList()
    await flushPromises()

    expect(mockCategoryApi.getEnabledTree).toHaveBeenCalled()
  })

  it('点击新增商品打开表单对话框（modelValue 置为 true）', async () => {
    const wrapper = mountProductList()
    await flushPromises()

    const addBtn = wrapper.findAll('button').find((b) => b.text().includes('新增商品'))
    await addBtn!.trigger('click')
    await flushPromises()

    const formComp = wrapper.findComponent({ name: 'ProductForm' })
    expect(formComp.exists()).toBe(true)
    expect(formComp.props('modelValue')).toBe(true)
  })

  it('搜索商品名称时重置分页并携带查询参数', async () => {
    const wrapper = mountProductList()
    await flushPromises()

    const nameInput = wrapper.find('input[placeholder="商品名称"]')
    await nameInput.setValue('Mac')
    await flushPromises()

    const calls = mockProductApi.getList.mock.calls
    const lastCall = calls[calls.length - 1][0] as any
    expect(lastCall.name).toBe('Mac')
    expect(lastCall.page).toBe(1)
  })

  it('点击删除时调用 delete API 并刷新列表', async () => {
    const wrapper = mountProductList()
    await flushPromises()

    mockProductApi.delete.mockResolvedValue({ code: 200, message: 'ok', data: true })

    const deleteBtn = wrapper.findAll('button').find((b) => b.text().includes('删除'))
    await deleteBtn!.trigger('click')
    await flushPromises()

    expect(mockProductApi.delete).toHaveBeenCalledWith(1)
    // 删除成功后重新加载列表
    expect(mockProductApi.getList.mock.calls.length).toBeGreaterThanOrEqual(2)
  })
})
