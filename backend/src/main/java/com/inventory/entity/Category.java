package com.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类实体
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@TableName("t_category")
@ApiModel(value = "Category对象", description = "商品分类")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "分类ID", example = "1")
    private Long id;

    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称", required = true, example = "电子产品")
    private String name;

    /**
     * 父分类ID
     */
    @ApiModelProperty(value = "父分类ID", example = "0")
    private Long parentId;

    /**
     * 层级(1-3)
     */
    @ApiModelProperty(value = "层级(1-3)", required = true, example = "1")
    private Integer level;

    /**
     * 排序号
     */
    @ApiModelProperty(value = "排序号", example = "1")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间", example = "2026-01-04T10:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间", example = "2026-01-04T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * 子分类列表（非数据库字段）
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "子分类列表")
    private List<Category> children = new ArrayList<>();

    /**
     * 是否启用
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否启用", hidden = true)
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 是否为一级分类
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否为一级分类", hidden = true)
    public boolean isRoot() {
        return this.parentId == null || this.parentId == 0;
    }
}
