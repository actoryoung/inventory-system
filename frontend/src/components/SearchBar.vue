<template>
  <div class="search-bar">
    <slot />
    <el-button
      v-if="showSearchButton"
      type="primary"
      :icon="Search"
      @click="emit('search')"
    >
      {{ searchText }}
    </el-button>
    <el-button v-if="showResetButton" @click="emit('reset')">
      {{ resetText }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'

/**
 * 搜索栏组件
 * 提供统一的搜索/重置按钮布局，筛选控件通过默认插槽传入。
 */
withDefaults(defineProps<{
  showSearchButton?: boolean
  showResetButton?: boolean
  searchText?: string
  resetText?: string
}>(), {
  showSearchButton: true,
  showResetButton: true,
  searchText: '搜索',
  resetText: '重置'
})

const emit = defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
}>()
</script>

<style scoped lang="scss">
.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
