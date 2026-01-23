#!/bin/bash
# 进销存管理系统 - 完整性验证脚本

echo "========================================="
echo "进销存管理系统 - 系统完整性验证"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查计数
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# 检查函数
check_item() {
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    local item_name="$1"
    local check_command="$2"

    echo -n "检查 $item_name... "
    if eval "$check_command" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 通过${NC}"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}✗ 失败${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS + 1))
        return 1
    fi
}

# 项目根目录
PROJECT_ROOT="D:/claude_template/portfolio-projects/inventory-system"
cd "$PROJECT_ROOT" || exit 1

echo "1. 检查项目结构"
echo "-------------------------------------------"
check_item "后端目录" "test -d backend"
check_item "前端目录" "test -d frontend"
check_item "Claude 配置" "test -d .claude"
check_item "SQL 脚本目录" "test -d backend/src/main/resources/sql"
echo ""

echo "2. 检查后端文件"
echo "-------------------------------------------"
check_item "Java 实体类" "find backend/src/main/java -name '*.java' | wc -l | grep -q '[1-9]'"
check_item "Category 实体" "test -f backend/src/main/java/com/inventory/entity/Category.java"
check_item "Product 实体" "test -f backend/src/main/java/com/inventory/entity/Product.java"
check_item "Inventory 实体" "test -f backend/src/main/java/com/inventory/entity/Inventory.java"
check_item "Inbound 实体" "test -f backend/src/main/java/com/inventory/entity/Inbound.java"
check_item "Outbound 实体" "test -f backend/src/main/java/com/inventory/entity/Outbound.java"
check_item "Controller 层" "test -f backend/src/main/java/com/inventory/controller/CategoryController.java"
check_item "Service 层" "test -f backend/src/main/java/com/inventory/service/CategoryService.java"
check_item "pom.xml" "test -f backend/pom.xml"
echo ""

echo "3. 检查前端文件"
echo "-------------------------------------------"
check_item "Vue 组件" "find frontend/src -name '*.vue' | wc -l | grep -q '[1-9]'"
check_item "API 接口" "test -f frontend/src/api/category.ts"
check_item "TypeScript 类型" "test -f frontend/src/types/category.ts"
check_item "路由配置" "test -f frontend/src/router/index.ts"
check_item "package.json" "test -f frontend/package.json"
echo ""

echo "4. 检查数据库脚本"
echo "-------------------------------------------"
check_item "分类表 SQL" "test -f backend/src/main/resources/sql/category.sql"
check_item "商品表 SQL" "test -f backend/src/main/resources/sql/product.sql"
check_item "库存表 SQL" "test -f backend/src/main/resources/sql/inventory.sql"
check_item "入库表 SQL" "test -f backend/src/main/resources/sql/inbound.sql"
check_item "出库表 SQL" "test -f backend/src/main/resources/sql/outbound.sql"
echo ""

echo "5. 检查文档"
echo "-------------------------------------------"
check_item "README" "test -f README.md"
check_item "CLAUDE.md" "test -f CLAUDE.md"
check_item "DEPLOYMENT.md" "test -f DEPLOYMENT.md"
check_item "项目进度文档" "test -f PROJECT_PROGRESS.md"
echo ""

echo "6. 检查 AI 配置"
echo "-------------------------------------------"
check_item "Orchestrator 配置" "test -f .claude/agents/orchestrator.md"
check_item "Agent 索引" "test -f .claude/agents/INDEX.md"
check_item "编排器配置" "test -f .claude/ORCHESTRATOR_CONFIG.md"
check_item "通用命令" "test -f .claude/commands/commit.md"
check_item "技能脚本" "test -f .claude/skills/code-generator.js"
echo ""

echo "========================================="
echo "验证结果汇总"
echo "========================================="
echo -e "总检查项: $TOTAL_CHECKS"
echo -e "${GREEN}通过: $PASSED_CHECKS${NC}"
echo -e "${RED}失败: $FAILED_CHECKS${NC}"
echo ""

if [ $FAILED_CHECKS -eq 0 ]; then
    echo -e "${GREEN}✓ 所有检查通过！系统结构完整。${NC}"
    exit 0
else
    echo -e "${RED}✗ 有 $FAILED_CHECKS 项检查失败，请检查系统完整性。${NC}"
    exit 1
fi
