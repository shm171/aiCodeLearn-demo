@echo off
cd /d "%~dp0"

echo ============================================
echo   AI Learn 管理端 - 后台常驻启动
echo ============================================
echo.

if not exist "node_modules\vite\package.json" (
  echo [1/2] 正在安装依赖，请稍候（首次约1-2分钟）...
  call pnpm install --node-linker=hoisted
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

netstat -ano | findstr ":5175" | findstr "LISTENING" >nul 2>&1
if not errorlevel 1 (
  echo [提示] 服务器已经在运行（5175 端口被占用）。
  echo 直接访问：http://localhost:5175
  pause
  exit /b 1
)

set "NODE=node"
where node >nul 2>&1
if errorlevel 1 (
  if exist "C:\Users\hp\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe" (
    set "NODE=C:\Users\hp\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe"
  ) else (
    echo [错误] 未找到 node，请先安装 Node.js 18 或更高版本。
    pause
    exit /b 1
  )
)

echo [2/2] 正在后台启动服务器（窗口隐藏，日志写入 server.log）...
echo 启动完成后网址为：http://localhost:5175
echo.

powershell -NoProfile -ExecutionPolicy Bypass -Command "& { $node='%NODE%'; $p = Start-Process -FilePath $node -ArgumentList @('node_modules/vite/bin/vite.js','--port','5175','--strictPort') -WorkingDirectory (Get-Location) -WindowStyle Hidden -RedirectStandardOutput (Join-Path (Get-Location) 'server.log') -RedirectStandardError (Join-Path (Get-Location) 'server.err.log') -PassThru; Set-Content -Path (Join-Path (Get-Location) 'server.pid') -Value $p.Id }"

timeout /t 4 /nobreak >nul
start "" "http://localhost:5175"
echo 已启动。如需停止，双击「停止管理端.bat」。
pause
