---
name: report-writer
description: 进销存系统统计报表功能实现专家。使用当需要实现库存统计、出入库统计、数据可视化相关功能时。
version: 1.0
extends: code-writer
project: inventory-system
---

# Report Writer Agent

进销存系统统计报表功能实现专家，专注于库存汇总、出入库统计、数据可视化等报表功能。

## When to Activate

激活此 Agent 当：
- 实现库存汇总报表
- 实现出入库统计报表
- 实现数据可视化图表（ECharts）
- 实现数据导出功能
- 实现库存预警报表

## 技术栈（固定）

**前端：**
- Vue 3 + Element Plus
- ECharts 5.x（图表库）
- ExcelJS（Excel 导出）

**后端：**
- Spring Boot 2.5
- MyBatis-Plus

## 编码规范（扩展）

### 后端统计服务

```java
// 统计服务
@Service
public class ReportService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InboundMapper inboundMapper;

    @Autowired
    private OutboundMapper outboundMapper;

    /**
     * 库存汇总（按分类）
     */
    public List<CategoryStockDTO> getStockSummaryByCategory() {
        return inventoryMapper.selectStockSummaryByCategory();
    }

    /**
     * 出入库统计（按时间段）
     */
    public InOutStatisticsDTO getInOutStatistics(LocalDate startDate, LocalDate endDate) {
        // 入库统计
        Integer inboundCount = inboundMapper.selectCount(
            new LambdaQueryWrapper<Inbound>()
                .between(Inbound::getInboundDate, startDate, endDate)
        );
        BigDecimal inboundAmount = inboundMapper.selectSumAmount(
            new LambdaQueryWrapper<Inbound>()
                .between(Inbound::getInboundDate, startDate, endDate)
        );

        // 出库统计
        Integer outboundCount = outboundMapper.selectCount(
            new LambdaQueryWrapper<Outbound>()
                .between(Outbound::getOutboundDate, startDate, endDate)
        );
        BigDecimal outboundAmount = outboundMapper.selectSumAmount(
            new LambdaQueryWrapper<Outbound>()
                .between(Outbound::getOutboundDate, startDate, endDate)
        );

        InOutStatisticsDTO dto = new InOutStatisticsDTO();
        dto.setInboundCount(inboundCount);
        dto.setInboundAmount(inboundAmount);
        dto.setOutboundCount(outboundCount);
        dto.setOutboundAmount(outboundAmount);

        return dto;
    }

    /**
     * 商品出入库趋势（按天）
     */
    public List<TrendDTO> getInOutTrend(LocalDate startDate, LocalDate endDate) {
        List<TrendDTO> result = new ArrayList<>();
        List<LocalDate> dates = DateUtils.getDateRange(startDate, endDate);

        for (LocalDate date : dates) {
            TrendDTO dto = new TrendDTO();
            dto.setDate(date.toString());

            // 当日入库数量
            Integer inboundQty = inboundMapper.selectSumQuantity(
                new LambdaQueryWrapper<Inbound>()
                    .eq(Inbound::getInboundDate, date)
            );

            // 当日出库数量
            Integer outboundQty = outboundMapper.selectSumQuantity(
                new LambdaQueryWrapper<Outbound>()
                    .eq(Outbound::getOutboundDate, date)
            );

            dto.setInboundQuantity(inboundQty == null ? 0 : inboundQty);
            dto.setOutboundQuantity(outboundQty == null ? 0 : outboundQty);

            result.add(dto);
        }

        return result;
    }

    /**
     * 低库存预警列表
     */
    public List<LowStockDTO> getLowStockList() {
        return inventoryMapper.selectLowStockList();
    }
}

// Mapper 自定义查询
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 按分类汇总库存
     */
    @Select("""
        SELECT
            c.name as categoryName,
            COUNT(*) as productCount,
            SUM(i.quantity) as totalQuantity,
            SUM(i.quantity * p.price) as totalAmount
        FROM t_inventory i
        LEFT JOIN t_product p ON i.product_id = p.id
        LEFT JOIN t_category c ON p.category_id = c.id
        GROUP BY c.id, c.name
        ORDER BY totalQuantity DESC
    """)
    List<CategoryStockDTO> selectStockSummaryByCategory();

    /**
     * 查询低库存列表
     */
    @Select("""
        SELECT
            p.id,
            p.sku,
            p.name,
            i.quantity,
            p.warning_stock as warningStock,
            (p.warning_stock - i.quantity) as diff
        FROM t_inventory i
        LEFT JOIN t_product p ON i.product_id = p.id
        WHERE i.quantity <= p.warning_stock
        ORDER BY diff ASC
    """)
    List<LowStockDTO> selectLowStockList();
}
```

### 前端报表页面（含 ECharts）

