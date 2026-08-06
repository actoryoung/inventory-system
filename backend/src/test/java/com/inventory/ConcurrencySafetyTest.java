package com.inventory;

import com.inventory.dto.InboundDTO;
import com.inventory.entity.Inventory;
import com.inventory.exception.BusinessException;
import com.inventory.service.InboundService;
import com.inventory.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 并发安全集成测试（真实 H2 数据库）
 *
 * 覆盖：
 * - P1-1 库存增减原子性：10 线程并发 addStock，最终库存 = 原值 + 1000
 * - P1-1 防超卖：2 线程并发 reduceStock(100)，原库存 150，仅一个成功
 * - P1-2 单号唯一：10 线程并发创建入库单，单号全部唯一
 *
 * @author inventory-system
 * @since 2026-08-06
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("并发安全集成测试 (ConcurrencySafety)")
class ConcurrencySafetyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InboundService inboundService;

    @Test
    @DisplayName("10 线程并发 addStock(100)，最终库存 = 原值 + 1000")
    void concurrentAddStockIsAtomic() throws Exception {
        Long productId = 1L;
        // 复位库存为 0
        inventoryService.adjustStock(productId, 0, "test-reset");

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                inventoryService.addStock(productId, 100);
                return null;
            }));
        }

        ready.await();
        start.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();

        Inventory inventory = inventoryService.getByProductId(productId);
        assertEquals(1000, inventory.getQuantity());
    }

    @Test
    @DisplayName("2 线程并发 reduceStock(100)，原库存 150，仅一个成功，最终库存 50")
    void concurrentReduceStockPreventsOversell() throws Exception {
        Long productId = 2L;
        // 复位库存为 150
        inventoryService.adjustStock(productId, 150, "test-reset");

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                try {
                    inventoryService.reduceStock(productId, 100);
                    results.add(true);
                } catch (BusinessException e) {
                    errors.add(e);
                }
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(50);
        }

        assertEquals(1, results.size(), "应恰好一个线程扣减成功");
        assertEquals(1, errors.size(), "应恰好一个线程因库存不足失败");
        Inventory inventory = inventoryService.getByProductId(productId);
        assertEquals(50, inventory.getQuantity());
    }

    @Test
    @DisplayName("10 线程并发创建入库单，单号全部唯一")
    void concurrentInboundNoAreUnique() throws Exception {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);

        List<Long> ids = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                InboundDTO dto = new InboundDTO();
                dto.setProductId(1L);
                dto.setQuantity(1);
                dto.setSupplier("并发测试-" + idx);
                dto.setInboundDate(LocalDateTime.now());
                ids.add(inboundService.create(dto));
            });
        }

        ready.await();
        start.countDown();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(50);
        }

        assertEquals(threadCount, ids.size());
        Set<String> uniqueNos = ids.stream()
                .map(id -> inboundService.getDetail(id).getInboundNo())
                .collect(Collectors.toSet());
        assertEquals(threadCount, uniqueNos.size(), "入库单号必须全部唯一");
        assertTrue(uniqueNos.stream().allMatch(no -> no.startsWith("IN")));
    }
}
