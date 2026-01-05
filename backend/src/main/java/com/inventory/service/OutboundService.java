package com.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inventory.dto.OutboundDTO;
import com.inventory.entity.Outbound;
import com.inventory.vo.OutboundVO;

/**
 * 出库单服务
 *
 * @author inventory-system
 * @since 2026-01-04
 */
public interface OutboundService extends IService<Outbound> {

    /**
     * 创建出库单
     *
     * @param dto 出库单数据
     * @return 出库单ID
     */
    Long create(OutboundDTO dto);

    /**
     * 更新出库单
     *
     * @param id  出库单ID
     * @param dto 出库单数据
     * @return 是否成功
     */
    boolean update(Long id, OutboundDTO dto);

    /**
     * 审核出库单
     *
     * @param id        出库单ID
     * @param approvedBy 审核人
     * @return 是否成功
     */
    boolean approve(Long id, String approvedBy);

    /**
     * 作废出库单
     *
     * @param id 出库单ID
     */
    void voidOutbound(Long id);

    /**
     * 获取出库单详情
     *
     * @param id 出库单ID
     * @return 出库单VO
     */
    OutboundVO getDetail(Long id);

    /**
     * 分页查询出库单
     *
     * @param productId  商品ID
     * @param status     状态
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param page       页码
     * @param size       每页大小
     * @return 分页结果
     */
    IPage<OutboundVO> page(Long productId, Integer status, String startDate, String endDate, int page, int size);
}
