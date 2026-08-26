@echo off
cd /d "%~dp0"

echo ============================================
echo   AI Learn 教师端 - 一键启动
echo ============================================
echo.

if not exist "node_modules\vite\package.json" (
  echo [1/2] 正在安装依赖，请稍候（首次约1-2分钟）...
  call pnpm install
  if errorlevel 1 (
    if exist "node_modules\vite\package.json" (
      echo [提示] 主要依赖已安装（存在少量非致命警告），继续启动...
    ) else (
      echo.
      echo [错误] 依赖安装失败，请检查网络后重新运行本脚本。
      pause
      exit /b 1
    )
  )
)

set "PKG=pnpm"
where pnpm >nul 2>&1
if errorlevel 1 (
  set "PKG=npm"
  where npm >nul 2>&1
  if errorlevel 1 (
    echo [错误] 未找到 pnpm / npm / node，请先安装 Node.js 18 或更高版本。
    pause
    exit /b 1
  )
)

echo [2/2] 正在启动开发服务器，请保持本窗口打开...
echo 稍后会自动打开浏览器：http://localhost:5173
echo 如需停止服务器，关闭本窗口或按 Ctrl+C。
echo 提示：若提示端口被占用，请先关闭旧的服务器窗口再运行本脚本。
echo.

REM 5 秒后自动打开浏览器（后台最小化窗口）
start /min cmd /c "timeout /t 5 /nobreak >nul & start http://localhost:5173"

REM 前台运行服务器（本窗口会显示日志）
%PKG% dev

echo.
echo 服务器已停止。按任意键关闭本窗口...
pause
