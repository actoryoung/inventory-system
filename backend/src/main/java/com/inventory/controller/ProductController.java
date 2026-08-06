package com.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.common.PageResult;
import com.inventory.common.Result;
import com.inventory.dto.ProductDTO;
import com.inventory.service.ProductService;
import com.inventory.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 创建商品
     */
    @ApiOperation("创建商品")
    @PostMapping
    public Result<Long> create(@Validated @RequestBody ProductDTO dto) {
        log.info("创建商品，dto={}", dto);
        Long id = productService.create(dto);
        return Result.ok("创建成功", id);
    }

    /**
     * 更新商品
     */
    @ApiOperation("更新商品")
    @PutMapping("/{id}")
    public Result<Boolean> update(
            @ApiParam("商品ID") @PathVariable Long id,
            @Validated @RequestBody ProductDTO dto) {
        log.info("更新商品，id={}, dto={}", id, dto);
        dto.setId(id);
        boolean success = productService.update(dto);
        return Result.ok("更新成功", success);
    }

    /**
     * 删除商品
     */
    @ApiOperation("删除商品")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@ApiParam("商品ID") @PathVariable Long id) {
        log.info("删除商品，id={}", id);
        boolean success = productService.delete(id);
        return Result.ok("删除成功", success);
    }

    /**
     * 批量删除商品
     */
    @ApiOperation("批量删除商品")
    @DeleteMapping("/batch")
    public Result<Integer> batchDelete(@RequestBody List<Long> ids) {
        log.info("批量删除商品，ids={}", ids);
        int count = productService.batchDelete(ids);
        return Result.ok("批量删除成功", count);
    }

    /**
     * 获取商品详情
     */
    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> getById(@ApiParam("商品ID") @PathVariable Long id) {
        log.info("获取商品详情，id={}", id);
        ProductVO product = productService.getById(id);
        return Result.ok(product);
    }

    /**
     * 分页查询商品列表
     */
    @ApiOperation("分页查询商品列表")
    @GetMapping
    public Result<PageResult<ProductVO>> page(
            @ApiParam("商品名称") @RequestParam(required = false) String name,
            @ApiParam("SKU") @RequestParam(required = false) String sku,
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询商品，name={}, sku={}, categoryId={}, status={}, page={}, size={}",
                name, sku, categoryId, status, page, size);

        IPage<ProductVO> pageResult = productService.page(name, sku, categoryId, status, page, size);
        return Result.ok(PageResult.from(pageResult));
    }

    /**
     * 搜索商品
     */
    @ApiOperation("搜索商品")
    @GetMapping("/search")
    public Result<List<ProductVO>> search(
            @ApiParam("关键词") @RequestParam String keyword) {
        log.info("搜索商品，keyword={}", keyword);
        List<ProductVO> list = productService.search(keyword);
        return Result.ok(list);
    }

    /**
     * 切换商品状态
     */
    @ApiOperation("切换商品状态")
    @PatchMapping("/{id}/status")
    public Result<Boolean> toggleStatus(
            @ApiParam("商品ID") @PathVariable Long id,
            @ApiParam("状态：0-禁用，1-启用") @RequestParam Integer status) {
        log.info("切换商品状态，id={}, status={}", id, status);
        boolean success = productService.toggleStatus(id, status);
        return Result.ok("状态更新成功", success);
    }

    /**
     * 检查SKU是否存在
     */
    @ApiOperation("检查SKU是否存在")
    @GetMapping("/check-sku")
    public Result<Boolean> checkSku(
            @ApiParam("SKU") @RequestParam String sku,
            @ApiParam("排除的商品ID") @RequestParam(required = false) Long excludeId) {
        log.info("检查SKU，sku={}, excludeId={}", sku, excludeId);
        boolean exists = productService.checkSkuExists(sku, excludeId);
        return Result.ok(exists);
    }

    /**
     * 获取低库存商品列表
     */
    @ApiOperation("获取低库存商品列表")
    @GetMapping("/low-stock")
    public Result<List<ProductVO>> getLowStockProducts() {
        log.info("获取低库存商品列表");
        List<ProductVO> list = productService.getLowStockProducts();
        return Result.ok(list);
    }
}
