package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inventory.dto.InboundDTO;
import com.inventory.entity.Inbound;
import com.inventory.entity.InboundSequence;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.InboundMapper;
import com.inventory.mapper.InboundSequenceMapper;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.InboundService;
import com.inventory.service.InventoryService;
import com.inventory.vo.InboundVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 入库单服务实现
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Service
public class InboundServiceImpl extends AbstractInOutServiceImpl<InboundMapper, Inbound, InboundDTO, InboundVO>
        implements InboundService {

    private final InboundSequenceMapper sequenceMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public InboundServiceImpl(
            InboundSequenceMapper sequenceMapper,
            ProductMapper productMapper,
            InventoryService inventoryService) {
        super(productMapper, inventoryService);
        this.sequenceMapper = sequenceMapper;
    }

    /* ==================== 抽象钩子实现 ==================== */

    @Override
    protected Inbound buildEntity(InboundDTO dto, String no) {
        Inbound inbound = new Inbound();
        inbound.setInboundNo(no);
        inbound.setProductId(dto.getProductId());
        inbound.setQuantity(dto.getQuantity());
        inbound.setSupplier(dto.getSupplier());
        inbound.setInboundDate(dto.getInboundDate());
        inbound.setStatus(Inbound.STATUS_PENDING);
        inbound.setRemark(dto.getRemark());
        inbound.setCreatedAt(LocalDateTime.now());
        inbound.setCreatedBy("system"); // TODO: 从当前登录用户获取
        return inbound;
    }

    @Override
    protected void applyUpdate(Inbound entity, InboundDTO dto) {
        entity.setProductId(dto.getProductId());
        entity.setQuantity(dto.getQuantity());
        entity.setSupplier(dto.getSupplier());
        entity.setInboundDate(dto.getInboundDate());
        entity.setRemark(dto.getRemark());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    protected boolean isPending(Inbound entity) {
        return entity.isPending();
    }

    @Override
    protected void markApproved(Inbound entity, String approvedBy) {
        entity.setStatus(Inbound.STATUS_APPROVED);
        entity.setApprovedBy(approvedBy);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    protected void markVoid(Inbound entity) {
        entity.setStatus(Inbound.STATUS_VOID);
        entity.setUpdatedAt(LocalDateTime.now());
    }

    @Override
    protected void executeStockAdjust(Inbound entity) {
        // 入库审核通过后增加库存
        inventoryService.addStock(entity.getProductId(), entity.getQuantity());
    }

    @Override
    protected InboundVO toVO(Inbound entity) {
        return InboundVO.fromEntity(entity);
    }

    @Override
    protected void setProductInfo(InboundVO vo, Product product) {
        vo.setProductName(product.getName());
        vo.setProductSku(product.getSku());
    }

    @Override
    protected String getOrderNo(Inbound entity) {
        return entity.getInboundNo();
    }

    @Override
    protected Long getProductId(Inbound entity) {
        return entity.getProductId();
    }

    @Override
    protected Long getProductId(InboundDTO dto) {
        return dto.getProductId();
    }

    /* ==================== 接口方法 ==================== */

    @Override
    public void voidInbound(Long id) {
        voidEntity(id);
    }

    @Override
    public IPage<InboundVO> page(Long productId, Integer status, String startDate, String endDate, int page, int size) {
        // 构建查询条件
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(productId != null, Inbound::getProductId, productId)
                .eq(status != null, Inbound::getStatus, status)
                .ge(startDate != null, Inbound::getInboundDate, startDate)
                .le(endDate != null, Inbound::getInboundDate, endDate)
                .orderByDesc(Inbound::getCreatedAt);

        // 分页查询
        Page<Inbound> pageParam = new Page<>(page, size);
        IPage<Inbound> pageResult = this.page(pageParam, wrapper);

        // 转换为VO并填充商品信息
        Page<InboundVO> voPage = new Page<>(page, size, pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream()
                .map(this::toVOWithProduct)
                .collect(Collectors.toList()));

        return voPage;
    }

    /**
     * 生成入库单号
     * 格式：IN + yyyyMMdd + 4位序号
     *
     * 使用 SELECT ... FOR UPDATE 行锁串行化并发取号，保证单号全局唯一。
     * 调用方 create() 处于事务内，行锁在事务提交时释放。
     */
    @Override
    protected String generateNo() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FORMATTER);

        InboundSequence sequence = sequenceMapper.selectForUpdate(today);
        int seqValue;
        if (sequence == null) {
            // 今天第一次创建，从1开始
            seqValue = 1;
            InboundSequence newSequence = new InboundSequence();
            newSequence.setSeqDate(today);
            newSequence.setSeqValue(seqValue);
            try {
                sequenceMapper.insert(newSequence);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发下已被其他事务创建，重新加锁递增
                sequence = sequenceMapper.selectForUpdate(today);
                seqValue = sequence.getSeqValue() + 1;
                if (seqValue > 9999) {
                    throw new BusinessException("今日入库单数量已达上限");
                }
                sequence.setSeqValue(seqValue);
                sequenceMapper.updateById(sequence);
            }
        } else {
            // 递增序号
            seqValue = sequence.getSeqValue() + 1;
            if (seqValue > 9999) {
                throw new BusinessException("今日入库单数量已达上限");
            }
            sequence.setSeqValue(seqValue);
            sequenceMapper.updateById(sequence);
        }

        // 生成单号：IN + yyyyMMdd + 4位序号
        return String.format("IN%s%04d", dateStr, seqValue);
    }
}
