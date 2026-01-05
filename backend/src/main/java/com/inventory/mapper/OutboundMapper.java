package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inventory.entity.Outbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单Mapper
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Mapper
public interface OutboundMapper extends BaseMapper<Outbound> {
}
