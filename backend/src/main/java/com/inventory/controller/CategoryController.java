package com.inventory.controller;

import com.inventory.common.Result;
import com.inventory.dto.CategoryDTO;
import com.inventory.service.CategoryService;
import com.inventory.vo.CategoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Api(tags = "商品分类管理")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 创建分类
     */
    @ApiOperation("创建分类")
    @PostMapping
    public Result<Long> create(@Validated @RequestBody CategoryDTO dto) {
        log.info("创建分类，dto={}", dto);
        Long id = categoryService.create(dto);
        return Result.ok("创建成功", id);
    }

    /**
     * 更新分类
     */
    @ApiOperation("更新分类")
    @PutMapping("/{id}")
    public Result<Boolean> update(
            @ApiParam("分类ID") @PathVariable Long id,
            @Validated @RequestBody CategoryDTO dto) {
        log.info("更新分类，id={}, dto={}", id, dto);
        dto.setId(id);
        boolean success = categoryService.update(dto);
        return Result.ok("更新成功", success);
    }

    /**
     * 删除分类
     */
    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@ApiParam("分类ID") @PathVariable Long id) {
        log.info("删除分类，id={}", id);
        boolean success = categoryService.delete(id);
        return Result.ok("删除成功", success);
    }

    /**
     * 获取分类详情
     */
    @ApiOperation("获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> getById(@ApiParam("分类ID") @PathVariable Long id) {
        log.info("获取分类详情，id={}", id);
        CategoryVO category = categoryService.getById(id);
        return Result.ok(category);
    }

    /**
     * 获取分类列表（树形结构）
     */
    @ApiOperation("获取分类树")
    @GetMapping("/tree")
    public Result<List<CategoryVO>> getTree() {
        log.info("获取分类树");
        List<CategoryVO> tree = categoryService.getTree();
        return Result.ok(tree);
    }

    /**
     * 获取启用的分类树（用于商品表单选择器）
     */
    @ApiOperation("获取启用的分类树")
    @GetMapping("/tree/enabled")
    public Result<List<CategoryVO>> getEnabledTree() {
        log.info("获取启用的分类树");
        List<CategoryVO> tree = categoryService.getEnabledTree();
        return Result.ok(tree);
    }

    /**
     * 获取分类列表（平铺）
     */
    @ApiOperation("获取分类列表")
    @GetMapping
    public Result<List<CategoryVO>> getList(
            @ApiParam("分类名称（模糊搜索）") @RequestParam(required = false) String name,
            @ApiParam("层级") @RequestParam(required = false) Integer level,
            @ApiParam("状态") @RequestParam(required = false) Integer status) {
        log.info("获取分类列表，name={}, level={}, status={}", name, level, status);

        List<CategoryVO> list;
        if (name != null && !name.isEmpty()) {
            list = categoryService.searchByName(name);
        } else if (level != null) {
            list = categoryService.getByLevel(level);
        } else {
            list = categoryService.getList();
        }

        return Result.ok(list);
    }

    /**
     * 获取子分类
     */
    @ApiOperation("获取子分类")
    @GetMapping("/children/{parentId}")
    public Result<List<CategoryVO>> getChildren(
            @ApiParam("父分类ID") @PathVariable Long parentId) {
        log.info("获取子分类，parentId={}", parentId);
        List<CategoryVO> children = categoryService.getChildren(parentId);
        return Result.ok(children);
    }

    /**
     * 切换分类状态
     */
    @ApiOperation("切换分类状态")
    @PatchMapping("/{id}/status")
    public Result<Boolean> toggleStatus(
            @ApiParam("分类ID") @PathVariable Long id,
            @ApiParam("状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("切换分类状态，id={}, status={}", id, status);
        boolean success = categoryService.toggleStatus(id, status);
        return Result.ok("状态更新成功", success);
    }

    /**
     * 检查分类名称是否重复
     */
    @ApiOperation("检查分类名称是否重复")
    @GetMapping("/check-name")
    public Result<Boolean> checkNameDuplicate(
            @ApiParam("分类名称") @RequestParam String name,
            @ApiParam("父分类ID") @RequestParam(required = false) Long parentId,
            @ApiParam("排除的分类ID") @RequestParam(required = false) Long excludeId) {
        log.info("检查分类名称，name={}, parentId={}, excludeId={}", name, parentId, excludeId);
        boolean duplicate = categoryService.isNameDuplicate(name, parentId, excludeId);
        return Result.ok(duplicate);
    }

    /**
     * 检查是否可以删除分类
     */
    @ApiOperation("检查是否可以删除分类")
    @GetMapping("/{id}/can-delete")
    public Result<Boolean> canDelete(@ApiParam("分类ID") @PathVariable Long id) {
        log.info("检查是否可以删除分类，id={}", id);
        boolean canDelete = categoryService.canDelete(id);
        return Result.ok(canDelete);
    }
}
