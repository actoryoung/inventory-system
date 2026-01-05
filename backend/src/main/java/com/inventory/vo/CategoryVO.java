package com.inventory.vo;

import com.inventory.entity.Category;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类视图对象
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Data
@ApiModel(value = "CategoryVO", description = "分类视图对象")
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分类ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "分类名称", example = "电子产品")
    private String name;

    @ApiModelProperty(value = "父分类ID", example = "0")
    private Long parentId;

    @ApiModelProperty(value = "层级(1-3)", example = "1")
    private Integer level;

    @ApiModelProperty(value = "排序号", example = "1")
    private Integer sortOrder;

    @ApiModelProperty(value = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @ApiModelProperty(value = "创建时间", example = "2026-01-04T10:00:00")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间", example = "2026-01-04T10:00:00")
    private LocalDateTime updatedAt;

    @ApiModelProperty(value = "子分类列表")
    private List<CategoryVO> children = new ArrayList<>();

    /**
     * 从 Entity 转换为 VO
     */
    public static CategoryVO fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setParentId(category.getParentId());
        vo.setLevel(category.getLevel());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());
        vo.setCreatedAt(category.getCreatedAt());
        vo.setUpdatedAt(category.getUpdatedAt());
        return vo;
    }

    /**
     * 从 Entity 列表转换为 VO 列表
     */
    public static List<CategoryVO> fromEntityList(List<Category> categories) {
        if (categories == null) {
            return new ArrayList<>();
        }
        List<CategoryVO> result = new ArrayList<>(categories.size());
        for (Category category : categories) {
            result.add(fromEntity(category));
        }
        return result;
    }
}
