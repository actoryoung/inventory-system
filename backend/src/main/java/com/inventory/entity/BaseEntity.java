package com.inventory.entity;

/**
 * 实体基类接口
 *
 * 为 Inbound/Outbound 等共享相同工作流的实体提供统一主键访问，
 * 供 {@link com.inventory.service.impl.AbstractInOutServiceImpl} 模板方法复用。
 *
 * @author inventory-system
 * @since 2026-08-06
 */
public interface BaseEntity {

    Long getId();

    void setId(Long id);
}
