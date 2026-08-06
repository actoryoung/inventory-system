package com.inventory;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 进销存管理系统启动类
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.inventory.mapper")
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
        log.info("\n========================================");
        log.info("进销存管理系统启动成功！");
        log.info("API 文档地址: http://localhost:8080/doc.html");
        log.info("========================================");
    }
}
