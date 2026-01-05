package com.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.inventory.dto.InboundDTO;
import com.inventory.entity.Inbound;
import com.inventory.vo.InboundVO;

/**
 * 入库单服务
 *
 * @author inventory-system
 * @since 2026-01-04
 */
public interface InboundService extends IService<Inbound> {

    /**
     * 创建入库单
     *
     * @param dto 入库单数据
     * @return 入库单ID
     */
    Long create(InboundDTO dto);

    /**
     * 更新入库单
     *
     * @param id  入库单ID
     * @param dto 入库单数据
     * @return 是否成功
     */
    boolean update(Long id, InboundDTO dto);

    /**
     * 审核入库单
     *
     * @param id        入库单ID
     * @param approvedBy 审核人
     * @return 是否成功
     */
    boolean approve(Long id, String approvedBy);

    /**
     * 作废入库单
     *
     * @param id 入库单ID
     * @return 是否成功
     */
    void voidInbound(Long id);

    /**
     * 获取入库单详情
     *
     * @param id 入库单ID
     * @return 入库单VO
     */
    InboundVO getDetail(Long id);

    /**
     * 分页查询入库单
     *
     * @param productId  商品ID
     * @param status     状态
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param page       页码
     * @param size       每页大小
     * @return 分页结果
     */
    IPage<InboundVO> page(Long productId, Integer status, String startDate, String endDate, int page, int size);
}
