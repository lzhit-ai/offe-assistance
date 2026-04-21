$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = Split-Path -Parent $scriptDir
$frontendDir = Join-Path $rootDir 'getoffer'

if (-not (Test-Path (Join-Path $frontendDir 'package.json'))) {
    Write-Host "[error] Frontend directory not found: $frontendDir" -ForegroundColor Red
    exit 1
}

if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    Write-Host '[error] Node.js was not found in PATH.' -ForegroundColor Red
    exit 1
}

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Host '[error] npm was not found in PATH.' -ForegroundColor Red
    exit 1
}

$env:VITE_API_BASE_URL = 'http://localhost:8080'

if ($env:GETOFFER_DRY_RUN -eq '1') {
    Write-Host "[dry-run] FRONTEND_DIR=$frontendDir"
    Write-Host "[dry-run] VITE_API_BASE_URL=$env:VITE_API_BASE_URL"
    Write-Host '[dry-run] npm.cmd run dev'
    exit 0
}

Write-Host "[frontend] Working directory: $frontendDir" -ForegroundColor Cyan
Write-Host '[frontend] Starting Vite...' -ForegroundColor Cyan

Push-Location $frontendDir
try {
    & npm.cmd run dev
    $exitCode = $LASTEXITCODE
}
finally {
    Pop-Location
}

Write-Host ''
Write-Host "[frontend] Process exited with code: $exitCode" -ForegroundColor Yellow
exit $exitCode
