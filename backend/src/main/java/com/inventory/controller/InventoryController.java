package com.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.dto.InventoryAdjustDTO;
import com.inventory.service.InventoryService;
import com.inventory.vo.InventoryVO;
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

    @Autowired
    private InventoryService inventoryService;

    /**
     * 获取库存列表（分页）
     */
    @ApiOperation("获取库存列表")
    @GetMapping
    public ResponseEntity<Map<String, Object>> page(
            @ApiParam("商品名称") @RequestParam(required = false) String productName,
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("是否只查低库存") @RequestParam(required = false) Boolean lowStock,
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询库存，productName={}, categoryId={}, lowStock={}, page={}, size={}",
                productName, categoryId, lowStock, page, size);

        IPage<InventoryVO> pageResult = inventoryService.page(productName, categoryId, lowStock, page, size);

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
     * 获取商品库存
     */
    @ApiOperation("获取商品库存")
    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getByProductId(
            @ApiParam("商品ID") @PathVariable Long productId) {
        log.info("获取商品库存，productId={}", productId);

        var inventory = inventoryService.getByProductId(productId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", inventory != null ? InventoryVO.fromEntity(inventory) : null);
        return ResponseEntity.ok(result);
    }

    /**
     * 调整库存
     */
    @ApiOperation("调整库存")
    @PutMapping("/{id}/adjust")
    public ResponseEntity<Map<String, Object>> adjustInventory(
            @ApiParam("库存ID") @PathVariable Long id,
            @Validated @RequestBody InventoryAdjustDTO dto) {
        log.info("调整库存，id={}, dto={}", id, dto);

        Map<String, Object> data = inventoryService.adjustInventory(id, dto);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "库存调整成功");
        result.put("data", data);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取低库存商品列表
     */
    @ApiOperation("获取低库存商品列表")
    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockList() {
        log.info("获取低库存商品列表");

        List<InventoryVO> list = inventoryService.getLowStockList();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", list);
        return ResponseEntity.ok(result);
    }

    /**
     * 检查库存是否充足
     */
    @ApiOperation("检查库存是否充足")
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkStock(
            @ApiParam("商品ID") @RequestParam Long productId,
            @ApiParam("需要数量") @RequestParam Integer quantity) {
        log.info("检查库存充足性，productId={}, quantity={}", productId, quantity);

        boolean sufficient = inventoryService.checkStock(productId, quantity);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", sufficient);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取库存汇总统计
     */
    @ApiOperation("获取库存汇总统计")
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        log.info("获取库存汇总统计");

        Map<String, Object> summary = inventoryService.getSummary();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", summary);
        return ResponseEntity.ok(result);
    }
}
