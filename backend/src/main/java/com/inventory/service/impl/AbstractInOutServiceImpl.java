package com.inventory.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inventory.entity.BaseEntity;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.ProductMapper;
import com.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * 入库/出库服务公共基类（模板方法模式）
 *
 * 入库单与出库单的 create/update/approve/delete/void/getDetail 流程完全对称，
 * 仅存在差异点：单号前缀、实体字段、库存增减方向。通过抽象钩子方法实现复用。
 *
 * @param <M> Mapper 类型
 * @param <T> 实体类型（Inbound / Outbound）
 * @param <D> DTO 类型
 * @param <V> VO 类型
 * @author inventory-system
 * @since 2026-08-06
 */
@Slf4j
public abstract class AbstractInOutServiceImpl<M extends BaseMapper<T>, T extends BaseEntity, D, V>
        extends ServiceImpl<M, T> {

    protected final ProductMapper productMapper;
    protected final InventoryService inventoryService;

    protected AbstractInOutServiceImpl(ProductMapper productMapper, InventoryService inventoryService) {
        this.productMapper = productMapper;
        this.inventoryService = inventoryService;
    }

    /* ==================== 抽象钩子方法 ==================== */

    /** 生成单据号（IN/OUT 前缀不同） */
    protected abstract String generateNo();

    /** 构建实体 */
    protected abstract T buildEntity(D dto, String no);

    /** 更新实体字段 */
    protected abstract void applyUpdate(T entity, D dto);

    /** 判断是否为待审核状态 */
    protected abstract boolean isPending(T entity);

    /** 标记为已审核 */
    protected abstract void markApproved(T entity, String approvedBy);

    /** 标记为已作废 */
    protected abstract void markVoid(T entity);

    /** 审核后执行库存调整（入库加库存，出库校验并减库存） */
    protected abstract void executeStockAdjust(T entity);

    /** 实体转 VO */
    protected abstract V toVO(T entity);

    /** 填充商品信息到 VO */
    protected abstract void setProductInfo(V vo, Product product);

    /** 获取单据编号（用于日志） */
    protected abstract String getOrderNo(T entity);

    /** 从实体获取商品ID */
    protected abstract Long getProductId(T entity);

    /** 从 DTO 获取商品ID */
    protected abstract Long getProductId(D dto);

    /* ==================== 公共工作流 ==================== */

    /**
     * 创建单据
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(D dto) {
        validateProductEnabled(dto);
        String no = generateNo();
        T entity = buildEntity(dto, no);
        boolean saved = this.save(entity);
        if (!saved) {
            throw new BusinessException("单据创建失败");
        }
        log.info("创建单据成功，id={}, no={}", entity.getId(), getOrderNo(entity));
        return entity.getId();
    }

    /**
     * 更新单据（仅待审核）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Long id, D dto) {
        T entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("单据不存在");
        }
        if (!isPending(entity)) {
            throw new BusinessException("只有待审核状态的单据可以修改");
        }
        validateProductEnabled(dto);
        applyUpdate(entity, dto);
        boolean success = this.updateById(entity);
        log.info("更新单据成功，id={}", id);
        return success;
    }

    /**
     * 删除单据（仅待审核，物理删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        T entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("单据不存在");
        }
        if (!isPending(entity)) {
            throw new BusinessException("只有待审核状态的单据可以删除");
        }
        boolean deleted = this.removeById(id);
        log.info("删除单据成功，id={}, no={}", id, getOrderNo(entity));
        return deleted;
    }

    /**
     * 审核单据（仅待审核）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long id, String approvedBy) {
        T entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("单据不存在");
        }
        if (!isPending(entity)) {
            throw new BusinessException("只有待审核状态的单据可以审核");
        }
        markApproved(entity, approvedBy);
        this.updateById(entity);
        executeStockAdjust(entity);
        log.info("审核单据成功，id={}, no={}", id, getOrderNo(entity));
        return true;
    }

    /**
     * 作废单据（仅待审核）
     */
    @Transactional(rollbackFor = Exception.class)
    public void voidEntity(Long id) {
        T entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("单据不存在");
        }
        if (!isPending(entity)) {
            throw new BusinessException("只有待审核状态的单据可以作废");
        }
        markVoid(entity);
        this.updateById(entity);
        log.info("作废单据成功，id={}, no={}", id, getOrderNo(entity));
    }

    /**
     * 获取单据详情
     */
    public V getDetail(Long id) {
        T entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("单据不存在");
        }
        return toVOWithProduct(entity);
    }

    /**
     * 实体转 VO 并填充商品信息
     */
    protected V toVOWithProduct(T entity) {
        V vo = toVO(entity);
        Product product = productMapper.selectById(getProductId(entity));
        if (product != null) {
            setProductInfo(vo, product);
        }
        return vo;
    }

    /**
     * 校验商品存在且启用
     */
    protected void validateProductEnabled(D dto) {
        Product product = productMapper.selectById(getProductId(dto));
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.isEnabled()) {
            throw new BusinessException("商品已禁用");
        }
    }
}
