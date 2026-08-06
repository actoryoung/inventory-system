<template>
  <div class="not-found">
    <h1 class="code">404</h1>
    <p class="desc">抱歉，您访问的页面不存在</p>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/product')
}
</script>

<style scoped lang="scss">
.not-found {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;

  .code {
    font-size: 96px;
    color: #409eff;
    margin: 0;
  }

  .desc {
    font-size: 16px;
    color: #909399;
    margin: 16px 0 24px;
  }
}
</style>
