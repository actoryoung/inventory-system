package com.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.inventory.dto.CategoryDTO;
import com.inventory.entity.Category;
import com.inventory.vo.CategoryVO;

import java.util.List;

/**
 * 商品分类服务接口
 *
 * @author inventory-system
 * @since 2026-01-04
 */
public interface CategoryService extends IService<Category> {

    /**
     * 创建分类
     *
     * @param dto 分类数据传输对象
     * @return 创建的分类ID
     */
    Long create(CategoryDTO dto);

    /**
     * 更新分类
     *
     * @param dto 分类数据传输对象
     * @return 是否成功
     */
    boolean update(CategoryDTO dto);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类视图对象
     */
    CategoryVO getById(Long id);

    /**
     * 获取所有分类（树形结构）
     *
     * @return 分类树
     */
    List<CategoryVO> getTree();

    /**
     * 获取启用的分类树（用于商品表单选择器）
     *
     * @return 启用的分类树
     */
    List<CategoryVO> getEnabledTree();

    /**
     * 获取分类列表（平铺）
     *
     * @return 分类列表
     */
    List<CategoryVO> getList();

    /**
     * 根据名称搜索分类
     *
     * @param name 分类名称
     * @return 匹配的分类列表
     */
    List<CategoryVO> searchByName(String name);

    /**
     * 根据层级查询分类
     *
     * @param level 层级
     * @return 分类列表
     */
    List<CategoryVO> getByLevel(Integer level);

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<CategoryVO> getChildren(Long parentId);

    /**
     * 切换分类状态
     *
     * @param id     分类ID
     * @param status 状态：0-禁用，1-启用
     * @return 是否成功
     */
    boolean toggleStatus(Long id, Integer status);

    /**
     * 检查分类名称是否重复（同一父分类下）
     *
     * @param name     分类名称
     * @param parentId 父分类ID
     * @param excludeId 排除的分类ID（更新时使用）
     * @return 是否重复
     */
    boolean isNameDuplicate(String name, Long parentId, Long excludeId);

    /**
     * 计算分类层级
     *
     * @param parentId 父分类ID
     * @return 层级值（1-3）
     */
    Integer calculateLevel(Long parentId);

    /**
     * 检查是否可以删除分类
     *
     * @param id 分类ID
     * @return 是否可以删除
     */
    boolean canDelete(Long id);

    /**
     * 构建分类树
     *
     * @param categories 分类列表
     * @return 分类树
     */
    List<CategoryVO> buildTree(List<Category> categories);
}
