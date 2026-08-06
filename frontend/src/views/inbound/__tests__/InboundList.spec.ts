/**
 * 入库列表组件测试（重写，对齐当前 InboundList 组件）
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import InboundList from '../InboundList.vue'
import inboundApi from '@/api/inbound'
import productApi from '@/api/product'

vi.mock('@/api/inbound', () => ({
  default: {
    getInboundList: vi.fn(),
    getInboundDetail: vi.fn(),
    createInbound: vi.fn(),
    updateInbound: vi.fn(),
    deleteInbound: vi.fn(),
    approveInbound: vi.fn(),
    voidInbound: vi.fn()
  }
}))

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

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
    ElMessageBox: { confirm: vi.fn().mockResolvedValue('confirm') }
  }
})

const mockInboundApi = vi.mocked(inboundApi)
const mockProductApi = vi.mocked(productApi)

const sampleInbounds = [
  {
    id: 1,
    inboundNo: 'IN202608060001',
    productId: 1,
    productSku: 'SKU001',
    productName: 'iPhone 15 Pro',
    quantity: 10,
    supplier: '供应商A',
    inboundDate: '2026-08-06 10:00:00',
    status: 0
  }
]

function mountInboundList() {
  return mount(InboundList, {
    global: {
      plugins: [ElementPlus],
      stubs: { teleport: true }
    }
  })
}

describe('InboundList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockProductApi.getList.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { records: [], total: 0, page: 1, size: 10 }
    })
    mockInboundApi.getInboundList.mockResolvedValue({
      code: 200,
      message: 'ok',
      data: { records: sampleInbounds, total: 1, page: 1, size: 10 }
    })
  })

  it('挂载时调用 getInboundList 并渲染入库单', async () => {
    const wrapper = mountInboundList()
    await flushPromises()

    expect(mockInboundApi.getInboundList).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('IN202608060001')
    expect(wrapper.text()).toContain('iPhone 15 Pro')
  })

  it('挂载时加载商品下拉列表', async () => {
    mountInboundList()
    await flushPromises()

    expect(mockProductApi.getList).toHaveBeenCalled()
  })

  it('渲染待审核状态标签', async () => {
    const wrapper = mountInboundList()
    await flushPromises()

    expect(wrapper.text()).toContain('待审核')
  })

  it('点击搜索按钮触发列表刷新', async () => {
    const wrapper = mountInboundList()
    await flushPromises()

    const searchBtn = wrapper.findAll('button').find((b) => b.text().includes('搜索'))
    await searchBtn!.trigger('click')
    await flushPromises()

    expect(mockInboundApi.getInboundList.mock.calls.length).toBeGreaterThanOrEqual(2)
  })
})
