<template>
  <div class="page-table">
    <el-table
      v-bind="$attrs"
      v-loading="loading"
      :data="data"
      border
      stripe
      style="width: 100%"
    >
      <slot />
    </el-table>
    <div v-if="showPagination" class="pagination-container">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :page-sizes="pageSizes"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @update:current-page="handlePageChange"
        @update:page-size="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 分页表格组件
 * 统一 el-table 加载态 + 分页条，表格列通过默认插槽传入，
 * 其余 el-table 属性（如 row-key、tree-props、selection-change）通过透传属性传入。
 * 树形表格等无分页场景可设置 show-pagination=false。
 */
defineOptions({ inheritAttrs: false })

withDefaults(defineProps<{
  loading?: boolean
  data: unknown[]
  total?: number
  page?: number
  size?: number
  pageSizes?: number[]
  showPagination?: boolean
}>(), {
  loading: false,
  total: 0,
  page: 1,
  size: 10,
  pageSizes: () => [10, 20, 50, 100],
  showPagination: true
})

const emit = defineEmits<{
  (e: 'update:page', value: number): void
  (e: 'update:size', value: number): void
  (e: 'refresh'): void
}>()

function handlePageChange(value: number) {
  emit('update:page', value)
  emit('refresh')
}

function handleSizeChange(value: number) {
  emit('update:size', value)
  emit('refresh')
}
</script>

<style scoped lang="scss">
.page-table {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
