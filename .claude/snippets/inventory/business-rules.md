# 进销存业务规则片段

## 商品管理业务规则

### 1. SKU 唯一性
```
规则：商品编码（SKU）必须唯一
校验时机：新增/编辑商品时
处理：重复则提示"商品编码已存在"
```

### 2. 价格校验
```
规则：销售价格、成本价格必须大于等于0
校验时机：保存商品时
处理：不符合则提示"价格不能为负数"
```

### 3. 商品分类
```
规则：商品必须属于有效的分类
校验时机：保存商品时
处理：分类不存在则提示"商品分类无效"
```

---

## 库存管理业务规则

### 1. 库存初始化
```
规则：新增商品时自动初始化库存记录
时机：商品创建成功后
动作：创建 t_inventory 记录，初始数量为用户输入值
```

### 2. 库存预警
```
规则：当库存数量 <= 预警值时，触发预警
条件：inventory.quantity <= inventory.warningStock OR product.warningStock
动作：
  - 在列表中标记为红色
  - 生成预警记录
  - 可选：发送通知
```

### 3. 库存更新时机
```
入库：inventory.quantity += inbound.quantity
出库：inventory.quantity -= outbound.quantity
调整：inventory.quantity = newQuantity
```

---

## 入库业务规则

### 1. 入库单号生成
```
格式：IN + yyyyMMdd + 4位序号
示例：IN202601040001
规则：
  - 每天序号重新从0001开始
  - 同一天内序号递增
  - 不可重复
```

### 2. 入库流程
```
1. 验证商品是否存在
2. 生成入库单号
3. 保存入库单记录
4. 增加对应商品库存
5. 记录库存变动日志
全部在事务中执行
```

### 3. 数据校验
```
- 商品ID必须有效
- 入库数量必须 > 0
- 入库日期不能为未来日期
- 供应商不能为空
```

---

## 出库业务规则

### 1. 出库单号生成
```
格式：OUT + yyyyMMdd + 4位序号
示例：OUT202601040001
规则：同入库单号
```

### 2. 出库流程
```
1. 验证商品是否存在
2. 检查库存是否充足
3. 生成出库单号
4. 保存出库单记录
5. 减少对应商品库存
6. 记录库存变动日志
全部在事务中执行
```

### 3. 库存校验
```
if (currentStock < outboundQuantity) {
    throw new BusinessException("库存不足");
}
```

### 4. 数据校验
```
- 商品ID必须有效
- 出库数量必须 > 0
- 出库数量 <= 当前库存
- 出库日期不能为未来日期
- 收货人不能为空
```

---

## 统计报表规则

### 1. 库存汇总
```
按分类汇总：
SELECT
    c.name as categoryName,
    COUNT(*) as productCount,
    SUM(i.quantity) as totalQuantity,
    SUM(i.quantity * p.price) as totalAmount
FROM t_inventory i
LEFT JOIN t_product p ON i.product_id = p.id
LEFT JOIN t_category c ON p.category_id = c.id
GROUP BY c.id
```

### 2. 出入库趋势
```
按天统计（最近N天）：
SELECT
    DATE(inbound_date) as date,
    SUM(quantity) as inboundQuantity
FROM t_inbound
WHERE inbound_date >= startDate
GROUP BY DATE(inbound_date)
ORDER BY date
```

### 3. 低库存查询
```
SELECT
    p.sku,
    p.name,
    i.quantity,
    p.warning_stock,
    (p.warning_stock - i.quantity) as diff
FROM t_inventory i
LEFT JOIN t_product p ON i.product_id = p.id
WHERE i.quantity <= p.warning_stock
ORDER BY diff ASC
```

---

## 状态流转规则

### 入库单状态
```
待审核(0) → 已审核(1)
         ↘ 已作废(2)

已审核(1) → 不能修改
已作废(2) → 不能恢复
```

### 出库单状态
```
待审核(0) → 已审核(1)
         ↘ 已作废(2)

已审核(1) → 库存已扣减，不能修改
已作废(2) → 不能恢复
```

---

## 权限控制规则

### 操作权限
```
新增商品：需登录
编辑商品：需登录
删除商品：需管理员或创建者
入库操作：需仓库管理员权限
出库操作：需仓库管理员权限
查看报表：需登录
```

### 数据权限
```
普通用户：只能查看自己创建的记录
管理员：可以查看所有记录
仓库管理员：可以操作出入库
```
