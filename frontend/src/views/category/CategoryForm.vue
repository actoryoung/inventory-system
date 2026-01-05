<template>
  <el-dialog
    :model-value="modelValue"
    :title="formData?.id ? '编辑分类' : '新增分类'"
    width="600px"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent="handleSubmit"
    >
      <el-form-item label="分类名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入分类名称"
          maxlength="50"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-form-item label="父分类" prop="parentId">
        <el-cascader
          v-model="parentIds"
          :options="parentCategoryOptions"
          :props="cascaderProps"
          placeholder="请选择父分类（不选则为一级分类）"
          clearable
          filterable
          style="width: 100%"
          @change="handleParentChange"
        />
      </el-form-item>

      <el-form-item label="排序号" prop="sortOrder">
        <el-input-number
          v-model="form.sortOrder"
          :min="0"
          :max="9999"
          controls-position="right"
          style="width: 200px"
        />
        <span style="margin-left: 10px; color: #999; font-size: 12px">
          数值越小越靠前
        </span>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 显示层级信息（只读） -->
      <el-form-item v-if="form.level" label="层级">
        <el-tag v-if="form.level === 1" type="primary">一级分类</el-tag>
        <el-tag v-else-if="form.level === 2" type="success">二级分类</el-tag>
        <el-tag v-else-if="form.level === 3" type="warning">三级分类</el-tag>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import categoryApi from '@/api/category'
import type { Category, CategoryFormData } from '@/types/category'

interface Props {
  modelValue: boolean
  formData?: CategoryFormData
  categoryTree?: Category[]
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: CategoryFormData): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const parentIds = ref<number[]>([])

// 表单数据
const form = ref<CategoryFormData>({
  name: '',
  parentId: null,
  level: 1,
  sortOrder: 0,
  status: 1
})

// 表单校验规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (value && value.trim()) {
          try {
            const res = await categoryApi.checkNameDuplicate(
              value.trim(),
              form.value.parentId,
              props.formData?.id
            )
            if (res.code === 200 && res.data) {
              callback(new Error('分类名称已存在'))
            } else {
              callback()
            }
          } catch (error) {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    { type: 'number', min: 0, max: 9999, message: '排序号范围 0-9999', trigger: 'blur' }
  ]
}

// 级联选择器配置
const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  checkStrictly: true,
  emitPath: true
}

// 父分类选项（过滤掉当前分类及其子分类）
const parentCategoryOptions = computed(() => {
  if (!props.categoryTree || props.categoryTree.length === 0) {
    return []
  }

  // 如果是编辑模式，需要过滤掉当前分类及其所有子分类
  const excludeId = props.formData?.id
  if (!excludeId) {
    return props.categoryTree
  }

  // 递归过滤
  const filterCategory = (categories: Category[]): Category[] => {
    return categories.reduce((result: Category[], category) => {
      // 跳过当前分类及其子分类
      if (category.id === excludeId) {
        return result
      }
      const filtered = {
        ...category,
        children: category.children ? filterCategory(category.children) : []
      }
      result.push(filtered)
      return result
    }, [])
  }

  return filterCategory(props.categoryTree)
})

// 监听传入的表单数据
watch(
  () => props.formData,
  (newData) => {
    if (newData) {
      form.value = { ...newData }
      // 设置父分类选择的值
      if (newData.parentId) {
        parentIds.value = [newData.parentId]
      } else {
        parentIds.value = []
      }
    } else {
      // 重置表单
      form.value = {
        name: '',
        parentId: null,
        level: 1,
        sortOrder: 0,
        status: 1
      }
      parentIds.value = []
    }
  },
  { immediate: true }
)

// 处理父分类选择变化
function handleParentChange(value: number[]) {
  if (value && value.length > 0) {
    const parentId = value[value.length - 1]
    form.value.parentId = parentId

    // 计算层级
    const findLevel = (categories: Category[], id: number, currentLevel: number): number => {
      for (const category of categories) {
        if (category.id === id) {
          return category.level || 1
        }
        if (category.children && category.children.length > 0) {
          const found = findLevel(category.children, id, currentLevel + 1)
          if (found > 0) {
            return found
          }
        }
      }
      return 0
    }

    if (props.categoryTree) {
      const parentLevel = findLevel(props.categoryTree, parentId, 1)
      form.value.level = parentLevel + 1
    }
  } else {
    form.value.parentId = null
    form.value.level = 1
  }
}

// 处理表单提交
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 检查层级限制
    if (form.value.level && form.value.level > 3) {
      ElMessage.error('分类层级不能超过 3 级')
      return
    }

    submitting.value = true

    // 提交表单数据
    emit('submit', {
      ...form.value,
      name: form.value.name.trim()
    })
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 处理关闭
function handleClose() {
  formRef.value?.resetFields()
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
:deep(.el-dialog__body) {
  padding-top: 20px;
}
</style>
