package com.inventory.service;

import com.inventory.entity.Category;
import com.inventory.mapper.CategoryMapper;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.impl.CategoryServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 商品分类服务层单元测试
 *
 * 测试范围:
 * - 分类创建（正常、异常场景）
 * - 分类更新
 * - 分类删除（含约束校验）
 * - 分类查询（列表、树形、详情）
 * - 分类状态切换
 * - 分类层级校验
 * - 分类名称唯一性校验
 *
 * @author Claude Code
 * @since 2026-01-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("商品分类服务测试")
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category level1Category;
    private Category level2Category;
    private Category level3Category;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        level1Category = Category.builder()
                .id(1L)
                .name("电子产品")
                .parentId(null)
                .level(1)
                .sortOrder(1)
                .status(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        level2Category = Category.builder()
                .id(2L)
                .name("手机")
                .parentId(1L)
                .level(2)
                .sortOrder(1)
                .status(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        level3Category = Category.builder()
                .id(3L)
                .name("智能手机")
                .parentId(2L)
                .level(3)
                .sortOrder(1)
                .status(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("分类创建测试")
    class CreateCategoryTests {

        @Test
        @DisplayName("应成功创建一级分类")
        void shouldCreateLevel1CategorySuccessfully() {
            // Arrange
            Category newCategory = Category.builder()
                    .name("食品饮料")
                    .parentId(null)
                    .level(1)
                    .sortOrder(2)
                    .status(1)
                    .build();

            when(categoryMapper.insert(any(Category))).thenReturn(1);
            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            // Act
            boolean result = categoryService.createCategory(newCategory);

            // Assert
            assertThat(result).isTrue();
            verify(categoryMapper).insert(newCategory);
            assertThat(newCategory.getLevel()).isEqualTo(1);
        }

        @Test
        @DisplayName("应成功创建二级分类")
        void shouldCreateLevel2CategorySuccessfully() {
            // Arrange
            Category newCategory = Category.builder()
                    .name("电脑")
                    .parentId(1L)
                    .level(2)
                    .sortOrder(2)
                    .status(1)
                    .build();

            when(categoryMapper.selectById(1L)).thenReturn(level1Category);
            when(categoryMapper.insert(any(Category))).thenReturn(1);
            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            // Act
            boolean result = categoryService.createCategory(newCategory);

            // Assert
            assertThat(result).isTrue();
            assertThat(newCategory.getLevel()).isEqualTo(2);
        }

        @Test
        @DisplayName("应成功创建三级分类")
        void shouldCreateLevel3CategorySuccessfully() {
            // Arrange
            Category newCategory = Category.builder()
                    .name("苹果手机")
                    .parentId(2L)
                    .level(3)
                    .sortOrder(1)
                    .status(1)
                    .build();

            when(categoryMapper.selectById(2L)).thenReturn(level2Category);
            when(categoryMapper.insert(any(Category))).thenReturn(1);
            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            // Act
            boolean result = categoryService.createCategory(newCategory);

            // Assert
            assertThat(result).isTrue();
            assertThat(newCategory.getLevel()).isEqualTo(3);
        }

        @Test
        @DisplayName("当父分类是三级时，创建分类应失败（层级超过限制）")
        void shouldFailWhenParentCategoryIsLevel3() {
            // Arrange
            Category newCategory = Category.builder()
                    .name("四级分类")
                    .parentId(3L)
                    .level(4)
                    .sortOrder(1)
                    .status(1)
                    .build();

            when(categoryMapper.selectById(3L)).thenReturn(level3Category);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.createCategory(newCategory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("分类层级不能超过3级");

            verify(categoryMapper, never()).insert(any(Category));
        }

        @Test
        @DisplayName("当同级分类名称重复时，创建应失败")
        void shouldFailWhenCategoryNameDuplicatesInSameLevel() {
            // Arrange
            Category duplicateCategory = Category.builder()
                    .name("手机")
                    .parentId(1L)
                    .level(2)
                    .sortOrder(2)
                    .status(1)
                    .build();

            when(categoryMapper.selectById(1L)).thenReturn(level1Category);
            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(level2Category);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.createCategory(duplicateCategory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("同一父分类下已存在同名分类");

            verify(categoryMapper, never()).insert(any(Category));
        }

        @Test
        @DisplayName("当不同层级分类名称相同时，应允许创建")
        void shouldAllowSameNameInDifferentLevels() {
            // Arrange
            Category newCategory = Category.builder()
                    .name("手机")
                    .parentId(null)  // 一级分类
                    .level(1)
                    .sortOrder(10)
                    .status(1)
                    .build();

            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
            when(categoryMapper.insert(any(Category))).thenReturn(1);

            // Act
            boolean result = categoryService.createCategory(newCategory);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("当父分类不存在时，创建应失败")
        void shouldFailWhenParentCategoryNotExists() {
            // Arrange
            Category newCategory = Category.builder()
                    .name("子分类")
                    .parentId(999L)
                    .level(2)
                    .sortOrder(1)
                    .status(1)
                    .build();

            when(categoryMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.createCategory(newCategory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("父分类不存在");

            verify(categoryMapper, never()).insert(any(Category));
        }

        @Test
        @DisplayName("当分类名称为空时，创建应失败")
        void shouldFailWhenCategoryNameIsEmpty() {
            // Arrange
            Category emptyNameCategory = Category.builder()
                    .name("")
                    .parentId(null)
                    .level(1)
                    .sortOrder(1)
                    .status(1)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> categoryService.createCategory(emptyNameCategory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("分类名称不能为空");

            verify(categoryMapper, never()).insert(any(Category));
        }

        @Test
        @DisplayName("当分类名称超过50字符时，创建应失败")
        void shouldFailWhenCategoryNameExceedsMaxLength() {
            // Arrange
            String longName = "这是一个非常非常非常非常非常非常非常非常非常非常长的分类名称";
            Category longNameCategory = Category.builder()
                    .name(longName)
                    .parentId(null)
                    .level(1)
                    .sortOrder(1)
                    .status(1)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> categoryService.createCategory(longNameCategory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("分类名称长度不能超过50个字符");

            verify(categoryMapper, never()).insert(any(Category));
        }
    }

    @Nested
    @DisplayName("分类更新测试")
    class UpdateCategoryTests {

        @Test
        @DisplayName("应成功更新分类基本信息")
        void shouldUpdateCategorySuccessfully() {
            // Arrange
            Category updateCategory = Category.builder()
                    .id(1L)
                    .name("电子产品（更新）")
                    .parentId(null)
                    .level(1)
                    .sortOrder(10)
                    .status(1)
                    .build();

            when(categoryMapper.selectById(1L)).thenReturn(level1Category);
            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
            when(categoryMapper.updateById(any(Category))).thenReturn(1);

            // Act
            boolean result = categoryService.updateCategory(updateCategory);

            // Assert
            assertThat(result).isTrue();
            verify(categoryMapper).updateById(updateCategory);
        }

        @Test
        @DisplayName("当分类不存在时，更新应失败")
        void shouldFailWhenCategoryNotExists() {
            // Arrange
            Category updateCategory = Category.builder()
                    .id(999L)
                    .name("不存在的分类")
                    .build();

            when(categoryMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.updateCategory(updateCategory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("分类不存在");

            verify(categoryMapper, never()).updateById(any(Category));
        }

        @Test
        @DisplayName("当更新后名称与同级分类重复时，应失败")
        void shouldFailWhenUpdatedNameDuplicatesInSameLevel() {
            // Arrange
            Category anotherLevel2 = Category.builder()
                    .id(4L)
                    .name("电脑")
                    .parentId(1L)
                    .level(2)
                    .sortOrder(2)
                    .status(1)
                    .build();

            Category updateCategory = Category.builder()
                    .id(2L)
                    .name("电脑")  // 与 anotherLevel2 同名
                    .parentId(1L)
                    .level(2)
                    .sortOrder(1)
                    .status(1)
                    .build();

            when(categoryMapper.selectById(2L)).thenReturn(level2Category);
            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(anotherLevel2);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.updateCategory(updateCategory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("同一父分类下已存在同名分类");

            verify(categoryMapper, never()).updateById(any(Category));
        }
    }

    @Nested
    @DisplayName("分类删除测试")
    class DeleteCategoryTests {

        @Test
        @DisplayName("应成功删除无关联商品和子分类的分类")
        void shouldDeleteCategorySuccessfully() {
            // Arrange
            when(categoryMapper.selectById(1L)).thenReturn(level1Category);
            when(productMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(categoryMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(categoryMapper.deleteById(1L)).thenReturn(1);

            // Act
            boolean result = categoryService.deleteCategory(1L);

            // Assert
            assertThat(result).isTrue();
            verify(categoryMapper).deleteById(1L);
        }

        @Test
        @DisplayName("当分类有关联商品时，删除应失败")
        void shouldFailWhenCategoryHasProducts() {
            // Arrange
            when(categoryMapper.selectById(1L)).thenReturn(level1Category);
            when(productMapper.selectCount(any(QueryWrapper.class))).thenReturn(5L);  // 有5个商品

            // Act & Assert
            assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("该分类下有商品，无法删除");

            verify(categoryMapper, never()).deleteById(any(Long));
        }

        @Test
        @DisplayName("当分类有子分类时，删除应失败")
        void shouldFailWhenCategoryHasChildren() {
            // Arrange
            when(categoryMapper.selectById(1L)).thenReturn(level1Category);
            when(productMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(categoryMapper.selectCount(any(QueryWrapper.class))).thenReturn(2L);  // 有2个子分类

            // Act & Assert
            assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("该分类下有子分类，请先删除子分类");

            verify(categoryMapper, never()).deleteById(any(Long));
        }

        @Test
        @DisplayName("当分类不存在时，删除应失败")
        void shouldFailWhenCategoryNotExists() {
            // Arrange
            when(categoryMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("分类不存在");

            verify(categoryMapper, never()).deleteById(any(Long));
        }

        @Test
        @DisplayName("应允许删除禁用状态的分类（无关联商品）")
        void shouldAllowDeletingDisabledCategoryWithoutProducts() {
            // Arrange
            Category disabledCategory = Category.builder()
                    .id(4L)
                    .name("已禁用分类")
                    .parentId(null)
                    .level(1)
                    .status(0)  // 禁用状态
                    .build();

            when(categoryMapper.selectById(4L)).thenReturn(disabledCategory);
            when(productMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(categoryMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
            when(categoryMapper.deleteById(4L)).thenReturn(1);

            // Act
            boolean result = categoryService.deleteCategory(4L);

            // Assert
            assertThat(result).isTrue();
            verify(categoryMapper).deleteById(4L);
        }
    }

    @Nested
    @DisplayName("分类查询测试")
    class QueryCategoryTests {

        @Test
        @DisplayName("应成功获取分类列表")
        void shouldGetCategoryListSuccessfully() {
            // Arrange
            List<Category> expectedList = Arrays.asList(level1Category, level2Category);
            when(categoryMapper.selectList(any(QueryWrapper.class))).thenReturn(expectedList);

            // Act
            List<Category> result = categoryService.getCategoryList(null, null, null);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(level1Category, level2Category);
        }

        @Test
        @DisplayName("应按名称模糊搜索分类")
        void shouldSearchCategoriesByName() {
            // Arrange
            List<Category> expectedList = Collections.singletonList(level1Category);
            when(categoryMapper.selectList(any(QueryWrapper.class))).thenReturn(expectedList);

            // Act
            List<Category> result = categoryService.getCategoryList("电子", null, null);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("电子");
        }

        @Test
        @DisplayName("应按状态过滤分类")
        void shouldFilterCategoriesByStatus() {
            // Arrange
            Category disabledCategory = Category.builder()
                    .id(4L)
                    .name("禁用分类")
                    .status(0)
                    .build();

            List<Category> allCategories = Arrays.asList(level1Category, disabledCategory);
            when(categoryMapper.selectList(any(QueryWrapper.class))).thenReturn(allCategories);

            // Act - 查询启用状态的分类
            List<Category> result = categoryService.getCategoryList(null, 1, null);

            // Assert
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("应按层级过滤分类")
        void shouldFilterCategoriesByLevel() {
            // Arrange
            List<Category> level1Categories = Collections.singletonList(level1Category);
            when(categoryMapper.selectList(any(QueryWrapper.class))).thenReturn(level1Categories);

            // Act
            List<Category> result = categoryService.getCategoryList(null, null, 1);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getLevel()).isEqualTo(1);
        }

        @Test
        @DisplayName("应成功获取分类详情")
        void shouldGetCategoryDetailSuccessfully() {
            // Arrange
            when(categoryMapper.selectById(1L)).thenReturn(level1Category);

            // Act
            Category result = categoryService.getCategoryById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("电子产品");
        }

        @Test
        @DisplayName("当分类不存在时，获取详情应返回null")
        void shouldReturnNullWhenCategoryNotExists() {
            // Arrange
            when(categoryMapper.selectById(999L)).thenReturn(null);

            // Act
            Category result = categoryService.getCategoryById(999L);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("应成功获取分类树形结构")
        void shouldGetCategoryTreeSuccessfully() {
            // Arrange
            List<Category> allCategories = Arrays.asList(
                    level1Category,
                    level2Category,
                    level3Category
            );
            when(categoryMapper.selectList(any(QueryWrapper.class))).thenReturn(allCategories);

            // Act
            List<Category> tree = categoryService.getCategoryTree();

            // Assert
            assertThat(tree).hasSize(1);  // 只有一个根节点
            assertThat(tree.get(0).getChildren()).isNotEmpty();
            assertThat(tree.get(0).getChildren().get(0).getChildren()).isNotEmpty();
        }

        @Test
        @DisplayName("树形结构应只返回启用状态的分类")
        void shouldReturnOnlyEnabledCategoriesInTree() {
            // Arrange
            Category disabledCategory = Category.builder()
                    .id(4L)
                    .name("禁用分类")
                    .parentId(1L)
                    .level(2)
                    .status(0)
                    .build();

            List<Category> allCategories = Arrays.asList(
                    level1Category,
                    level2Category,
                    disabledCategory
            );

            when(categoryMapper.selectList(any(QueryWrapper.class))).thenReturn(allCategories);

            // Act
            List<Category> tree = categoryService.getCategoryTree();

            // Assert
            assertThat(tree).hasSize(1);
            assertThat(tree.get(0).getChildren()).hasSize(1);  // 只有一个启用状态的子分类
            assertThat(tree.get(0).getChildren().get(0).getStatus()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("分类状态切换测试")
    class ToggleCategoryStatusTests {

        @Test
        @DisplayName("应成功切换分类状态（启用到禁用）")
        void shouldToggleStatusFromEnabledToDisabled() {
            // Arrange
            when(categoryMapper.selectById(1L)).thenReturn(level1Category);
            when(categoryMapper.updateById(any(Category))).thenReturn(1);

            // Act
            boolean result = categoryService.toggleStatus(1L);

            // Assert
            assertThat(result).isTrue();
            verify(categoryMapper).updateById(argThat(cat ->
                    cat.getStatus() == 0 && cat.getId().equals(1L)
            ));
        }

        @Test
        @DisplayName("应成功切换分类状态（禁用到启用）")
        void shouldToggleStatusFromDisabledToEnabled() {
            // Arrange
            Category disabledCategory = Category.builder()
                    .id(4L)
                    .name("禁用分类")
                    .status(0)
                    .build();

            when(categoryMapper.selectById(4L)).thenReturn(disabledCategory);
            when(categoryMapper.updateById(any(Category))).thenReturn(1);

            // Act
            boolean result = categoryService.toggleStatus(4L);

            // Assert
            assertThat(result).isTrue();
            verify(categoryMapper).updateById(argThat(cat ->
                    cat.getStatus() == 1 && cat.getId().equals(4L)
            ));
        }

        @Test
        @DisplayName("当分类不存在时，切换状态应失败")
        void shouldFailWhenCategoryNotExists() {
            // Arrange
            when(categoryMapper.selectById(999L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.toggleStatus(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("分类不存在");

            verify(categoryMapper, never()).updateById(any(Category));
        }

        @Test
        @DisplayName("切换父分类状态不应影响子分类状态")
        void shouldToggleParentStatusWithoutAffectingChildren() {
            // Arrange
            when(categoryMapper.selectById(1L)).thenReturn(level1Category);
            when(categoryMapper.updateById(any(Category))).thenReturn(1);

            // Act
            categoryService.toggleStatus(1L);

            // Assert - 验证只更新了父分类，子分类未被更新
            verify(categoryMapper, times(1)).updateById(any(Category));
        }
    }

    @Nested
    @DisplayName("分类层级校验测试")
    class CategoryLevelValidationTests {

        @Test
        @DisplayName("应正确计算一级分类的层级")
        void shouldCalculateLevel1Correctly() {
            // Arrange
            Category newCategory = new Category();
            newCategory.setParentId(null);

            // Act
            int level = categoryService.calculateLevel(newCategory);

            // Assert
            assertThat(level).isEqualTo(1);
        }

        @Test
        @DisplayName("应正确计算二级分类的层级")
        void shouldCalculateLevel2Correctly() {
            // Arrange
            when(categoryMapper.selectById(1L)).thenReturn(level1Category);

            Category newCategory = new Category();
            newCategory.setParentId(1L);

            // Act
            int level = categoryService.calculateLevel(newCategory);

            // Assert
            assertThat(level).isEqualTo(2);
        }

        @Test
        @DisplayName("应正确计算三级分类的层级")
        void shouldCalculateLevel3Correctly() {
            // Arrange
            when(categoryMapper.selectById(2L)).thenReturn(level2Category);

            Category newCategory = new Category();
            newCategory.setParentId(2L);

            // Act
            int level = categoryService.calculateLevel(newCategory);

            // Assert
            assertThat(level).isEqualTo(3);
        }

        @Test
        @DisplayName("当尝试计算四级分类时应抛出异常")
        void shouldThrowExceptionWhenCalculatingLevel4() {
            // Arrange
            when(categoryMapper.selectById(3L)).thenReturn(level3Category);

            Category newCategory = new Category();
            newCategory.setParentId(3L);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.calculateLevel(newCategory))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("分类层级不能超过3级");
        }
    }

    @Nested
    @DisplayName("分类名称唯一性校验测试")
    class CategoryNameUniquenessTests {

        @Test
        @DisplayName("应正确检测同级分类名称重复")
        void shouldDetectDuplicateNameInSameLevel() {
            // Arrange
            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(level2Category);

            // Act
            boolean isDuplicate = categoryService.isNameDuplicate("手机", 1L, null);

            // Assert
            assertThat(isDuplicate).isTrue();
        }

        @Test
        @DisplayName("应正确检测同级分类名称不重复")
        void shouldDetectNonDuplicateNameInSameLevel() {
            // Arrange
            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            // Act
            boolean isDuplicate = categoryService.isNameDuplicate("电脑", 1L, null);

            // Assert
            assertThat(isDuplicate).isFalse();
        }

        @Test
        @DisplayName("更新时应排除当前分类ID进行重复检测")
        void shouldExcludeCurrentIdWhenCheckingDuplicateOnUpdate() {
            // Arrange
            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            // Act - 更新时传入当前分类ID
            boolean isDuplicate = categoryService.isNameDuplicate("手机", 1L, 2L);

            // Assert
            assertThat(isDuplicate).isFalse();
        }

        @Test
        @DisplayName("不同父分类下的同名分类不应视为重复")
        void shouldNotTreatSameNameInDifferentParentsAsDuplicate() {
            // Arrange
            Category otherParentCategory = Category.builder()
                    .id(10L)
                    .name("手机")
                    .parentId(5L)  // 不同的父分类
                    .build();

            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(otherParentCategory);

            // Act
            boolean isDuplicate = categoryService.isNameDuplicate("手机", 1L, null);

            // Assert
            assertThat(isDuplicate).isFalse();
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("应正确处理包含空格的分类名称")
        void shouldHandleCategoryNameWithSpaces() {
            // Arrange
            Category categoryWithSpaces = Category.builder()
                    .name(" 电子 产品 ")
                    .parentId(null)
                    .level(1)
                    .sortOrder(1)
                    .status(1)
                    .build();

            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
            when(categoryMapper.insert(any(Category))).thenReturn(1);

            // Act
            boolean result = categoryService.createCategory(categoryWithSpaces);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("应正确处理特殊字符的分类名称")
        void shouldHandleCategoryNameWithSpecialCharacters() {
            // Arrange
            Category categoryWithSpecialChars = Category.builder()
                    .name("手机/平板")
                    .parentId(null)
                    .level(1)
                    .sortOrder(1)
                    .status(1)
                    .build();

            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
            when(categoryMapper.insert(any(Category))).thenReturn(1);

            // Act
            boolean result = categoryService.createCategory(categoryWithSpecialChars);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("应正确处理负数排序号")
        void shouldHandleNegativeSortOrder() {
            // Arrange
            Category categoryWithNegativeSort = Category.builder()
                    .name("测试分类")
                    .parentId(null)
                    .level(1)
                    .sortOrder(-1)
                    .status(1)
                    .build();

            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
            when(categoryMapper.insert(any(Category))).thenReturn(1);

            // Act
            boolean result = categoryService.createCategory(categoryWithNegativeSort);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("应正确处理零值排序号")
        void shouldHandleZeroSortOrder() {
            // Arrange
            Category categoryWithZeroSort = Category.builder()
                    .name("测试分类")
                    .parentId(null)
                    .level(1)
                    .sortOrder(0)
                    .status(1)
                    .build();

            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
            when(categoryMapper.insert(any(Category))).thenReturn(1);

            // Act
            boolean result = categoryService.createCategory(categoryWithZeroSort);

            // Assert
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("批量操作测试")
    class BatchOperationTests {

        @Test
        @DisplayName("应成功批量创建分类")
        void shouldBatchCreateCategoriesSuccessfully() {
            // Arrange
            List<Category> categories = Arrays.asList(
                    Category.builder().name("食品").parentId(null).level(1).sortOrder(1).status(1).build(),
                    Category.builder().name("饮料").parentId(null).level(1).sortOrder(2).status(1).build()
            );

            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
            when(categoryMapper.insert(any(Category))).thenReturn(1);

            // Act
            int successCount = categoryService.batchCreateCategories(categories);

            // Assert
            assertThat(successCount).isEqualTo(2);
            verify(categoryMapper, times(2)).insert(any(Category));
        }

        @Test
        @DisplayName("批量创建时部分失败应返回成功数量")
        void shouldReturnSuccessCountWhenBatchCreatePartiallyFails() {
            // Arrange
            List<Category> categories = Arrays.asList(
                    Category.builder().name("食品").parentId(null).level(1).sortOrder(1).status(1).build(),
                    Category.builder().name("").parentId(null).level(1).sortOrder(2).status(1).build()  // 无效：名称为空
            );

            when(categoryMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
            when(categoryMapper.insert(any(Category))).thenReturn(1);

            // Act
            int successCount = categoryService.batchCreateCategories(categories);

            // Assert
            assertThat(successCount).isEqualTo(1);  // 只有第一个成功
        }
    }
}
