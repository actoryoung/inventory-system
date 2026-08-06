package com.inventory.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.inventory.entity.Outbound;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.OutboundMapper;
import com.inventory.service.impl.OutboundServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 出库单删除逻辑单元测试
 *
 * 覆盖 P0-4：仅"待审核"状态可删除（物理删除），
 * "已审核"/"已作废"状态删除时抛业务异常。
 *
 * @author inventory-system
 * @since 2026-08-06
 */
@DisplayName("出库单删除逻辑测试 (OutboundServiceDelete)")
class OutboundServiceDeleteTest {

    @Mock
    private OutboundMapper outboundMapper;

    private OutboundServiceImpl outboundService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        outboundService = new OutboundServiceImpl();
        // ServiceImpl 的 baseMapper 需要显式注入
        ReflectionTestUtils.setField(outboundService, "baseMapper", outboundMapper);
        // removeById 依赖 MyBatis-Plus TableInfo，需手动初始化实体元数据
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Outbound.class);
    }

    private Outbound outboundWithStatus(int status) {
        Outbound outbound = new Outbound();
        outbound.setId(1L);
        outbound.setOutboundNo("OUT202608060001");
        outbound.setStatus(status);
        return outbound;
    }

    @Test
    @DisplayName("删除待审核出库单成功")
    void deletePendingOutboundSucceeds() {
        when(outboundMapper.selectById(1L)).thenReturn(outboundWithStatus(Outbound.STATUS_PENDING));
        when(outboundMapper.deleteById(1L)).thenReturn(1);

        boolean deleted = outboundService.delete(1L);

        assertTrue(deleted);
        verify(outboundMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除已审核出库单抛异常")
    void deleteApprovedOutboundThrows() {
        when(outboundMapper.selectById(1L)).thenReturn(outboundWithStatus(Outbound.STATUS_APPROVED));

        BusinessException ex = assertThrows(BusinessException.class, () -> outboundService.delete(1L));

        assertTrue(ex.getMessage().contains("只有待审核状态"));
        verify(outboundMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除已作废出库单抛异常")
    void deleteVoidOutboundThrows() {
        when(outboundMapper.selectById(1L)).thenReturn(outboundWithStatus(Outbound.STATUS_VOID));

        assertThrows(BusinessException.class, () -> outboundService.delete(1L));

        verify(outboundMapper, never()).deleteById(any());
    }
}
