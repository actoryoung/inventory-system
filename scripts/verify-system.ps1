# 进销存管理系统 - 完整性验证脚本 (PowerShell)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "进销存管理系统 - 系统完整性验证" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# 项目根目录
$PROJECT_ROOT = "D:\claude_template\portfolio-projects\inventory-system"
Set-Location $PROJECT_ROOT

# 检查计数
$TOTAL_CHECKS = 0
$PASSED_CHECKS = 0
$FAILED_CHECKS = 0

# 检查函数
function Test-Check {
    param(
        [string]$Name,
        [scriptblock]$TestScript
    )

    $TOTAL_CHECKS++
    Write-Host -NoNewline "检查 $Name... "

    try {
        $result = & $TestScript 2>$null
        if ($result) {
            Write-Host "✓ 通过" -ForegroundColor Green
            $PASSED_CHECKS++
            return $true
        } else {
            Write-Host "✗ 失败" -ForegroundColor Red
            $FAILED_CHECKS++
            return $false
        }
    } catch {
        Write-Host "✗ 失败" -ForegroundColor Red
        $FAILED_CHECKS++
        return $false
    }
}

Write-Host "1. 检查项目结构" -ForegroundColor Yellow
Write-Host "-------------------------------------------"
Test-Check "后端目录" { Test-Path "backend" }
Test-Check "前端目录" { Test-Path "frontend" }
Test-Check "Claude 配置" { Test-Path ".claude" }
Test-Check "SQL 脚本目录" { Test-Path "backend\src\main\resources\sql" }
Write-Host ""

Write-Host "2. 检查后端文件" -ForegroundColor Yellow
Write-Host "-------------------------------------------"
Test-Check "Java 实体类" { (Get-ChildItem -Path "backend\src\main\java" -Filter "*.java" -Recurse -ErrorAction SilentlyContinue).Count -gt 0 }
Test-Check "Category 实体" { Test-Path "backend\src\main\java\com\inventory\entity\Category.java" }
Test-Check "Product 实体" { Test-Path "backend\src\main\java\com\inventory\entity\Product.java" }
Test-Check "Inventory 实体" { Test-Path "backend\src\main\java\com\inventory\entity\Inventory.java" }
Test-Check "Inbound 实体" { Test-Path "backend\src\main\java\com\inventory\entity\Inbound.java" }
Test-Check "Outbound 实体" { Test-Path "backend\src\main\java\com\inventory\entity\Outbound.java" }
Test-Check "Controller 层" { Test-Path "backend\src\main\java\com\inventory\controller\CategoryController.java" }
Test-Check "Service 层" { Test-Path "backend\src\main\java\com\inventory\service\CategoryService.java" }
Test-Check "pom.xml" { Test-Path "backend\pom.xml" }
Write-Host ""

Write-Host "3. 检查前端文件" -ForegroundColor Yellow
Write-Host "-------------------------------------------"
Test-Check "Vue 组件" { (Get-ChildItem -Path "frontend\src" -Filter "*.vue" -Recurse -ErrorAction SilentlyContinue).Count -gt 0 }
Test-Check "API 接口" { Test-Path "frontend\src\api\category.ts" }
Test-Check "TypeScript 类型" { Test-Path "frontend\src\types\category.ts" }
Test-Check "路由配置" { Test-Path "frontend\src\router\index.ts" }
Test-Check "package.json" { Test-Path "frontend\package.json" }
Write-Host ""

Write-Host "4. 检查数据库脚本" -ForegroundColor Yellow
Write-Host "-------------------------------------------"
Test-Check "分类表 SQL" { Test-Path "backend\src\main\resources\sql\category.sql" }
Test-Check "商品表 SQL" { Test-Path "backend\src\main\resources\sql\product.sql" }
Test-Check "库存表 SQL" { Test-Path "backend\src\main\resources\sql\inventory.sql" }
Test-Check "入库表 SQL" { Test-Path "backend\src\main\resources\sql\inbound.sql" }
Test-Check "出库表 SQL" { Test-Path "backend\src\main\resources\sql\outbound.sql" }
Write-Host ""

Write-Host "5. 检查文档" -ForegroundColor Yellow
Write-Host "-------------------------------------------"
Test-Check "README" { Test-Path "README.md" }
Test-Check "CLAUDE.md" { Test-Path "CLAUDE.md" }
Test-Check "DEPLOYMENT.md" { Test-Path "DEPLOYMENT.md" }
Test-Check "项目进度文档" { Test-Path "PROJECT_PROGRESS.md" }
Write-Host ""

Write-Host "6. 检查 AI 配置" -ForegroundColor Yellow
Write-Host "-------------------------------------------"
Test-Check "Orchestrator 配置" { Test-Path ".claude\agents\orchestrator.md" }
Test-Check "Agent 索引" { Test-Path ".claude\agents\INDEX.md" }
Test-Check "编排器配置" { Test-Path ".claude\ORCHESTRATOR_CONFIG.md" }
Test-Check "通用命令" { Test-Path ".claude\commands\commit.md" }
Test-Check "技能脚本" { Test-Path ".claude\skills\code-generator.js" }
Write-Host ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "验证结果汇总" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "总检查项: $TOTAL_CHECKS"
Write-Host "通过: $PASSED_CHECKS" -ForegroundColor Green
Write-Host "失败: $FAILED_CHECKS" -ForegroundColor Red
Write-Host ""

if ($FAILED_CHECKS -eq 0) {
    Write-Host "✓ 所有检查通过！系统结构完整。" -ForegroundColor Green
    exit 0
} else {
    Write-Host "✗ 有 $FAILED_CHECKS 项检查失败，请检查系统完整性。" -ForegroundColor Red
    exit 1
}
