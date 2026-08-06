package com.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.inventory.common.PageResult;
import com.inventory.common.Result;
import com.inventory.dto.InboundDTO;
import com.inventory.service.InboundService;
import com.inventory.vo.InboundVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 入库单控制器
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Api(tags = "入库管理")
@RestController
@RequestMapping("/api/inbound")
public class InboundController {

    private final InboundService inboundService;

    public InboundController(InboundService inboundService) {
        this.inboundService = inboundService;
    }

    /**
     * 创建入库单
     */
    @ApiOperation("创建入库单")
    @PostMapping
    public Result<Long> create(@Validated @RequestBody InboundDTO dto) {
        log.info("创建入库单，dto={}", dto);

        Long id = inboundService.create(dto);
        return Result.ok("入库单创建成功", id);
    }

    /**
     * 获取入库单详情
     */
    @ApiOperation("获取入库单详情")
    @GetMapping("/{id}")
    public Result<InboundVO> getDetail(
            @ApiParam("入库单ID") @PathVariable Long id) {
        log.info("获取入库单详情，id={}", id);

        InboundVO vo = inboundService.getDetail(id);
        return Result.ok(vo);
    }

    /**
     * 获取入库单列表
     */
    @ApiOperation("获取入库单列表")
    @GetMapping
    public Result<PageResult<InboundVO>> page(
            @ApiParam("商品ID") @RequestParam(required = false) Long productId,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("开始日期") @RequestParam(required = false) String startDate,
            @ApiParam("结束日期") @RequestParam(required = false) String endDate,
            @ApiParam("页码") @RequestParam(defaultValue = "1") int page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") int size) {
        log.info("分页查询入库单，productId={}, status={}, startDate={}, endDate={}, page={}, size={}",
                productId, status, startDate, endDate, page, size);

        IPage<InboundVO> pageResult = inboundService.page(productId, status, startDate, endDate, page, size);
        return Result.ok(PageResult.from(pageResult));
    }

    /**
     * 更新入库单
     */
    @ApiOperation("更新入库单")
    @PutMapping("/{id}")
    public Result<Boolean> update(
            @ApiParam("入库单ID") @PathVariable Long id,
            @Validated @RequestBody InboundDTO dto) {
        log.info("更新入库单，id={}, dto={}", id, dto);

        boolean success = inboundService.update(id, dto);
        return Result.ok("入库单更新成功", success);
    }

    /**
     * 删除入库单
     */
    @ApiOperation("删除入库单")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(
            @ApiParam("入库单ID") @PathVariable Long id) {
        log.info("删除入库单，id={}", id);

        boolean success = inboundService.delete(id);
        return Result.ok("入库单删除成功", success);
    }

    /**
     * 审核入库单
     */
    @ApiOperation("审核入库单")
    @PatchMapping("/{id}/approve")
    public Result<Boolean> approve(
            @ApiParam("入库单ID") @PathVariable Long id,
            @ApiParam("审核人") @RequestParam(defaultValue = "system") String approvedBy) {
        log.info("审核入库单，id={}, approvedBy={}", id, approvedBy);

        boolean success = inboundService.approve(id, approvedBy);
        return Result.ok("入库单审核成功", success);
    }

    /**
     * 作废入库单
     */
    @ApiOperation("作废入库单")
    @PatchMapping("/{id}/void")
    public Result<Void> voidInbound(
            @ApiParam("入库单ID") @PathVariable Long id) {
        log.info("作废入库单，id={}", id);

        inboundService.voidInbound(id);
        return Result.ok("入库单作废成功", null);
    }
}
