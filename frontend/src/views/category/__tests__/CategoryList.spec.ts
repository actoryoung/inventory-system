/**
 * 商品分类列表组件测试（重写，对齐当前 CategoryList 组件）
 */
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import CategoryList from '../CategoryList.vue'
import categoryApi from '@/api/category'

vi.mock('@/api/category', () => ({
  default: {
    getTree: vi.fn(),
    getEnabledTree: vi.fn(),
    getList: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    toggleStatus: vi.fn(),
    canDelete: vi.fn(),
    checkNameDuplicate: vi.fn()
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

const mockCategoryApi = vi.mocked(categoryApi)

const sampleCategories = [
  {
    id: 1,
    name: '电子产品',
    parentId: null,
    level: 1,
    sortOrder: 0,
    status: 1,
    children: [
      { id: 2, name: '手机', parentId: 1, level: 2, sortOrder: 1, status: 1, children: [] }
    ]
  },
  {
    id: 3,
    name: '办公用品',
    parentId: null,
    level: 1,
    sortOrder: 2,
    status: 1,
    children: []
  }
]

function mountCategoryList() {
  return mount(CategoryList, {
    global: {
      plugins: [ElementPlus],
      stubs: { teleport: true }
    }
  })
}

describe('CategoryList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCategoryApi.getTree.mockResolvedValue({ code: 200, message: 'ok', data: sampleCategories })
  })

  it('挂载时调用 getTree 并渲染分类', async () => {
    const wrapper = mountCategoryList()
    await flushPromises()

    expect(mockCategoryApi.getTree).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('电子产品')
  })

  it('搜索名称时进行客户端过滤', async () => {
    const wrapper = mountCategoryList()
    await flushPromises()

    const searchInput = wrapper.find('input[placeholder="输入分类名称搜索"]')
    await searchInput.setValue('手机')
    await flushPromises()

    // 命中子分类时保留父链，同时过滤掉无关的分类
    expect(wrapper.text()).toContain('手机')
    expect(wrapper.text()).toContain('电子产品')
    expect(wrapper.text()).not.toContain('办公用品')
  })

  it('点击编辑打开分类表单对话框（modelValue 置为 true）', async () => {
    const wrapper = mountCategoryList()
    await flushPromises()

    const editBtn = wrapper.findAll('button').find((b) => b.text().includes('编辑'))
    await editBtn!.trigger('click')
    await flushPromises()

    const formComp = wrapper.findComponent({ name: 'CategoryForm' })
    expect(formComp.exists()).toBe(true)
    expect(formComp.props('modelValue')).toBe(true)
  })

  it('点击删除时先调用 canDelete 再调用 delete API', async () => {
    mockCategoryApi.canDelete.mockResolvedValue({ code: 200, message: 'ok', data: true })
    mockCategoryApi.delete.mockResolvedValue({ code: 200, message: 'ok', data: true })

    const wrapper = mountCategoryList()
    await flushPromises()

    const deleteBtn = wrapper.findAll('button').find((b) => b.text().includes('删除'))
    await deleteBtn!.trigger('click')
    await flushPromises()

    expect(mockCategoryApi.canDelete).toHaveBeenCalledWith(1)
    expect(mockCategoryApi.delete).toHaveBeenCalledWith(1)
  })
})
