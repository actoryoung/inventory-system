package com.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inventory.entity.OutboundSequence;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 出库单号序号Mapper
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Mapper
public interface OutboundSequenceMapper extends BaseMapper<OutboundSequence> {

    /**
     * 行锁查询今日序号
     *
     * 在事务内使用 SELECT ... FOR UPDATE 串行化并发取号，保证单号唯一。
     *
     * @param date 日期
     * @return 序号记录（可能为 null）
     */
    @Select("SELECT * FROM t_outbound_sequence WHERE seq_date = #{date} FOR UPDATE")
    OutboundSequence selectForUpdate(@Param("date") LocalDate date);
}
