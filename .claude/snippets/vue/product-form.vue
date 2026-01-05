# Vue 3 商品表单片段

```vue
<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑商品' : '新增商品'"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="商品编码" prop="sku">
            <el-input
              v-model="form.sku"
              placeholder="请输入商品编码"
              :disabled="isEdit"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入商品名称" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="商品分类" prop="categoryId">
            <el-select
              v-model="form.categoryId"
              placeholder="请选择分类"
              style="width: 100%"
            >
              <el-option
                v-for="item in categoryList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计量单位" prop="unit">
            <el-select v-model="form.unit" placeholder="请选择单位">
              <el-option label="个" value="个" />
              <el-option label="件" value="件" />
              <el-option label="箱" value="箱" />
              <el-option label="套" value="套" />
              <el-option label="kg" value="kg" />
              <el-option label="g" value="g" />
              <el-option label="L" value="L" />
              <el-option label="ml" value="ml" />
              <el-option label="米" value="米" />
            </el-select>
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
          <el-form-item label="成本价格" prop="costPrice">
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
          <el-form-item label="初始库存" prop="initStock">
            <el-input-number
              v-model="form.initStock"
              :min="0"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预警库存" prop="warningStock">
            <el-input-number
              v-model="form.warningStock"
              :min="0"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="商品规格" prop="specification">
        <el-input
          v-model="form.specification"
          type="textarea"
          :rows="2"
          placeholder="请输入商品规格，如：颜色、尺寸等"
        />
      </el-form-item>

      <el-form-item label="商品描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入商品描述"
        />
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          placeholder="请输入备注"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { createProduct, updateProduct, checkSku } from '@/api/product';
import { getCategoryList } from '@/api/category';

interface Props {
  modelValue: boolean;
  data?: any;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue', 'refresh']);

const visible = ref(props.modelValue);
const loading = ref(false);
const formRef = ref();
const categoryList = ref([]);

const isEdit = computed(() => !!props.data?.id);

const form = reactive({
  id: null,
  sku: '',
  name: '',
  categoryId: null,
  unit: '',
  price: 0,
  costPrice: 0,
  initStock: 0,
  warningStock: 10,
  specification: '',
  description: '',
  remark: ''
});

const rules = {
  sku: [
    { required: true, message: '请输入商品编码', trigger: 'blur' },
    { validator: checkSkuExist, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请选择计量单位', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入销售价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ]
};

// SKU 唯一性校验
const checkSkuExist = async (rule: any, value: string, callback: any) => {
  if (!value) {
    callback();
    return;
  }

  if (isEdit.value && props.data?.sku === value) {
    callback();
    return;
  }

  try {
    const { data } = await checkSku(value);
    if (data.exists) {
      callback(new Error('商品编码已存在'));
    } else {
      callback();
    }
  } catch (error) {
    callback();
  }
};

// 获取分类列表
const fetchCategories = async () => {
  const { data } = await getCategoryList({ page: 1, size: 1000 });
  categoryList.value = data.records;
};

// 提交
const handleSubmit = async () => {
  await formRef.value.validate();

  loading.value = true;
  try {
    if (isEdit.value) {
      await updateProduct(form);
      ElMessage.success('修改成功');
    } else {
      await createProduct(form);
      ElMessage.success('新增成功');
    }
    emit('refresh');
    handleClose();
  } catch (error) {
    ElMessage.error('操作失败');
  } finally {
    loading.value = false;
  }
};

// 关闭
const handleClose = () => {
  formRef.value?.resetFields();
  emit('update:modelValue', false);
};

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val;
    if (val) {
      fetchCategories();
      if (props.data) {
        Object.assign(form, props.data);
      } else {
        Object.assign(form, {
          id: null,
          sku: '',
          name: '',
          categoryId: null,
          unit: '',
          price: 0,
          costPrice: 0,
          initStock: 0,
          warningStock: 10,
          specification: '',
          description: '',
          remark: ''
        });
      }
    }
  }
);
</script>
```
