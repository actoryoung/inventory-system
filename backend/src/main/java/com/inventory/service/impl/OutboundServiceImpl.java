package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inventory.dto.OutboundDTO;
import com.inventory.entity.Outbound;
import com.inventory.entity.OutboundSequence;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.OutboundMapper;
import com.inventory.mapper.OutboundSequenceMapper;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.InventoryService;
import com.inventory.service.OutboundService;
import com.inventory.vo.OutboundVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 出库单服务实现
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Service
public class OutboundServiceImpl extends AbstractInOutServiceImpl<OutboundMapper, Outbound, OutboundDTO, OutboundVO>
        implements OutboundService {

    private final OutboundSequenceMapper sequenceMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public OutboundServiceImpl(
            OutboundSequenceMapper sequenceMapper,
            ProductMapper productMapper,
            InventoryService inventoryService) {
        super(productMapper, inventoryService);
        this.sequenceMapper = sequenceMapper;
    }

    /* ==================== 抽象钩子实现 ==================== */

    @Override
    protected Outbound buildEntity(OutboundDTO dto, String no) {
        Outbound outbound = new Outbound();
        outbound.setOutboundNo(no);
        outbound.setProductId(dto.getProductId());
        outbound.setQuantity(dto.getQuantity());
        outbound.setReceiver(dto.getReceiver());
        outbound.setReceiverPhone(dto.getReceiverPhone());
        outbound.setOutboundDate(dto.getOutboundDate());
        outbound.setStatus(Outbound.STATUS_PENDING);
        outbound.setRemark(dto.getRemark());
        outbound.setCreatedAt(LocalDateTime.now());
        outbound.setCreatedBy("system"); // TODO: 从当前登录用户获取
        return outbound;
    }

    @Override
    protected void applyUpdate(Outbound entity, OutboundDTO dto) {
        entity.setProductId(dto.getProductId());
        entity.setQuantity(dto.getQuantity());
        entity.setReceiver(dto.getReceiver());
        entity.setReceiverPhone(dto.getReceiverPhone());
        entity.setOutboundDate(dto.getOutboundDate());
        entity.setRemark(dto.getRemark());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    protected boolean isPending(Outbound entity) {
        return entity.isPending();
    }

    @Override
    protected void markApproved(Outbound entity, String approvedBy) {
        entity.setStatus(Outbound.STATUS_APPROVED);
        entity.setApprovedBy(approvedBy);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    protected void markVoid(Outbound entity) {
        entity.setStatus(Outbound.STATUS_VOID);
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    protected void executeStockAdjust(Outbound entity) {
        // 出库审核通过后扣减库存（原子扣减，库存不足抛异常回滚）
        inventoryService.reduceStock(entity.getProductId(), entity.getQuantity());
    }

    @Override
    protected OutboundVO toVO(Outbound entity) {
        return OutboundVO.fromEntity(entity);
    }

    @Override
    protected void setProductInfo(OutboundVO vo, Product product) {
        vo.setProductName(product.getName());
        vo.setProductSku(product.getSku());
    }

    @Override
    protected String getOrderNo(Outbound entity) {
        return entity.getOutboundNo();
    }

    @Override
    protected Long getProductId(Outbound entity) {
        return entity.getProductId();
    }

    @Override
    protected Long getProductId(OutboundDTO dto) {
        return dto.getProductId();
    }

    /* ==================== 接口方法 ==================== */

    @Override
    public void voidOutbound(Long id) {
        voidEntity(id);
    }

    @Override
    public IPage<OutboundVO> page(Long productId, Integer status, String startDate, String endDate, int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(productId != null, Outbound::getProductId, productId)
                .eq(status != null, Outbound::getStatus, status)
                .ge(startDate != null, Outbound::getOutboundDate, startDate)
                .le(endDate != null, Outbound::getOutboundDate, endDate)
                .orderByDesc(Outbound::getCreatedAt);

        // 分页查询
        Page<Outbound> pageParam = new Page<>(page, size);
        IPage<Outbound> pageResult = this.page(pageParam, wrapper);

        // 转换为VO并填充商品信息
        Page<OutboundVO> voPage = new Page<>(page, size, pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream()
                .map(this::toVOWithProduct)
                .collect(Collectors.toList()));

        return voPage;
    }

    /**
     * 生成出库单号
     * 格式：OUT + yyyyMMdd + 4位序号
     *
     * 使用 SELECT ... FOR UPDATE 行锁串行化并发取号，保证单号全局唯一。
     * 调用方 create() 处于事务内，行锁在事务提交时释放。
     */
    @Override
    protected String generateNo() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FORMATTER);

        OutboundSequence sequence = sequenceMapper.selectForUpdate(today);
        int seqValue;
        if (sequence == null) {
            // 今天第一次创建，从1开始
            seqValue = 1;
            OutboundSequence newSequence = new OutboundSequence();
            newSequence.setSeqDate(today);
            newSequence.setSeqValue(seqValue);
            try {
                sequenceMapper.insert(newSequence);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发下已被其他事务创建，重新加锁递增
                sequence = sequenceMapper.selectForUpdate(today);
                seqValue = sequence.getSeqValue() + 1;
                if (seqValue > 9999) {
                    throw new BusinessException("今日出库单数量已达上限");
                }
                sequence.setSeqValue(seqValue);
                sequenceMapper.updateById(sequence);
            }
        } else {
            // 递增序号
            seqValue = sequence.getSeqValue() + 1;
            if (seqValue > 9999) {
                throw new BusinessException("今日出库单数量已达上限");
            }
            sequence.setSeqValue(seqValue);
            sequenceMapper.updateById(sequence);
        }

        // 生成单号：OUT + yyyyMMdd + 4位序号
        return String.format("OUT%s%04d", dateStr, seqValue);
    }
}
