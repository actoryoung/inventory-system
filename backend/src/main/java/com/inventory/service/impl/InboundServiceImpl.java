package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 入库单服务实现
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@Service
public class InboundServiceImpl extends ServiceImpl<InboundMapper, Inbound> implements InboundService {

    @Autowired
    private InboundSequenceMapper sequenceMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private InventoryService inventoryService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(InboundDTO dto) {
        // 1. 验证商品存在且启用
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.isEnabled()) {
            throw new BusinessException("商品已禁用，无法创建入库单");
        }

        // 2. 生成入库单号
        String inboundNo = generateInboundNo();

        // 3. 创建入库单
        Inbound inbound = new Inbound();
        inbound.setInboundNo(inboundNo);
        inbound.setProductId(dto.getProductId());
        inbound.setQuantity(dto.getQuantity());
        inbound.setSupplier(dto.getSupplier());
        inbound.setInboundDate(dto.getInboundDate());
        inbound.setStatus(Inbound.STATUS_PENDING);
        inbound.setRemark(dto.getRemark());
        inbound.setCreatedAt(LocalDateTime.now());
        inbound.setCreatedBy("system"); // TODO: 从当前登录用户获取

        this.save(inbound);
        log.info("创建入库单成功，id={}, inboundNo={}", inbound.getId(), inbound.getInboundNo());

        return inbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Long id, InboundDTO dto) {
        // 1. 验证入库单存在
        Inbound inbound = this.getById(id);
        if (inbound == null) {
            throw new BusinessException("入库单不存在");
        }

        // 2. 只有待审核状态可以修改
        if (!inbound.isPending()) {
            throw new BusinessException("只有待审核状态的入库单可以修改");
        }

        // 3. 验证商品存在且启用
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.isEnabled()) {
            throw new BusinessException("商品已禁用");
        }

        // 4. 更新入库单
        inbound.setProductId(dto.getProductId());
        inbound.setQuantity(dto.getQuantity());
        inbound.setSupplier(dto.getSupplier());
        inbound.setInboundDate(dto.getInboundDate());
        inbound.setRemark(dto.getRemark());
        inbound.setUpdatedAt(LocalDateTime.now());

        boolean success = this.updateById(inbound);
        log.info("更新入库单成功，id={}", id);
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long id, String approvedBy) {
        // 1. 验证入库单存在
        Inbound inbound = this.getById(id);
        if (inbound == null) {
            throw new BusinessException("入库单不存在");
        }

        // 2. 只有待审核状态可以审核
        if (!inbound.isPending()) {
            throw new BusinessException("只有待审核状态的入库单可以审核");
        }

        // 3. 更新状态
        inbound.setStatus(Inbound.STATUS_APPROVED);
        inbound.setApprovedBy(approvedBy);
        inbound.setApprovedAt(LocalDateTime.now());
        inbound.setUpdatedAt(LocalDateTime.now());
        this.updateById(inbound);

        // 4. 增加库存
        inventoryService.addStock(inbound.getProductId(), inbound.getQuantity());

        log.info("审核入库单成功，id={}, inboundNo={}, quantity={}", id, inbound.getInboundNo(), inbound.getQuantity());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidInbound(Long id) {
        // 1. 验证入库单存在
        Inbound inbound = this.getById(id);
        if (inbound == null) {
            throw new BusinessException("入库单不存在");
        }

        // 2. 只有待审核状态可以作废
        if (!inbound.isPending()) {
            throw new BusinessException("只有待审核状态的入库单可以作废");
        }

        // 3. 更新状态
        inbound.setStatus(Inbound.STATUS_VOID);
        inbound.setUpdatedAt(LocalDateTime.now());
        this.updateById(inbound);

        log.info("作废入库单成功，id={}, inboundNo={}", id, inbound.getInboundNo());
    }

    @Override
    public InboundVO getDetail(Long id) {
        Inbound inbound = this.getById(id);
        if (inbound == null) {
            throw new BusinessException("入库单不存在");
        }

        InboundVO vo = InboundVO.fromEntity(inbound);

        // 查询商品信息
        Product product = productMapper.selectById(inbound.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductSku(product.getSku());
        }

        return vo;
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

        // 转换为VO
        Page<InboundVO> voPage = new Page<>(page, size, pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream().map(inbound -> {
            InboundVO vo = InboundVO.fromEntity(inbound);
            Product product = productMapper.selectById(inbound.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductSku(product.getSku());
            }
            return vo;
        }).toList());

        return voPage;
    }

    /**
     * 生成入库单号
     * 格式：IN + yyyyMMdd + 4位序号
     */
    private String generateInboundNo() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FORMATTER);

        // 查询今天的序号
        InboundSequence sequence = sequenceMapper.selectById(today);
        int seqValue;
        if (sequence == null) {
            // 今天第一次创建，从1开始
            seqValue = 1;
            sequence = new InboundSequence();
            sequence.setSeqDate(today);
            sequence.setSeqValue(seqValue);
            sequenceMapper.insert(sequence);
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
