package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inventory.entity.Inbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单Mapper
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Mapper
public interface InboundMapper extends BaseMapper<Inbound> {
}
