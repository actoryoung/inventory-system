<template>
  <el-button
    :type="type"
    :size="size"
    :loading="loading"
    @click="handleClick"
  >
    <slot>{{ label }}</slot>
  </el-button>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'

/**
 * 二次确认按钮组件
 * 用于删除/作废等危险操作，统一确认弹窗。
 */
const props = withDefaults(defineProps<{
  label?: string
  message?: string
  title?: string
  confirmButtonText?: string
  cancelButtonText?: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  size?: 'large' | 'default' | 'small'
}>(), {
  label: '删除',
  message: '确定要执行该操作吗？',
  title: '提示',
  confirmButtonText: '确定',
  cancelButtonText: '取消',
  type: 'danger',
  size: 'small'
})

const emit = defineEmits<{
  (e: 'confirm'): void
}>()

const loading = ref(false)

async function handleClick() {
  try {
    await ElMessageBox.confirm(props.message, props.title, {
      confirmButtonText: props.confirmButtonText,
      cancelButtonText: props.cancelButtonText,
      type: 'warning'
    })
    loading.value = true
    emit('confirm')
  } catch (error) {
    // 用户取消，忽略
  } finally {
    loading.value = false
  }
}
</script>
