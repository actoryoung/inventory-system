package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inventory.entity.OutboundSequence;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单号序号Mapper
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Mapper
public interface OutboundSequenceMapper extends BaseMapper<OutboundSequence> {
}
