package com.inventory.controller;

import com.inventory.common.Result;
import com.inventory.service.StatisticsService;
import com.inventory.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计报表控制器
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Api(tags = "统计报表")
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取数据看板
     */
    @ApiOperation("获取数据看板")
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        log.info("获取数据看板");

        DashboardVO dashboard = statisticsService.getDashboard();
        return Result.ok(dashboard);
    }

    /**
     * 获取出入库趋势
     */
    @ApiOperation("获取出入库趋势")
    @GetMapping("/trend")
    public Result<TrendVO> getTrend(
            @ApiParam("天数") @RequestParam(defaultValue = "30") int days) {
        log.info("获取出入库趋势，days={}", days);

        TrendVO trend = statisticsService.getTrend(days);
        return Result.ok(trend);
    }

    /**
     * 获取库存分类分布
     */
    @ApiOperation("获取库存分类分布")
    @GetMapping("/category-distribution")
    public Result<List<CategoryDistributionVO>> getCategoryDistribution() {
        log.info("获取库存分类分布");

        List<CategoryDistributionVO> distribution = statisticsService.getCategoryDistribution();
        return Result.ok(distribution);
    }

    /**
     * 获取低库存列表
     */
    @ApiOperation("获取低库存列表")
    @GetMapping("/low-stock")
    public Result<List<LowStockVO>> getLowStockList() {
        log.info("获取低库存列表");

        List<LowStockVO> lowStockList = statisticsService.getLowStockList();
        return Result.ok(lowStockList);
    }
}
