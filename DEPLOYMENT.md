# 进销存管理系统 - 完整部署指南

> **项目状态**: 100% 完成 | **最后更新**: 2026-01-23
> **GLM 模型配置**: Orchestrator (glm-4.7) + 子代理 (glm-4.6) + 高并发降级 (glm-4.5-air)

---

## 系统概览

### 技术栈
| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + TypeScript + Element Plus | 3.x |
| 后端 | Spring Boot + MyBatis-Plus | 2.5+ |
| 数据库 | MySQL | 8.0+ |
| AI 辅助 | GLM 模型系列 | 4.7/4.6/4.5-air |

### 模块清单
| 模块 | 状态 | API 数量 | 前端页面 |
|------|------|---------|---------|
| 分类管理 | ✅ 100% | 6 | 2 |
| 商品管理 | ✅ 100% | 6 | 2 |
| 库存管理 | ✅ 100% | 6 | 1 |
| 入库管理 | ✅ 100% | 7 | 1 |
| 出库管理 | ✅ 100% | 7 | 1 |
| 统计报表 | ✅ 100% | 4 | 1 |
| **总计** | **6/6** | **36** | **8** |

---

## 快速部署

### 前置要求

```bash
# 必需软件
- Node.js 18+
- Java 17+
- MySQL 8.0+
- Git
```

### 一、数据库初始化

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE inventory_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 导入 SQL 脚本（按顺序）
cd D:\claude_template\portfolio-projects\inventory-system\backend\src\main\resources\sql

mysql -u root -p inventory_system < category.sql
mysql -u root -p inventory_system < product.sql
mysql -u root -p inventory_system < inventory.sql
mysql -u root -p inventory_system < inbound.sql
mysql -u root -p inventory_system < outbound.sql

# 3. 验证表创建
mysql -u root -p inventory_system -e "SHOW TABLES;"
# 应显示: t_category, t_product, t_inventory, t_inbound, t_inbound_sequence, t_outbound, t_outbound_sequence
```

### 二、后端部署

```bash
cd D:\claude_template\portfolio-projects\inventory-system\backend

# 1. 配置数据库连接
# 编辑 src/main/resources/application.yml
# 修改数据库连接信息

# 2. 构建项目
mvn clean package -DskipTests

# 3. 运行后端
java -jar target/inventory-system-1.0.0.jar

# 或使用开发模式
mvn spring-boot:run

# 4. 验证后端启动
# 访问: http://localhost:8080/doc.html (Knife4j API 文档)
```

**application.yml 配置示例:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/inventory_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 三、前端部署

```bash
cd D:\claude_template\portfolio-projects\inventory-system\frontend

# 1. 安装依赖
npm install

# 2. 安装 echarts（统计报表需要）
npm install echarts

# 3. 开发模式运行
npm run dev
# 访问: http://localhost:5173

# 4. 生产构建
npm run build
# 输出: dist/ 目录

