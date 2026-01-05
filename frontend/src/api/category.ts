import request from '@/utils/request'
import type { ApiResponse, Category, CategoryFormData, CategoryQuery } from '@/types/category'

/**
 * 商品分类 API
 */
export const categoryApi = {
  /**
   * 创建分类
   */
  create(data: CategoryFormData): Promise<ApiResponse<number>> {
    return request.post('/api/categories', data)
  },

  /**
   * 更新分类
   */
  update(id: number, data: CategoryFormData): Promise<ApiResponse<boolean>> {
    return request.put(`/api/categories/${id}`, data)
  },

  /**
   * 删除分类
   */
  delete(id: number): Promise<ApiResponse<boolean>> {
    return request.delete(`/api/categories/${id}`)
  },

  /**
   * 获取分类详情
   */
  getById(id: number): Promise<ApiResponse<Category>> {
    return request.get(`/api/categories/${id}`)
  },

  /**
   * 获取分类树
   */
  getTree(): Promise<ApiResponse<Category[]>> {
    return request.get('/api/categories/tree')
  },

  /**
   * 获取启用的分类树（用于商品表单选择器）
   */
  getEnabledTree(): Promise<ApiResponse<Category[]>> {
    return request.get('/api/categories/tree/enabled')
  },

  /**
   * 获取分类列表
   */
  getList(params?: CategoryQuery): Promise<ApiResponse<Category[]>> {
    return request.get('/api/categories', { params })
  },

  /**
   * 获取子分类
   */
  getChildren(parentId: number): Promise<ApiResponse<Category[]>> {
    return request.get(`/api/categories/children/${parentId}`)
  },

  /**
   * 切换分类状态
   */
  toggleStatus(id: number, status: number): Promise<ApiResponse<boolean>> {
    return request.patch(`/api/categories/${id}/status`, null, {
      params: { status }
    })
  },

  /**
   * 检查分类名称是否重复
   */
  checkNameDuplicate(
    name: string,
    parentId?: number | null,
    excludeId?: number
  ): Promise<ApiResponse<boolean>> {
    return request.get('/api/categories/check-name', {
      params: { name, parentId, excludeId }
    })
  },

  /**
   * 检查是否可以删除分类
   */
  canDelete(id: number): Promise<ApiResponse<boolean>> {
    return request.get(`/api/categories/${id}/can-delete`)
  }
}

export default categoryApi
