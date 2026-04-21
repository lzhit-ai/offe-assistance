@echo off
setlocal

set "ROOT_DIR=%~dp0"
set "BACKEND_PS=%ROOT_DIR%scripts\start-backend.ps1"
set "FRONTEND_PS=%ROOT_DIR%scripts\start-frontend.ps1"

if not exist "%BACKEND_PS%" (
    echo [error] Missing backend script: %BACKEND_PS%
    pause
    exit /b 1
)

if not exist "%FRONTEND_PS%" (
    echo [error] Missing frontend script: %FRONTEND_PS%
    pause
    exit /b 1
)

if /i "%GETOFFER_DRY_RUN%"=="1" (
    echo [dry-run] ROOT_DIR=%ROOT_DIR%
    echo [dry-run] powershell -NoExit -ExecutionPolicy Bypass -File "%BACKEND_PS%"
    echo [dry-run] powershell -NoExit -ExecutionPolicy Bypass -File "%FRONTEND_PS%"
    echo [dry-run] start http://localhost:5173
    exit /b 0
)

echo Starting GetOffer...

start "GetOffer Backend" powershell -NoExit -ExecutionPolicy Bypass -File "%BACKEND_PS%"
timeout /t 6 /nobreak >nul

start "GetOffer Frontend" powershell -NoExit -ExecutionPolicy Bypass -File "%FRONTEND_PS%"
timeout /t 6 /nobreak >nul

start "" "http://localhost:5173"
exit /b 0