# 5. 预览生产构建
npm run preview
```

---

## 系统验证清单

### 后端验证

- [ ] 后端服务启动成功 (端口 8080)
- [ ] API 文档可访问 (http://localhost:8080/doc.html)
- [ ] 数据库连接正常
- [ ] 所有 36 个 API 接口可调用

### 前端验证

- [ ] 前端服务启动成功 (端口 5173)
- [ ] 登录页面正常显示
- [ ] 所有 8 个页面可访问
- [ ] API 调用正常

### 功能验证

| 功能模块 | 测试项 | 状态 |
|---------|-------|------|
| 分类管理 | CRUD、树形展示、状态切换 | ⬜ |
| 商品管理 | CRUD、SKU 唯一性、分页搜索 | ⬜ |
| 库存管理 | 查询、调整、预警 | ⬜ |
| 入库管理 | CRUD、单号生成、审核 | ⬜ |
| 出库管理 | CRUD、库存验证、审核 | ⬜ |
| 统计报表 | 看板、趋势图、分布图 | ⬜ |

---

## API 接口清单

### 基础路径
```
http://localhost:8080/api
```

### 分类管理 (6个)
```
POST   /category              创建分类
GET    /category/tree         获取分类树
GET    /category/{id}         获取分类详情
PUT    /category/{id}         更新分类
DELETE /category/{id}         删除分类
PATCH  /category/{id}/status  切换状态
```

### 商品管理 (6个)
```
POST   /product               创建商品
GET    /product/{id}          获取商品详情
GET    /product               获取商品列表（分页）
PUT    /product/{id}          更新商品
DELETE /product/{id}          删除商品
PATCH  /product/{id}/status   切换状态
```

### 库存管理 (6个)
```
GET    /inventory             获取库存列表（分页）
GET    /inventory/product/{productId}  获取商品库存
PUT    /inventory/{id}/adjust 调整库存
GET    /inventory/low-stock   获取低库存列表
POST   /inventory/check       检查库存充足性
GET    /inventory/summary     获取库存汇总
```

### 入库管理 (7个)
```
POST   /inbound               创建入库单
GET    /inbound/{id}          获取入库单详情
GET    /inbound               获取入库单列表
PUT    /inbound/{id}          更新入库单
DELETE /inbound/{id}          删除入库单
PATCH  /inbound/{id}/approve  审核入库单
PATCH  /inbound/{id}/void     作废入库单
```

### 出库管理 (7个)
```
POST   /outbound              创建出库单
GET    /outbound/{id}         获取出库单详情
GET    /outbound              获取出库单列表
PUT    /outbound/{id}         更新出库单
DELETE /outbound/{id}         删除出库单
PATCH  /outbound/{id}/approve 审核出库单
PATCH  /outbound/{id}/void    作废出库单
```

### 统计报表 (4个)
```
GET    /statistics/dashboard              获取数据看板
GET    /statistics/trend                 获取出入库趋势
GET    /statistics/category-distribution 获取库存分类分布
GET    /statistics/low-stock              获取低库存列表
```

---

## 目录结构

```
inventory-system/
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/inventory/
│   │   ├── entity/                 # 实体类 (7个)
│   │   ├── dto/                    # 数据传输对象 (5个)
│   │   ├── vo/                     # 视图对象 (7个)
│   │   ├── mapper/                 # MyBatis Mapper (7个)
│   │   ├── service/                # 服务层 (12个)
│   │   ├── controller/             # 控制器 (6个)
│   │   ├── config/                 # 配置类 (3个)
│   │   └── exception/              # 异常处理 (1个)
│   ├── src/main/resources/
│   │   ├── sql/                    # 数据库脚本 (5个)
│   │   └── application.yml         # 配置文件
│   └── pom.xml                     # Maven 配置
│
├── frontend/                       # Vue 3 前端
│   ├── src/
│   │   ├── api/                    # API 接口 (6个)
│   │   ├── types/                  # TypeScript 类型 (6个)
│   │   ├── views/                  # 页面组件 (8个)
│   │   ├── router/                 # 路由配置
│   │   ├── utils/                  # 工具函数
│   │   └── main.ts                 # 入口文件
│   ├── package.json
│   └── vite.config.ts
│
├── .claude/                        # AI 辅助开发配置
│   ├── agents/                     # Agent 配置 (GLM 模型)
│   ├── commands/                   # 自定义命令
│   ├── skills/                     # 技能脚本
│   ├── ORCHESTRATOR_CONFIG.md      # 编排器配置
│   └── specs/                      # 功能规范文档
│
├── CLAUDE.md                       # 项目说明
├── DEPLOYMENT.md                   # 本部署文档
└── README.md                       # 项目简介
```

---

## 故障排查

### 常见问题

**1. 后端启动失败**
```bash
# 检查 Java 版本
java -version  # 需要 17+

# 检查数据库连接
mysql -u root -p inventory_system

# 查看日志
tail -f backend/logs/spring.log
```

**2. 前端无法连接后端**
```bash
# 检查后端是否运行
curl http://localhost:8080/api/category/tree

# 检查前端 API 配置
# frontend/src/utils/request.ts 中的 baseURL
```

**3. 数据库连接错误**
```bash
# 验证数据库存在
mysql -u root -p -e "SHOW DATABASES LIKE 'inventory_system';"

# 验证表存在
mysql -u root -p inventory_system -e "SHOW TABLES;"
```

**4. 端口冲突**
```bash
# 检查端口占用
netstat -ano | findstr :8080  # 后端端口
netstat -ano | findstr :5173  # 前端端口

# 修改端口
# 后端: application.yml 中的 server.port
# 前端: vite.config.ts 中的 server.port
```

---

## 生产部署

### 后端部署

```bash
# 1. 构建 JAR
cd backend
mvn clean package -DskipTests

# 2. 使用 systemd 管理 (Linux)
sudo nano /etc/systemd/system/inventory-api.service
```

**inventory-api.service:**
```ini
[Unit]
Description=Inventory Management API
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/var/www/inventory-system/backend
ExecStart=/usr/bin/java -jar /var/www/inventory-system/backend/inventory-system-1.0.0.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable inventory-api
sudo systemctl start inventory-api
```

### 前端部署

```bash
# 1. 构建
cd frontend
npm run build

# 2. 使用 Nginx 托管
sudo nano /etc/nginx/sites-available/inventory-app
```

**nginx 配置:**
```nginx
server {
    listen 80;
    server_name inventory.example.com;

    root /var/www/inventory-system/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/inventory-app /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## 维护和更新

### 日志位置
- 后端: `backend/logs/spring.log`
- 前端: 浏览器控制台

### 数据库备份
```bash
# 备份
mysqldump -u root -p inventory_system > backup_$(date +%Y%m%d).sql

# 恢复
mysql -u root -p inventory_system < backup_20260123.sql
```

### 版本更新
```bash
git pull origin main
mvn clean package -DskipTests
npm install
npm run build
```

---

## 许可证

MIT License

---

## 联系方式

- 项目维护者: [您的名字]
- 问题反馈: GitHub Issues
