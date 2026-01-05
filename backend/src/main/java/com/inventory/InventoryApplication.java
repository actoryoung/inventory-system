package com.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 进销存管理系统启动类
 *
 * @author inventory-system
 * @since 2026-01-04
 */
@SpringBootApplication
@MapperScan("com.inventory.mapper")
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("进销存管理系统启动成功！");
        System.out.println("API 文档地址: http://localhost:8080/doc.html");
        System.out.println("========================================\n");
    }
}
