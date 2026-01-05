package com.inventory.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 分类数据传输对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "CategoryDTO", description = "分类数据传输对象")
public class CategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID（更新时需要）
     */
    @ApiModelProperty(value = "分类ID", example = "1")
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    @ApiModelProperty(value = "分类名称", required = true, example = "电子产品")
    private String name;

    /**
     * 父分类ID（一级分类为null或0）
     */
    @ApiModelProperty(value = "父分类ID", example = "0")
    private Long parentId;

    /**
     * 层级（自动计算）
     */
    @ApiModelProperty(value = "层级(1-3)", example = "1")
    private Integer level;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空")
    @ApiModelProperty(value = "排序号", required = true, example = "1")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @ApiModelProperty(value = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
}
