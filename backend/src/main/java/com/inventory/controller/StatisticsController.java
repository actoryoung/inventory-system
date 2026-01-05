package com.inventory.controller;

import com.inventory.service.StatisticsService;
import com.inventory.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取数据看板
     */
    @ApiOperation("获取数据看板")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        log.info("获取数据看板");

        DashboardVO dashboard = statisticsService.getDashboard();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", dashboard);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取出入库趋势
     */
    @ApiOperation("获取出入库趋势")
    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getTrend(
            @ApiParam("天数") @RequestParam(defaultValue = "30") int days) {
        log.info("获取出入库趋势，days={}", days);

        TrendVO trend = statisticsService.getTrend(days);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", trend);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取库存分类分布
     */
    @ApiOperation("获取库存分类分布")
    @GetMapping("/category-distribution")
    public ResponseEntity<Map<String, Object>> getCategoryDistribution() {
        log.info("获取库存分类分布");

        List<CategoryDistributionVO> distribution = statisticsService.getCategoryDistribution();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", distribution);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取低库存列表
     */
    @ApiOperation("获取低库存列表")
    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockList() {
        log.info("获取低库存列表");

        List<LowStockVO> lowStockList = statisticsService.getLowStockList();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", lowStockList);
        return ResponseEntity.ok(result);
    }
}
