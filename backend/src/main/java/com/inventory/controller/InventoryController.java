package com.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.common.PageResult;
import com.inventory.common.Result;
import com.inventory.dto.InventoryAdjustDTO;
import com.inventory.entity.Inventory;
import com.inventory.service.InventoryService;
import com.inventory.vo.InventoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 库存控制器
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Api(tags = "库存管理")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * 获取库存列表（分页）
     */
    @ApiOperation("获取库存列表")
    @GetMapping
    public Result<PageResult<InventoryVO>> page(
            @ApiParam("商品名称") @RequestParam(required = false) String productName,
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("是否只查低库存") @RequestParam(required = false) Boolean lowStock,
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询库存，productName={}, categoryId={}, lowStock={}, page={}, size={}",
                productName, categoryId, lowStock, page, size);

        IPage<InventoryVO> pageResult = inventoryService.page(productName, categoryId, lowStock, page, size);
        return Result.ok(PageResult.from(pageResult));
    }

    /**
     * 获取商品库存
     */
    @ApiOperation("获取商品库存")
    @GetMapping("/product/{productId}")
    public Result<InventoryVO> getByProductId(
            @ApiParam("商品ID") @PathVariable Long productId) {
        log.info("获取商品库存，productId={}", productId);

        Inventory inventory = inventoryService.getByProductId(productId);
        return Result.ok(inventory != null ? InventoryVO.fromEntity(inventory) : null);
    }

    /**
     * 调整库存
     */
    @ApiOperation("调整库存")
    @PutMapping("/{id}/adjust")
    public Result<Map<String, Object>> adjustInventory(
            @ApiParam("库存ID") @PathVariable Long id,
            @Validated @RequestBody InventoryAdjustDTO dto) {
        log.info("调整库存，id={}, dto={}", id, dto);

        Map<String, Object> data = inventoryService.adjustInventory(id, dto);
        return Result.ok("库存调整成功", data);
    }

    /**
     * 获取低库存商品列表
     */
    @ApiOperation("获取低库存商品列表")
    @GetMapping("/low-stock")
    public Result<List<InventoryVO>> getLowStockList() {
        log.info("获取低库存商品列表");

        List<InventoryVO> list = inventoryService.getLowStockList();
        return Result.ok(list);
    }

    /**
     * 检查库存是否充足
     */
    @ApiOperation("检查库存是否充足")
    @PostMapping("/check")
    public Result<Boolean> checkStock(
            @ApiParam("商品ID") @RequestParam Long productId,
            @ApiParam("需要数量") @RequestParam Integer quantity) {
        log.info("检查库存充足性，productId={}, quantity={}", productId, quantity);

        boolean sufficient = inventoryService.checkStock(productId, quantity);
        return Result.ok(sufficient);
    }

    /**
     * 获取库存汇总统计
     */
    @ApiOperation("获取库存汇总统计")
    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary() {
        log.info("获取库存汇总统计");

        Map<String, Object> summary = inventoryService.getSummary();
        return Result.ok(summary);
    }
}
