<template>
  <el-dialog
    :model-value="modelValue"
    :title="formData?.id ? '编辑商品' : '新增商品'"
    width="700px"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      @submit.prevent="handleSubmit"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="SKU编码" prop="sku">
            <el-input
              v-model="form.sku"
              placeholder="请输入SKU编码"
              maxlength="50"
              show-word-limit
              clearable
              @blur="checkSku"
            />
            <span v-if="skuExists" class="error-tip">SKU已存在</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="商品名称" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入商品名称"
              maxlength="100"
              show-word-limit
              clearable
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="商品分类" prop="categoryId">
            <el-cascader
              v-model="categoryIds"
              :options="categoryTreeOptions"
              :props="cascaderProps"
              placeholder="请选择分类"
              clearable
              filterable
              style="width: 100%"
              @change="handleCategoryChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计量单位">
            <el-input v-model="form.unit" placeholder="如：台、件、箱" clearable />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="销售价格" prop="price">
            <el-input-number
              v-model="form.price"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="成本价格">
            <el-input-number
              v-model="form.costPrice"
              :min="0"
              :precision="2"
              :step="0.01"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="预警库存" prop="warningStock">
            <el-input-number
              v-model="form.warningStock"
              :min="0"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="商品规格">
        <el-input
          v-model="form.specification"
          placeholder="如：256GB 深空黑色"
          maxlength="200"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-form-item label="商品描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入商品描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          placeholder="请输入备注"
          maxlength="500"
          show-word-limit
        />
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
import productApi from '@/api/product'
import type { Product, ProductFormData } from '@/types/product'

interface Props {
  modelValue: boolean
  formData?: ProductFormData
  categoryTree?: any[]
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: ProductFormData): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const categoryIds = ref<number[]>([])
const skuExists = ref(false)

// 表单数据
const form = ref<ProductFormData>({
  sku: '',
  name: '',
  categoryId: 0,
  unit: '',
  price: 0,
  costPrice: 0,
  warningStock: 0,
  status: 1
})

// 表单校验规则
const rules: FormRules = {
  sku: [
    { required: true, message: '请输入SKU编码', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (value && value.trim()) {
          if (skuExists.value) {
            callback(new Error('SKU已存在'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入销售价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ],
  warningStock: [
    { required: true, message: '请输入预警库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '预警库存不能为负数', trigger: 'blur' }
  ]
}

// 级联选择器配置
const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  checkStrictly: true
}

// 分类选项（只显示启用的分类）
const categoryTreeOptions = computed(() => {
  if (!props.categoryTree || props.categoryTree.length === 0) {
    return []
  }
  return props.categoryTree
})

// 监听传入的表单数据
watch(
  () => props.formData,
  (newData) => {
    if (newData) {
      form.value = { ...newData }
      // 设置分类选择
      if (newData.categoryId) {
        categoryIds.value = [newData.categoryId]
      } else {
        categoryIds.value = []
      }
    } else {
      // 重置表单
      form.value = {
        sku: '',
        name: '',
        categoryId: 0,
        unit: '',
        price: 0,
        costPrice: 0,
        warningStock: 0,
        status: 1
      }
      categoryIds.value = []
    }
    skuExists.value = false
  },
  { immediate: true }
)

// 处理分类选择变化
function handleCategoryChange(value: number[]) {
  if (value && value.length > 0) {
    form.value.categoryId = value[value.length - 1]
  } else {
    form.value.categoryId = 0
  }
}

// 检查 SKU 是否存在
async function checkSku() {
  if (!form.value.sku || form.value.sku.trim() === '') {
    return
  }

  try {
    const res = await productApi.checkSkuExists(
      form.value.sku.trim(),
      props.formData?.id
    )
    if (res.code === 200) {
      skuExists.value = res.data
    }
  } catch (error) {
    console.error('检查SKU失败:', error)
  }
}

// 处理表单提交
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitting.value = true

    // 提交表单数据
    emit('submit', {
      ...form.value,
      sku: form.value.sku.trim()
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
  skuExists.value = false
  emit('update:modelValue', false)
}
</script>

<style scoped lang="scss">
:deep(.el-dialog__body) {
  padding-top: 20px;
}

.error-tip {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}
</style>
