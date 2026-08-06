<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="layout-aside">
      <div class="layout-logo">进销存管理系统</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#ffffff"
        class="layout-menu"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="layout-body">
      <!-- 顶栏：面包屑 -->
      <el-header class="layout-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
        </el-breadcrumb>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

interface MenuItem {
  path: string
  title: string
  icon: string
}

const route = useRoute()

const menus: MenuItem[] = [
  { path: '/product', title: '商品管理', icon: 'Goods' },
  { path: '/category', title: '商品分类', icon: 'FolderOpened' },
  { path: '/inventory', title: '库存管理', icon: 'Box' },
  { path: '/inbound', title: '入库管理', icon: 'Download' },
  { path: '/outbound', title: '出库管理', icon: 'Upload' },
  { path: '/statistics', title: '统计报表', icon: 'DataAnalysis' }
]

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 当前页面标题（用于面包屑）
const currentTitle = computed(() => (route.meta?.title as string) || '')
</script>

<style scoped lang="scss">
.layout-container {
  height: 100%;
}

.layout-aside {
  background: #001529;
  overflow: hidden;

  .layout-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 1px;
  }

  .layout-menu {
    border-right: none;
  }
}

.layout-body {
  display: flex;
  flex-direction: column;
}

.layout-header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
}

.layout-main {
  background: #f5f5f5;
  padding: 16px;
  overflow: auto;
}
</style>
