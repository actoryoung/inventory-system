# Spring Boot 实体类片段

## 商品实体 (Product.java)

```java
package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("t_product")
public class Product {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品编码（SKU）
     */
    private String sku;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 计量单位
     */
    private String unit;

    /**
     * 销售价格
     */
    private BigDecimal price;

    /**
     * 成本价格
     */
    private BigDecimal costPrice;

    /**
     * 商品规格
     */
    private String specification;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 预警库存
     */
    private Integer warningStock;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

## 库存实体 (Inventory.java)

```java
package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存实体
 */
@Data
@TableName("t_inventory")
public class Inventory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 库存数量
     */
    private Integer quantity;

    /**
     * 预警值
     */
    private Integer warningStock;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

## 入库单实体 (Inbound.java)

```java
package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单实体
 */
@Data
@TableName("t_inbound")
public class Inbound {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 入库单号
     */
    private String inboundNo;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 入库数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 供应商
     */
    private String supplier;

    /**
     * 入库日期
     */
    private LocalDateTime inboundDate;

    /**
     * 状态：0-待审核 1-已审核 2-已作废
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

## 出库单实体 (Outbound.java)

```java
package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库单实体
 */
@Data
@TableName("t_outbound")
public class Outbound {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 出库单号
     */
    private String outboundNo;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 出库数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 收货人
     */
    private String receiver;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 出库日期
     */
    private LocalDateTime outboundDate;

    /**
     * 状态：0-待审核 1-已审核 2-已作废
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```
