<template>
  <div class="category-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">商品分类管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增分类</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchText"
        placeholder="输入分类名称搜索"
        :prefix-icon="Search"
        clearable
        style="width: 300px"
        @input="handleSearch"
      />
      <el-select
        v-model="filterLevel"
        placeholder="筛选层级"
        clearable
        style="width: 150px; margin-left: 10px"
        @change="handleFilter"
      >
        <el-option label="一级分类" :value="1" />
        <el-option label="二级分类" :value="2" />
        <el-option label="三级分类" :value="3" />
      </el-select>
      <el-select
        v-model="filterStatus"
        placeholder="筛选状态"
        clearable
        style="width: 150px; margin-left: 10px"
        @change="handleFilter"
      >
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
    </div>

    <!-- 分类表格 -->
    <el-table
      v-loading="loading"
      :data="filteredTableData"
      row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      border
      stripe
      style="width: 100%; margin-top: 20px"
      :expand-row-keys="expandedKeys"
      @expand-change="handleExpandChange"
    >
      <el-table-column prop="name" label="分类名称" min-width="200">
        <template #default="{ row }">
          <span v-if="row.level === 1" class="level-badge level-1">一级</span>
          <span v-else-if="row.level === 2" class="level-badge level-2">二级</span>
          <span v-else-if="row.level === 3" class="level-badge level-3">三级</span>
          <span style="margin-left: 8px">{{ row.name }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="sortOrder" label="排序" width="100" align="center" />

      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>

      <el-table-column prop="createdAt" label="创建时间" width="180" />

      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.level && row.level < 3"
            type="success"
            size="small"
            :icon="Plus"
            @click="handleAddChild(row)"
          >
            添加子分类
          </el-button>
          <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分类表单对话框 -->
    <CategoryForm
      v-model="formVisible"
      :form-data="formData"
      :category-tree="flatCategories"
      @submit="handleFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import categoryApi from '@/api/category'
import type { Category, CategoryFormData } from '@/types/category'
import CategoryForm from './CategoryForm.vue'

// 响应式数据
const loading = ref(false)
const tableData = ref<Category[]>([])
const allCategories = ref<Category[]>([])
const searchText = ref('')
const filterLevel = ref<number>()
const filterStatus = ref<number>()
const expandedKeys = ref<number[]>([])
const formVisible = ref(false)
const formData = ref<CategoryFormData>({
  name: '',
  parentId: null,
  sortOrder: 0,
  status: 1
})

// 扁平化的分类列表（用于表单中的父分类选择）
const flatCategories = computed(() => {
  const flatten = (categories: Category[]): Category[] => {
    const result: Category[] = []
    categories.forEach((cat) => {
      result.push(cat)
      if (cat.children && cat.children.length > 0) {
        result.push(...flatten(cat.children))
      }
    })
    return result
  }
  return flatten(allCategories.value)
})

// 当前显示的表格数据
const filteredTableData = computed(() => {
  let data = [...allCategories.value]

  // 名称搜索
  if (searchText.value) {
    const searchLower = searchText.value.toLowerCase()
    data = filterByName(data, searchLower)
  }

  // 层级筛选
  if (filterLevel.value !== undefined) {
    data = filterByLevel(data, filterLevel.value)
  }

  // 状态筛选
  if (filterStatus.value !== undefined) {
    data = filterByStatus(data, filterStatus.value)
  }

  return data
})

// 按名称过滤（保留子分类）
function filterByName(categories: Category[], name: string): Category[] {
  return categories.reduce((result: Category[], category) => {
    const match = category.name.toLowerCase().includes(name)
    const filteredChildren =
      category.children?.filter((child) => child.name.toLowerCase().includes(name)) || []

    if (match || filteredChildren.length > 0) {
      result.push({
        ...category,
        children: filteredChildren.length > 0 ? filteredChildren : category.children
      })
    }
    return result
  }, [])
}

// 按层级过滤
function filterByLevel(categories: Category[], level: number): Category[] {
  return categories.reduce((result: Category[], category) => {
    if (category.level === level) {
      result.push({ ...category, children: [] })
    } else if (category.children && category.children.length > 0) {
      const filteredChildren = filterByLevel(category.children, level)
      if (filteredChildren.length > 0) {
        result.push({ ...category, children: filteredChildren })
      }
    }
    return result
  }, [])
}

// 按状态过滤
function filterByStatus(categories: Category[], status: number): Category[] {
  return categories.reduce((result: Category[], category) => {
    if (category.status === status) {
      const filteredChildren = category.children
        ? filterByStatus(category.children, status)
        : []
      result.push({ ...category, children: filteredChildren })
    } else if (category.children && category.children.length > 0) {
      const filteredChildren = filterByStatus(category.children, status)
      if (filteredChildren.length > 0) {
        result.push({ ...category, children: filteredChildren })
      }
    }
    return result
  }, [])
}

// 加载分类树
async function loadCategories() {
  loading.value = true
  try {
    const res = await categoryApi.getTree()
    if (res.code === 200) {
      allCategories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
function handleSearch() {
  // 搜索由 computed 属性自动处理
}

// 处理筛选
function handleFilter() {
  // 筛选由 computed 属性自动处理
}

// 处理展开/折叠
function handleExpandChange(row: Category, expandedRows: Category[]) {
  expandedKeys.value = expandedRows.map((r) => r.id!)
}

// 处理新增
function handleAdd() {
  formData.value = {
    name: '',
    parentId: null,
    level: 1,
    sortOrder: 0,
    status: 1
  }
  formVisible.value = true
}

// 处理添加子分类
function handleAddChild(row: Category) {
  formData.value = {
    name: '',
    parentId: row.id,
    level: (row.level || 0) + 1,
    sortOrder: 0,
    status: 1
  }
  formVisible.value = true
}

// 处理编辑
function handleEdit(row: Category) {
  formData.value = {
    id: row.id,
    name: row.name,
    parentId: row.parentId,
    level: row.level,
    sortOrder: row.sortOrder || 0,
    status: row.status || 1
  }
  formVisible.value = true
}

// 处理删除
async function handleDelete(row: Category) {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.name}"吗？删除后不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 检查是否可以删除
    const canDeleteRes = await categoryApi.canDelete(row.id!)
    if (canDeleteRes.code === 200 && !canDeleteRes.data) {
      ElMessage.error('该分类下有商品或子分类，无法删除')
      return
    }

    loading.value = true
    const res = await categoryApi.delete(row.id!)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadCategories()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  } finally {
    loading.value = false
  }
}

// 处理状态切换
async function handleStatusChange(row: Category) {
  try {
    const res = await categoryApi.toggleStatus(row.id!, row.status!)
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      // 失败时恢复原状态
      row.status = row.status === 1 ? 0 : 1
    }
  } catch (error) {
    // 失败时恢复原状态
    row.status = row.status === 1 ? 0 : 1
    console.error('状态更新失败:', error)
  }
}

// 处理表单提交
async function handleFormSubmit(data: CategoryFormData) {
  loading.value = true
  try {
    let res
    if (data.id) {
      res = await categoryApi.update(data.id, data)
    } else {
      res = await categoryApi.create(data)
    }

    if (res.code === 200) {
      ElMessage.success(data.id ? '更新成功' : '创建成功')
      formVisible.value = false
      await loadCategories()
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.category-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.search-bar {
  display: flex;
  align-items: center;
}

.level-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
  color: #fff;

  &.level-1 {
    background: #409eff;
  }

  &.level-2 {
    background: #67c23a;
  }

  &.level-3 {
    background: #e6a23c;
  }
}
</style>
