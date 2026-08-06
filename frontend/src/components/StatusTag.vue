<template>
  <el-tag :type="type" size="small">{{ text }}</el-tag>
</template>

<script setup lang="ts">
/**
 * 状态标签组件
 * 统一入库/出库单据状态展示样式。
 */
defineProps<{
  text: string
  type?: 'success' | 'warning' | 'danger' | 'info'
}>()
</script>
