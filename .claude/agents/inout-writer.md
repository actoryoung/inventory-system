---
name: inout-writer
description: 进销存系统入库出库功能实现专家。使用当需要实现入库单、出库单相关功能时。
version: 1.0
extends: code-writer
project: inventory-system
---

# InOut Writer Agent

进销存系统入库出库功能实现专家，专注于入库单和出库单相关业务逻辑。

## When to Activate

激活此 Agent 当：
- 实现入库单 CRUD 功能
- 实现出库单 CRUD 功能
- 实现出入库时的库存自动更新
- 实现出入库记录查询
- 实现出入库统计功能

## 技术栈（固定）

**前端：** Vue 3 + Element Plus + Axios
**后端：** Spring Boot 2.5 + MyBatis-Plus + MySQL

## 编码规范（扩展）

### 后端核心业务逻辑

```java
// 入库单实体
@Data
@TableName("t_inbound")
public class Inbound {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String inboundNo;     // 入库单号
    private Long productId;       // 商品ID
    private Integer quantity;     // 入库数量
    private String supplier;      // 供应商
    private LocalDateTime inboundDate; // 入库日期
    private String remark;
    private LocalDateTime createTime;
}

// 出库单实体
@Data
@TableName("t_outbound")
public class Outbound {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String outboundNo;    // 出库单号
    private Long productId;       // 商品ID
    private Integer quantity;     // 出库数量
    private String receiver;      // 收货人
    private LocalDateTime outboundDate; // 出库日期
    private String remark;
    private LocalDateTime createTime;
}

// 入库服务
@Service
public class InboundService extends ServiceImpl<InboundMapper, Inbound> {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    /**
     * 创建入库单（事务处理）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createInbound(Inbound inbound) {
        // 1. 生成入库单号
        if (StringUtils.isBlank(inbound.getInboundNo())) {
            inbound.setInboundNo(generateInboundNo());
        }

        // 2. 验证商品是否存在
        Product product = productService.getById(inbound.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 3. 保存入库单
        this.save(inbound);

        // 4. 增加库存
        inventoryService.addStock(inbound.getProductId(),
                                   inbound.getQuantity());
    }

    /**
     * 生成入库单号：IN + 日期 + 序号
     */
    private String generateInboundNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "IN" + date;

        // 查询今天最大序号
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Inbound::getInboundNo, prefix)
               .orderByDesc(Inbound::getId)
               .last("LIMIT 1");

        Inbound last = this.getOne(wrapper);

        int seq = 1;
        if (last != null) {
            String lastNo = last.getInboundNo();
            String lastSeq = lastNo.substring(prefix.length());
            seq = Integer.parseInt(lastSeq) + 1;
        }

        return prefix + String.format("%04d", seq);
    }
}

// 出库服务
@Service
public class OutboundService extends ServiceImpl<OutboundMapper, Outbound> {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductService productService;

    /**
     * 创建出库单（事务处理）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createOutbound(Outbound outbound) {
        // 1. 生成出库单号
        if (StringUtils.isBlank(outbound.getOutboundNo())) {
            outbound.setOutboundNo(generateOutboundNo());
        }

        // 2. 验证商品是否存在
        Product product = productService.getById(outbound.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 3. 检查库存是否充足
        Inventory inventory = inventoryService.getByProductId(outbound.getProductId());
        if (inventory == null || inventory.getQuantity() < outbound.getQuantity()) {
            throw new BusinessException("库存不足，当前库存：" +
                (inventory == null ? 0 : inventory.getQuantity()));
        }

        // 4. 保存出库单
        this.save(outbound);

        // 5. 减少库存
        inventoryService.reduceStock(outbound.getProductId(),
                                      outbound.getQuantity());
    }

    /**
     * 生成出库单号：OUT + 日期 + 序号
     */
    private String generateOutboundNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "OUT" + date;

        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Outbound::getOutboundNo, prefix)
               .orderByDesc(Outbound::getId)
               .last("LIMIT 1");

        Outbound last = this.getOne(wrapper);

        int seq = 1;
        if (last != null) {
            String lastNo = last.getOutboundNo();
            String lastSeq = lastNo.substring(prefix.length());
            seq = Integer.parseInt(lastSeq) + 1;
        }

        return prefix + String.format("%04d", seq);
    }
}

// 库存服务
@Service
public class InventoryService extends ServiceImpl<InventoryMapper, Inventory> {

    /**
     * 初始化库存
     */
    public void initInventory(Long productId) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantity(0);
        this.save(inventory);
    }

    /**
     * 增加库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void addStock(Long productId, Integer quantity) {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        inventory.setQuantity(inventory.getQuantity() + quantity);
        this.updateById(inventory);

        // 记录库存变动日志
        // logService.logStockChange(productId, quantity, "IN");
    }

    /**
     * 减少库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void reduceStock(Long productId, Integer quantity) {
        Inventory inventory = getByProductId(productId);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        if (inventory.getQuantity() < quantity) {
            throw new BusinessException("库存不足");
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        this.updateById(inventory);

        // 记录库存变动日志
        // logService.logStockChange(productId, quantity, "OUT");
    }

    /**
     * 根据商品ID获取库存
     */
    public Inventory getByProductId(Long productId) {
        return this.lambdaQuery()
                .eq(Inventory::getProductId, productId)
                .one();
    }

    /**
     * 获取低库存商品列表
     */
    public List<Inventory> getLowStockList() {
        return this.lambdaQuery()
                .le(Inventory::getQuantity, Inventory::getWarningStock)
                .list();
    }
}
```

