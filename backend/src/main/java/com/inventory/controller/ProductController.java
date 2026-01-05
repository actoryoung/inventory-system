package com.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.dto.ProductDTO;
import com.inventory.service.ProductService;
import com.inventory.vo.ProductVO;
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
 * 商品控制器
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 创建商品
     */
    @ApiOperation("创建商品")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Validated @RequestBody ProductDTO dto) {
        log.info("创建商品，dto={}", dto);
        Long id = productService.create(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "创建成功");
        result.put("data", id);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新商品
     */
    @ApiOperation("更新商品")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @ApiParam("商品ID") @PathVariable Long id,
            @Validated @RequestBody ProductDTO dto) {
        log.info("更新商品，id={}, dto={}", id, dto);
        dto.setId(id);
        boolean success = productService.update(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");
        result.put("data", success);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除商品
     */
    @ApiOperation("删除商品")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@ApiParam("商品ID") @PathVariable Long id) {
        log.info("删除商品，id={}", id);
        boolean success = productService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "删除成功");
        result.put("data", success);
        return ResponseEntity.ok(result);
    }

    /**
     * 批量删除商品
     */
    @ApiOperation("批量删除商品")
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDelete(
            @RequestBody List<Long> ids) {
        log.info("批量删除商品，ids={}", ids);
        int count = productService.batchDelete(ids);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "批量删除成功");
        result.put("data", count);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取商品详情
     */
    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@ApiParam("商品ID") @PathVariable Long id) {
        log.info("获取商品详情，id={}", id);
        ProductVO product = productService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", product);
        return ResponseEntity.ok(result);
    }

    /**
     * 分页查询商品列表
     */
    @ApiOperation("分页查询商品列表")
    @GetMapping
    public ResponseEntity<Map<String, Object>> page(
            @ApiParam("商品名称") @RequestParam(required = false) String name,
            @ApiParam("SKU") @RequestParam(required = false) String sku,
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询商品，name={}, sku={}, categoryId={}, status={}, page={}, size={}",
                name, sku, categoryId, status, page, size);

        IPage<ProductVO> pageResult = productService.page(name, sku, categoryId, status, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", Map.of(
                "records", pageResult.getRecords(),
                "total", pageResult.getTotal(),
                "page", pageResult.getCurrent(),
                "size", pageResult.getSize()
        ));
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索商品
     */
    @ApiOperation("搜索商品")
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @ApiParam("关键词") @RequestParam String keyword) {
        log.info("搜索商品，keyword={}", keyword);
        List<ProductVO> list = productService.search(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    /**
     * 切换商品状态
     */
    @ApiOperation("切换商品状态")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleStatus(
            @ApiParam("商品ID") @PathVariable Long id,
            @ApiParam("状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("切换商品状态，id={}, status={}", id, status);
        boolean success = productService.toggleStatus(id, status);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "状态更新成功");
        result.put("data", success);
        return ResponseEntity.ok(result);
    }

    /**
     * 检查SKU是否存在
     */
    @ApiOperation("检查SKU是否存在")
    @GetMapping("/check-sku")
    public ResponseEntity<Map<String, Object>> checkSku(
            @ApiParam("SKU") @RequestParam String sku,
            @ApiParam("排除的商品ID") @RequestParam(required = false) Long excludeId) {
        log.info("检查SKU，sku={}, excludeId={}", sku, excludeId);
        boolean exists = productService.checkSkuExists(sku, excludeId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", exists);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取低库存商品列表
     */
    @ApiOperation("获取低库存商品列表")
    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockProducts() {
        log.info("获取低库存商品列表");
        List<ProductVO> list = productService.getLowStockProducts();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", list);
        return ResponseEntity.ok(result);
    }
}
