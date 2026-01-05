package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.entity.Category;
import com.inventory.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 商品分类控制器集成测试
 *
 * 测试范围:
 * - REST API 端点测试
 * - 请求/响应格式验证
 * - 错误码验证
 * - HTTP 状态码验证
 * - 数据库事务测试
 *
 * @author Claude Code
 * @since 2026-01-04
 */
@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
@DisplayName("商品分类控制器集成测试")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private Category level1Category;
    private Category level2Category;
    private Category level3Category;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        LocalDateTime now = LocalDateTime.now();

        level1Category = Category.builder()
                .id(1L)
                .name("电子产品")
                .parentId(null)
                .level(1)
                .sortOrder(1)
                .status(1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        level2Category = Category.builder()
                .id(2L)
                .name("手机")
                .parentId(1L)
                .level(2)
                .sortOrder(1)
                .status(1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        level3Category = Category.builder()
                .id(3L)
                .name("智能手机")
                .parentId(2L)
                .level(3)
                .sortOrder(1)
                .status(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Nested
    @DisplayName("POST /api/categories - 创建分类")
    class CreateCategoryEndpointTests {

        @Test
        @DisplayName("应成功创建一级分类并返回201")
        void shouldCreateLevel1CategoryAndReturn201() throws Exception {
            // Arrange
            when(categoryService.createCategory(any(Category.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(level1Category)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("创建成功"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("电子产品"))
                    .andExpect(jsonPath("$.data.level").value(1))
                    .andExpect(jsonPath("$.data.parentId").isEmpty());

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("应成功创建二级分类并返回201")
        void shouldCreateLevel2CategoryAndReturn201() throws Exception {
            // Arrange
            when(categoryService.createCategory(any(Category.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(level2Category)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("创建成功"))
                    .andExpect(jsonPath("$.data.level").value(2))
                    .andExpect(jsonPath("$.data.parentId").value(1));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("当分类名称为空时应返回400")
        void shouldReturn400WhenNameIsEmpty() throws Exception {
            // Arrange
            Category invalidCategory = Category.builder()
                    .name("")
                    .parentId(null)
                    .level(1)
                    .build();

            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new IllegalArgumentException("分类名称不能为空"));

            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidCategory)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("分类名称不能为空"));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("当分类层级超过3级时应返回400")
        void shouldReturn400WhenLevelExceeds3() throws Exception {
            // Arrange
            Category invalidCategory = Category.builder()
                    .name("四级分类")
                    .parentId(3L)
                    .level(4)
                    .build();

            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new IllegalArgumentException("分类层级不能超过3级"));

            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidCategory)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("分类层级不能超过3级"));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("当同级分类名称重复时应返回400")
        void shouldReturn400WhenNameDuplicates() throws Exception {
            // Arrange
            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new IllegalArgumentException("同一父分类下已存在同名分类"));

            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(level2Category)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("同一父分类下已存在同名分类"));

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("当请求体为空时应返回400")
        void shouldReturn400WhenRequestBodyIsEmpty() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("当Content-Type不是JSON时应返回415")
        void shouldReturn415WhenContentTypeIsNotJson() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("electronics"))
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("PUT /api/categories/{id} - 更新分类")
    class UpdateCategoryEndpointTests {

        @Test
        @DisplayName("应成功更新分类并返回200")
        void shouldUpdateCategoryAndReturn200() throws Exception {
            // Arrange
            Category updateCategory = Category.builder()
                    .id(1L)
                    .name("电子产品（更新）")
                    .parentId(null)
                    .level(1)
                    .sortOrder(1)
                    .status(1)
                    .build();

            when(categoryService.updateCategory(any(Category.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateCategory)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("更新成功"));

            verify(categoryService).updateCategory(any(Category.class));
        }

        @Test
        @DisplayName("当分类不存在时应返回404")
        void shouldReturn404WhenCategoryNotExists() throws Exception {
            // Arrange
            Category updateCategory = Category.builder()
                    .id(999L)
                    .name("不存在的分类")
                    .build();

            when(categoryService.updateCategory(any(Category.class)))
                    .thenThrow(new IllegalArgumentException("分类不存在"));

            // Act & Assert
            mockMvc.perform(put("/api/categories/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateCategory)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("分类不存在"));

            verify(categoryService).updateCategory(any(Category.class));
        }

        @Test
        @DisplayName("当更新后名称重复时应返回400")
        void shouldReturn400WhenUpdatedNameDuplicates() throws Exception {
            // Arrange
            Category updateCategory = Category.builder()
                    .id(1L)
                    .name("手机")  // 与其他同级分类重复
                    .parentId(1L)
                    .level(2)
                    .build();

            when(categoryService.updateCategory(any(Category.class)))
                    .thenThrow(new IllegalArgumentException("同一父分类下已存在同名分类"));

            // Act & Assert
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateCategory)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("同一父分类下已存在同名分类"));

            verify(categoryService).updateCategory(any(Category.class));
        }

        @Test
        @DisplayName("当路径ID与请求体ID不一致时应返回400")
        void shouldReturn400WhenPathIdNotMatchBodyId() throws Exception {
            // Arrange
            Category updateCategory = Category.builder()
                    .id(2L)  // 与路径ID不一致
                    .name("电子产品")
                    .build();

            // Act & Assert
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateCategory)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("路径ID与请求体ID不一致"));

            verify(categoryService, never()).updateCategory(any(Category.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/categories/{id} - 删除分类")
    class DeleteCategoryEndpointTests {

        @Test
        @DisplayName("应成功删除分类并返回200")
        void shouldDeleteCategoryAndReturn200() throws Exception {
            // Arrange
            when(categoryService.deleteCategory(1L)).thenReturn(true);

            // Act & Assert
            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("删除成功"));

            verify(categoryService).deleteCategory(1L);
        }

        @Test
        @DisplayName("当分类有关联商品时应返回400")
        void shouldReturn400WhenCategoryHasProducts() throws Exception {
            // Arrange
            when(categoryService.deleteCategory(1L))
                    .thenThrow(new IllegalStateException("该分类下有商品，无法删除"));

            // Act & Assert
            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("该分类下有商品，无法删除"));

            verify(categoryService).deleteCategory(1L);
        }

        @Test
        @DisplayName("当分类有子分类时应返回400")
        void shouldReturn400WhenCategoryHasChildren() throws Exception {
            // Arrange
            when(categoryService.deleteCategory(1L))
                    .thenThrow(new IllegalStateException("该分类下有子分类，请先删除子分类"));

            // Act & Assert
            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value("该分类下有子分类，请先删除子分类"));

            verify(categoryService).deleteCategory(1L);
        }

        @Test
        @DisplayName("当分类不存在时应返回404")
        void shouldReturn404WhenCategoryNotExists() throws Exception {
            // Arrange
            when(categoryService.deleteCategory(999L))
                    .thenThrow(new IllegalArgumentException("分类不存在"));

            // Act & Assert
            mockMvc.perform(delete("/api/categories/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("分类不存在"));

            verify(categoryService).deleteCategory(999L);
        }

        @Test
        @DisplayName("当ID格式无效时应返回400")
        void shouldReturn400WhenIdIsInvalid() throws Exception {
            // Act & Assert
            mockMvc.perform(delete("/api/categories/invalid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/categories/{id} - 获取分类详情")
    class GetCategoryByIdEndpointTests {

        @Test
        @DisplayName("应成功获取分类详情并返回200")
        void shouldGetCategoryDetailAndReturn200() throws Exception {
            // Arrange
            when(categoryService.getCategoryById(1L)).thenReturn(level1Category);

            // Act & Assert
            mockMvc.perform(get("/api/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("电子产品"))
                    .andExpect(jsonPath("$.data.level").value(1));

            verify(categoryService).getCategoryById(1L);
        }

        @Test
        @DisplayName("当分类不存在时应返回404")
        void shouldReturn404WhenCategoryNotExists() throws Exception {
            // Arrange
            when(categoryService.getCategoryById(999L)).thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/api/categories/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("分类不存在"));

            verify(categoryService).getCategoryById(999L);
        }

        @Test
        @DisplayName("当ID格式无效时应返回400")
        void shouldReturn400WhenIdIsInvalid() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/categories/invalid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/categories - 获取分类列表")
    class GetCategoryListEndpointTests {

        @Test
        @DisplayName("应成功获取分类列表并返回200")
        void shouldGetCategoryListAndReturn200() throws Exception {
            // Arrange
            List<Category> categories = Arrays.asList(level1Category, level2Category);
            when(categoryService.getCategoryList(null, null, null)).thenReturn(categories);

            // Act & Assert
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].name").value("电子产品"))
                    .andExpect(jsonPath("$.data[1].name").value("手机"));

            verify(categoryService).getCategoryList(null, null, null);
        }

        @Test
        @DisplayName("应支持按名称模糊搜索")
        void shouldSupportSearchByName() throws Exception {
            // Arrange
            List<Category> categories = Collections.singletonList(level1Category);
            when(categoryService.getCategoryList("电子", null, null)).thenReturn(categories);

            // Act & Assert
            mockMvc.perform(get("/api/categories")
                            .param("name", "电子"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].name").value("电子产品"));

            verify(categoryService).getCategoryList("电子", null, null);
        }

        @Test
        @DisplayName("应支持按状态过滤")
        void shouldSupportFilterByStatus() throws Exception {
            // Arrange
            List<Category> categories = Arrays.asList(level1Category, level2Category);
            when(categoryService.getCategoryList(null, 1, null)).thenReturn(categories);

            // Act & Assert
            mockMvc.perform(get("/api/categories")
                            .param("status", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data", hasSize(2)));

            verify(categoryService).getCategoryList(null, 1, null);
        }

        @Test
        @DisplayName("应支持按层级过滤")
        void shouldSupportFilterByLevel() throws Exception {
            // Arrange
            List<Category> categories = Collections.singletonList(level1Category);
            when(categoryService.getCategoryList(null, null, 1)).thenReturn(categories);

            // Act & Assert
            mockMvc.perform(get("/api/categories")
                            .param("level", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].level").value(1));

            verify(categoryService).getCategoryList(null, null, 1);
        }

        @Test
        @DisplayName("应支持组合查询条件")
        void shouldSupportCombinedFilters() throws Exception {
            // Arrange
            List<Category> categories = Collections.singletonList(level2Category);
            when(categoryService.getCategoryList("手机", 1, 2)).thenReturn(categories);

            // Act & Assert
            mockMvc.perform(get("/api/categories")
                            .param("name", "手机")
                            .param("status", "1")
                            .param("level", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data", hasSize(1)));

            verify(categoryService).getCategoryList("手机", 1, 2);
        }

        @Test
        @DisplayName("当列表为空时应返回空数组")
        void shouldReturnEmptyArrayWhenNoCategories() throws Exception {
            // Arrange
            when(categoryService.getCategoryList(anyString(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/categories")
                            .param("name", "不存在的分类"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("当状态参数格式无效时应返回400")
        void shouldReturn400WhenStatusIsInvalid() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/categories")
                            .param("status", "invalid"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("当层级参数格式无效时应返回400")
        void shouldReturn400WhenLevelIsInvalid() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/categories")
                            .param("level", "invalid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/categories/tree - 获取分类树")
    class GetCategoryTreeEndpointTests {

        @Test
        @DisplayName("应成功获取分类树并返回200")
        void shouldGetCategoryTreeAndReturn200() throws Exception {
            // Arrange
            level1Category.setChildren(Arrays.asList(level2Category));
            level2Category.setChildren(Arrays.asList(level3Category));
            level3Category.setChildren(Collections.emptyList());

            List<Category> tree = Arrays.asList(level1Category);
            when(categoryService.getCategoryTree()).thenReturn(tree);

            // Act & Assert
            mockMvc.perform(get("/api/categories/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].name").value("电子产品"))
                    .andExpect(jsonPath("$.data[0].children").isArray())
                    .andExpect(jsonPath("$.data[0].children", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].children[0].name").value("手机"))
                    .andExpect(jsonPath("$.data[0].children[0].children[0].name").value("智能手机"));

            verify(categoryService).getCategoryTree();
        }

        @Test
        @DisplayName("当树为空时应返回空数组")
        void shouldReturnEmptyArrayWhenTreeIsEmpty() throws Exception {
            // Arrange
            when(categoryService.getCategoryTree()).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/categories/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("PATCH /api/categories/{id}/status - 切换分类状态")
    class ToggleCategoryStatusEndpointTests {

        @Test
        @DisplayName("应成功切换分类状态并返回200")
        void shouldToggleStatusAndReturn200() throws Exception {
            // Arrange
            when(categoryService.toggleStatus(1L)).thenReturn(true);

            // Act & Assert
            mockMvc.perform(patch("/api/categories/1/status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("状态更新成功"));

            verify(categoryService).toggleStatus(1L);
        }

        @Test
        @DisplayName("当分类不存在时应返回404")
        void shouldReturn404WhenCategoryNotExists() throws Exception {
            // Arrange
            when(categoryService.toggleStatus(999L))
                    .thenThrow(new IllegalArgumentException("分类不存在"));

            // Act & Assert
            mockMvc.perform(patch("/api/categories/999/status"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("分类不存在"));

            verify(categoryService).toggleStatus(999L);
        }

        @Test
        @DisplayName("当ID格式无效时应返回400")
        void shouldReturn400WhenIdIsInvalid() throws Exception {
            // Act & Assert
            mockMvc.perform(patch("/api/categories/invalid/status"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("请求响应格式验证")
    class RequestResponseFormatTests {

        @Test
        @DisplayName("成功响应应包含标准格式")
        void shouldReturnStandardSuccessFormat() throws Exception {
            // Arrange
            when(categoryService.getCategoryList(null, null, null))
                    .thenReturn(Arrays.asList(level1Category));

            // Act & Assert
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("错误响应应包含标准格式")
        void shouldReturnStandardErrorFormat() throws Exception {
            // Arrange
            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new IllegalArgumentException("分类名称不能为空"));

            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("响应数据应包含所有必要字段")
        void shouldIncludeAllRequiredFieldsInResponse() throws Exception {
            // Arrange
            when(categoryService.getCategoryById(1L)).thenReturn(level1Category);

            // Act & Assert
            mockMvc.perform(get("/api/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.name").exists())
                    .andExpect(jsonPath("$.data.level").exists())
                    .andExpect(jsonPath("$.data.status").exists())
                    .andExpect(jsonPath("$.data.createdAt").exists())
                    .andExpect(jsonPath("$.data.updatedAt").exists());
        }

        @Test
        @DisplayName("日期时间格式应为ISO 8601")
        void shouldFormatDateTimeAsISO8601() throws Exception {
            // Arrange
            when(categoryService.getCategoryById(1L)).thenReturn(level1Category);

            // Act & Assert
            mockMvc.perform(get("/api/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.createdAt").isString())
                    .andExpect(jsonPath("$.data.updatedAt").isString());
        }
    }

    @Nested
    @DisplayName("数据库事务测试")
    class TransactionTests {

        @Test
        @DisplayName("创建失败时应回滚事务")
        void shouldRollbackWhenCreateFails() throws Exception {
            // Arrange
            when(categoryService.createCategory(any(Category.class)))
                    .thenThrow(new RuntimeException("数据库错误"));

            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(level1Category)))
                    .andExpect(status().isInternalServerError());

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("更新失败时应回滚事务")
        void shouldRollbackWhenUpdateFails() throws Exception {
            // Arrange
            when(categoryService.updateCategory(any(Category.class)))
                    .thenThrow(new RuntimeException("数据库错误"));

            // Act & Assert
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(level1Category)))
                    .andExpect(status().isInternalServerError());

            verify(categoryService).updateCategory(any(Category.class));
        }

        @Test
        @DisplayName("删除失败时应回滚事务")
        void shouldRollbackWhenDeleteFails() throws Exception {
            // Arrange
            when(categoryService.deleteCategory(1L))
                    .thenThrow(new RuntimeException("数据库错误"));

            // Act & Assert
            mockMvc.perform(delete("/api/categories/1"))
                    .andExpect(status().isInternalServerError());

            verify(categoryService).deleteCategory(1L);
        }
    }

    @Nested
    @DisplayName("并发请求测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("应正确处理并发创建请求")
        void shouldHandleConcurrentCreateRequests() throws Exception {
            // Arrange
            when(categoryService.createCategory(any(Category.class))).thenReturn(true);

            // Act & Assert - 模拟并发请求
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(level1Category)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(level2Category)))
                    .andExpect(status().isCreated());

            verify(categoryService, times(2)).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("应正确处理并发更新请求")
        void shouldHandleConcurrentUpdateRequests() throws Exception {
            // Arrange
            when(categoryService.updateCategory(any(Category.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(put("/api/categories/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(level1Category)))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/categories/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(level2Category)))
                    .andExpect(status().isOk());

            verify(categoryService, times(2)).updateCategory(any(Category.class));
        }
    }

    @Nested
    @DisplayName("安全性测试")
    class SecurityTests {

        @Test
        @DisplayName("应防止SQL注入攻击")
        void shouldPreventSQLInjection() throws Exception {
            // Arrange
            when(categoryService.getCategoryList(anyString(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            // Act & Assert - 尝试SQL注入
            mockMvc.perform(get("/api/categories")
                            .param("name", "'; DROP TABLE t_category; --"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());

            verify(categoryService).getCategoryList(contains("DROP"), anyInt(), anyInt());
        }

        @Test
        @DisplayName("应防止XSS攻击")
        void shouldPreventXSSAttack() throws Exception {
            // Arrange
            Category xssCategory = Category.builder()
                    .name("<script>alert('XSS')</script>")
                    .parentId(null)
                    .level(1)
                    .build();

            when(categoryService.createCategory(any(Category.class))).thenReturn(true);

            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(xssCategory)))
                    .andExpect(status().isCreated());

            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        @DisplayName("应验证请求体大小限制")
        void shouldValidateRequestSizeLimit() throws Exception {
            // Arrange - 创建超大请求体
            String hugeName = String.join("", Collections.nCopies(10000, "a"));
            Category hugeCategory = Category.builder()
                    .name(hugeName)
                    .parentId(null)
                    .level(1)
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(hugeCategory)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {

        @Test
        @DisplayName("分类列表查询应在合理时间内完成")
        void shouldReturnCategoryListQuickly() throws Exception {
            // Arrange
            List<Category> largeList = Arrays.asList(
                    level1Category, level2Category, level3Category
            );
            when(categoryService.getCategoryList(null, null, null)).thenReturn(largeList);

            long startTime = System.currentTimeMillis();

            // Act & Assert
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk());

            long duration = System.currentTimeMillis() - startTime;

            // Assert - 响应时间应小于200ms
            assertThat(duration).isLessThan(200);
        }

        @Test
        @DisplayName("分类树查询应在合理时间内完成")
        void shouldReturnCategoryTreeQuickly() throws Exception {
            // Arrange
            level1Category.setChildren(Arrays.asList(level2Category));
            List<Category> tree = Arrays.asList(level1Category);
            when(categoryService.getCategoryTree()).thenReturn(tree);

            long startTime = System.currentTimeMillis();

            // Act & Assert
            mockMvc.perform(get("/api/categories/tree"))
                    .andExpect(status().isOk());

            long duration = System.currentTimeMillis() - startTime;

            // Assert - 响应时间应小于500ms
            assertThat(duration).isLessThan(500);
        }
    }
}
