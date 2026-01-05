package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inventory.entity.*;
import com.inventory.mapper.*;
import com.inventory.service.StatisticsService;
import com.inventory.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计报表服务实现
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InboundMapper inboundMapper;

    @Autowired
    private OutboundMapper outboundMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO dashboard = new DashboardVO();

        // 总商品数（启用状态）
        Long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)
        );
        dashboard.setTotalProducts(productCount.intValue());

        // 总库存量
        List<Inventory> inventories = inventoryMapper.selectList(null);
        int totalQuantity = inventories.stream().mapToInt(Inventory::getQuantity).sum();
        dashboard.setTotalQuantity(totalQuantity);

        // 库存总额（需要查询商品成本价）
        double totalAmount = 0.0;
        for (Inventory inv : inventories) {
            Product product = productMapper.selectById(inv.getProductId());
            if (product != null && product.getCostPrice() != null) {
                totalAmount += inv.getQuantity() * product.getCostPrice().doubleValue();
            }
        }
        dashboard.setTotalAmount(totalAmount);

        // 低库存数量
        long lowStockCount = inventories.stream()
                .filter(inv -> inv.getQuantity() < inv.getWarningStock())
                .count();
        dashboard.setLowStockCount((int) lowStockCount);

        return dashboard;
    }

    @Override
    public TrendVO getTrend(int days) {
        // 限制天数范围
        if (days < 1) days = 30;
        if (days > 90) days = 90;

        LocalDateTime startDate = LocalDateTime.now().minusDays(days - 1);
        LocalDateTime endDate = LocalDateTime.now().with(LocalTime.MAX);

        // 获取已审核的入库记录
        List<Inbound> inboundList = inboundMapper.selectList(
                new LambdaQueryWrapper<Inbound>()
                        .eq(Inbound::getStatus, Inbound.STATUS_APPROVED)
                        .between(Inbound::getInboundDate, startDate, endDate)
        );

        // 获取已审核的出库记录
        List<Outbound> outboundList = outboundMapper.selectList(
                new LambdaQueryWrapper<Outbound>()
                        .eq(Outbound::getStatus, Outbound.STATUS_APPROVED)
                        .between(Outbound::getOutboundDate, startDate, endDate)
        );

        // 按日期聚合入库数据
        Map<String, Integer> inboundMap = inboundList.stream()
                .collect(Collectors.groupingBy(
                        ib -> ib.getInboundDate().toLocalDate().toString(),
                        Collectors.summingInt(Inbound::getQuantity)
                ));

        // 按日期聚合出库数据
        Map<String, Integer> outboundMap = outboundList.stream()
                .collect(Collectors.groupingBy(
                        ob -> ob.getOutboundDate().toLocalDate().toString(),
                        Collectors.summingInt(Outbound::getQuantity)
                ));

        // 生成日期列表（按天）
        List<String> dates = new ArrayList<>();
        List<Integer> inboundQuantities = new ArrayList<>();
        List<Integer> outboundQuantities = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            dates.add(date);
            inboundQuantities.add(inboundMap.getOrDefault(date, 0));
            outboundQuantities.add(outboundMap.getOrDefault(date, 0));
        }

        TrendVO trend = new TrendVO();
        trend.setDates(dates);
        trend.setInboundQuantities(inboundQuantities);
        trend.setOutboundQuantities(outboundQuantities);

        return trend;
    }

    @Override
    public List<CategoryDistributionVO> getCategoryDistribution() {
        // 获取所有库存
        List<Inventory> inventories = inventoryMapper.selectList(null);

        // 按分类聚合
        Map<Long, List<Inventory>> categoryMap = new HashMap<>();
        for (Inventory inv : inventories) {
            Product product = productMapper.selectById(inv.getProductId());
            if (product != null) {
                categoryMap.computeIfAbsent(product.getCategoryId(), k -> new ArrayList<>()).add(inv);
            }
        }

        // 计算总库存量
        int totalQuantity = inventories.stream().mapToInt(Inventory::getQuantity).sum();

        // 构建结果
        List<CategoryDistributionVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Inventory>> entry : categoryMap.entrySet()) {
            Category category = categoryMapper.selectById(entry.getKey());
            if (category != null) {
                int quantity = entry.getValue().stream().mapToInt(Inventory::getQuantity).sum();
                double percentage = totalQuantity > 0
                        ? BigDecimal.valueOf(quantity * 100.0 / totalQuantity)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue()
                        : 0.0;

                CategoryDistributionVO vo = new CategoryDistributionVO();
                vo.setCategoryName(category.getName());
                vo.setQuantity(quantity);
                vo.setPercentage(percentage);
                result.add(vo);
            }
        }

        // 按数量降序排序
        result.sort((a, b) -> b.getQuantity().compareTo(a.getQuantity()));

        return result;
    }

    @Override
    public List<LowStockVO> getLowStockList() {
        // 获取所有库存
        List<Inventory> inventories = inventoryMapper.selectList(null);

        List<LowStockVO> result = new ArrayList<>();
        for (Inventory inv : inventories) {
            if (inv.getQuantity() < inv.getWarningStock()) {
                Product product = productMapper.selectById(inv.getProductId());
                if (product != null) {
                    Category category = categoryMapper.selectById(product.getCategoryId());

                    LowStockVO vo = new LowStockVO();
                    vo.setProductId(product.getId());
                    vo.setProductSku(product.getSku());
                    vo.setProductName(product.getName());
                    vo.setCategoryName(category != null ? category.getName() : "-");
                    vo.setQuantity(inv.getQuantity());
                    vo.setWarningStock(inv.getWarningStock());
                    vo.setShortage(inv.getWarningStock() - inv.getQuantity());
                    result.add(vo);
                }
            }
        }

        return result;
    }
}
