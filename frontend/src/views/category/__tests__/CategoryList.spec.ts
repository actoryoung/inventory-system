/**
 * 商品分类列表组件测试
 *
 * 测试范围:
 * - 分类列表渲染
 * - 树形展开/折叠
 * - 搜索功能
 * - 状态切换
 * - 新增/编辑/删除操作
 * - 分页功能
 *
 * @author Claude Code
 * @since 2026-01-04
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import CategoryList from '../CategoryList.vue'
import * as categoryApi from '@/api/category'

// Mock API 模块
vi.mock('@/api/category', () => ({
  getCategoryList: vi.fn(),
  getCategoryTree: vi.fn(),
  deleteCategory: vi.fn(),
  updateCategoryStatus: vi.fn(),
  exportCategories: vi.fn(),
  importCategories: vi.fn()
}))

// Mock Element Plus 组件
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn()
  }
}))

describe('CategoryList.vue', () => {
  let wrapper: VueWrapper<any>

  // Mock 测试数据
  const mockCategoryTree = [
    {
      id: 1,
      name: '电子产品',
      parentId: null,
      level: 1,
      sortOrder: 1,
      status: 1,
      children: [
        {
          id: 2,
          name: '手机',
          parentId: 1,
          level: 2,
          sortOrder: 1,
          status: 1,
          children: [
            {
              id: 3,
              name: '智能手机',
              parentId: 2,
              level: 3,
              sortOrder: 1,
              status: 1,
              children: []
            }
          ]
        },
        {
          id: 4,
          name: '电脑',
          parentId: 1,
          level: 2,
          sortOrder: 2,
          status: 1,
          children: []
        }
      ]
    },
    {
      id: 5,
      name: '食品饮料',
      parentId: null,
      level: 1,
      sortOrder: 2,
      status: 1,
      children: []
    }
  ]

  beforeEach(() => {
    // 重置所有 mock
    vi.clearAllMocks()

    // Mock API 响应
    vi.mocked(categoryApi.getCategoryTree).mockResolvedValue(mockCategoryTree)

    // 挂载组件
    wrapper = mount(CategoryList, {
      global: {
        stubs: {
          'el-table': true,
          'el-table-column': true,
          'el-button': true,
          'el-input': true,
          'el-dialog': true,
          'el-form': true,
          'el-form-item': true,
          'el-tree-select': true,
          'el-switch': true,
          'el-pagination': true,
          'el-tag': true
        }
      }
    })
  })

  describe('组件初始化', () => {
    it('应正确渲染组件', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('应在挂载时加载分类列表', async () => {
      await wrapper.vm.$nextTick()
      expect(categoryApi.getCategoryTree).toHaveBeenCalled()
    })

    it('应初始化搜索表单', () => {
      expect(wrapper.vm.searchForm).toEqual({
        name: '',
        status: null
      })
    })

    it('应初始化分页参数', () => {
      expect(wrapper.vm.pagination).toEqual({
        currentPage: 1,
        pageSize: 10,
        total: 0
      })
    })
  })

  describe('分类列表渲染', () => {
    it('应正确渲染分类树形数据', async () => {
      wrapper.vm.categoryTree = mockCategoryTree
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.categoryTree).toHaveLength(2)
      expect(wrapper.vm.categoryTree[0].name).toBe('电子产品')
      expect(wrapper.vm.categoryTree[0].children).toHaveLength(2)
    })

    it('应正确显示分类层级标识', async () => {
      wrapper.vm.categoryTree = mockCategoryTree
      await wrapper.vm.$nextTick()

      const level1Category = wrapper.vm.categoryTree[0]
      const level2Category = level1Category.children[0]
      const level3Category = level2Category.children[0]

      expect(wrapper.vm.getLevelTag(level1Category.level)).toBe('一级分类')
      expect(wrapper.vm.getLevelTag(level2Category.level)).toBe('二级分类')
      expect(wrapper.vm.getLevelTag(level3Category.level)).toBe('三级分类')
    })

    it('应正确显示分类状态', () => {
      const enabledCategory = { status: 1 }
      const disabledCategory = { status: 0 }

      expect(wrapper.vm.getStatusText(enabledCategory.status)).toBe('启用')
      expect(wrapper.vm.getStatusText(disabledCategory.status)).toBe('禁用')
    })

    it('应正确显示状态标签类型', () => {
      expect(wrapper.vm.getStatusType(1)).toBe('success')
      expect(wrapper.vm.getStatusType(0)).toBe('info')
    })
  })

  describe('树形展开/折叠', () => {
    it('应支持展开所有节点', async () => {
      wrapper.vm.categoryTree = mockCategoryTree
      await wrapper.vm.expandAll()

      // 验证所有节点都已展开
      const allExpanded = wrapper.vm.checkAllExpanded(wrapper.vm.categoryTree)
      expect(allExpanded).toBe(true)
    })

    it('应支持折叠所有节点', async () => {
      wrapper.vm.categoryTree = mockCategoryTree
      await wrapper.vm.collapseAll()

      // 验证所有节点都已折叠
      const allCollapsed = wrapper.vm.checkAllCollapsed(wrapper.vm.categoryTree)
      expect(allCollapsed).toBe(true)
    })

    it('应支持单个节点的展开/折叠', async () => {
      wrapper.vm.categoryTree = mockCategoryTree
      const firstCategory = wrapper.vm.categoryTree[0]

      // 展开第一个节点
      await wrapper.vm.toggleExpand(firstCategory)
      expect(firstCategory.expanded).toBe(true)

      // 折叠第一个节点
      await wrapper.vm.toggleExpand(firstCategory)
      expect(firstCategory.expanded).toBe(false)
    })
  })

  describe('搜索功能', () => {
    it('应支持按分类名称搜索', async () => {
      wrapper.vm.searchForm.name = '电子'
      await wrapper.vm.handleSearch()

      expect(categoryApi.getCategoryTree).toHaveBeenCalledWith({
        name: '电子',
        status: null
      })
    })

    it('应支持按状态过滤', async () => {
      wrapper.vm.searchForm.status = 1
      await wrapper.vm.handleSearch()

      expect(categoryApi.getCategoryTree).toHaveBeenCalledWith({
        name: '',
        status: 1
      })
    })

    it('应支持组合搜索条件', async () => {
      wrapper.vm.searchForm.name = '手机'
      wrapper.vm.searchForm.status = 1
      await wrapper.vm.handleSearch()

      expect(categoryApi.getCategoryTree).toHaveBeenCalledWith({
        name: '手机',
        status: 1
      })
    })

    it('应支持重置搜索条件', async () => {
      wrapper.vm.searchForm.name = '电子'
      wrapper.vm.searchForm.status = 1
      await wrapper.vm.handleReset()

      expect(wrapper.vm.searchForm.name).toBe('')
      expect(wrapper.vm.searchForm.status).toBe(null)
      expect(categoryApi.getCategoryTree).toHaveBeenCalledWith({
        name: '',
        status: null
      })
    })

    it('应在搜索时显示加载状态', async () => {
      vi.mocked(categoryApi.getCategoryTree).mockImplementation(() => new Promise(() => {}))
      wrapper.vm.searchForm.name = '电子'
      wrapper.vm.handleSearch()

      expect(wrapper.vm.loading).toBe(true)
    })

    it('应在搜索完成后隐藏加载状态', async () => {
      vi.mocked(categoryApi.getCategoryTree).mockResolvedValue(mockCategoryTree)
      wrapper.vm.searchForm.name = '电子'
      await wrapper.vm.handleSearch()

      expect(wrapper.vm.loading).toBe(false)
    })
  })

  describe('状态切换', () => {
    it('应成功切换分类状态', async () => {
      vi.mocked(categoryApi.updateCategoryStatus).mockResolvedValue({ success: true })

      await wrapper.vm.handleStatusChange(1, 0)

      expect(categoryApi.updateCategoryStatus).toHaveBeenCalledWith(1, 0)
      expect(ElMessage.success).toHaveBeenCalledWith('状态更新成功')
    })

    it('应在状态切换失败时显示错误信息', async () => {
      vi.mocked(categoryApi.updateCategoryStatus).mockRejectedValue(new Error('更新失败'))

      await wrapper.vm.handleStatusChange(1, 0)

      expect(ElMessage.error).toHaveBeenCalledWith('状态更新失败')
    })

    it('应禁用正在切换状态的分类操作', async () => {
      wrapper.vm.statusChangingIds = [1]

      expect(wrapper.vm.isStatusChanging(1)).toBe(true)
      expect(wrapper.vm.isStatusChanging(2)).toBe(false)
    })
  })

  describe('新增分类', () => {
    it('应打开新增对话框', async () => {
      await wrapper.vm.handleAdd()

      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('新增分类')
      expect(wrapper.vm.form).toEqual({
        id: null,
        name: '',
        parentId: null,
        level: 1,
        sortOrder: 0,
        status: 1
      })
    })

    it('应选择父分类后自动计算层级', async () => {
      wrapper.vm.form.parentId = 1
      await wrapper.vm.$nextTick()

      // 假设父分类是一级分类，子分类应为二级
      expect(wrapper.vm.form.level).toBe(2)
    })

    it('应成功创建分类', async () => {
      vi.mocked(categoryApi.createCategory).mockResolvedValue({ success: true })

      wrapper.vm.form = {
        id: null,
        name: '新分类',
        parentId: null,
        level: 1,
        sortOrder: 1,
        status: 1
      }

      await wrapper.vm.handleSubmit()

      expect(categoryApi.createCategory).toHaveBeenCalledWith(wrapper.vm.form)
      expect(ElMessage.success).toHaveBeenCalledWith('创建成功')
      expect(wrapper.vm.dialogVisible).toBe(false)
    })

    it('应在创建失败时显示错误信息', async () => {
      vi.mocked(categoryApi.createCategory).mockRejectedValue(new Error('创建失败'))

      wrapper.vm.form = {
        id: null,
        name: '新分类',
        parentId: null,
        level: 1,
        sortOrder: 1,
        status: 1
      }

      await wrapper.vm.handleSubmit()

      expect(ElMessage.error).toHaveBeenCalledWith('创建失败')
    })

    it('应验证表单必填字段', async () => {
      wrapper.vm.form.name = ''

      const isValid = await wrapper.vm.validateForm()

      expect(isValid).toBe(false)
    })
  })

  describe('编辑分类', () => {
    it('应打开编辑对话框并预填充数据', async () => {
      const category = {
        id: 1,
        name: '电子产品',
        parentId: null,
        level: 1,
        sortOrder: 1,
        status: 1
      }

      await wrapper.vm.handleEdit(category)

      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('编辑分类')
      expect(wrapper.vm.form).toEqual(category)
    })

    it('应成功更新分类', async () => {
      vi.mocked(categoryApi.updateCategory).mockResolvedValue({ success: true })

      wrapper.vm.form = {
        id: 1,
        name: '电子产品（更新）',
        parentId: null,
        level: 1,
        sortOrder: 1,
        status: 1
      }

      await wrapper.vm.handleSubmit()

      expect(categoryApi.updateCategory).toHaveBeenCalledWith(1, wrapper.vm.form)
      expect(ElMessage.success).toHaveBeenCalledWith('更新成功')
      expect(wrapper.vm.dialogVisible).toBe(false)
    })

    it('应在更新失败时显示错误信息', async () => {
      vi.mocked(categoryApi.updateCategory).mockRejectedValue(new Error('更新失败'))

      wrapper.vm.form = {
        id: 1,
        name: '电子产品',
        parentId: null,
        level: 1,
        sortOrder: 1,
        status: 1
      }

      await wrapper.vm.handleSubmit()

      expect(ElMessage.error).toHaveBeenCalledWith('更新失败')
    })
  })

  describe('删除分类', () => {
    it('应显示确认对话框', async () => {
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm')

      await wrapper.vm.handleDelete(1)

      expect(ElMessageBox.confirm).toHaveBeenCalled()
    })

    it('应在用户确认后删除分类', async () => {
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm')
      vi.mocked(categoryApi.deleteCategory).mockResolvedValue({ success: true })

      await wrapper.vm.handleDelete(1)

      expect(categoryApi.deleteCategory).toHaveBeenCalledWith(1)
      expect(ElMessage.success).toHaveBeenCalledWith('删除成功')
    })

    it('应在用户取消时不执行删除', async () => {
      vi.mocked(ElMessageBox.confirm).mockRejectedValue('cancel')

      await wrapper.vm.handleDelete(1)

      expect(categoryApi.deleteCategory).not.toHaveBeenCalled()
    })

    it('应在删除失败时显示错误信息', async () => {
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm')
      vi.mocked(categoryApi.deleteCategory).mockRejectedValue(new Error('删除失败'))

      await wrapper.vm.handleDelete(1)

      expect(ElMessage.error).toHaveBeenCalledWith('删除失败')
    })

    it('应在有关联商品时显示提示信息', async () => {
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm')
      vi.mocked(categoryApi.deleteCategory).mockRejectedValue(
        new Error('该分类下有商品，无法删除')
      )

      await wrapper.vm.handleDelete(1)

      expect(ElMessage.error).toHaveBeenCalledWith('该分类下有商品，无法删除')
    })
  })

  describe('批量操作', () => {
    it('应支持批量删除分类', async () => {
      vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm')
      vi.mocked(categoryApi.batchDeleteCategories).mockResolvedValue({ success: true })

      wrapper.vm.selectedIds = [1, 2, 3]
      await wrapper.vm.handleBatchDelete()

      expect(categoryApi.batchDeleteCategories).toHaveBeenCalledWith([1, 2, 3])
      expect(ElMessage.success).toHaveBeenCalledWith('批量删除成功')
    })

    it('应在未选择分类时禁用批量删除', () => {
      wrapper.vm.selectedIds = []

      expect(wrapper.vm.canBatchDelete).toBe(false)
    })

    it('应在选择分类后启用批量删除', () => {
      wrapper.vm.selectedIds = [1, 2]

      expect(wrapper.vm.canBatchDelete).toBe(true)
    })
  })

  describe('数据导出', () => {
    it('应成功导出分类数据', async () => {
      vi.mocked(categoryApi.exportCategories).mockResolvedValue({
        data: new Blob(['test data']),
        headers: {
          'content-disposition': 'attachment; filename=categories.xlsx'
        }
      })

      await wrapper.vm.handleExport()

      expect(categoryApi.exportCategories).toHaveBeenCalled()
      expect(ElMessage.success).toHaveBeenCalledWith('导出成功')
    })

    it('应在导出失败时显示错误信息', async () => {
      vi.mocked(categoryApi.exportCategories).mockRejectedValue(new Error('导出失败'))

      await wrapper.vm.handleExport()

      expect(ElMessage.error).toHaveBeenCalledWith('导出失败')
    })
  })

  describe('数据导入', () => {
    it('应打开导入对话框', async () => {
      await wrapper.vm.handleImport()

      expect(wrapper.vm.importDialogVisible).toBe(true)
    })

    it('应成功导入分类数据', async () => {
      const mockFile = new File(['test'], 'categories.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      vi.mocked(categoryApi.importCategories).mockResolvedValue({
        success: 10,
        failed: 0,
        errors: []
      })

      await wrapper.vm.handleImportSubmit(mockFile)

      expect(categoryApi.importCategories).toHaveBeenCalledWith(mockFile)
      expect(ElMessage.success).toHaveBeenCalledWith('导入成功：成功10条，失败0条')
      expect(wrapper.vm.importDialogVisible).toBe(false)
    })

    it('应在导入失败时显示错误详情', async () => {
      const mockFile = new File(['test'], 'categories.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      vi.mocked(categoryApi.importCategories).mockResolvedValue({
        success: 8,
        failed: 2,
        errors: ['第3行：分类名称重复', '第5行：层级超过限制']
      })

      await wrapper.vm.handleImportSubmit(mockFile)

      expect(ElMessage.warning).toHaveBeenCalledWith('导入完成：成功8条，失败2条')
    })

    it('应验证文件格式', () => {
      const validFile = new File(['test'], 'test.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      })

      const invalidFile = new File(['test'], 'test.txt', {
        type: 'text/plain'
      })

      expect(wrapper.vm.validateFileType(validFile)).toBe(true)
      expect(wrapper.vm.validateFileType(invalidFile)).toBe(false)
    })
  })

  describe('分页功能', () => {
    it('应正确计算分页信息', () => {
      wrapper.vm.flatCategories = Array.from({ length: 25 }, (_, i) => ({
        id: i + 1,
        name: `分类${i + 1}`
      }))

      wrapper.vm.pagination.pageSize = 10
      wrapper.vm.pagination.currentPage = 1

      const pagedCategories = wrapper.vm.getPagedCategories()

      expect(pagedCategories).toHaveLength(10)
    })

    it('应在切换页码时更新列表', async () => {
      wrapper.vm.flatCategories = Array.from({ length: 25 }, (_, i) => ({
        id: i + 1,
        name: `分类${i + 1}`
      }))

      await wrapper.vm.handlePageChange(2)

      expect(wrapper.vm.pagination.currentPage).toBe(2)
    })

    it('应在改变每页数量时更新列表', async () => {
      wrapper.vm.flatCategories = Array.from({ length: 25 }, (_, i) => ({
        id: i + 1,
        name: `分类${i + 1}`
      }))

      await wrapper.vm.handleSizeChange(20)

      expect(wrapper.vm.pagination.pageSize).toBe(20)
      expect(wrapper.vm.pagination.currentPage).toBe(1)
    })
  })

  describe('辅助方法', () => {
    it('应正确扁平化分类树', () => {
      wrapper.vm.categoryTree = mockCategoryTree
      const flatCategories = wrapper.vm.flattenCategories(mockCategoryTree)

      expect(flatCategories).toHaveLength(5) // 2个一级 + 2个二级 + 1个三级
    })

    it('应正确过滤分类树', () => {
      wrapper.vm.categoryTree = mockCategoryTree
      const filtered = wrapper.vm.filterCategories('电子')

      expect(filtered).toHaveLength(1)
      expect(filtered[0].name).toContain('电子')
    })

    it('应正确计算分类缩进', () => {
      expect(wrapper.vm.getIndent(1)).toBe(0)
      expect(wrapper.vm.getIndent(2)).toBe(20)
      expect(wrapper.vm.getIndent(3)).toBe(40)
    })
  })

  describe('错误处理', () => {
    it('应在API请求失败时显示错误信息', async () => {
      vi.mocked(categoryApi.getCategoryTree).mockRejectedValue(new Error('网络错误'))

      await wrapper.vm.loadCategories()

      expect(ElMessage.error).toHaveBeenCalledWith('加载分类列表失败')
      expect(wrapper.vm.loading).toBe(false)
    })

    it('应在网络错误时显示重试提示', async () => {
      vi.mocked(categoryApi.getCategoryTree).mockRejectedValue(new Error('Network Error'))

      await wrapper.vm.loadCategories()

      expect(wrapper.vm.error).toBeTruthy()
    })
  })

  describe('性能测试', () => {
    it('应在大量数据下正常渲染', async () => {
      const largeCategoryTree = Array.from({ length: 100 }, (_, i) => ({
        id: i + 1,
        name: `分类${i + 1}`,
        level: 1,
        children: []
      }))

      vi.mocked(categoryApi.getCategoryTree).mockResolvedValue(largeCategoryTree)

      const startTime = Date.now()
      await wrapper.vm.loadCategories()
      const endTime = Date.now()

      expect(endTime - startTime).toBeLessThan(1000) // 应在1秒内完成
    })
  })

  describe('可访问性', () => {
    it('应支持键盘导航', () => {
      // 测试Enter键确认删除
      const event = new KeyboardEvent('keydown', { key: 'Enter' })
      Object.defineProperty(event, 'target', { writable: false, value: wrapper.vm.$el })

      wrapper.vm.handleKeydown(event)
      // 验证键盘事件处理逻辑
    })

    it('应正确设置ARIA属性', () => {
      // 验证表格的ARIA属性
      const table = wrapper.findComponent({ name: 'el-table' })
      expect(table.attributes('aria-label')).toBeTruthy()
    })
  })
})
