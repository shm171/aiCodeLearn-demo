@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo 正在安装依赖...
set "PKG=pnpm"
where pnpm >nul 2>&1
if errorlevel 1 (
  set "PKG=npm"
  where npm >nul 2>&1
  if errorlevel 1 (
    echo [错误] 未找到 pnpm / npm / node，请先安装 Node.js 18+。
    pause
    exit /b 1
  )
)
call %PKG% install
if errorlevel 1 (
  echo 安装失败，请检查网络后重试。
  pause
  exit /b 1
)
echo 依赖安装完成。
pause