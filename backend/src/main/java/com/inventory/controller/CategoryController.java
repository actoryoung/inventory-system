package com.inventory.controller;

import com.inventory.dto.CategoryDTO;
import com.inventory.entity.Category;
import com.inventory.service.CategoryService;
import com.inventory.vo.CategoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private CategoryService categoryService;

    /**
     * 创建分类
     */
    @ApiOperation("创建分类")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Validated @RequestBody CategoryDTO dto) {
        log.info("创建分类，dto={}", dto);
        Long id = categoryService.create(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "创建成功");
        result.put("data", id);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新分类
     */
    @ApiOperation("更新分类")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @ApiParam("分类ID") @PathVariable Long id,
            @Validated @RequestBody CategoryDTO dto) {
        log.info("更新分类，id={}, dto={}", id, dto);
        dto.setId(id);
        boolean success = categoryService.update(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        result.put("data", success);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除分类
     */
    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@ApiParam("分类ID") @PathVariable Long id) {
        log.info("删除分类，id={}", id);
        boolean success = categoryService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        result.put("data", success);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取分类详情
     */
    @ApiOperation("获取分类详情")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@ApiParam("分类ID") @PathVariable Long id) {
        log.info("获取分类详情，id={}", id);
        CategoryVO category = categoryService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", category);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取分类列表（树形结构）
     */
    @ApiOperation("获取分类树")
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getTree() {
        log.info("获取分类树");
        List<CategoryVO> tree = categoryService.getTree();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", tree);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取启用的分类树（用于商品表单选择器）
     */
    @ApiOperation("获取启用的分类树")
    @GetMapping("/tree/enabled")
    public ResponseEntity<Map<String, Object>> getEnabledTree() {
        log.info("获取启用的分类树");
        List<CategoryVO> tree = categoryService.getEnabledTree();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", tree);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取分类列表（平铺）
     */
    @ApiOperation("获取分类列表")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getList(
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

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取子分类
     */
    @ApiOperation("获取子分类")
    @GetMapping("/children/{parentId}")
    public ResponseEntity<Map<String, Object>> getChildren(
            @ApiParam("父分类ID") @PathVariable Long parentId) {
        log.info("获取子分类，parentId={}", parentId);
        List<CategoryVO> children = categoryService.getChildren(parentId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", children);
        return ResponseEntity.ok(result);
    }

    /**
     * 切换分类状态
     */
    @ApiOperation("切换分类状态")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleStatus(
            @ApiParam("分类ID") @PathVariable Long id,
            @ApiParam("状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("切换分类状态，id={}, status={}", id, status);
        boolean success = categoryService.toggleStatus(id, status);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "状态更新成功");
        result.put("data", success);
        return ResponseEntity.ok(result);
    }

    /**
     * 检查分类名称是否重复
     */
    @ApiOperation("检查分类名称是否重复")
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkNameDuplicate(
            @ApiParam("分类名称") @RequestParam String name,
            @ApiParam("父分类ID") @RequestParam(required = false) Long parentId,
            @ApiParam("排除的分类ID") @RequestParam(required = false) Long excludeId) {
        log.info("检查分类名称，name={}, parentId={}, excludeId={}", name, parentId, excludeId);
        boolean duplicate = categoryService.isNameDuplicate(name, parentId, excludeId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", duplicate);
        return ResponseEntity.ok(result);
    }

    /**
     * 检查是否可以删除分类
     */
    @ApiOperation("检查是否可以删除分类")
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<Map<String, Object>> canDelete(@ApiParam("分类ID") @PathVariable Long id) {
        log.info("检查是否可以删除分类，id={}", id);
        boolean canDelete = categoryService.canDelete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", canDelete);
        return ResponseEntity.ok(result);
    }
}
