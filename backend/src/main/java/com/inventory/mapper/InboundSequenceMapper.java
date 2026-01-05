package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inventory.entity.InboundSequence;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单号序号Mapper
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Mapper
public interface InboundSequenceMapper extends BaseMapper<InboundSequence> {
}
