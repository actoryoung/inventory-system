package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inventory.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品分类 Mapper 接口
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询所有分类（树形结构）
     *
     * @return 所有分类列表
     */
    @Select("SELECT * FROM t_category ORDER BY level ASC, sort_order ASC, id ASC")
    List<Category> selectAllCategories();

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Select("SELECT * FROM t_category WHERE parent_id = #{parentId} ORDER BY sort_order ASC, id ASC")
    List<Category> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据分类名称和父分类ID查询
     *
     * @param name     分类名称
     * @param parentId 父分类ID
     * @return 分类对象
     */
    @Select("SELECT * FROM t_category WHERE name = #{name} AND " +
            "(parent_id = #{parentId} OR (parent_id IS NULL AND #{parentId} IS NULL))")
    Category selectByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);

    /**
     * 查询启用状态的分类
     *
     * @return 启用的分类列表
     */
    @Select("SELECT * FROM t_category WHERE status = 1 ORDER BY level ASC, sort_order ASC")
    List<Category> selectEnabledCategories();

    /**
     * 根据名称模糊搜索分类
     *
     * @param name 分类名称（支持模糊匹配）
     * @return 匹配的分类列表
     */
    @Select("SELECT * FROM t_category WHERE name LIKE CONCAT('%', #{name}, '%') " +
            "ORDER BY level ASC, sort_order ASC")
    List<Category> selectByNameLike(@Param("name") String name);

    /**
     * 检查分类是否有子分类
     *
     * @param parentId 父分类ID
     * @return 子分类数量
     */
    @Select("SELECT COUNT(*) FROM t_category WHERE parent_id = #{parentId}")
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 检查分类是否有关联的商品
     *
     * @param categoryId 分类ID
     * @return 关联的商品数量
     */
    @Select("SELECT COUNT(*) FROM t_product WHERE category_id = #{categoryId}")
    int countRelatedProducts(@Param("categoryId") Long categoryId);

    /**
     * 查询指定层级的分类
     *
     * @param level 层级
     * @return 分类列表
     */
    @Select("SELECT * FROM t_category WHERE level = #{level} ORDER BY sort_order ASC")
    List<Category> selectByLevel(@Param("level") Integer level);
}
