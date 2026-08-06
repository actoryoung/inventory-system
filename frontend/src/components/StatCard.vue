<template>
  <div class="stat-card" :class="`stat-${color}`">
    <div class="label">{{ label }}</div>
    <div class="value">{{ value }}</div>
  </div>
</template>

<script setup lang="ts">
/**
 * 汇总统计卡片组件
 * 用于库存汇总、统计报表等页面的指标展示。
 */
defineProps<{
  label: string
  value: string | number
  color?: 'blue' | 'green' | 'orange' | 'red'
}>()
</script>

<style scoped lang="scss">
.stat-card {
  flex: 1;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  border-left: 3px solid #409eff;

  &.stat-green {
    border-left-color: #67c23a;
  }

  &.stat-orange {
    border-left-color: #e6a23c;
  }

  &.stat-red {
    border-left-color: #f56c6c;
  }

  .label {
    font-size: 12px;
    color: #909399;
    margin-bottom: 8px;
  }

  .value {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
  }
}
</style>
