import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/product'
  },
  {
    path: '/category',
    name: 'Category',
    component: () => import('@/views/category/CategoryList.vue'),
    meta: {
      title: '商品分类管理',
      icon: 'FolderOpened'
    }
  },
  {
    path: '/product',
    name: 'Product',
    component: () => import('@/views/product/ProductList.vue'),
    meta: {
      title: '商品管理',
      icon: 'Goods'
    }
  },
  {
    path: '/inventory',
    name: 'Inventory',
    component: () => import('@/views/inventory/InventoryList.vue'),
    meta: {
      title: '库存管理',
      icon: 'Box'
    }
  },
  {
    path: '/inbound',
    name: 'Inbound',
    component: () => import('@/views/inbound/InboundList.vue'),
    meta: {
      title: '入库管理',
      icon: 'Download'
    }
  },
  {
    path: '/outbound',
    name: 'Outbound',
    component: () => import('@/views/outbound/OutboundList.vue'),
    meta: {
      title: '出库管理',
      icon: 'Upload'
    }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/views/statistics/Statistics.vue'),
    meta: {
      title: '统计报表',
      icon: 'DataAnalysis'
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 进销存管理系统`
  }
  next()
})

export default router
