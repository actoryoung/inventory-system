package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inventory.dto.OutboundDTO;
import com.inventory.entity.Inventory;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 出库单服务实现
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Service
public class OutboundServiceImpl extends ServiceImpl<OutboundMapper, Outbound> implements OutboundService {

    @Autowired
    private OutboundSequenceMapper sequenceMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InventoryService inventoryService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OutboundDTO dto) {
        // 1. 验证商品存在且启用
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.isEnabled()) {
            throw new BusinessException("商品已禁用，无法创建出库单");
        }

        // 2. 生成出库单号
        String outboundNo = generateOutboundNo();

        // 3. 创建出库单
        Outbound outbound = new Outbound();
        outbound.setOutboundNo(outboundNo);
        outbound.setProductId(dto.getProductId());
        outbound.setQuantity(dto.getQuantity());
        outbound.setReceiver(dto.getReceiver());
        outbound.setReceiverPhone(dto.getReceiverPhone());
        outbound.setOutboundDate(dto.getOutboundDate());
        outbound.setStatus(Outbound.STATUS_PENDING);
        outbound.setRemark(dto.getRemark());
        outbound.setCreatedAt(LocalDateTime.now());
        outbound.setCreatedBy("system"); // TODO: 从当前登录用户获取

        this.save(outbound);
        log.info("创建出库单成功，id={}, outboundNo={}", outbound.getId(), outbound.getOutboundNo());

        return outbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Long id, OutboundDTO dto) {
        // 1. 验证出库单存在
        Outbound outbound = this.getById(id);
        if (outbound == null) {
            throw new BusinessException("出库单不存在");
        }

        // 2. 只有待审核状态可以修改
        if (!outbound.isPending()) {
            throw new BusinessException("只有待审核状态的出库单可以修改");
        }

        // 3. 验证商品存在且启用
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.isEnabled()) {
            throw new BusinessException("商品已禁用");
        }

        // 4. 更新出库单
        outbound.setProductId(dto.getProductId());
        outbound.setQuantity(dto.getQuantity());
        outbound.setReceiver(dto.getReceiver());
        outbound.setReceiverPhone(dto.getReceiverPhone());
        outbound.setOutboundDate(dto.getOutboundDate());
        outbound.setRemark(dto.getRemark());
        outbound.setUpdatedAt(LocalDateTime.now());

        boolean success = this.updateById(outbound);
        log.info("更新出库单成功，id={}", id);
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long id, String approvedBy) {
        // 1. 验证出库单存在
        Outbound outbound = this.getById(id);
        if (outbound == null) {
            throw new BusinessException("出库单不存在");
        }

        // 2. 只有待审核状态可以审核
        if (!outbound.isPending()) {
            throw new BusinessException("只有待审核状态的出库单可以审核");
        }

        // 3. 验证库存充足
        Inventory inventory = inventoryService.getByProductId(outbound.getProductId());
        if (inventory == null || inventory.getQuantity() < outbound.getQuantity()) {
            int currentStock = inventory != null ? inventory.getQuantity() : 0;
            throw new BusinessException("库存不足，当前库存为" + currentStock);
        }

        // 4. 更新状态
        outbound.setStatus(Outbound.STATUS_APPROVED);
        outbound.setApprovedBy(approvedBy);
        outbound.setApprovedAt(LocalDateTime.now());
        outbound.setUpdatedAt(LocalDateTime.now());
        this.updateById(outbound);

        // 5. 减少库存
        inventoryService.reduceStock(outbound.getProductId(), outbound.getQuantity());

        log.info("审核出库单成功，id={}, outboundNo={}, quantity={}", id, outbound.getOutboundNo(), outbound.getQuantity());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidOutbound(Long id) {
        // 1. 验证出库单存在
        Outbound outbound = this.getById(id);
        if (outbound == null) {
            throw new BusinessException("出库单不存在");
        }

        // 2. 只有待审核状态可以作废
        if (!outbound.isPending()) {
            throw new BusinessException("只有待审核状态的出库单可以作废");
        }

        // 3. 更新状态
        outbound.setStatus(Outbound.STATUS_VOID);
        outbound.setUpdatedAt(LocalDateTime.now());
        this.updateById(outbound);

        log.info("作废出库单成功，id={}, outboundNo={}", id, outbound.getOutboundNo());
    }

    @Override
    public OutboundVO getDetail(Long id) {
        Outbound outbound = this.getById(id);
        if (outbound == null) {
            throw new BusinessException("出库单不存在");
        }

        OutboundVO vo = OutboundVO.fromEntity(outbound);

        // 查询商品信息
        Product product = productMapper.selectById(outbound.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductSku(product.getSku());
        }

        return vo;
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

        // 转换为VO
        Page<OutboundVO> voPage = new Page<>(page, size, pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream().map(outbound -> {
            OutboundVO vo = OutboundVO.fromEntity(outbound);
            Product product = productMapper.selectById(outbound.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductSku(product.getSku());
            }
            return vo;
        }).toList());

        return voPage;
    }

    /**
     * 生成出库单号
     * 格式：OUT + yyyyMMdd + 4位序号
     */
    private String generateOutboundNo() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FORMATTER);

        // 查询今天的序号
        OutboundSequence sequence = sequenceMapper.selectById(today);
        int seqValue;
        if (sequence == null) {
            // 今天第一次创建，从1开始
            seqValue = 1;
            sequence = new OutboundSequence();
            sequence.setSeqDate(today);
            sequence.setSeqValue(seqValue);
            sequenceMapper.insert(sequence);
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