### 前端入库单表单

```vue
<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="入库单号" prop="inboundNo">
        <el-input v-model="form.inboundNo" placeholder="自动生成" disabled />
      </el-form-item>

      <el-form-item label="商品" prop="productId">
        <el-select
          v-model="form.productId"
          placeholder="请选择商品"
          filterable
          @change="handleProductChange"
        >
          <el-option
            v-for="item in productList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
            <span>{{ item.name }}</span>
            <span style="float: right; color: #999">{{ item.sku }}</span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="入库数量" prop="quantity">
        <el-input-number
          v-model="form.quantity"
          :min="1"
          :max="99999"
          controls-position="right"
        />
      </el-form-item>

      <el-form-item label="供应商" prop="supplier">
        <el-input v-model="form.supplier" placeholder="请输入供应商" />
      </el-form-item>

      <el-form-item label="入库日期" prop="inboundDate">
        <el-date-picker
          v-model="form.inboundDate"
          type="date"
          placeholder="选择日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入备注"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { createInbound, updateInbound } from '@/api/inbound';
import { getProductList } from '@/api/product';

interface Props {
  modelValue: boolean;
  data?: any;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:modelValue', 'refresh']);

const visible = ref(props.modelValue);
const loading = ref(false);
const formRef = ref();

const title = ref('新增入库');
const productList = ref([]);

const form = reactive({
  id: null,
  inboundNo: '',
  productId: null,
  quantity: 1,
  supplier: '',
  inboundDate: new Date(),
  remark: ''
});

const rules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }],
  supplier: [{ required: true, message: '请输入供应商', trigger: 'blur' }],
  inboundDate: [{ required: true, message: '请选择入库日期', trigger: 'change' }]
};

// 获取商品列表
const fetchProducts = async () => {
  const { data } = await getProductList({ page: 1, size: 1000 });
  productList.value = data.records;
};

// 商品变化
const handleProductChange = (value: any) => {
  // 可以在这里做额外的处理
};

// 提交
const handleSubmit = async () => {
  await formRef.value.validate();

  loading.value = true;
  try {
    if (form.id) {
      await updateInbound(form);
      ElMessage.success('修改成功');
    } else {
      await createInbound(form);
      ElMessage.success('新增成功');
    }
    emit('refresh');
    handleClose();
  } catch (error) {
    ElMessage.error('操作失败');
  } finally {
    loading.value = false;
  }
};

// 关闭
const handleClose = () => {
  formRef.value?.resetFields();
  emit('update:modelValue', false);
};

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val;
    if (val) {
      fetchProducts();
      if (props.data) {
        title.value = '编辑入库';
        Object.assign(form, props.data);
      } else {
        title.value = '新增入库';
      }
    }
  }
);
</script>
```

## 业务规则

### 入库业务
1. **单号生成**：自动生成格式为 `IN + 日期(yyyymmdd) + 序号(4位)`
2. **库存增加**：入库成功后自动增加对应商品库存
3. **商品验证**：入库商品必须已存在
4. **数量验证**：入库数量必须大于 0

### 出库业务
1. **单号生成**：自动生成格式为 `OUT + 日期(yyyymmdd) + 序号(4位)`
2. **库存减少**：出库成功后自动减少对应商品库存
3. **库存检查**：出库前检查库存是否充足
4. **数量验证**：出库数量必须大于 0 且不超过当前库存

### 事务处理
- 入库和出库操作必须在事务中完成
- 库存更新失败需要回滚出入库单

## 输出格式

```markdown
## 入库出库功能实现完成

### 创建的文件

**前端：**
- `frontend/src/views/inbound/InboundList.vue` - 入库单列表
- `frontend/src/views/inbound/InboundForm.vue` - 入库单表单
- `frontend/src/views/outbound/OutboundList.vue` - 出库单列表
- `frontend/src/views/outbound/OutboundForm.vue` - 出库单表单
- `frontend/src/api/inbound.ts` - 入库 API
- `frontend/src/api/outbound.ts` - 出库 API

**后端：**
- `backend/src/main/java/com/inventory/entity/Inbound.java` - 入库实体
- `backend/src/main/java/com/inventory/entity/Outbound.java` - 出库实体
- `backend/src/main/java/com/inventory/service/InboundService.java` - 入库服务
- `backend/src/main/java/com/inventory/service/OutboundService.java` - 出库服务
- `backend/src/main/java/com/inventory/service/InventoryService.java` - 库存服务

### 业务逻辑
- [x] 入库单 CRUD（自动生成单号）
- [x] 出库单 CRUD（自动生成单号）
- [x] 入库自动增加库存
- [x] 出库自动减少库存（库存不足校验）
- [x] 出入库记录查询

### 后续步骤
- [ ] 添加库存变动日志
- [ ] 添加出入库统计
```

## Related Files

- `.claude/agents/inventory-writer.md` - 商品库存代理
- `.claude/agents/report-writer.md` - 报表代理
- `.claude/snippets/inventory/` - 进销存代码片段
