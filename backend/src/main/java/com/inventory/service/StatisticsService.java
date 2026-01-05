package com.inventory.service;

import com.inventory.vo.*;

import java.util.List;

/**
 * 统计报表服务
 *
 * @author inventory-system
 * @since 2026-01-04
 */
public interface StatisticsService {

    /**
     * 获取数据看板
     *
     * @return 看板数据
     */
    DashboardVO getDashboard();

    /**
     * 获取出入库趋势
     *
     * @param days 天数（1-90）
     * @return 趋势数据
     */
    TrendVO getTrend(int days);

    /**
     * 获取库存分类分布
     *
     * @return 分类分布数据
     */
    List<CategoryDistributionVO> getCategoryDistribution();

    /**
     * 获取低库存列表
     *
     * @return 低库存列表
     */
    List<LowStockVO> getLowStockList();
}
