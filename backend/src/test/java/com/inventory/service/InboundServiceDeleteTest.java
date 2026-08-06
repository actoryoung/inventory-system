package com.inventory.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.inventory.entity.Inbound;
import com.inventory.exception.BusinessException;
import com.inventory.mapper.InboundMapper;
import com.inventory.service.impl.InboundServiceImpl;
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
 * 入库单删除逻辑单元测试
 *
 * 覆盖 P0-4：仅"待审核"状态可删除（物理删除），
 * "已审核"/"已作废"状态删除时抛业务异常。
 *
 * @author inventory-system
 * @since 2026-08-06
 */
@DisplayName("入库单删除逻辑测试 (InboundServiceDelete)")
class InboundServiceDeleteTest {

    @Mock
    private InboundMapper inboundMapper;

    private InboundServiceImpl inboundService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        inboundService = new InboundServiceImpl();
        // ServiceImpl 的 baseMapper 需要显式注入
        ReflectionTestUtils.setField(inboundService, "baseMapper", inboundMapper);
        // removeById 依赖 MyBatis-Plus TableInfo，需手动初始化实体元数据
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Inbound.class);
    }

    private Inbound inboundWithStatus(int status) {
        Inbound inbound = new Inbound();
        inbound.setId(1L);
        inbound.setInboundNo("IN202608060001");
        inbound.setStatus(status);
        return inbound;
    }

    @Test
    @DisplayName("删除待审核入库单成功")
    void deletePendingInboundSucceeds() {
        when(inboundMapper.selectById(1L)).thenReturn(inboundWithStatus(Inbound.STATUS_PENDING));
        when(inboundMapper.deleteById(1L)).thenReturn(1);

        boolean deleted = inboundService.delete(1L);

        assertTrue(deleted);
        verify(inboundMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除已审核入库单抛异常")
    void deleteApprovedInboundThrows() {
        when(inboundMapper.selectById(1L)).thenReturn(inboundWithStatus(Inbound.STATUS_APPROVED));

        BusinessException ex = assertThrows(BusinessException.class, () -> inboundService.delete(1L));

        assertTrue(ex.getMessage().contains("只有待审核状态"));
        verify(inboundMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("删除已作废入库单抛异常")
    void deleteVoidInboundThrows() {
        when(inboundMapper.selectById(1L)).thenReturn(inboundWithStatus(Inbound.STATUS_VOID));

        assertThrows(BusinessException.class, () -> inboundService.delete(1L));

        verify(inboundMapper, never()).deleteById(any());
    }
}