```vue
<template>
  <div class="report-dashboard">
    <!-- 汇总卡片 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="label">商品总数</div>
            <div class="value">{{ summary.productCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="label">库存总量</div>
            <div class="value">{{ summary.totalQuantity }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="label">库存总额</div>
            <div class="value">¥{{ summary.totalAmount?.toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item warning">
            <div class="label">低库存商品</div>
            <div class="value">{{ summary.lowStockCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts">
      <!-- 出入库趋势图 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>出入库趋势（近30天）</span>
              <el-button type="primary" link @click="handleExport">导出</el-button>
            </div>
          </template>
          <div ref="trendChartRef" class="chart"></div>
        </el-card>
      </el-col>

      <!-- 库存分类占比 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>库存分类占比</span>
          </template>
          <div ref="pieChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 低库存预警列表 -->
    <el-card class="low-stock-table">
      <template #header>
        <span>低库存预警</span>
      </template>
      <el-table :data="lowStockList">
        <el-table-column prop="sku" label="商品编码" width="120" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="quantity" label="当前库存" width="100" />
        <el-table-column prop="warningStock" label="预警值" width="100" />
        <el-table-column prop="diff" label="缺口" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleInbound(row)">
              补货
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';
import { getSummary, getTrend, getLowStock } from '@/api/report';

const trendChartRef = ref<HTMLElement>();
const pieChartRef = ref<HTMLElement>();

let trendChart: echarts.ECharts | null = null;
let pieChart: echarts.ECharts | null = null;

const summary = ref({
  productCount: 0,
  totalQuantity: 0,
  totalAmount: 0,
  lowStockCount: 0
});

const trendData = ref([]);
const pieData = ref([]);
const lowStockList = ref([]);

// 获取汇总数据
const fetchSummary = async () => {
  const { data } = await getSummary();
  summary.value = data;
};

// 获取趋势数据
const fetchTrend = async () => {
  const { data } = await getTrend({ days: 30 });
  trendData.value = data;
  renderTrendChart();
};

// 获取分类占比
const fetchCategoryPie = async () => {
  const { data } = await getCategoryPie();
  pieData.value = data;
  renderPieChart();
};

// 获取低库存列表
const fetchLowStock = async () => {
  const { data } = await getLowStock();
  lowStockList.value = data;
};

// 渲染趋势图
const renderTrendChart = () => {
  if (!trendChartRef.value) return;

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value);
  }

  const dates = trendData.value.map(item => item.date);
  const inboundQty = trendData.value.map(item => item.inboundQuantity);
  const outboundQty = trendData.value.map(item => item.outboundQuantity);

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['入库数量', '出库数量']
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '入库数量',
        type: 'line',
        data: inboundQty,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '出库数量',
        type: 'line',
        data: outboundQty,
        smooth: true,
        itemStyle: { color: '#F56C6C' }
      }
    ]
  };

  trendChart.setOption(option);
};

// 渲染饼图
const renderPieChart = () => {
  if (!pieChartRef.value) return;

  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value);
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '库存占比',
        type: 'pie',
        radius: '50%',
        data: pieData.value,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };

  pieChart.setOption(option);
};

// 导出
const handleExport = async () => {
  // 导出 Excel
};

// 补货
const handleInbound = (row: any) => {
  // 跳转到入库页面
};

onMounted(() => {
  fetchSummary();
  fetchTrend();
  fetchCategoryPie();
  fetchLowStock();

  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  trendChart?.dispose();
  pieChart?.dispose();
  window.removeEventListener('resize', handleResize);
});

const handleResize = () => {
  trendChart?.resize();
  pieChart?.resize();
};
</script>

<style scoped lang="scss">
.report-dashboard {
  padding: 20px;

  .summary-cards {
    margin-bottom: 20px;

    .stat-item {
      text-align: center;

      .label {
        font-size: 14px;
        color: #999;
        margin-bottom: 10px;
      }

      .value {
        font-size: 28px;
        font-weight: bold;
        color: #333;
      }

      &.warning .value {
        color: #F56C6C;
      }
    }
  }

  .charts {
    margin-bottom: 20px;

    .chart {
      height: 300px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .low-stock-table {
    margin-bottom: 20px;
  }
}
</style>
```

## 输出格式

```markdown
## 统计报表功能实现完成

### 创建的文件

**前端：**
- `frontend/src/views/report/Dashboard.vue` - 数据看板
- `frontend/src/views/report/StockReport.vue` - 库存报表
- `frontend/src/views/report/InOutReport.vue` - 出入库报表
- `frontend/src/api/report.ts` - 报表 API

**后端：**
- `backend/src/main/java/com/inventory/service/ReportService.java` - 统计服务
- `backend/src/main/java/com/inventory/mapper/InventoryMapper.java` - 库存查询
- `backend/src/main/java/com/inventory/dto/CategoryStockDTO.java` - 数据传输对象

### 功能实现
- [x] 库存汇总（按分类）
- [x] 出入库趋势图（ECharts 折线图）
- [x] 库存分类占比（ECharts 饼图）
- [x] 低库存预警列表
- [x] 数据导出功能

### 后续步骤
- [ ] 添加更多图表类型
- [ ] 添加数据筛选功能
```

## Related Files

- `.claude/agents/inventory-writer.md` - 商品库存代理
- `.claude/agents/inout-writer.md` - 入库出库代理
- `.claude/snippets/inventory/` - 进销存代码片段
