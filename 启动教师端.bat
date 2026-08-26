@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ============================================
echo   AI Learn 教师端 - 一键启动
echo ============================================
echo.

REM 1. 依赖缺失时先安装
if not exist "node_modules" (
  echo [1/2] 正在安装依赖，请稍候...
  call pnpm install
  if errorlevel 1 (
    echo.
    echo [错误] 依赖安装失败，请检查网络后重新运行本脚本。
    pause
    exit /b 1
  )
)

REM 2. 选择可用的包管理器（优先 pnpm，其次 npm）
set "PKG=pnpm"
where pnpm >nul 2>&1
if errorlevel 1 (
  set "PKG=npm"
  where npm >nul 2>&1
  if errorlevel 1 (
    echo [错误] 未找到 pnpm / npm / node。
    echo 请安装 Node.js 18 或更高版本后重试。
    pause
    exit /b 1
  )
)

echo [2/2] 正在启动开发服务器（http://localhost:5173）...
echo 稍后会自动打开浏览器；请不要关闭弹出的黑色窗口。
echo 如需停止，关闭黑色窗口或按 Ctrl+C。
echo.
start "AI Learn Dev Server" cmd /k "chcp 65001 >nul && %PKG% dev"
timeout /t 6 /nobreak >nul
start "" "http://localhost:5173"
echo.
echo 已尝试打开浏览器。若页面未出现，请手动访问：http://localhost:5173
echo.
pause