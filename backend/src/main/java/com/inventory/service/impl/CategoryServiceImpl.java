package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inventory.dto.CategoryDTO;
import com.inventory.entity.Category;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.CategoryMapper;
import com.inventory.service.CategoryService;
import com.inventory.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    private static final int MAX_LEVEL = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CategoryDTO dto) {
        // 1. 校验分类名称
        if (isNameDuplicate(dto.getName(), dto.getParentId(), null)) {
            throw new BusinessException("分类名称已存在");
        }

        // 2. 计算层级
        Integer level = calculateLevel(dto.getParentId());
        if (level > MAX_LEVEL) {
            throw new BusinessException("分类层级不能超过" + MAX_LEVEL + "级");
        }

        // 3. 创建分类
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setLevel(level);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        boolean saved = this.save(category);
        if (!saved) {
            throw new BusinessException("分类创建失败");
        }

        log.info("创建分类成功，name={}, level={}, id={}", category.getName(), level, category.getId());
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(CategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }

        Category exist = this.getById(dto.getId());
        if (exist == null) {
            throw new BusinessException("分类不存在");
        }

        // 校验名称唯一性（排除当前分类）
        if (isNameDuplicate(dto.getName(), dto.getParentId(), dto.getId())) {
            throw new BusinessException("分类名称已存在");
        }

        // 更新分类
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setId(dto.getId());

        // 如果修改了父分类，需要重新计算层级
        if ((dto.getParentId() == null && exist.getParentId() != null) ||
                (dto.getParentId() != null && !dto.getParentId().equals(exist.getParentId()))) {
            Integer level = calculateLevel(dto.getParentId());
            if (level > MAX_LEVEL) {
                throw new BusinessException("分类层级不能超过" + MAX_LEVEL + "级");
            }
            category.setLevel(level);
        }

        boolean updated = this.updateById(category);
        log.info("更新分类成功，id={}, name={}", category.getId(), category.getName());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }

        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否可以删除
        if (!canDelete(id)) {
            throw new BusinessException("该分类下有商品或子分类，无法删除");
        }

        boolean deleted = this.removeById(id);
        log.info("删除分类成功，id={}, name={}", id, category.getName());
        return deleted;
    }

    @Override
    public CategoryVO getById(Long id) {
        if (id == null) {
            return null;
        }
        Category category = this.baseMapper.selectById(id);
        return CategoryVO.fromEntity(category);
    }

    @Override
    public List<CategoryVO> getTree() {
        List<Category> allCategories = this.baseMapper.selectAllCategories();
        return buildTree(allCategories);
    }

    @Override
    public List<CategoryVO> getEnabledTree() {
        List<Category> enabledCategories = this.baseMapper.selectEnabledCategories();
        return buildTree(enabledCategories);
    }

    @Override
    public List<CategoryVO> getList() {
        List<Category> categories = this.list();
        return CategoryVO.fromEntityList(categories);
    }

    @Override
    public List<CategoryVO> searchByName(String name) {
        if (!StringUtils.hasText(name)) {
            return new ArrayList<>();
        }
        List<Category> categories = this.baseMapper.selectByNameLike(name);
        return CategoryVO.fromEntityList(categories);
    }

    @Override
    public List<CategoryVO> getByLevel(Integer level) {
        if (level == null || level < 1 || level > MAX_LEVEL) {
            return new ArrayList<>();
        }
        List<Category> categories = this.baseMapper.selectByLevel(level);
        return CategoryVO.fromEntityList(categories);
    }

    @Override
    public List<CategoryVO> getChildren(Long parentId) {
        List<Category> children;
        if (parentId == null || parentId == 0) {
            // 查询一级分类
            children = this.lambdaQuery()
                    .isNull(Category::getParentId)
                    .or()
                    .eq(Category::getParentId, 0)
                    .list();
        } else {
            children = this.baseMapper.selectByParentId(parentId);
        }
        return CategoryVO.fromEntityList(children);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效");
        }

        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        category.setStatus(status);
        boolean updated = this.updateById(category);
        log.info("切换分类状态成功，id={}, status={}", id, status);
        return updated;
    }

    @Override
    public boolean isNameDuplicate(String name, Long parentId, Long excludeId) {
        if (!StringUtils.hasText(name)) {
            return false;
        }

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, name);

        if (parentId == null || parentId == 0) {
            wrapper.and(w -> w.isNull(Category::getParentId)
                    .or().eq(Category::getParentId, 0));
        } else {
            wrapper.eq(Category::getParentId, parentId);
        }

        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }

        return this.count(wrapper) > 0;
    }

    @Override
    public Integer calculateLevel(Long parentId) {
        if (parentId == null || parentId == 0) {
            return 1;
        }

        Category parent = this.getById(parentId);
        if (parent == null) {
            throw new BusinessException("父分类不存在");
        }

        return parent.getLevel() + 1;
    }

    @Override
    public boolean canDelete(Long id) {
        // 检查是否有子分类
        int childrenCount = this.baseMapper.countChildren(id);
        if (childrenCount > 0) {
            return false;
        }

        // 检查是否有关联的商品
        int productCount = this.baseMapper.countRelatedProducts(id);
        if (productCount > 0) {
            return false;
        }

        return true;
    }

    @Override
    public List<CategoryVO> buildTree(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为VO
        List<CategoryVO> vos = CategoryVO.fromEntityList(categories);

        // 按 parentId 分组
        List<CategoryVO> roots = new ArrayList<>();
        java.util.Map<Long, List<CategoryVO>> parentMap = new java.util.HashMap<>();

        for (CategoryVO vo : vos) {
            Long parentId = vo.getParentId();
            if (parentId == null || parentId == 0) {
                roots.add(vo);
            } else {
                parentMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(vo);
            }
        }

        // 递归设置子分类
        setChildren(roots, parentMap);

        return roots;
    }

    /**
     * 递归设置子分类
     */
    private void setChildren(List<CategoryVO> parents, java.util.Map<Long, List<CategoryVO>> parentMap) {
        for (CategoryVO parent : parents) {
            List<CategoryVO> children = parentMap.get(parent.getId());
            if (children != null && !children.isEmpty()) {
                parent.setChildren(children);
                setChildren(children, parentMap);
            }
        }
    }
}
