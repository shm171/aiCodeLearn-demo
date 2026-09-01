@echo off
cd /d "%~dp0"

if not exist "server.pid" (
  echo 未找到 server.pid，尝试按端口 5175 停止...
  for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5175" ^| findstr "LISTENING"') do taskkill /F /PID %%a >nul 2>&1
  pause
  exit /b 1
)

set /p PID=<server.pid
taskkill /F /PID %PID% >nul 2>&1
if errorlevel 1 (
  echo 进程 %PID% 已不存在（可能已停止）。
) else (
  echo 已停止服务器（PID %PID%）。
)
del /q server.pid >nul 2>&1
del /q server.log >nul 2>&1
del /q server.err.log >nul 2>&1
pause
