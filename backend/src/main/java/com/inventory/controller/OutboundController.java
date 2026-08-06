package com.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.common.PageResult;
import com.inventory.common.Result;
import com.inventory.dto.OutboundDTO;
import com.inventory.service.OutboundService;
import com.inventory.vo.OutboundVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 出库单控制器
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Api(tags = "出库管理")
@RestController
@RequestMapping("/api/outbound")
public class OutboundController {

    private final OutboundService outboundService;

    public OutboundController(OutboundService outboundService) {
        this.outboundService = outboundService;
    }

    /**
     * 创建出库单
     */
    @ApiOperation("创建出库单")
    @PostMapping
    public Result<Long> create(@Validated @RequestBody OutboundDTO dto) {
        log.info("创建出库单，dto={}", dto);

        Long id = outboundService.create(dto);
        return Result.ok("出库单创建成功", id);
    }

    /**
     * 获取出库单详情
     */
    @ApiOperation("获取出库单详情")
    @GetMapping("/{id}")
    public Result<OutboundVO> getDetail(
            @ApiParam("出库单ID") @PathVariable Long id) {
        log.info("获取出库单详情，id={}", id);

        OutboundVO vo = outboundService.getDetail(id);
        return Result.ok(vo);
    }

    /**
     * 获取出库单列表
     */
    @ApiOperation("获取出库单列表")
    @GetMapping
    public Result<PageResult<OutboundVO>> page(
            @ApiParam("商品ID") @RequestParam(required = false) Long productId,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("开始日期") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(required = false) String endDate,
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询出库单，productId={}, status={}, startDate={}, endDate={}, page={}, size={}",
                productId, status, startDate, endDate, page, size);

        IPage<OutboundVO> pageResult = outboundService.page(productId, status, startDate, endDate, page, size);
        return Result.ok(PageResult.from(pageResult));
    }

    /**
     * 更新出库单
     */
    @ApiOperation("更新出库单")
    @PutMapping("/{id}")
    public Result<Boolean> update(
            @ApiParam("出库单ID") @PathVariable Long id,
            @Validated @RequestBody OutboundDTO dto) {
        log.info("更新出库单，id={}, dto={}", id, dto);

        boolean success = outboundService.update(id, dto);
        return Result.ok("出库单更新成功", success);
    }

    /**
     * 删除出库单
     */
    @ApiOperation("删除出库单")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(
            @ApiParam("出库单ID") @PathVariable Long id) {
        log.info("删除出库单，id={}", id);

        boolean success = outboundService.delete(id);
        return Result.ok("出库单删除成功", success);
    }

    /**
     * 审核出库单
     */
    @ApiOperation("审核出库单")
    @PatchMapping("/{id}/approve")
    public Result<Boolean> approve(
            @ApiParam("出库单ID") @PathVariable Long id,
            @ApiParam("审核人") @RequestParam(defaultValue = "system") String approvedBy) {
        log.info("审核出库单，id={}, approvedBy={}", id, approvedBy);

        boolean success = outboundService.approve(id, approvedBy);
        return Result.ok("出库单审核成功", success);
    }

    /**
     * 作废出库单
     */
    @ApiOperation("作废出库单")
    @PatchMapping("/{id}/void")
    public Result<Void> voidOutbound(
            @ApiParam("出库单ID") @PathVariable Long id) {
        log.info("作废出库单，id={}", id);

        outboundService.voidOutbound(id);
        return Result.ok("出库单作废成功", null);
    }
}
